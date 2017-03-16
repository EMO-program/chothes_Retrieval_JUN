package GuoJun.texture;
import GuoJun.color.similarity.ifs.ISimilarityArray;
import GuoJun.texture.similarity.impl.MinkowskiArrayDistance;
import GuoJun.texture.similarity.impl.MinkowskiDirDistance;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

public class LTxXORP {
    final static double PI = 3.14159265358979323846;

    static final int IR_UNIPATS[] = { 0, 1, 2, 4, 8, 16, 27, 34, 35 };
    static final int PREDICATE = 1;//LBP半径R
    static final int GRAY_DIMENSION=8;//灰度空间量化quantization----代号Q

    static final int OPERATOR =1;//  1：正常lbp   2：异或   3：同或
    static final int GETGRAY_M =2;//  1：通过心理学公式提取灰度空间   2：按照RGB和HSV的转换公式提取V
    static final int TEXMODE =3;//  1：标准8纹理基元模式   2：变种15纹理基元模式  3：抗旋转6纹理基元模式
    static final boolean USE_TEXTON =true;// 表示是否使用纹理基元模式
    static final int BITS = 8;//LBP邻点数量P
    static final int DIFF = PREDICATE << 1;//边缘点的数量？
    static final int LBPMODE =  4;// 1：无删减lbp   2：统一模式LBP   3：旋转不变LBP  4：旋转统一LBP
    public static final int PATSNUM = 58;//uniform patterns 数
    static final int ROTSUM=36;//旋转置换群特征数
    static final int ROTADDPAT=9;//旋转置换群加uniform特征数
    static final double PWEIGHT=1;//设置为最大值10000表示不使用相位特征
    static final int TEXTONLEVEL=1;
    static final boolean USECUT=true;
    static final int LBPNUM =  PATSNUM;
    static final boolean USE_GLCM = false;
    // uniform patterns
    static final int UNIPATS[] = { 0, 1, 2, 3, 4, 6, 7, 8, 12, 14, 15, 16, 24,
            28, 30, 31, 32, 48, 56, 60, 62, 63, 64, 96, 112, 120, 124, 126, 127,
            128, 129, 131, 135, 143, 159, 191, 192, 193, 195, 199, 207, 223, 224,
            225, 227, 231, 239, 240, 241, 243, 247, 248, 249, 251, 252, 253, 254,
            255 };


    class IntegerPoint {
        int x;
        int y;
    }

    class DoublePoint {
        double x;
        double y;
    }
    //圆形LBP邻点坐标辅助计算
    IntegerPoint[] points;
    DoublePoint[] offsets;



    public LTxXORP() {
        int i;
        offsets = new DoublePoint[BITS];
        points = new IntegerPoint[BITS];

        for (i = 0; i < BITS; i++) {
            points[i] = new IntegerPoint();
            offsets[i] = new DoublePoint();
        }
    }

    //判断两个数是否属于同一个旋转置换群
    public static boolean JudgeRotationSim(int feature1,int feature2){

        String f1= Integer.toBinaryString(feature1);//十进制转二进制
        int zero1=BITS-f1.length();
        if(zero1>0){
            for(int i=0;i<zero1;i++)
                f1="0"+f1;
        }
        String f2= Integer.toBinaryString(feature2);//十进制转二进制
       int zero2=BITS-f2.length();
        if(zero2>0){
            for(int i=0;i<zero2;i++)
                f2="0"+f2;
        }
        for(int i=0;i<BITS;i++){
            if(f1.equals(f2)) return true;
            else{
                f2=f2.substring(1)+f2.substring(0,1);
            }
        }
        return false;
    }

   static int[][] RotaIvaPats=new int[ROTSUM][BITS];//设置为静态对象只计算一次
    //初始化旋转置换群
    public static void setRotaIvaPats(){

        int [] Rmark=new int[ROTSUM];
        for(int l=0;l<ROTSUM;l++)
            Rmark[l]=0;
        //初始化
        for(int i=0;i<ROTSUM;i++)
            for(int j=0;j<BITS;j++)
                RotaIvaPats[i][j]=-1;

        for(int k=0;k<256;k++){
            for(int n=0;n<ROTSUM;n++){
                if(RotaIvaPats[n][0]==-1){
                    RotaIvaPats[n][0]=k;
                    Rmark[n]++;
                    break;
                }
                else if(JudgeRotationSim(k,RotaIvaPats[n][0])){
                    RotaIvaPats[n][ Rmark[n]++ ]=k;
                    break;
                }
            }
        }

        //用uniform模式再筛选一次特征
//        for(int i=0;i<ROTSUM;i++){
//            for(int j=0;j<BITS;j++) {
//                for(int k=0;k<PATSNUM;k++){
//                    if(RotaIvaPats[i][j]==UNIPATS[k])   break;
//                    else if(k==PATSNUM-1)
//                        RotaIvaPats[i][j]=-1;
//                }
//                System.out.print(RotaIvaPats[i][j] + ",");
//            }
//           System.out.println();
//        }
//        int i=0;
    }

