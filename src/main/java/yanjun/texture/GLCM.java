package yanjun.texture;

import yanjun.texture.similarity.impl.MinkowskiArrayDistance;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by EMO on 2017/2/22.
 * 灰度共生矩阵的特征：

 角二阶矩（Angular Second Moment, ASM)
 ASM=sum(p(i,j).^2)    p(i,j)指归一后的灰度共生矩阵
 角二阶矩是图像灰度分布均匀程度和纹理粗细的一个度量，当图像纹理绞细致、灰度分布均匀时，能量值较大，反之，较小。

 熵（Entropy, ENT)
 ENT=sum(p(i,j)*(-ln(p(i,j)))
 是描述图像具有的信息量的度量，表明图像的复杂程序，当复杂程序高时，熵值较大，反之则较小。

 反差分矩阵（Inverse Differential Moment, IDM)
 IDM=sum(p(i,j)/(1+(i-j)^2))
 反映了纹理的清晰程度和规则程度，纹理清晰、规律性较强、易于描述的，值较大；杂乱无章的，难于描述的，值较小。
 */

public class GLCM {
    static final int GETGRAY_M =2;//  1：通过心理学公式提取灰度空间   2：按照RGB和HSV的转换公式提取V
    static final int TEXMODE =2;//  1：标准8纹理基元模式   2：变种15纹理基元模式
    static final boolean USE_TEXTON =true;// 表示是否使用纹理基元模式
    static final int OFFSETS =0;// 表示方向，0：0度   1：45度   2：-90度    3：135度
    static final int  RADIUS =1;//比较半径
   // static final int DIFF = RADIUS << 1;//边缘点的数量？
    static final int GRAY_DIMENSION=1;//灰度空间量化
    static final double PWEIGHT=1;//设置为最大值10000表示不使用相位特征

    //获得texton图像
    int TextonDetecte(int num1,int num2,int num3,int num4,double[]DirPhase){
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
                else if (num3 == num4 && num1 == num2 && num3 != num1) {textonIndex = 6;DirPhase[2]+=2;}
                else if (num2 == num4 && num1 != num3 && num2 != num1 && num2 != num3) {textonIndex = 4;DirPhase[0]++;}
                else if (num1 == num3 && num2 != num4 && num1 != num2 && num1 != num4) {textonIndex = 5;DirPhase[0]++;}
                else if (num1 == num3 && num2 == num4 && num1 != num2) {textonIndex = 6;DirPhase[0]+=2;}
                else if (num1 == num4 && num2 != num3 && num1 != num2 && num1 != num3) {textonIndex = 7;DirPhase[3]++;}
                else if (num2 == num3 && num1 != num4 && num2 != num1 && num2 != num4) {textonIndex = 8;DirPhase[1]++;}
                else if (num2 == num3 && num1 == num4 && num2 != num1) {textonIndex = 9;DirPhase[3]++;DirPhase[1]++;}
                else if (num1 == num2 && num2 == num3 && num1 != num4) {textonIndex = 10;DirPhase[3]++;}
                else if (num1 == num2 && num2 == num4 && num1 != num3) {textonIndex = 11;DirPhase[1]++;}
                else if (num2 == num3 && num3 == num4 && num2 != num1) {textonIndex = 12;DirPhase[3]++;}
                else if (num1 == num3 && num3 == num4 && num2 != num1) {textonIndex = 13;DirPhase[1]++;}
                else if (num1 == num2 && num2 == num3 && num3 == num4) textonIndex = 14;
                else if (num1 != num2 && num1 != num3 && num1 != num4 && num2 != num3 && num2 != num4 && num3 != num4) textonIndex = 0;
                break;
        }
        return textonIndex;
    }
    void getTextonImg(int[] data, int row, int column, int[] TextonHist,double[] DirPhase){
        if(row%2!=0) row=row-1;
        if(column%2!=0) column=column-1;
        int index=0;
        for (int i = 0; i < row; i=i+2) {
            for (int j = 0; j < column; j=j+2) {
                TextonHist[index++] = TextonDetecte(data[i * column + j],data[i * column + j+1],data[(i+1) * column + j],data[(i+1) * column + j+1],DirPhase);
            }
        }


    }
    //特征提取方法
    public static double[] getGLCMFeature(String imgPath) {
        int i, j;
        int featureNUM;
        double[] feature;
        if(TEXMODE==1) featureNUM=8;
        if(TEXMODE==2) featureNUM=15;

        if(USE_TEXTON) {
            feature = new double[featureNUM + 8];
        }
        else {feature = new double[256/GRAY_DIMENSION];}

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
            DirPhase=new double[4];
            GLCM glcm = new GLCM();

            //初始化相位特征
            for( i=0;i<4;i++){
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
                glcm.getTextonImg(data,height,width,TextonHist,DirPhase);
                // Extract LBP feature
                glcm.glcmFeature(TextonHist, (height/2), (width/2), feature);
                if(TEXMODE==2) {
                    int phaseSUM = 0;
                    for (i = 0; i < 4; i++) {
                        phaseSUM += DirPhase[i];
                    }
                    for (i = 0, j = LTxXORP.ROTSUM + 1; i < 4; i++, j++) {
                        DirPhase[i] /= phaseSUM * PWEIGHT;
                        //      System.out.print("相位方向"+i+"的特征值为"+DirPhase[i]);
                        feature[j] = DirPhase[i];
                    }
                }

                //   System.out.println();

            }

            else  glcm.glcmFeature(data, height, width, feature);
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
        public void glcmFeature(int[] Textonimg, int height, int width, double[] feature){
            for (int r = 0; r < height - RADIUS; r++) {
                for (int c = 0; c < width - RADIUS; c++) {
                    OFFSETS

                }
                }


    }
    public static void main(String[] args) {
        //String path1 = "src/test/resources/fabric252/1.jpg";
        //String path2 = "src/test/resources/fabric252/2.jpg";
        String path1 = "upload/551";//测试输入对象
        String path2 = "upload/552";
        String path3 = "upload/553";
        String path4 = "upload/332";
        String path5 = "upload/336";

        //     LTxXORP LTxXORP = new LTxXORP();
        double[] feature1 = LTxXORP.getLBPFeature(path1);
//        double[] feature2 = LTxXORP.getLBPFeature(path2);
//        double[] feature3 = LTxXORP.getLBPFeature(path3);
//        double[] feature4 = LTxXORP.getLBPFeature(path4);
//        double[] feature5 = LTxXORP.getLBPFeature(path5);
        double[] feature;
        double lbpDistance;
        MinkowskiArrayDistance textureStrategy=new MinkowskiArrayDistance();
        java.util.List<String> flist=new ArrayList<String>();
        flist.add(path2);
        flist.add(path3);
        flist.add(path4);
        flist.add(path5);
        int i=2;
        for(String path:flist){
            feature = LTxXORP.getLBPFeature(path);
            lbpDistance = textureStrategy.similarity(feature1, feature);

            for (double f : feature1)
                System.out.print(f + ", ");

            System.out.println();

            for (double f : feature)
                System.out.print(f + ", ");

            System.out.println();
            System.out.println("图1和图"+i+++"的相似度为"+Math.pow(Math.E,-lbpDistance));
        }


    }
}
