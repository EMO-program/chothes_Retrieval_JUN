package yanjun.color.similarity.ifs;

import java.util.Map;

public interface ISimilarityMap {

	public double similarity(Map<Integer, Double> data1, Map<Integer, Double> data2);
}