    void compareNeighbors(int[] value, int center, int neighbor, int shift) {
        switch (OPERATOR) {
            case 1:
            if (center <= neighbor) {
                value[0] |= 1 << shift;
            } else {
                value[0] |= 0 << shift;
            }
            break;

            case 2:
                if (center != neighbor) {
                    value[0] |= 1 << shift;
                } else {
                    value[0] |= 0 << shift;
                }
                break;
            case 3:
                if (center == neighbor) {
                    value[0] |= 1 << shift;
                } else {
                    value[0] |= 0 << shift;
                }
                break;

        }
    }

    void calculatePoints() {
        int bit;

        double step = 2 * PI / BITS;
        double tmpX, tmpY;
        //圆形LBP实现原理
        for (bit = 0; bit < BITS; bit++) {
            // bit * step 为角度
            tmpX = PREDICATE * Math.cos(bit * step);
            tmpY = PREDICATE * Math.sin(bit * step);

            points[bit].x = (int) tmpX;
            points[bit].y = (int) tmpY;

            offsets[bit].x = tmpX - points[bit].x;
            offsets[bit].y = tmpY - points[bit].y;

            if (offsets[bit].x < 1.0e-10 && offsets[bit].x > -1.0e-10) {
                offsets[bit].x = 0;
            }
            if (offsets[bit].y < 1.0e-10 && offsets[bit].y > -1.0e-10) {
                offsets[bit].y = 0;
            }

            if (tmpX < 0 && offsets[bit].x != 0) {
                points[bit].x -= 1;
                offsets[bit].x += 1;
            }

            if (tmpY < 0 && offsets[bit].y != 0) {
                points[bit].y -= 1;
                offsets[bit].y += 1;
            }

//			System.out.println("x: " + points[bit].x + " y: " + points[bit].y);
//			System.out.println(" offset x: " + offsets[bit].x + " offset y: " + offsets[bit].y);
        }
    }
    //处理对角线上的邻点（1，2，4，6），计算其坐标位置并计算出它的灰度值返回结果
    double interpolate(int ul, int rl, int lb, int rb, int i) {
        double dx = 1 - offsets[i].x;
        double dy = 1 - offsets[i].y;

        return ul * dx * dy + rl * offsets[i].x * dy + lb * dx * offsets[i].y
                + rb * offsets[i].x * offsets[i].y;
    }

    /**
     *
     * @param data 图像灰度值
     * @param row 图像高度
     * @param column 图像宽度
     * @param hist81 特征值数组
     */
    void histogram81(int[] data, int row, int column, int[] hist81) {
        int i;
        int r, c;
        int[] value = new int[] { 0 };

        int leap = column * PREDICATE;

        // nb - index of points（8个邻点坐标计算）
        int[] nb = new int[8];

        nb[0] = 0;
        nb[1] = nb[0] + PREDICATE;
        nb[2] = nb[1] + PREDICATE;
        nb[3] = nb[2] + leap;
        nb[4] = nb[3] + leap;
        nb[5] = nb[4] - PREDICATE;
        nb[6] = nb[5] - PREDICATE;
        nb[7] = nb[6] - leap;



        // center - index of center point
        int center = nb[7] + PREDICATE;

        nb[0] = center + points[5].x + points[5].y * column;
        nb[2] = center + points[7].x + points[7].y * column;
        nb[4] = center + points[1].x + points[1].y * column;
        nb[6] = center + points[3].x + points[3].y * column;


//		for (int j = 0; j < nb.length; j++)
//			System.out.println(nb[j]);
        //遍历时忽略DIFF个边缘点
        for (r = 0; r < row - DIFF; r++) {
            for (c = 0; c < column - DIFF; c++) {
                value[0] = 0;//存储LBP特征值

                compareNeighbors(value, data[center], data[nb[1]], 1);
                compareNeighbors(value, data[center], data[nb[3]], 3);
                compareNeighbors(value, data[center], data[nb[5]], 5);
                compareNeighbors(value, data[center], data[nb[7]], 7);

                compareNeighbors(
                        value,
                        data[center],
                        (int) (interpolate(data[nb[0]], data[nb[0] + 1], data[nb[0]
                                + column], data[nb[0] + column + 1], 5) + 0.5), 0);
                compareNeighbors(
                        value,
                        data[center],
                        (int) (interpolate(data[nb[2]], data[nb[2] + 1], data[nb[2]
                                + column], data[nb[2] + column + 1], 7) + 0.5), 2);
                compareNeighbors(
                        value,
                        data[center],
                        (int) (interpolate(data[nb[4]], data[nb[4] + 1], data[nb[4]
                                + column], data[nb[4] + column + 1], 1) + 0.5), 4);
                compareNeighbors(
                        value,
                        data[center],
                        (int) (interpolate(data[nb[6]], data[nb[6] + 1], data[nb[6]
                                + column], data[nb[6] + column + 1], 3) + 0.5), 6);

                for (i = 0; i < 8; i++) {
                    nb[i]++;
                }

                center++;
                //统计各个LBP值的出现频率
                hist81[value[0]]++;
            }

            for (i = 0; i < 8; i++) {
                nb[i] += DIFF;
            }

            center += DIFF;
        }
    }

