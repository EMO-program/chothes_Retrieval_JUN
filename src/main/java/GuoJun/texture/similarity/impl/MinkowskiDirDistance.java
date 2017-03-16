package GuoJun.texture.similarity.impl;
import GuoJun.color.similarity.ifs.ISimilarityArray;
import GuoJun.properties.Config;

public class  MinkowskiDirDistance implements ISimilarityArray {

    private static final double q = 1.0;
    static int mmcount = 1;

    @Override
    public double similarity(double[] data1, double[] data2) {
        // TODO Auto-generated method stub
        int count=0 ,minDir=0;
        double distance;
        double mindistance = Config.MA_SIZE;
        for(int j=0;j<4;j++) {
            count=j;
            distance = 0;
            for(int i=0;i<4;i++) {
                distance += Math.pow(Math.abs(data1[i] - data2[count]), q);
                count=(count+1)%4;
            }
            if(distance<mindistance) {
                mindistance=distance;
                minDir=j;
            }
        }
        distance = Math.pow(Math.abs(data1[4] - data2[4]), q);
        mindistance+=distance;
        if(minDir!=0) mindistance*=1.1;//让原来的距离与最短距离取平均值也不错
        return Math.pow(mindistance, 1 / q);
    }
}