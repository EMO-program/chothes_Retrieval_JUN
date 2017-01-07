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

import org.apache.log4j.Logger;

import yanjun.color.similarity.ifs.ISimilarityMap;
import yanjun.properties.Config;
import yanjun.util.ColorUtil;
import yanjun.util.DBHelper;
import yanjun.util.MatchUtil;

public class ColorMatch {
	
	private static Logger logger = Logger.getLogger(ColorMatch.class);
	
	private ISimilarityMap matchStrategy;

	public ColorMatch(ISimilarityMap matchStrategy) {
		this.matchStrategy = matchStrategy;
	}
	
	@SuppressWarnings({ "unchecked", "null" })
   public List<String> calcSimilarity(String imgPath) {

		FileInputStream imageFile;
		
		double minDistance = Double.MAX_VALUE;

		// 返回查询结果
		List<String> matchUrls = new ArrayList<String>();
		// 保存检索排序结果
		Map<String, Double> resMap = new HashMap<String, Double>();

		try {

			imageFile = new FileInputStream(imgPath);
			BufferedImage bufferImage;

			bufferImage = ImageIO.read(imageFile);
			// 获取HSV量化后的颜色直方图数据 一维向量表示
			long start = System.currentTimeMillis();
			
			double[] sourceHist = ColorUtil.getImageHSV(bufferImage);
			
			Map<Integer, Double> sourceHistMap = new LinkedHashMap<Integer, Double>();
			for (int i = 0; i < sourceHist.length; i++) {
				sourceHistMap.put(i, sourceHist[i]);
			}
			
			sourceHistMap = MatchUtil.sortByValue(sourceHistMap);
			sourceHistMap = MatchUtil.getSamePercentage(sourceHistMap, Config.TopPercentage);
			
			long end = System.currentTimeMillis();
			logger.debug("extrect color feature cost: " + (end - start) + "ms");
			
			// 提取数据库数据
//			List<Object> list = DBHelper.fetchALLCloth();
			List<Object> list = DBHelper.fetchClothLimit(Config.LIMITNUMBER);
			

			// 每一条数据库对应的数据
			double tempRes = 0;
			for (int i = 0; i < list.size(); i++) {

				Map<String, Object> map = (Map<String, Object>) list.get(i);

				Object candidateData = map.get("histogram");
				Object candidatePath = map.get("path");
				
				Map<Integer, Double> candidateHistMap = MatchUtil.jsonToMap2((String)candidateData);
				Map<Integer, Double> tempSourceHistMap = new LinkedHashMap<Integer, Double>(sourceHistMap);
				
				// 计算相似度
				tempRes = matchStrategy.similarity(tempSourceHistMap, candidateHistMap);
				
				resMap.put((String) candidatePath, tempRes);
	
				if (tempRes < minDistance)
					minDistance = tempRes;
			
			}
			
			Map<String, Double> resultMap;
//			System.out.println("Min Distance : " + (float) minDistance);
			resultMap = MatchUtil.sortByValueAsc(resMap);

			int counter = 0;
			for (Map.Entry<String, Double> map : resultMap.entrySet()) {

				if (counter >= Config.finalResultNumber)
					break;
				
				matchUrls.add(map.getKey());
				counter++;
				logger.debug(map.getValue() + " " + map.getKey());
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