    public void Subdata(int[]data,int[][]subdata,int width, int height ){
        String test="";
        int Swidth,Sheight;
        if(width%2!=0)  Swidth=width-1;
        else Swidth=width;
        if(height%2!=0)  Sheight=height-1;
        else Sheight=height;
        for (int i = 0; i < Sheight; i++) {
                for (int j = 0; j < Swidth; j++) {
                   if(i<Sheight/2&&j<Swidth/2)
                   {subdata[0][i*(Swidth/2)+j]=data[i*width+j];}
                     if(i<(Sheight/2)&&j>=Swidth/2)
                     { subdata[1][i*(Swidth/2)+j-Swidth/2]=data[i*width+j];}
                      if(i>=(Sheight/2)&&j<Swidth/2)
                      { subdata[2][(i-(Swidth/2))*(Swidth/2)+j]=data[i*width+j];}
                        if(i>=(Sheight/2)&&j>=Swidth/2)
                        { subdata[3][(i-(Swidth/2))*(Swidth/2)+j-Swidth/2]=data[i*width+j];}
//                    if(i==200&&j==260) {
//                       System.out.print("test");
//                    }
                   // test="出错区间"+Integer.toString(i)+"和"+Integer.toString(j);
                   // System.out.print(test);
                }

            }
      //  System.out.print(test);
        //原始数据
//        for (int i = 0; i < height; i++) {
//            for (int j = 0; j < width; j++) {
//                System.out.print(data[i*width+j]+" ");
//            }
//            System.out.println();
//        }
//
//        for(int c=0;c<4;c++) {
//            System.out.println("data"+c+"数据为：");
//            for (int i = 0; i < height / 2; i++) {
//                for (int j = 0; j < width / 2; j++) {
//                    System.out.print(subdata[c][i * (width / 2) + j] + " ");
//                }
//                System.out.println();
//            }
//        }


    }




    //计算快速LBP特征。
    public int fastlbpFeature(int[][] Textonimg, int height, int width, double[][] feature) {
        double[][]distance=new double[4][4];
        int resulttexton=0;
        int i;
        int sum0 = 0,sum1 = 0,sum2 = 0,sum3 = 0;//图像被计算的总像素数
        MinkowskiArrayDistance textureStrategy=new MinkowskiArrayDistance();
      //  int sumpats = 0;//uniform模式的总像素数

        calculatePoints();
        //用于存储uniform统一化截减前的完整LBP特征
        int[] histFull0 = new int[256];
        int[] histFull1 = new int[256];
        int[] histFull2 = new int[256];
        int[] histFull3 = new int[256];

        for (i = 0; i < 256; i++) {
            histFull0[i] = 0;
            histFull1[i] = 0;
            histFull2[i] = 0;
            histFull3[i] = 0;
        }
        //开始计算LBP特征值并将结果存放至histFull中
        histogram81(Textonimg[0], height, width, histFull0);
        histogram81(Textonimg[1], height, width, histFull1);
        histogram81(Textonimg[2], height, width, histFull2);
        histogram81(Textonimg[3], height, width, histFull3);

//		for (int j = 0; j < histFull.length; j++)
//			System.out.println(histFull[j]);
/////////////////用旋转置换群重新分配特征值/////////////////////////////////////////////////
        for (i = 0; i < ROTSUM; i++) {
            for(int j=0;j<BITS;j++) {
                if(RotaIvaPats[i][j]!=-1) {
                    feature[0][i] += histFull0[RotaIvaPats[i][j]];
                    feature[1][i] += histFull1[RotaIvaPats[i][j]];
                    feature[2][i] += histFull2[RotaIvaPats[i][j]];
                    feature[3][i] += histFull3[RotaIvaPats[i][j]];
                }
            }
           // sumpats += feature[i];
        }
        ///////////////////将符合uniform模式的LBP特征值提取出来作为最终纹理特征/////////
//        for (i = 0; i < PATSNUM; i++) {
//            feature[i] = histFull[UNIPATS[i]];
//            sumpats += feature[i];
//        }


        for (i = 0; i < 256; i++) {
            sum0 += histFull0[i];
            sum1 += histFull1[i];
            sum2 += histFull2[i];
            sum3 += histFull3[i];
        }


        //统一化（将特征转成百分比频率）
        for (i = 0; i < feature[0].length; i++) {
            feature[0][i] = feature[0][i] / (float) sum0;
            feature[1][i] = feature[1][i] / (float) sum1;
            feature[2][i] = feature[2][i] / (float) sum2;
            feature[3][i] = feature[3][i] / (float) sum3;
        }
           for(int j=0;j<4;j++){
               for(int k=0;k<4;k++){
                 distance[j][k] = textureStrategy.similarity(feature[j], feature[k]);
                   System.out.print(distance[j][k]+"   ");
               }
               System.out.println();
        }

        for(int j=0;j<4;j++){
            for(int k=0;k<4;k++){

                if(distance[j][k]<0.5)  resulttexton=1;
            }
        }
return resulttexton;
    }

