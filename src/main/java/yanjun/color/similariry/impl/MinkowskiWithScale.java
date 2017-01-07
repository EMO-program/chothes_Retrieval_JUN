package yanjun.color.similariry.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import yanjun.color.similarity.ifs.ISimilarityMap;
import yanjun.properties.Config;
import yanjun.util.ColorUtil;

public class MinkowskiWithScale implements ISimilarityMap {
	
	static double totalValueDiffCount = 0;

	private static final double r = 1.0;

	static double sameColorNum = 0;
	static double sameColorQuantity = 0;

	static double sameQuantiNum = 0;
	static double sameQuantiArea = 0;

	static List<Integer> list1 = new ArrayList<Integer>();
	static List<Integer> list2 = new ArrayList<Integer>();

	@Override
	public double similarity(Map<Integer, Double> data1,
	      Map<Integer, Double> data2) {
		// TODO Auto-generated method stub
		Map<Integer, Double> maxMap, minMap;

		maxMap = (data1.size() > data2.size()) ? data1 : data2;

		minMap = (data1 == maxMap) ? data2 : data1;
		// minMap = (data1.size() < data2.size()) ? data1 : data2;

		double maxSize = maxMap.size();
		double minSize = minMap.size();

		double count = 1;

		double ratio = maxSize / minSize;
		double inverseRation = 1.0 / ratio;
		double tempRatio = ratio;
		double remain = 0;
		double distance = 0;

		double totalColorDiff = 0;
		double totalValueDiff = 0;

		if (ratio > Config.scaleRatio)
			return Config.MAX_DISTANCE;

		if (minSize < maxSize)
			for (Entry<Integer, Double> entry : minMap.entrySet())
				minMap.put(entry.getKey(), entry.getValue() * inverseRation);

		Iterator<Map.Entry<Integer, Double>> maxIt = maxMap.entrySet().iterator();
		Iterator<Map.Entry<Integer, Double>> minIt = minMap.entrySet().iterator();

		Map.Entry<Integer, Double> minEntry = minIt.next();

		list1.clear();
		list2.clear();
		sameColorNum = 0;
		sameColorQuantity = 0;
		
		sameQuantiNum = 0;
		sameQuantiArea = 0;
		
		int index = 0;
		double threshold = maxSize * Config.sameColorNumPercent;

		while (maxIt.hasNext()) {
			
			if (index++ <= threshold) {
				if (sameColorNum >= Math.floor(threshold) && sameColorQuantity >= Config.sameColorNumPercent)
					return 0;
			}
			
//			if (index < maxSize * threshold) {
//				if (sameQuantiNum > threshold && getColorIntersectionNum(list1, list2) < 3);
//				return Config.MAX_DISTANCE;
//			}

			Map.Entry<Integer, Double> maxEntry = maxIt.next();

			if (count < tempRatio + Config.FixError) {

				// double q = getSimilarity(maxEntry, minEntry);
				// System.out.println("q: " + q + " " + maxEntry.getKey() + ":" +
				// maxEntry.getValue() + ", " + minEntry.getKey() + ":" +
				// minEntry.getValue());
				 list1.add(maxEntry.getKey());
				 list2.add(minEntry.getKey());
				// distance += q;

				compareTwoHist(maxEntry, minEntry);

				double colorDiff = getKeySimilariry(maxEntry.getKey(),
				      minEntry.getKey());
				totalColorDiff += colorDiff;

				double valueDiff = getValueSimilarity(maxEntry.getValue(),
				      minEntry.getValue());
				totalValueDiff += valueDiff;

				if (maxSize != minSize)
					count++;
				// 如果两者长度一样，即不拉伸
				else {
					if (!minIt.hasNext())
						break;
					minEntry = minIt.next();
					// if (!minIt.hasNext())
					// break;
				}
			} else {
				remain = tempRatio - (count - 1);

				Map.Entry<Integer, Double> preMinEntry = minEntry;
				minEntry = minIt.next();

				// double q1 = getSimilarity(maxEntry, preMinEntry);
				// double q2 = getSimilarity(maxEntry, minEntry);
				//
				// distance += remain * q1 + (1 - remain) * q2;
				//
				 list1.add(maxEntry.getKey());
				 list2.add(minEntry.getKey());

				compareTwoHist(maxEntry, preMinEntry);

				double colorDiff1 = getKeySimilariry(maxEntry.getKey(),
				      preMinEntry.getKey());
				double colorDiff2 = getKeySimilariry(maxEntry.getKey(),
				      minEntry.getKey());
				totalColorDiff += remain * colorDiff1 + (1 - remain) * colorDiff2;

				compareTwoHist(maxEntry, minEntry);

				double valueDiff1 = getValueSimilarity(maxEntry.getValue(),
				      preMinEntry.getValue());
				double valueDiff2 = getValueSimilarity(maxEntry.getValue(),
				      minEntry.getValue());
				totalValueDiff += remain * valueDiff1 + (1 - remain) * valueDiff2;

				count = 1;
				tempRatio = ratio - (1 - remain);

			}

		}

		// return Math.pow(distance, 1 / r);

		totalColorDiff = totalColorDiff / maxSize;

		if (totalColorDiff == 0 && totalValueDiff == 0)
			return 0;

//		if (totalColorDiff == 0) {
//			return 0;
//		}
		// return Math.sqrt(Math.pow(totalValueDiff, 1/r));
		// return Math.pow(totalValueDiff, 1/2);
		// return Math.pow(Math.pow(totalValueDiff, 1/r), 2);

//		if (totalValueDiff == 0) {
//			// return HSVfactor;
//			// return Math.sqrt(totalColorDiff);
//			totalValueDiffCount++;
//			return Math.pow(totalColorDiff, 1 / 2);
//		}

//		System.out.println("totalValueDiffCount: " + totalValueDiffCount);
		
//		 return totalColorDiff * Math.pow(totalValueDiff, 1/r);

//		return 0.7 * totalColorDiff + 0.3 * Math.pow(totalValueDiff, 1 / r);
		
		return totalColorDiff * (1 - Math.pow(totalValueDiff, 1/r));

		// // 颜色值相同情况下, 相同颜色数量大于C同时其统计量大于一半以上
		// if (sameColorNum / maxSize >= 0.8 && sameColorQuantity >= 0.6)
		// return 0;
		//
		// // 统计量相同情况下, 其所占统计量达Q, 判断其颜色的交集是否满足一定比例
		// if (sameQuantiArea / 0.98 >= 0.8)
		// if (getColorIntersectionNum(list1, list2) / maxSize >= 0.7 )
		// return 0;
		//
		// return distance;

	}

