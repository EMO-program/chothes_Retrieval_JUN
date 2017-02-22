package yanjun.database;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import yanjun.properties.Config;
import yanjun.texture.LBP;
import yanjun.texture.LTxXORP;
import yanjun.util.ColorUtil;
import yanjun.util.DBHelper;
import yanjun.util.MatchUtil;

import com.google.gson.Gson;

public class DBStore {

	public static void main(String[] args) {
		String path = null;
	//	LTxXORP.setRotaIvaPats();
		for (int i = 1; i <= 1628; i++) {
			path = "src/main/resources/static/Farbic-MA/" + i + ".jpg";
			storeTextureToDB(path);
			//storeToDB2(path);
		}

		// for (int i = 0; i <= 9; i++) {
		//
		// path = "ukbench/full/ukbench0000" + i + ".jpg";
		// storeToDB(path);
		// }
		//
		// for (int i = 10; i <= 99; i++) {
		// path = "ukbench/full/ukbench000" + i + ".jpg";
		// storeToDB(path);
		// }
		//
		// for (int i = 100; i <= 999; i++) {
		// path = "ukbench/full/ukbench00" + i + ".jpg";
		// storeToDB(path);
		// }
	}
	public static void storeToDB2(String path) {
		FileInputStream imageFile;
		try {
			imageFile = new FileInputStream(path);
			BufferedImage bufferImage;

			bufferImage = ImageIO.read(imageFile);
			// 获取HSV量化后的直方图数据 一维向量表示
			double[] hist = ColorUtil.getImageHSV(bufferImage);

			Map<Integer, Double> histMap = new LinkedHashMap<Integer, Double>();
			for (int i = 0; i < hist.length; i++) {
				histMap.put(i, hist[i]);
			}

			histMap = MatchUtil.sortByValue(histMap);
			histMap = MatchUtil.getSamePercentage(histMap,
			      Config.TopPercentage);

//			for (Entry<Integer, Double> entry : histMap.entrySet()) {
//				System.out.print(entry.getKey() + ":" + entry.getValue() + ", ");
//			}
//			
			String histogramString = new Gson().toJson(histMap);

			// String sql = "update base_cloth set histogram = ? where path = ?";
			String sql = "insert into " + Config.dbTable
			      + " (path, histogram) values (?, ?)";

			// Object[] data = {jsonObject.toString(), path};
			Object[] data = { path, histogramString };
			
			DBHelper.updateData(sql, data);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeToDB(String path) {
		File imageFile = new File(path);

		BufferedImage bf;
		try {
			bf = ImageIO.read(imageFile);
			double[] hist = ColorUtil.getImageHSV(bf);

			Map<Integer, Double> histMap = new HashMap<Integer, Double>();

			for (int i = 0; i < hist.length; i++) {
				histMap.put(i, hist[i]);
			}

			histMap = MatchUtil.sortByValue(histMap);
			histMap = MatchUtil.getSamePercentage(histMap, Config.TopPercentage);

			String histogramString = new Gson().toJson(histMap);

			// String sql = "update base_cloth set histogram = ? where path = ?";
			String sql = "insert into " + Config.dbTable
			      + " (path, histogram) values (?, ?)";

			// Object[] data = {jsonObject.toString(), path};
			Object[] data = { path, histogramString };

			// System.out.println(NormalUtil.translateUkbench2Integer(path));

			// Object[] data = { NormalUtil.translateUkbench2Integer(path),
			// histogramString };

			DBHelper.updateData(sql, data);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void storeTextureToDB(String path) {
		FileInputStream imageFile;
		try {
			imageFile = new FileInputStream(path);
			BufferedImage bufferImage;

			bufferImage = ImageIO.read(imageFile);

			// 获取HSV量化后的直方图数据 一维向量表示
			double[] lbpFeature = LBP.getLBPFeature(path);

			String lbpString = new Gson().toJson(lbpFeature);

			// String sql = "update base_cloth set histogram = ? where path = ?";
			String sql = "insert into " + Config.dbTable
			      + " (path, lbpFeature) values (?, ?)";

			// Object[] data = {jsonObject.toString(), path};
			Object[] data = { path, lbpString };

			DBHelper.updateData(sql, data);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeToDBArr(String path) {
		File imageFile = new File(path);

		BufferedImage bf;
		try {
			bf = ImageIO.read(imageFile);
			double[] hist = ColorUtil.getImageHSV(bf);

			String histogramString = new Gson().toJson(hist);

			// String sql = "update base_cloth set histogram = ? where path = ?";
			String sql = "insert into " + Config.dbTable
			      + " (path, histogram) values (?, ?)";

			Object[] data = { path, histogramString };

			DBHelper.updateData(sql, data);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
