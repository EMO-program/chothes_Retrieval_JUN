package yanjun.main;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import yanjun.color.similariry.impl.MinkowskiWithScale;
import yanjun.match.ColorMatch;
import yanjun.match.HierarchyImageMatch;
import yanjun.model.Fabric;
import yanjun.model.Model;
import yanjun.model.Result;
import yanjun.properties.Config;
import yanjun.texture.similarity.impl.MinkowskiArrayDistance;
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
			return freeMarkerEngine.render(new ModelAndView(null, "parameter.ftl"));
		});

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
			ColorMatch colorMatch = new ColorMatch(new MinkowskiWithScale());
			HierarchyImageMatch hierarchyImageMatch = new HierarchyImageMatch(new MinkowskiWithScale(), new MinkowskiArrayDistance());

			// 相似度计算
//	      List<String> resultImagePaths = colorMatch.calcSimilarity(queryImagePath);
			// 传入上传路径
			List<String> resultImagePaths = hierarchyImageMatch.calcSimilarity(queryImagePath);


			Result result = new Result();
			result.setQueryPath(uploadImageName);
			result.setQueryName(uploadImageName);

//	      LinkedList<ImageUrl> urlsFound = new LinkedList<ImageUrl>();
			List<String> handleResultPaths = new ArrayList<String>();
			for (String resultImagePath : resultImagePaths) {
				String imageName = resultImagePath.substring(resultImagePath
						.indexOf(Config.fileName) + Config.fileName.length());
				String resultPath = Config.fileName + imageName;
				handleResultPaths.add(resultPath);
//		      logger.debug(resultPath);
//		      urlsFound.add(new ImageUrl(resultPath));
			}

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
