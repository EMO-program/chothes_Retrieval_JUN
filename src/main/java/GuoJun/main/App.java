package GuoJun.main;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import spark.ModelAndView;
import spark.Request;
import spark.Spark;
import spark.template.freemarker.FreeMarkerEngine;
import GuoJun.color.similariry.impl.MinkowskiWithScale;
import GuoJun.match.ColorMatch;
import GuoJun.match.HierarchyImageMatch;
import GuoJun.match.TextureMatch;
import GuoJun.model.Fabric;
import GuoJun.model.Model;
import GuoJun.model.Result;
import GuoJun.properties.Config;
import GuoJun.texture.similarity.impl.MinkowskiArrayDistance;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;



public class App {

	private static Logger logger = Logger.getLogger(App.class);
	private static String UPLOADFILE = "uploads/";

	public static void main(String[] args) {

		// 指定Spark静态文件位置, 根路径是resource
		Spark.staticFileLocation("/static");

		// 设置图片上传路径 相对路径的起始位置是项目位置
		File file = new File("upload");
		file.mkdir();
		Spark.externalStaticFileLocation("upload");


		FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
		Configuration freeMarkerConfiguration = new Configuration();
		freeMarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(App.class, "/"));
		freeMarkerEngine.setConfiguration(freeMarkerConfiguration);

		Model model = new Model();

		// home page
		get("/", (req, res) -> {
			return freeMarkerEngine.render(new ModelAndView(null, "index.ftl"));
		});

		get("/parameter", (req, res) -> {
			Map p_attributes = new HashMap<>();
			p_attributes.put("scaleRatio", Config.scaleRatio);
			p_attributes.put("SCNPercent", Config.sameColorNumPercent);
			p_attributes.put("SCQPercent", Config.sameColorQuantityPercent);
			p_attributes.put("resultNumber", Config.resultNumber);

			return freeMarkerEngine.render(new ModelAndView(p_attributes, "parameter.ftl"));
		});
        //首页参数设置
		post("/parameter", (req, res) -> {

			String body = req.body();
			String[] lists = body.split("&");



//			Gson gson = new Gson();
//			InitConfig inits = gson.fromJson(req.body(), InitConfig.class);

			Config.scaleRatio = Integer.parseInt(lists[0].split("=")[1]);
			Config.sameColorNumPercent = Float.parseFloat(lists[1].split("=")[1]);
			Config.sameColorQuantityPercent = Float.parseFloat(lists[2].split("=")[1]);
			Config.resultNumber = Float.parseFloat(lists[3].split("=")[1]);

			res.redirect("/");

			return null;
		});

		get("/detail", (req, res) -> {

			int fabricId = Integer.parseInt(req.queryParams("fabricid"));
			System.out.println(fabricId);
			Fabric fabric = model.getFabric(fabricId);

			Map attributes = new HashMap<>();
			attributes.put("fabric", fabric);

			return freeMarkerEngine.render(new ModelAndView(attributes, "detail.ftl"));

		});