    //将灰度图片执行lbp算法
    public void lbpFeature(int[] Textonimg, int height, int width, double[] feature) {
        int i;
        int sum = 0;//图像被计算的总像素数
        int sumpats = 0;//uniform模式的总像素数

        calculatePoints();
        //用于存储uniform统一化截减前的完整LBP特征
        int[] histFull = new int[256];

        for (i = 0; i < 256; i++) {
            histFull[i] = 0;
        }
        //开始计算LBP特征值并将结果存放至histFull中
        histogram81(Textonimg, height, width, histFull);

//		for (int j = 0; j < histFull.length; j++)
//			System.out.println(histFull[j]);
/////////////////用旋转置换群重新分配特征值/////////////////////////////////////////////////
        if(LBPNUM==ROTSUM){
        for (i = 0; i < ROTSUM; i++) {
            for(int j=0;j<BITS;j++) {
                if(RotaIvaPats[i][j]!=-1)
                  feature[i] += histFull[RotaIvaPats[i][j]];
            }
            sumpats += feature[i];
        }
        }
        /////////////////将符合uniform模式的LBP特征值提取出来作为最终纹理特征/////////
        else if(LBPNUM==PATSNUM) {
            for (i = 0; i < PATSNUM; i++) {
                feature[i] = histFull[UNIPATS[i]];
                sumpats += feature[i];
            }
        }

        for (i = 0; i < 256; i++) {
            sum += histFull[i];
        }

        feature[LBPNUM] = sum - sumpats;//特征向量最后一位存储全部计算像素与uniform截取的像素的差值,旋转模式此值等于0；PATSNUM
        //统一化（将特征转成百分比频率）
        for (i = 0; i < LBPNUM+1; i++) {
            feature[i] = feature[i] / (float) sum;
        }
    int id=1;

    }
    /**
     *
     *  data 图像灰度值
     *  row 图像高度
     *  column 图像宽度
     *  TextonHist texton图像特征值数组
     */
    //获得texton图像
    int TextonDetecte(int num1,int num2,int num3,int num4,double[]DirPhase){
        int textonIndex=0;
        int cut1,cut2,cut3;
        if(USECUT) {
            cut1 = 256 / GRAY_DIMENSION / 3;
            cut2 = cut1 * 2;
           // cut3 = cut1 * 3;
        }
        else{
             cut1 = 300;
             cut2 = 300;
             //cut3 = 300;
        }
        switch (TEXMODE) {
            case 1://标准8纹理基元
         if(num1==num2&&num3!=num4) textonIndex=1;
         else if(num2==num4&&num1!=num3) textonIndex=2;
         else if(num3==num4&&num1!=num2) textonIndex=3;
         else if(num1==num3&&num2!=num4) textonIndex=4;
         else if(num1==num4&&num2!=num3) textonIndex=5;
         else if(num2==num3&&num1!=num4) textonIndex=6;
         else if(num2==num3&&num1==num4&&num1==num2) textonIndex=7;
            break;

            case 2://变种15纹理基元
            if (num1 == num2 && num3 != num4 && num1 != num3 && num1 != num4){
                textonIndex = 1;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[2]++;
            }
            else if (num3 == num4 && num1 != num2 && num3 != num1 && num3 != num2) {
                textonIndex = 2;
                if(num3>cut1&&num3<cut2)textonIndex+=14;
                else if(num3>cut2) textonIndex+=28;
                DirPhase[2]++;
            }
            else if (num3 == num4 && num1 == num2 && num3 != num1) {
                textonIndex = 6;
                DirPhase[2]+=2;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
            }
            else if (num2 == num4 && num1 != num3 && num2 != num1 && num2 != num3) {
                textonIndex = 4;
                if(num2>cut1&&num2<cut2)textonIndex+=14;
                else if(num2>cut2) textonIndex+=28;
                DirPhase[0]++;
            }
            else if (num1 == num3 && num2 != num4 && num1 != num2 && num1 != num4) {
                textonIndex = 5;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[0]++;
            }
            else if (num1 == num3 && num2 == num4 && num1 != num2) {
                textonIndex = 6;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[0]+=2;
            }
            else if (num1 == num4 && num2 != num3 && num1 != num2 && num1 != num3) {
                textonIndex = 7;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[3]++;
            }
            else if (num2 == num3 && num1 != num4 && num2 != num1 && num2 != num4) {
                textonIndex = 8;
                if(num2>cut1&&num2<cut2)textonIndex+=14;
                else if(num2>cut2) textonIndex+=28;
                DirPhase[1]++;
            }
            else if (num2 == num3 && num1 == num4 && num2 != num1) {
                textonIndex = 9;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[3]++;DirPhase[1]++;
            }
            else if (num1 == num2 && num2 == num3 && num1 != num4) {
                textonIndex = 10;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[4]++;
            }
            else if (num1 == num2 && num2 == num4 && num1 != num3) {
                textonIndex = 11;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[4]++;
            }
            else if (num2 == num3 && num3 == num4 && num2 != num1) {
                textonIndex = 12;
                if(num2>cut1&&num2<cut2)textonIndex+=14;
                else if(num2>cut2) textonIndex+=28;
                DirPhase[4]++;
            }
            else if (num1 == num3 && num3 == num4 && num2 != num1) {
                textonIndex = 13;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
                DirPhase[4]++;
            }
            else if (num1 == num2 && num2 == num3 && num3 == num4) {
                textonIndex = 14;
                if(num1>cut1&&num1<cut2)textonIndex+=14;
                else if(num1>cut2) textonIndex+=28;
            }
            else if (num1 != num2 && num1 != num3 && num1 != num4 && num2 != num3 && num2 != num4 && num3 != num4) textonIndex = 0;
            break;
            case 3://变种5+4+4纹理基元
                if (num1 == num2 && num3 != num4 && num1 != num3 && num1 != num4){
                    textonIndex = 1;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[2]++;
                }
                else if (num3 == num4 && num1 != num2 && num3 != num1 && num3 != num2) {
                    textonIndex = 1;
                    if(num3>cut1&&num3<cut2)textonIndex+=4;
                    else if(num3>cut2) textonIndex+=4;
                    DirPhase[2]++;
                }
                else if (num3 == num4 && num1 == num2 && num3 != num1) {
                    textonIndex = 2;
                    DirPhase[2]+=2;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                }
                else if (num2 == num4 && num1 != num3 && num2 != num1 && num2 != num3) {
                    textonIndex = 1;
                    if(num2>cut1&&num2<cut2)textonIndex+=4;
                    else if(num2>cut2) textonIndex+=4;
                    DirPhase[0]++;
                }
                else if (num1 == num3 && num2 != num4 && num1 != num2 && num1 != num4) {
                    textonIndex = 1;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[0]++;
                }
                else if (num1 == num3 && num2 == num4 && num1 != num2) {
                    textonIndex = 2;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[0]+=2;
                }
                else if (num1 == num4 && num2 != num3 && num1 != num2 && num1 != num3) {
                    textonIndex = 1;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[3]++;
                }
                else if (num2 == num3 && num1 != num4 && num2 != num1 && num2 != num4) {
                    textonIndex = 1;
                    if(num2>cut1&&num2<cut2)textonIndex+=4;
                    else if(num2>cut2) textonIndex+=4;
                    DirPhase[1]++;
                }
                else if (num2 == num3 && num1 == num4 && num2 != num1) {
                    textonIndex = 2;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[3]++;DirPhase[1]++;
                }
                else if (num1 == num2 && num2 == num3 && num1 != num4) {
                    textonIndex = 3;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[4]++;
                }
                else if (num1 == num2 && num2 == num4 && num1 != num3) {
                    textonIndex = 3;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[4]++;
                }
                else if (num2 == num3 && num3 == num4 && num2 != num1) {
                    textonIndex = 3;
                    if(num2>cut1&&num2<cut2)textonIndex+=4;
                    else if(num2>cut2) textonIndex+=4;
                    DirPhase[4]++;
                }
                else if (num1 == num3 && num3 == num4 && num2 != num1) {
                    textonIndex = 3;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                    DirPhase[4]++;
                }
                else if (num1 == num2 && num2 == num3 && num3 == num4) {
                    textonIndex = 4;
                    if(num1>cut1&&num1<cut2)textonIndex+=4;
                    else if(num1>cut2) textonIndex+=4;
                }
                else if (num1 != num2 && num1 != num3 && num1 != num4 && num2 != num3 && num2 != num4 && num3 != num4) textonIndex = 0;
                break;
            case 4://变种15纹理基元
                if (num1 == num2 && num3 != num4 && num1 != num3 && num1 != num4){
                    textonIndex = 1;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[2]++;
                }
                else if (num3 == num4 && num1 != num2 && num3 != num1 && num3 != num2) {
                    textonIndex = 2;
                    if(num3>cut1&&num3<cut2)textonIndex+=14;
                    else if(num3>cut2) textonIndex+=28;
                    DirPhase[2]++;
                }
                else if (num3 == num4 && num1 == num2 && num3 != num1) {
                    textonIndex = 7;
                    DirPhase[2]+=2;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                }
                else if (num2 == num4 && num1 != num3 && num2 != num1 && num2 != num3) {
                    textonIndex = 3;
                    if(num2>cut1&&num2<cut2)textonIndex+=14;
                    else if(num2>cut2) textonIndex+=28;
                    DirPhase[0]++;
                }
                else if (num1 == num3 && num2 != num4 && num1 != num2 && num1 != num4) {
                    textonIndex = 4;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[0]++;
                }
                else if (num1 == num3 && num2 == num4 && num1 != num2) {
                    textonIndex = 8;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[0]+=2;
                }
                else if (num1 == num4 && num2 != num3 && num1 != num2 && num1 != num3) {
                    textonIndex = 5;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[3]++;
                }
                else if (num2 == num3 && num1 != num4 && num2 != num1 && num2 != num4) {
                    textonIndex = 6;
                    if(num2>cut1&&num2<cut2)textonIndex+=14;
                    else if(num2>cut2) textonIndex+=28;
                    DirPhase[1]++;
                }
                else if (num2 == num3 && num1 == num4 && num2 != num1) {
                    textonIndex = 9;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[3]++;DirPhase[1]++;
                }
                else if (num1 == num2 && num2 == num3 && num1 != num4) {
                    textonIndex = 10;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[4]++;
                }
                else if (num1 == num2 && num2 == num4 && num1 != num3) {
                    textonIndex = 11;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[4]++;
                }
                else if (num2 == num3 && num3 == num4 && num2 != num1) {
                    textonIndex = 12;
                    if(num2>cut1&&num2<cut2)textonIndex+=14;
                    else if(num2>cut2) textonIndex+=28;
                    DirPhase[4]++;
                }
                else if (num1 == num3 && num3 == num4 && num2 != num1) {
                    textonIndex = 13;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                    DirPhase[4]++;
                }
                else if (num1 == num2 && num2 == num3 && num3 == num4) {
                    textonIndex = 14;
                    if(num1>cut1&&num1<cut2)textonIndex+=14;
                    else if(num1>cut2) textonIndex+=28;
                }
                else if (num1 != num2 && num1 != num3 && num1 != num4 && num2 != num3 && num2 != num4 && num3 != num4) textonIndex = 0;
                break;
        }
          return textonIndex;
    }
    void getTextonImg(int[] data, int row, int column, int[] TextonHist,double[] DirPhase){
        //初始化方向数组
        for (int i = 0; i < 5; i++)    DirPhase[i] = 0;
        int Srow,Scolumn;
        if(row%2!=0) Srow=row-1;
        else Srow=row;
        if(column%2!=0) Scolumn=column-1;
        else Scolumn=column;
        int index=0;
        for (int i = 0; i < Srow; i=i+2) {
            for (int j = 0; j < Scolumn; j=j+2) {
                TextonHist[index++] = TextonDetecte(data[i * column + j],data[i * column + j+1],data[(i+1) * column + j],data[(i+1) * column + j+1],DirPhase);
            }
        }


    }
    //特征提取方法
    public static double[] getLBPFeature(String imgPath) {
        int i, j,GNUM;
        double[] feature;
        if(USE_GLCM) GNUM=4;
        else GNUM=0;
        // LBP feature
       if(USE_TEXTON) {
           feature = new double[LTxXORP.LBPNUM + 1 + 5 + GNUM];
       }
        else   feature = new double[LTxXORP.LBPNUM + 1 + GNUM];
        int height; // Height of Image
        int width; // Width of Image
        int[] data; // Block Data
        int[] TextonHist;//纹理基元图
        double[] DirPhase;
        BufferedImage img;
        try {
            img = ImageIO.read(new File(imgPath));//读取参数路径的图片数据
            height = img.getHeight();
            width = img.getWidth();

            data = new int[height * width];

            DirPhase=new double[5];
            LTxXORP LTxXORP = new LTxXORP();

            //初始化相位特征
            for( i=0;i<5;i++){
                DirPhase[i]=0;
            }
            float Gpercent = 1f/GRAY_DIMENSION;
            //著名的心理学公式
            // 彩色图像转换成无彩色的灰度图像Y=0.299*R + 0.578*G + 0.114*B
            for (i = 0; i < width; i++) {
                for (j = 0; j < height; j++) {
                    Color c = new Color(img.getRGB(i, j));
                     if(GETGRAY_M==1){
                    data[j * width + i] = (int) (0.299 * c.getRed() + 0.578
                            * c.getGreen() + 0.114 * c.getBlue());
                     }
                    else if(GETGRAY_M==2) {
                         data[j * width + i] = Math.max(Math.max(c.getRed(), c.getGreen()), c.getBlue());
                     }
                    data[j * width + i]= (int) Math.floor( data[j * width + i] * Gpercent );
                }
            }
            //先将灰度图像转换成纹理基元图像

            if(USE_TEXTON){
                TextonHist=new int[(height/2) *(width/2)];
                LTxXORP.getTextonImg(data, height, width, TextonHist, DirPhase);
                int  []subTextonHist=TextonHist.clone();
                int  []tempTextonHist=TextonHist.clone();


                if(TEXTONLEVEL>1){

                   // int []tempTextonHist=TextonHist.clone();
                    for( i=2;i<TEXTONLEVEL+1;i++){
                        subTextonHist=new int[(height/(int)Math.pow(2,i)) *(width/(int)Math.pow(2,i))];
                        LTxXORP.getTextonImg(tempTextonHist, height/(int)Math.pow(2,i-1), width/(int)Math.pow(2,i-1), subTextonHist, DirPhase);
                        tempTextonHist=subTextonHist.clone();

                            //打印纹理基元图像数据
                            for (int k = 0; k < (height/(int)Math.pow(2,i));k++) {
                             for (j = 0; j < (width/(int)Math.pow(2,i)); j++) {
                                 System.out.print(subTextonHist[k* (width/(int)Math.pow(2,i))+j]+" ");
                                     }
                                     System.out.println();
                                 }

                    }
                }
                // Extract LBP feature
                LTxXORP.lbpFeature(subTextonHist, (height/(int)Math.pow(2,TEXTONLEVEL)), (width/(int)Math.pow(2,TEXTONLEVEL)), feature);
                if(TEXMODE!=1) {
                    int phaseSUM = 0;
                    for (i = 0; i < 5; i++) {
                        phaseSUM += DirPhase[i];
                    }
                    for (i = 0, j = LTxXORP.LBPNUM + 1; i < 5; i++, j++) {
                        DirPhase[i] /= phaseSUM * PWEIGHT;
                        //      System.out.print("相位方向"+i+"的特征值为"+DirPhase[i]);
                        feature[j] = DirPhase[i];
                    }

//                    //高斯归一化
//                    int dnum=4;
//                    double mean=0;
//                    double StandardD=0;
//                    for(i = 0, j = LTxXORP.LBPNUM + 1; i < dnum; i++, j++)
//                        mean+=feature[j];
//                    mean/=dnum;
//
//                    for(i = 0, j = LTxXORP.LBPNUM + 1; i < dnum; i++, j++){
//                        StandardD+= Math.pow(feature[j]-mean,2);
//                    }
//                    StandardD=Math.sqrt(StandardD);
//                    for(i = 0, j = LTxXORP.LBPNUM + 1; i < dnum; i++, j++){
//                        feature[j]= (feature[j]-mean)/StandardD;
//                    }


                }

             //   System.out.println();

            }

            else  LTxXORP.lbpFeature(data, height, width, feature);

////////////////////////////////LBP特征分割/////////////////////////////////////////////////////////////
//           double[] TiruLBP9=new double[9];
//            double[] TiruLBP27=new double[27];
//            int k;
//            int uriCount=0;int less_uriCount=0;
//            for(int w=0;w<36;w++){
//                for( k=0;k<9;k++){
//                    if(w==IR_UNIPATS[k]){
//                        TiruLBP9[uriCount++]=feature[w];
//                        break;
//                    }
//                }
//                if(k==9)
//                    TiruLBP27[less_uriCount++]=feature[w];
//            }

//            uriCount=0;less_uriCount=0;
//            for(int w=0;w<36;w++){
//                if(w<9) feature[w]= TiruLBP9[uriCount++];
//                else  feature[w]= TiruLBP27[less_uriCount++];
//            }
//
             if(USE_GLCM) {
                 double[] glcmFeature = GLCM.getGLCMFeature(imgPath);
                 for (i = 0, j = feature.length - 4; i < 4; i++, j++) {

                     feature[j] = glcmFeature[i];
                 }
             }

            //Boss之不可行方案2
//             int Stexton;
//            double [][]subfeature;
//            subfeature=new double[4][LTxXORP.LBPNUM];
//            int[][]subdata=new int[4][(height/2) * (width/2)];
//            LTxXORP.Subdata(data,subdata,height,width);
//            Stexton=LTxXORP.fastlbpFeature(subdata, height/2, width/2, subfeature);
////////////////////////////////测试////////////////////////////////////////////////////////////////
            //打印原始数据data
//            for (i = 0; i < width; i++) {
//                for (j = 0; j < height; j++) {
//                    System.out.print(data[i*width+j]+" ");
//                }
//                System.out.println();
//            }
//            System.out.println();System.out.println();System.out.println();

            //打印纹理基元图像数据
//            for (i = 0; i < width/2; i++) {
//                for (j = 0; j < height/2; j++) {
//                    System.out.print(TextonHist[i*(width/2)+j]+" ");
//                }
//                System.out.println();
//            }
////////////////////////////////////////////////////////////////////////////////////////////////////////



//			System.out.println(feature.length);
//			System.out.println(Arrays.toString(feature));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return feature;

    }

    public static double[] getGridLBPFeature(int[] data, int width, int height) {

        // LBP feature
        double[] feature = new double[LBP.PATSNUM + 1];

        LBP lbp = new LBP();

        // Extract LBP feature
        lbp.lbpFeature(data, height, width, feature);

//			System.out.println(feature.length);
//			System.out.println(Arrays.toString(feature));

        return feature;

    }



    public static void main(String[] args) {
//        String path3 = "upload/1593";//测试输入对象
//        String path2 = "upload/1594";
//        String path1 = "upload/1595";
//        String path4 = "upload/1596";
//        String path5 = "upload/1169";
//        String path6 = "upload/904";
//        String path7 = "upload/491";
        String path1 = "upload/1571";//测试输入对象
        String path2 = "upload/1569";
        String path3 = "upload/1570";
        String path4 = "upload/1572";
        String path5 = "upload/1208";
        String path6 = "upload/1001";
        String path7 = "upload/602";
//        String path1 = "upload/337";//测试输入对象
//        String path2 = "upload/338";
//        String path3 = "upload/339";
//        String path4 = "upload/340";
//        String path5 = "upload/341";
//        String path6 = "upload/342";
//        String path7 = "upload/343";
//        String path8 = "upload/344";


        setRotaIvaPats();

        double[] feature1 = LTxXORP.getLBPFeature(path1);
        double[] lbpfeature1,lbpGfeature1,lbpDfeature1,feature;
        double[] lbpfeature,lbpGfeature,lbpDfeature;
        double lbpDistance,dDistance,GDistance,distance;
        if(USE_TEXTON) {
            lbpfeature1 = Arrays.copyOfRange(feature1, 0, LBPNUM + 1);
            lbpDfeature1 = Arrays.copyOfRange(feature1, LBPNUM + 1, LBPNUM + 6);
            lbpGfeature1 = Arrays.copyOfRange(feature1, LBPNUM + 6, LBPNUM + 10);
        }
        else{
            lbpfeature1 = Arrays.copyOfRange(feature1, 0, LBPNUM + 1);
            lbpGfeature1 = Arrays.copyOfRange(feature1, LBPNUM + 1, LBPNUM + 5);
        }
        ISimilarityArray textureStrategy=new MinkowskiArrayDistance();
        ISimilarityArray distanceStrategy=new MinkowskiDirDistance();
        List<String> flist=new ArrayList<String>();
          flist.add(path2);
          flist.add(path3);
          flist.add(path4);
          flist.add(path5);
          flist.add(path6);
          flist.add(path7);
         // flist.add(path8);
        int i=1;
        for(String path:flist){
            feature = LTxXORP.getLBPFeature(path);
            if(USE_TEXTON) {
                lbpfeature = Arrays.copyOfRange(feature, 0, LBPNUM + 1);
                lbpDfeature = Arrays.copyOfRange(feature, LBPNUM + 1, LBPNUM + 6);
                lbpGfeature = Arrays.copyOfRange(feature, LBPNUM + 6, LBPNUM + 10);
            }
            else{
                lbpfeature = Arrays.copyOfRange(feature, 0, LBPNUM + 1);
                lbpGfeature = Arrays.copyOfRange(feature, LBPNUM + 1, LBPNUM + 5);
            }
            lbpDistance = textureStrategy.similarity(lbpfeature1, lbpfeature);
            if(USE_TEXTON)  dDistance = distanceStrategy.similarity(lbpDfeature1, lbpDfeature);
            GDistance = textureStrategy.similarity(lbpGfeature1, lbpGfeature);
            if(USE_TEXTON)
            distance=0.5*lbpDistance+0.3*dDistance+0.3*GDistance;
            else
                distance=0.7*lbpDistance+0.2*GDistance;
            for (double f : feature1)
                System.out.print(f + ", ");

            System.out.println();

            for (double f : feature)
                System.out.print(f + ", ");

            System.out.println();
            if(USE_TEXTON)
            System.out.println("图0和图"+i+"的方向相似度为"+Math.pow(Math.E,-dDistance));
            System.out.println("图0和图"+i+"的GLCM相似度为"+Math.pow(Math.E,-GDistance));
            System.out.println("图0和图"+i+"的lbp相似度为"+Math.pow(Math.E,-lbpDistance));
            System.out.println("图0和图"+i+++"的总相似度为"+Math.pow(Math.E,-distance));
        }


    }

}