package yanjun.match;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import yanjun.color.similarity.ifs.ISimilarityArray;
import yanjun.color.similarity.ifs.ISimilarityMap;
import yanjun.properties.Config;
import yanjun.texture.LBP;
import yanjun.util.ColorUtil;
import yanjun.util.DBHelper;
import yanjun.util.MatchUtil;

public class HierarchyImageMatch {

	private ISimilarityMap colorStrategy;
	private ISimilarityArray textureStrategy;

	public HierarchyImageMatch(ISimilarityMap colorStrategy, ISimilarityArray textureStrategy) {
		this.colorStrategy = colorStrategy;
		this.textureStrategy = textureStrategy;
	}
	
	@SuppressWarnings({ "unchecked", "null" })
   public List<String> calcSimilarity(String imgPath) {
		// TODO Auto-generated method stub

		FileInputStream imageFile;
		double minDistance = Double.MAX_VALUE;

		// 返回查询结果
		List<String> matchUrls = new ArrayList<String>();

		// 保存检索排序结果
		Map<String, Double> colorResMap = new HashMap<String, Double>();
		Map<String, String> textureResMap = new HashMap<String, String>();

		try {

			imageFile = new FileInputStream(imgPath);
			BufferedImage bufferImage;

			bufferImage = ImageIO.read(imageFile);
			// 获取HSV量化后的颜色直方图数据 一维向量表示
			//得到图片的颜色一维数组特征向量（各颜色分量频率（统计量））
			double[] sourceHist = ColorUtil.getImageHSV(bufferImage);
			double[] lbpSourceFecture = LBP.getLBPFeature(imgPath);
			
			Map<Integer, Double> sourceHistMap = new LinkedHashMap<Integer, Double>();
			for (int i = 0; i < sourceHist.length; i++) {
				sourceHistMap.put(i, sourceHist[i]);
			}
			//直方图排序
			sourceHistMap = MatchUtil.sortByValue(sourceHistMap);
			//取直方图频率总量百分之85的向量
			sourceHistMap = MatchUtil.getSamePercentage(sourceHistMap, Config.TopPercentage);
			
//			for(Entry<Integer, Double> entry : sourceHistMap.entrySet()) {
//				System.out.print(entry.getKey() + ":" + entry.getValue() + ", ");
//			}
//			System.out.println();
//			System.out.println("---------------------------------");
			
			// 提取数据库数据
			List<Object> list = DBHelper.fetchALLCloth();

			// long startTime = System.currentTimeMillis();

			// 计算每一条数据库对应的图像数据其颜色相似度并存储colorResMap中；把每个条数据的路径和纹理特征提取出来并存储在textureResMap的键值对中。
			double tempRes = 0;
			for (int i = 0; i < list.size(); i++) {

				Map<String, Object> map = (Map<String, Object>) list.get(i);

				Object candidateData = map.get("histogram");
				Object candidatePath = map.get("path");
				Object candidateLBP = map.get("lbpFeature");

				//先进行颜色匹配
				Map<Integer, Double> candidateHistMap = MatchUtil.jsonToMap2((String)candidateData);
				Map<Integer, Double> tempSourceHistMap = new LinkedHashMap<Integer, Double>(sourceHistMap);

				
				// 计算相似度
				tempRes = colorStrategy.similarity(tempSourceHistMap, candidateHistMap);
				
				colorResMap.put((String) candidatePath, tempRes);
				textureResMap.put((String) candidatePath, (String) candidateLBP); 
	
				// 判断衡量标准选取距离还是相似度
				if (tempRes < minDistance)
					minDistance = tempRes;
			
			}

			Map<String, Double> tempResultMap;
			
			System.out.println("Min Distance : " + (float) minDistance);
			tempResultMap = MatchUtil.sortByValueAsc(colorResMap);//先将经过颜色匹配的排序过的所有结果存储在临时结果集合里
		
			int counter = 0;

			Map<String, Double> lbpResultMap = new HashMap<String, Double>();
			for (Map.Entry<String, Double> map : tempResultMap.entrySet()) {

				if (counter >= Config.resultNumber)
					break;

				// 从快速搜索中选取TOP N结果，继续进行纹理特征匹配
               //获取当前由颜色匹配相似度排序的结果并提取其LBP纹理特征
				String candidateLBPFecture = textureResMap.get(map.getKey());
				
				double[] lbpTargetFeature = MatchUtil.jsonToArr(candidateLBPFecture);
				double lbpDistance = textureStrategy.similarity(lbpTargetFeature, lbpSourceFecture);
				lbpResultMap.put(map.getKey(), lbpDistance);
				
				counter++;
//				System.out.println(map.getValue() + " " + map.getKey());
			}
			
			System.out.println("============== finish color =================");
			
			Map<String, Double> finalResult = MatchUtil.sortByValueAsc(lbpResultMap);
			
			counter = 0;
			for (Map.Entry<String, Double> map : finalResult.entrySet()) {
				if (counter >= Config.finalResultNumber)
					break;
				//matchUrls截取只存储finalResultNumber数量的查询结果
				matchUrls.add(map.getKey());
				counter ++;
				System.out.println(map.getValue() + " " + map.getKey());
				
			}
			
			System.out.println();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return matchUrls;
	}
}
