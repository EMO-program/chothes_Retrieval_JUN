package GuoJun.util;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import GuoJun.properties.Config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MatchUtil {

	/**
	 * json格式转为数组
	 * @param data
	 * @return
	 */
	public static double[] jsonToArr (String data) {
		Gson gson = new Gson();
		return gson.fromJson(data, double[].class);
	}
	
	/**
	 * josn格式转为Map格式
	 * 
	 * @param data
	 * @return
	 */
	public static Map<Integer, Double> jsonToMap2(String data) {
		Gson gson = new Gson();
		Type mapType = new TypeToken<Map<Integer, Double>>(){}.getType();
		Map<Integer, Double> map = gson.fromJson(data, mapType);
		return map;
	}


	/**
	 * 按value值进行排序
	 * 
	 * @param map
	 * @return
	 */
	public static Map<Integer, Double> sortByValue(Map<Integer, Double> map) {
		List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(
		      map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {

			@Override
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				// TODO Auto-generated method stub
				if (o2.getValue() - o1.getValue() > 0)
					return 1;
				if (o2.getValue() - o1.getValue() < 0)
					return -1;
				else
					return 0;
			}

		});

		Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
		for (Map.Entry<Integer, Double> entry : list)
			result.put(entry.getKey(), entry.getValue());

		return result;

	}
	
	
	public static Map<String, Double> sortByValueAsc(Map<String, Double> map) {
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(
		      map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				// TODO Auto-generated method stub
//				if (o2.getValue() - o1.getValue() > 0)
//					return -1;
//				if (o2.getValue() - o1.getValue() < 0)
//					return 1;
//				else
//					return 0;
				
				return o1.getValue().compareTo(o2.getValue());
				
				
			}

		});

		Map<String, Double> result = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, Double> entry : list)
			result.put(entry.getKey(), entry.getValue());

		return result;

	}

	/**
	 * 按 Value 值进行排序
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue2(
	      Map<K, V> map) {

		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				// TODO Auto-generated method stub
				return (o1.getValue().compareTo(o2.getValue()));
			}

		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
			result.put(entry.getKey(), entry.getValue());

		return result;

	}

	/**
	 * 获取指定百分比的排序数据
	 * 
	 * @param map
	 * @param percent
	 * @return
	 */
	public static double[] getSamePercentage(double[] arr, float percent) {
		
		double sum = 0;
		int index = 0;
		for (int i = 0; i < arr.length; i++) {
			if (sum > percent)
				break;
			
			index++;
		}
		
		double[] result = new double[index];
		for (int i = 0; i < result.length; i++)
			result[i] = arr[i];
		
		return result;
	}
	
	
	public static Map<Integer, Double> getSamePercentage(
	      Map<Integer, Double> map, float percent) {

		float sum = 0;
		Map<Integer, Double> newMap = new LinkedHashMap<>();
		for (Map.Entry<Integer, Double> resMap : map.entrySet()) {
			if (sum >= percent + Config.FixError)
				break;
			newMap.put(resMap.getKey(), resMap.getValue());
			sum += resMap.getValue();

		}
		return newMap;
	}
}
