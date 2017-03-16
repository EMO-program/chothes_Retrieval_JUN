package GuoJun.texture;

import GuoJun.properties.Config;
import GuoJun.texture.similarity.impl.MinkowskiArrayDistance;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
public class GLCM {
    static final int GETGRAY_M =2;//  1：通过心理学公式提取灰度空间   2：按照RGB和HSV的转换公式提取V
    static final int TEXMODE =3;//  1：标准8纹理基元模式   2：变种15纹理基元模式
    static final boolean USE_TEXTON =true;// 表示是否使用纹理基元模式
    static final boolean USE_PARSE =false;// 表示是否使用方向特征
    static final int OFFSETS =0;// 表示方向，0：0度   1：45度   2：90度    3：135度
    static final int  RADIUS =2;//比较半径
   // static final int DIFF = RADIUS << 1;//边缘点的数量？
    static final int GRAY_DIMENSION=8;//灰度空间量化
    static final double PWEIGHT=1;//设置为最大值10000表示不使用相位特征
    static final double Pmatch=1;//表示相位特征的匹配权重
    static final double Lmatch=1;//表示共生矩阵能力特征的权重

    //获得texton图像
    int TextonDetecte(int num1,int num2,int num3,int num4,double[]DirPhase){
        int textonIndex=0;
        int cut1=256/GRAY_DIMENSION/3;
        int cut2=cut1*2;
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
//            case 2://变种43纹理基元
//                if (num1 == num2 && num3 != num4 && num1 != num3 && num1 != num4){
//                    textonIndex = 1;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[2]++;
//                }
//                else if (num3 == num4 && num1 != num2 && num3 != num1 && num3 != num2) {
//                    textonIndex = 2;
//                    if(num3>cut1&&num3<cut2)textonIndex+=14;
//                    else if(num3>cut2) textonIndex+=28;
//                    DirPhase[2]++;
//                }
//                else if (num3 == num4 && num1 == num2 && num3 != num1) {
//                    textonIndex = 6;
//                    DirPhase[2]+=2;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                }
//                else if (num2 == num4 && num1 != num3 && num2 != num1 && num2 != num3) {
//                    textonIndex = 4;
//                    if(num2>cut1&&num2<cut2)textonIndex+=14;
//                    else if(num2>cut2) textonIndex+=28;
//                    DirPhase[0]++;
//                }
//                else if (num1 == num3 && num2 != num4 && num1 != num2 && num1 != num4) {
//                    textonIndex = 5;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[0]++;
//                }
//                else if (num1 == num3 && num2 == num4 && num1 != num2) {
//                    textonIndex = 6;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[0]+=2;
//                }
//                else if (num1 == num4 && num2 != num3 && num1 != num2 && num1 != num3) {
//                    textonIndex = 7;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[3]++;
//                }
//                else if (num2 == num3 && num1 != num4 && num2 != num1 && num2 != num4) {
//                    textonIndex = 8;
//                    if(num2>cut1&&num2<cut2)textonIndex+=14;
//                    else if(num2>cut2) textonIndex+=28;
//                    DirPhase[1]++;
//                }
//                else if (num2 == num3 && num1 == num4 && num2 != num1) {
//                    textonIndex = 9;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[3]++;DirPhase[1]++;
//                }
//                else if (num1 == num2 && num2 == num3 && num1 != num4) {
//                    textonIndex = 10;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[4]++;
//                }
//                else if (num1 == num2 && num2 == num4 && num1 != num3) {
//                    textonIndex = 11;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[4]++;
//                }
//                else if (num2 == num3 && num3 == num4 && num2 != num1) {
//                    textonIndex = 12;
//                    if(num2>cut1&&num2<cut2)textonIndex+=14;
//                    else if(num2>cut2) textonIndex+=28;
//                    DirPhase[4]++;
//                }
//                else if (num1 == num3 && num3 == num4 && num2 != num1) {
//                    textonIndex = 13;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                    DirPhase[4]++;
//                }
//                else if (num1 == num2 && num2 == num3 && num3 == num4) {
//                    textonIndex = 14;
//                    if(num1>cut1&&num1<cut2)textonIndex+=14;
//                    else if(num1>cut2) textonIndex+=28;
//                }
//                else if (num1 != num2 && num1 != num3 && num1 != num4 && num2 != num3 && num2 != num4 && num3 != num4) textonIndex = 0;
//                break;
        }
        return textonIndex;
    }
    void getTextonImg(int[] data, int row, int column, int[] TextonHist,double[] DirPhase){
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
    public static double[] getGLCMFeature(String imgPath) {
        int i, j;
        int featureNUM;
        double [] finalfeature;
        double[][] feature;
        if(TEXMODE==1) featureNUM=8;
        if(TEXMODE==2) featureNUM=15;
        if(TEXMODE==3) featureNUM=13;

        if(USE_TEXTON) {
            feature = new double[4][featureNUM*featureNUM];
            if(USE_PARSE) finalfeature=new double[9];
            else finalfeature=new double[4];
        }
        else {feature = new double[4][(256/GRAY_DIMENSION)*(256/GRAY_DIMENSION)];
            finalfeature=new double[4];}

        //feature初始化
        for(int k=0;k<4;k++)
          for(int f=0;f<feature.length;f++)
            feature[k][f]=0;
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
            GLCM glcm = new GLCM();

            //初始化相位特征
            for( i=0;i<5;i++){
                DirPhase[i]=0;
            }
            float Gpercent = 1f/GRAY_DIMENSION;
          //  int maxcolor=0,mincolor=0;
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
                   // if (data[j * width + i] >maxcolor) maxcolor=data[j * width + i] ;
                  //  if (data[j * width + i] <mincolor) mincolor=data[j * width + i] ;
                    data[j * width + i]= (int) Math.floor( data[j * width + i] * Gpercent );
                }
            }
             // System.out.print("最大灰度值"+maxcolor+"  最小灰度值"+maxcolor);
            //先将灰度图像转换成纹理基元图像
            if(USE_TEXTON){
                glcm.getTextonImg(data,height,width,TextonHist,DirPhase);
                // Extract LBP feature
                glcm.glcmFeature(TextonHist, (height/2), (width/2), feature);
                if(TEXMODE!=1) {
                    int phaseSUM = 0;
                    for (i = 0; i < 5; i++) {
                        phaseSUM += DirPhase[i];
                    }

                    for (i = 0; i < 5; i++) {
                        DirPhase[i] /= phaseSUM * PWEIGHT;
                        //      System.out.print("相位方向"+i+"的特征值为"+DirPhase[i]);
                    }
                }


                //   System.out.println();

            }
            else  glcm.glcmFeature(data, height, width, feature);
            int flength,fwidth;
            if(USE_TEXTON){
                fwidth=featureNUM;
            }
            else {
                fwidth = 256 / GRAY_DIMENSION;
            }
            flength=feature[0].length;
            //初始化特征向量
            double [][]gfeature=new double [4][4];
            for(i=0;i<4;i++)
              for(j=0;j<4;j++)
                  gfeature[i][j]=0;


                double u1=0,u2=0,s1=0,s2=0,c=0;
            for(int d=0;d<4;d++){
                for (i = 0; i <flength; i++)   {

                   gfeature[d][0] += Math.pow(feature[d][i],2);// 角二阶矩（Angular Second Moment, ASM)
//                    for(j = 0; j <flength; j++){
//                        u1+=j/fwidth*feature[d][j];
//                        u2+=j%fwidth*feature[d][j];
//                    }
//                    for(j = 0; j <flength; j++){
//                        s1+=feature[d][j]*Math.pow(j/fwidth-u1,2);
//                        s2+=feature[d][j]*Math.pow(j%fwidth-u2,2);
//                        c=(j/fwidth)*(j%fwidth)*feature[d][j];
//                    }
//                    if(s1!=0&&s2!=0) {
//                        gfeature[d][0] += ((i/fwidth)*(i%fwidth)*feature[d][i]-u1*u2)/(Math.sqrt(s1)*Math.sqrt(s2));// 相关性（CORR)
//                    }


                    gfeature[d][1] += Math.pow(i/fwidth-i%fwidth,2)*feature[d][i];   //Con 对比度

                    gfeature[d][2] += feature[d][i]/(1+Math.pow(i/fwidth-i%fwidth,2));// 反差分矩阵（Inverse Differential Moment, IDM--HOM一致性)

                if(feature[d][i]!=0)  gfeature[d][3] += (-1)*feature[d][i]*(Math.log(feature[d][i])/Math.log(Math.E));//熵（Entropy, ENT)

            }
            }

            //求四个方向的均值

            for(i=0;i<4;i++)
                for(j=0;j<4;j++)
                finalfeature[i]+=gfeature[j][i];

            for(j=0;j<4;j++)
                finalfeature[j]/=4;
            int fnum;
            if(USE_TEXTON&&USE_PARSE){
                for(i=0,j=4;j<9;j++,i++)
                finalfeature[j]=DirPhase[i];
                fnum=9;

            }else fnum=4;
             //高斯归一化

            double mean=0;
            double StandardD=0;
            for(i=0;i<fnum;i++)
                mean+=finalfeature[i];
                mean/=fnum;

            for(i=0;i<fnum;i++){
                StandardD+= Math.pow(finalfeature[i]-mean,2);
            }
            StandardD=Math.sqrt(StandardD);
            for(i=0;i<fnum;i++){
                finalfeature[i]= (finalfeature[i]-mean)/StandardD;
            }

           // feature[feature.length-3]/=20;
           // feature[feature.length-1]/=2;


            /**
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

        return finalfeature;

    }
        public void glcmFeature(int[] Textonimg, int height, int width, double[][] feature){
            int px1,fwidth;
            int []px2=new int[4];
            int []glcmsum={0,0,0,0};
            if(USE_TEXTON){
            if(TEXMODE==2) fwidth=15;
            else if(TEXMODE==1) fwidth=8;
            else if(TEXMODE==3) fwidth=13;
            }
            else fwidth=256/GRAY_DIMENSION;
            for (int h = RADIUS; h < height-RADIUS; h++) {
                for (int w = RADIUS; w < width-RADIUS; w++) {
                    px1=Textonimg[h*width+w];
//                    if(OFFSETS==0) px2=Textonimg[h*width+(w+RADIUS)];
//                    else if(OFFSETS==1) px2=Textonimg[(h-RADIUS)*width+(w+RADIUS)];
//                    else if(OFFSETS==2) px2=Textonimg[(h-RADIUS)*width+w];
//                    else if(OFFSETS==3) px2=Textonimg[(h-RADIUS)*width+(w-RADIUS)];
                    px2[0]=Textonimg[h*width+(w+RADIUS)];
                    px2[1]=Textonimg[(h-RADIUS)*width+(w+RADIUS)];
                    px2[2]=Textonimg[(h-RADIUS)*width+w];
                    px2[3]=Textonimg[(h-RADIUS)*width+(w-RADIUS)];
                    feature[0][px1*fwidth+px2[0]]+=1;
                    feature[1][px1*fwidth+px2[1]]+=1;
                    feature[2][px1*fwidth+px2[2]]+=1;
                    feature[3][px1*fwidth+px2[3]]+=1;
                }
                }
            int flength=feature[0].length;
            for(int j=0;j<4;j++){
            for (int i = 0; i < flength; i++) {
                 glcmsum[j]+=feature[j][i];
                }
            }
             //归一化
            for(int j=0;j<4;j++){
            for (int i = 0; i < flength; i++) {
                feature[j][i] = feature[j][i] / (float) glcmsum[j];
            }
            }
    }
    public static void main(String[] args) {
//        String path1 = "upload/1593";//测试输入对象
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
        double[] GLCMfeature1;

        long start = System.currentTimeMillis();
        double[] feature1 = GLCM.getGLCMFeature(path1);
        long end = System.currentTimeMillis();
        System.out.println("单个特征提取时间为： " + (end - start) + "ms");

            DIRfeature1=Arrays.copyOfRange(feature1,4,8);
            GLCMfeature1=Arrays.copyOfRange(feature1,0,4);

//        double[] feature2 = LTxXORP.getLBPFeature(path2);
//        double[] feature3 = LTxXORP.getLBPFeature(path3);
//        double[] feature4 = LTxXORP.getLBPFeature(path4);
//        double[] feature5 = LTxXORP.getLBPFeature(path5);
        double[] feature;
        double glcmDistance;
        double dirDistance=0;
        double l_glcmDistance;
        double[] DIRfeature;
        double[] GLCMfeature;
        double[] L_GLCMfeature;
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
            feature =  GLCM.getGLCMFeature(path);
             end = System.currentTimeMillis();
            System.out.println("单个特征提取时间为： " + (end - start) + "ms");

//            if(USE_TEXTON){
//                 DIRfeature=Arrays.copyOfRange(feature,feature.length-8,feature.length-4);
//                 GLCMfeature=Arrays.copyOfRange(feature,0,feature.length-8);
//            }
//            else{GLCMfeature=Arrays.copyOfRange(feature1,0,feature1.length-4);  }
//            L_GLCMfeature=Arrays.copyOfRange(feature,feature.length-4,feature.length);
            DIRfeature=Arrays.copyOfRange(feature,4,8);
            GLCMfeature=Arrays.copyOfRange(feature,0,4);


            if(USE_TEXTON) {
                dirDistance = textureStrategy.similarity(DIRfeature1, DIRfeature);
              }
            glcmDistance = textureStrategy.similarity(GLCMfeature1, GLCMfeature);


            for (double f : DIRfeature1)
                System.out.print(f + ", ");
            for (double f : GLCMfeature1)
                System.out.print(f + ", ");

            System.out.println();
            for (double f : DIRfeature)
                System.out.print(f + ", ");
            for (double f : GLCMfeature)
                System.out.print(f + ", ");

            System.out.println();
            if(USE_TEXTON) {
                System.out.println("图1和图"+i+"的方向相似度为"+Math.pow(Math.E,-dirDistance));
            }
//           System.out.println("图1和图"+i+"的共生矩阵相似度为"+Math.pow(Math.E,-glcmDistance));
           System.out.println("图1和图"+i+"的共生矩阵能量特征相似度为"+Math.pow(Math.E,-glcmDistance));
           System.out.println("图1和图"+i+++"的总特征相似度为"+Math.pow(Math.E,-(glcmDistance*Lmatch+dirDistance*Pmatch)));
//            if(USE_TEXTON) {
//                System.out.println("图1和图"+i+"的方向相似度为"+dirDistance);
//            }
//            System.out.println("图1和图"+i+"的共生矩阵相似度为"+glcmDistance);
//            System.out.println("图1和图"+i+"的共生矩阵能量特征相似度为"+l_glcmDistance);
//            System.out.println("图1和图"+i+++"的总特征相似度为"+(glcmDistance+l_glcmDistance*Lmatch+dirDistance*Pmatch));
     }


    }
}
