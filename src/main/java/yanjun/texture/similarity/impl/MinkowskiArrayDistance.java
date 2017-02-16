package yanjun.texture.similarity.impl;

import yanjun.color.similarity.ifs.ISimilarityArray;

public class MinkowskiArrayDistance implements ISimilarityArray{

	private static final double q = 1.0;
	static int mmcount=1;
	@Override
   public double similarity(double[] data1, double[] data2) {
	   // TODO Auto-generated method stub
		int i = 0;
		double distance = 0;
		while (i < data1.length && i < data2.length) {
			distance += Math.pow(Math.abs(data1[i] - data2[i]), q);
			i++;
		}

	   return Math.pow(distance, 1/q);
   }

}
