package GuoJun.texture.similarity.impl;

import GuoJun.color.similarity.ifs.ISimilarityArray;

public class MinkowskiArrayDistance implements ISimilarityArray{

	public final static int MATHOD = 1; // 1:minkowskiDistance ,  2: canberra , 3:d1
	private static final double q = 1.0;
	static int mmcount=1;
	@Override
   public double similarity(double[] data1, double[] data2) {
	   // TODO Auto-generated method stub
		double distance;
		int i;
		if(MATHOD ==1){
			 i = 0;
			distance = 0;
			while (i < data1.length && i < data2.length) {
				distance += Math.pow(Math.abs(data1[i] - data2[i]), q);
				i++;
			}
		}
		if(MATHOD ==2){
			i = 0;
			distance = 0;
			while (i < data1.length && i < data2.length) {
				distance += ( Math.abs(data1[i] - data2[i]) )/( Math.abs(data1[i]) + Math.abs(data2[i]) );
				i++;
			}
		}
		if(MATHOD ==3){
			i = 0;
			distance = 0;
			while (i < data1.length && i < data2.length) {
				distance += Math.abs( (data1[i] - data2[i]) / ( 1 + data1[i] + data2[i]) );
				i++;
			}
		}
		return Math.pow(distance, 1 / q);
   }

}
