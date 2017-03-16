package GuoJun.texture;
import GuoJun.texture.similarity.impl.MinkowskiArrayDistance;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

public class TextonSub {
    final static double PI = 3.14159265358979323846;

    static final int PREDICATE = 2;//LBP半径R
    static final int GETGRAY_M =2;//  1：通过心理学公式提取灰度空间   2：按照RGB和HSV的转换公式提取V
    static final int TEXMODE =2;//  1：标准8纹理基元模式   2：变种15纹理基元模式
    static final double PWEIGHT=1;//设置为最大值10000表示不使用相位特征
    static final int GRAY_DIMENSION=8;//灰度空间量化
    static final double Pmatch=1;//表示相位特征的匹配权重
    static final double Lmatch=1;//表示共生矩阵能力特征的权重

    /**
     *
     *  data 图像灰度值
     *  row 图像高度
     *  column 图像宽度
     *  TextonHist texton图像特征值数组
     */
    //获得texton图像
    public static int TextonDetecte(int num1,int num2,int num3,int num4,double[]DirPhase){
        int textonIndex=0;
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
                if (num1 == num2 && num3 != num4 && num1 != num3 && num1 != num4){ textonIndex = 1;DirPhase[2]++;}
                else if (num3 == num4 && num1 != num2 && num3 != num1 && num3 != num2) {textonIndex = 2;DirPhase[2]++;}
                else if (num3 == num4 && num1 == num2 && num3 != num1) {textonIndex = 3;DirPhase[2]+=2;}
                else if (num2 == num4 && num1 != num3 && num2 != num1 && num2 != num3) {textonIndex = 4;DirPhase[0]++;}
                else if (num1 == num3 && num2 != num4 && num1 != num2 && num1 != num4) {textonIndex = 5;DirPhase[0]++;}
                else if (num1 == num3 && num2 == num4 && num1 != num2) {textonIndex = 6;DirPhase[0]+=2;}
                else if (num1 == num4 && num2 != num3 && num1 != num2 && num1 != num3) {textonIndex = 7;DirPhase[3]++;}
                else if (num2 == num3 && num1 != num4 && num2 != num1 && num2 != num4) {textonIndex = 8;DirPhase[1]++;}
                else if (num2 == num3 && num1 == num4 && num2 != num1) {textonIndex = 9;DirPhase[3]++;DirPhase[1]++;}
                else if (num1 == num2 && num2 == num3 && num1 != num4) {textonIndex = 10;DirPhase[4]++;}
                else if (num1 == num2 && num2 == num4 && num1 != num3) {textonIndex = 11;DirPhase[4]++;}
                else if (num2 == num3 && num3 == num4 && num2 != num1) {textonIndex = 12;DirPhase[4]++;}
                else if (num1 == num3 && num3 == num4 && num2 != num1) {textonIndex = 13;DirPhase[4]++;}
                else if (num1 == num2 && num2 == num3 && num3 == num4) textonIndex = 14;
                else if (num1 != num2 && num1 != num3 && num1 != num4 && num2 != num3 && num2 != num4 && num3 != num4) textonIndex = 0;
                break;
        }
        return textonIndex;
    }
    public static void getTextonImg(int[] data, int row, int column, int[] TextonHist,double[] DirPhase,double[] feature){
        if(row%2!=0) row=row-1;
        if(column%2!=0) column=column-1;
        int index=0;
        int texindex,sum=0;
        for (int i = 0; i < row; i=i+2) {
            for (int j = 0; j < column; j=j+2) {
                texindex = TextonDetecte(data[i * column + j],data[i * column + j+1],data[(i+1) * column + j],data[(i+1) * column + j+1],DirPhase);
                TextonHist[index++] = texindex;
                 feature[texindex]++;
                sum++;
            }
        }
        for(int i=0;i<15;i++)
            feature[i]=feature[i]/sum;

    }
    //特征提取方法
    public static double[] getTextonFeature(String imgPath) {
        int i, j,featureNum;
       if(TEXMODE==2) featureNum=15;
       if(TEXMODE==1) featureNum=8;
        // LBP feature
        double[] feature = new double[featureNum+5];
        for(int f=0;f<feature.length;f++) feature[f]=0;

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
            TextonHist=new int[(height/2) *(width/2)];
            DirPhase=new double[5];

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

                TextonSub.getTextonImg(data,height,width,TextonHist,DirPhase,feature);



                if(TEXMODE==2) {
                    int phaseSUM = 0;
                    for (i = 0; i < 5; i++) {
                        phaseSUM += DirPhase[i];
                    }
                    for (i = 0, j = featureNum; i < 5; i++, j++) {
                        DirPhase[i] /= phaseSUM * PWEIGHT;
                        //      System.out.print("相位方向"+i+"的特征值为"+DirPhase[i]);
                        feature[j] = DirPhase[i];
                    }
                }
             //高斯归一化

//            double mean=0;
//            double StandardD=0;
//            for(i=0;i<featureNum+4;i++)
//                mean+=feature[i];
//            mean/=featureNum+4;
//
//            for(i=0;i<featureNum+4;i++){
//                StandardD+= Math.pow(feature[i]-mean,2);
//            }
//            StandardD=Math.sqrt(StandardD);
//            for(i=0;i<featureNum+4;i++){
//                feature[i]= (feature[i]-mean)/StandardD;
//            }
//                //   System.out.println();




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
    public static void main(String[] args) {
//                String path1 = "upload/1593";//测试输入对象
//        String path2 = "upload/1594";
//        String path3 = "upload/1595";
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
        double[] DIRfeature1;
        double[] Texfeature1;

        long start = System.currentTimeMillis();

        double[] feature1 = TextonSub.getTextonFeature(path1);
        long end = System.currentTimeMillis();
        System.out.println("单个特征提取时间为： " + (end - start) + "ms");

        DIRfeature1= Arrays.copyOfRange(feature1,feature1.length-5,feature1.length);
        Texfeature1=Arrays.copyOfRange(feature1,0,feature1.length-5);

//        double[] feature2 = LTxXORP.getLBPFeature(path2);
//        double[] feature3 = LTxXORP.getLBPFeature(path3);
//        double[] feature4 = LTxXORP.getLBPFeature(path4);
//        double[] feature5 = LTxXORP.getLBPFeature(path5);
        double[] feature;
        double TexDistance=0;
        double dirDistance=0;

        double[] DIRfeature;
        double[] Texfeature;
        MinkowskiArrayDistance textureStrategy=new MinkowskiArrayDistance();
        java.util.List<String> flist=new ArrayList<String>();
        flist.add(path2);
        flist.add(path3);
        flist.add(path4);
        flist.add(path5);
        flist.add(path6);
        flist.add(path7);
        int i=2;
        for(String path:flist){
            start = System.currentTimeMillis();
            feature =  TextonSub.getTextonFeature(path);
            end = System.currentTimeMillis();
            System.out.println("单个特征提取时间为： " + (end - start) + "ms");

//            if(USE_TEXTON){
//                 DIRfeature=Arrays.copyOfRange(feature,feature.length-8,feature.length-4);
//                 GLCMfeature=Arrays.copyOfRange(feature,0,feature.length-8);
//            }
//            else{GLCMfeature=Arrays.copyOfRange(feature1,0,feature1.length-4);  }
//            L_GLCMfeature=Arrays.copyOfRange(feature,feature.length-4,feature.length);

            DIRfeature= Arrays.copyOfRange(feature,feature.length-5,feature1.length);
            Texfeature=Arrays.copyOfRange(feature,0,feature.length-5);


                dirDistance = textureStrategy.similarity(DIRfeature1, DIRfeature);

            TexDistance = textureStrategy.similarity(Texfeature1, feature);


            for (double f : DIRfeature1)
                System.out.print(f + ", ");
            for (double f : Texfeature1)
                System.out.print(f + ", ");

            System.out.println();
            for (double f : DIRfeature)
                System.out.print(f + ", ");
            for (double f : Texfeature)
                System.out.print(f + ", ");

            System.out.println();

                System.out.println("图1和图"+i+"的方向相似度为"+Math.pow(Math.E,-dirDistance));
//           System.out.println("图1和图"+i+"的共生矩阵相似度为"+Math.pow(Math.E,-glcmDistance));
            System.out.println("图1和图"+i+"的共生矩阵能量特征相似度为"+Math.pow(Math.E,-TexDistance));
            System.out.println("图1和图"+i+++"的总特征相似度为"+Math.pow(Math.E,-(TexDistance*Lmatch+dirDistance*Pmatch)));
//            if(USE_TEXTON) {
//                System.out.println("图1和图"+i+"的方向相似度为"+dirDistance);
//            }
//            System.out.println("图1和图"+i+"的共生矩阵相似度为"+glcmDistance);
//            System.out.println("图1和图"+i+"的共生矩阵能量特征相似度为"+l_glcmDistance);
//            System.out.println("图1和图"+i+++"的总特征相似度为"+(glcmDistance+l_glcmDistance*Lmatch+dirDistance*Pmatch));
        }


    }

}