        //主页搜索主方法路径”/upload“
		post("/upload", "multipart/form-data", (req, res) -> {

			logger.debug("scaleRatio: " + Config.scaleRatio);
			logger.debug("sameColorNumPercent: " + Config.sameColorNumPercent);
			logger.debug("sameColorQuantityPercent: " + Config.sameColorQuantityPercent);
			logger.debug("resultNumber: " + Config.resultNumber);

			Path tempFile = Files.createTempFile(file.toPath(), "", "");

			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

			try (InputStream input = req.raw().getPart("file").getInputStream()) { // getPart needs to use same "name" as input field in form
				Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
			}

//			return "<h1>You uploaded this image:<h1><img src='" + tempFile.getFileName() + "'>";

			// 处理上传
//			String uploadImageName = handleUploadImage(req);
			String uploadImageName = tempFile.getFileName().toString();
			String queryImagePath = Config.uploadImageFile + uploadImageName;

			// read query image url
			//颜色匹配方法定义
			ColorMatch colorMatch = new ColorMatch(new MinkowskiWithScale());
			TextureMatch textureMatch=new TextureMatch(new MinkowskiArrayDistance());
			//多层匹配方法定义
			HierarchyImageMatch hierarchyImageMatch = new HierarchyImageMatch(new MinkowskiWithScale(), new MinkowskiArrayDistance());

			// 通过颜色相似度检索结果
	     // List<String> resultImagePaths = colorMatch.calcSimilarity(queryImagePath);
			//通过纹理相似度来检索结果
	     List<String> resultImagePaths = textureMatch.calcSimilarity(queryImagePath);
			//通过多层检索计算输入图像和数据库所有图片相似度（颜色加纹理），并返回finalResultNumber张搜索结果的路径
		//	List<String> resultImagePaths = hierarchyImageMatch.calcSimilarity(queryImagePath);


			Result result = new Result();
			result.setQueryPath(uploadImageName);
			result.setQueryName(uploadImageName);

//	      LinkedList<ImageUrl> urlsFound = new LinkedList<ImageUrl>();
			//将搜索结果图片路径转变成工程当地路径用于输出
			List<String> handleResultPaths = new ArrayList<String>();
			for (String resultImagePath : resultImagePaths) {
				String imageName = resultImagePath.substring(resultImagePath
						.indexOf(Config.fileName) + Config.fileName.length());
				String resultPath = Config.fileName + imageName;
				handleResultPaths.add(resultPath);
//		      logger.debug(resultPath);
//		      urlsFound.add(new ImageUrl(resultPath));
			}
            //将输入图片以及搜索结果都存在result里面
			result.setResList(handleResultPaths);


			Map attributes = new HashMap<>();
			attributes.put("result", result);

			return freeMarkerEngine.render(new ModelAndView(attributes, "result.ftl"));
		});


	}

	private static String handleUploadImage (Request req) {

		long maxFileSize = 100000000;       // the maximum size allowed for uploaded files
		long maxRequestSize = 100000000;    // the maximum size allowed for multipart/form-data requests
		int fileSizeThreshold = 1024;

		MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
				UPLOADFILE, maxFileSize, maxRequestSize, fileSizeThreshold);
		req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

		String fName = null;
		try {

			fName = req.raw().getPart("file").getSubmittedFileName();
			System.out.println("Title: " + req.raw().getParameter("title"));
			System.out.println("File: " + fName);

			Part uploadedFile = req.raw().getPart("file");
			Path out = Paths.get(UPLOADFILE + fName);
			try (final InputStream in = uploadedFile.getInputStream()) {
				Files.copy(in, out);
				uploadedFile.delete();
			}

			multipartConfigElement = null;
			uploadedFile = null;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}

		return fName;
	}



	public static class ImageUrl {
		public ImageUrl(String url) {
			this.url = url;
		}

		public ImageUrl() {
		}

		public String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}

	public static class InitConfig {
		double scaleRatio;
		double sameColorNumPercent;
		double sameColorQuantityPercent;
		double intermediateResult;

		public double getScaleRatio() {
			return scaleRatio;
		}
		public void setScaleRatio(double scaleRatio) {
			this.scaleRatio = scaleRatio;
		}
		public double getSameColorNumPercent() {
			return sameColorNumPercent;
		}
		public void setSameColorNumPercent(double sameColorNumPercent) {
			this.sameColorNumPercent = sameColorNumPercent;
		}
		public double getSameColorQuantityPercent() {
			return sameColorQuantityPercent;
		}
		public void setSameColorQuantityPercent(double sameColorQuantityPercent) {
			this.sameColorQuantityPercent = sameColorQuantityPercent;
		}
		public double getResultNumber() {
			return intermediateResult;
		}
		public void setResultNumber(double intermediateResult) {
			this.intermediateResult = intermediateResult;
		}


	}

}