	private static double getColorIntersectionNum(List<Integer> data1,
	      List<Integer> data2) {

		Set<Integer> set = new HashSet<Integer>();
		addList2Set(set, list1);
		addList2Set(set, list2);

		return set.size();
	}

	private static void addList2Set(Set<Integer> set, List<Integer> list) {
		for (Integer i : list)
			set.add(i);
	}

	private static void compareTwoHist(Map.Entry<Integer, Double> d1,
	      Map.Entry<Integer, Double> d2) {
		int[] HSVArr1 = ColorUtil.HSVtoArray(d1.getKey());
		int[] HSVArr2 = ColorUtil.HSVtoArray(d2.getKey());
		double HSVfactor = getHSVFactor(HSVArr1, HSVArr2);

		double quantity1 = d1.getValue();
		double quantity2 = d2.getValue();
		double quaSimilarity = Math.abs(quantity1 - quantity2);
		
		if (HSVfactor == 0) {
			sameColorNum ++;
			sameColorQuantity += (quantity1 + quantity2) / 2;
		}
		
		if (quaSimilarity == 0) {
			// return HSVfactor;
			sameQuantiNum ++;
			sameQuantiArea += quantity1;
		}
		
	}

	private static double getSimilarity(Map.Entry<Integer, Double> d1,
	      Map.Entry<Integer, Double> d2) {

		int[] HSVArr1 = ColorUtil.HSVtoArray(d1.getKey());
		int[] HSVArr2 = ColorUtil.HSVtoArray(d2.getKey());
		double HSVfactor = getHSVFactor(HSVArr1, HSVArr2);

		double quantity1 = d1.getValue();
		double quantity2 = d2.getValue();
		double quaSimilarity = Math.abs(quantity1 - quantity2);

		if (HSVfactor == 0 && quaSimilarity == 0)
			return 0;

		if (HSVfactor == 0) {
			sameColorNum++;
			sameColorQuantity += (quantity1 + quantity2) / 2;
			return quaSimilarity;
		}

		if (quaSimilarity == 0) {
			// return HSVfactor;
			sameQuantiNum++;
			sameQuantiArea += quantity1;
			return HSVfactor;
		}

		// return Math.pow(quaSimilarity, q) * Math.pow(HSVfactor, q);
		// return Math.pow(quaSimilarity, r) * HSVfactor;
		return quaSimilarity * HSVfactor;
	}

	private static double getValueSimilarity(double v1, double v2) {
		double quaSimilarity = Math.abs(v1 - v2);
		return Math.pow(quaSimilarity, r);
	}

	private static double getKeySimilariry(Integer k1, Integer k2) {
		int[] HSVArr1 = ColorUtil.HSVtoArray(k1);
		int[] HSVArr2 = ColorUtil.HSVtoArray(k2);
		double HSVfactor = getHSVFactor(HSVArr1, HSVArr2);

		return HSVfactor;
	}

	static double HFactor = 1f / Config.H_DIMENSION * 0.7;
	static double SFactor = 1f / Config.S_DIMENSION * 0.15;
	static double IFactor = 1f / Config.I_DIMENSION * 0.15;

	private static double getHSVFactor(int[] HSV1, int[] HSV2) {

		// double total1 = HSV1[0] + HSV1[1] + HSV1[2];
		// double total2 = HSV2[0] + HSV2[1] + HSV2[2];
		//
		// return Math.abs(HSV1[0]/total1 - HSV2[0]/total2)*0.7
		// + Math.abs(HSV1[1]/total1 - HSV2[1]/total2)*0.2
		// + Math.abs(HSV1[2]/total1 - HSV2[2]/total2)*0.1;

//		 return Math.abs(HSV1[0] - HSV2[0]) * HFactor + Math.abs(HSV1[1] -
//		 HSV2[1]) * SFactor + Math.abs(HSV1[2] - HSV2[2]) * IFactor;

		double res1 = (HSV1[0] - HSV2[0]) == 0 ? 0 : Math.pow(
		      (HSV1[0] - HSV2[0]), 2) * HFactor;
		double res2 = (HSV1[1] - HSV2[1]) == 0 ? 0 : Math.pow(
		      (HSV1[1] - HSV2[1]), 2) * SFactor;
		double res3 = (HSV1[2] - HSV2[2]) == 0 ? 0 : Math.pow(
		      (HSV1[2] - HSV2[2]), 2) * IFactor;

		return Math.sqrt(res1 + res2 + res3);

	}

}
