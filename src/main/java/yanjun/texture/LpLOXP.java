package yanjun.texture;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LpLOXP {
    final static double PI = 3.14159265358979323846;

    static final int PREDICATE = 2;//LBP半径R

    static final int BITS = 8;//LBP邻点数量P
    static final int DIFF = PREDICATE << 1;//边缘点的数量？
    public static final int PATSNUM = 58;//uniform patterns 数
    static final int ROTSUM=36;//旋转置换群特征数
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



    public LpLOXP() {
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
        for(int i=0;i<ROTSUM;i++){
            for(int j=0;j<BITS;j++) {
                //用uniform模式再筛选一次特征
//                for(int k=0;k<PATSNUM;k++){
//                    if(RotaIvaPats[i][j]==UNIPATS[k])   break;
//                    else if(k==PATSNUM-1)
//                        RotaIvaPats[i][j]=0;
//                }
                System.out.print(RotaIvaPats[i][j] + ",");
            }
            System.out.println();}
    }

    void compareNeighbors(int[] value, int center, int neighbor, int shift) {
        if (center < neighbor) {
            value[0] |= 1 << shift;
        } else {
            value[0] |= 0 << shift;
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
        //用旋转置换群重新分配特征值；
        for (i = 0; i < ROTSUM; i++) {
            for(int j=0;j<BITS;j++) {
                if(RotaIvaPats[i][j]!=-1)
                  feature[i] += histFull[RotaIvaPats[i][j]];
            }
            sumpats += feature[i];
        }
        for (i = 0; i < 256; i++) {
            sum += histFull[i];
        }

         feature[ROTSUM] = sum - sumpats;//在这里这个数等于0；
        //统一化（将特征转成百分比频率）
        for (i = 0; i < feature.length; i++) {
            feature[i] = feature[i] / (float) sum;
        }

        //将符合uniform模式的LBP特征值提取出来作为最终纹理特征
//        for (i = 0; i < PATSNUM; i++) {
//            feature[i] = histFull[UNIPATS[i]];
//            sumpats += feature[i];
//        }
//
//        for (i = 0; i < 256; i++) {
//            sum += histFull[i];
//        }
//
//        feature[PATSNUM] = sum - sumpats;//特征向量最后一位存储全部计算像素与uniform截取的像素的差值
//        //统一化（将特征转成百分比频率）
//        for (i = 0; i < feature.length; i++) {
//            feature[i] = feature[i] / (float) sum;
//        }
    }
    /**
     *
     *  data 图像灰度值
     *  row 图像高度
     *  column 图像宽度
     *  TextonHist texton图像特征值数组
     */
    //获得texton图像
    int TextonDetecte(int num1,int num2,int num3,int num4){
        int textonIndex=0;
         if(num1==num2&&num3!=num4) textonIndex=1;
         else if(num2==num4&&num1!=num3) textonIndex=2;
         else if(num3==num4&&num1!=num2) textonIndex=3;
         else if(num1==num3&&num2!=num4) textonIndex=4;
         else if(num1==num4&&num2!=num3) textonIndex=5;
         else if(num2==num3&&num1!=num4) textonIndex=6;
         else if(num2==num3&&num1==num4&&num1==num2) textonIndex=7;

          return textonIndex;
    }
    void getTextonImg(int[] data, int row, int column, int[] TextonHist){
        if(row%2!=0) row=row-1;
        if(column%2!=0) column=column-1;
        int index=0;
        for (int i = 0; i < row; i=i+2) {
            for (int j = 0; j < column; j=j+2) {
                TextonHist[index++] = TextonDetecte(data[i * column + j],data[i * column + j+1],data[(i+1) * column + j],data[(i+1) * column + j+1]);
            }
        }


    }
    //特征提取方法
    public static double[] getLBPFeature(String imgPath) {
        int i, j;

        // LBP feature
        double[] feature = new double[LpLOXP.ROTSUM + 1];

        int height; // Height of Image
        int width; // Width of Image
        int[] data; // Block Data
        int[] TextonHist;//纹理基元图
        BufferedImage img;
        try {
            img = ImageIO.read(new File(imgPath));//读取参数路径的图片数据
            height = img.getHeight();
            width = img.getWidth();

            data = new int[height * width];
            TextonHist=new int[(height/2) *(width/2)];
            LpLOXP LpLOXP = new LpLOXP();

            //著名的心理学公式
            // 彩色图像转换成无彩色的灰度图像Y=0.299*R + 0.578*G + 0.114*B
            for (i = 0; i < width; i++) {
                for (j = 0; j < height; j++) {
                    Color c = new Color(img.getRGB(i, j));
                    data[j * width + i] = (int) (0.299 * c.getRed() + 0.578
                            * c.getGreen() + 0.114 * c.getBlue());
                }
            }
            //先将灰度图像转换成纹理基元图像
            LpLOXP.getTextonImg(data,height,width,TextonHist);
////////////////////////////////测试////////////////////////////////////////////////////////////////
//            //打印原始数据data
//            for (i = 0; i < width; i++) {
//                for (j = 0; j < height; j++) {
//                    System.out.print(data[i*width+j]+" ");
//                }
//                System.out.println();
//            }
//            System.out.println();System.out.println();System.out.println();
//
//            //打印纹理基元图像数据
//            for (i = 0; i < width/2; i++) {
//                for (j = 0; j < height/2; j++) {
//                    System.out.print(TextonHist[i*(width/2)+j]+" ");
//                }
//                System.out.println();
//            }
////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Extract LBP feature
            LpLOXP.lbpFeature(TextonHist, (height/2), (width/2), feature);

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
        //String path1 = "src/test/resources/fabric252/1.jpg";
        //String path2 = "src/test/resources/fabric252/2.jpg";
        String path1 = "upload/94";
        String path2 = "upload/23";
        setRotaIvaPats();
        LpLOXP LpLOXP = new LpLOXP();
        double[] feature1 = LpLOXP.getLBPFeature(path1);
        double[] feature2 = LpLOXP.getLBPFeature(path2);

        for (double f : feature1)
            System.out.print(f + ", ");

        System.out.println();

        for (double f : feature2)
            System.out.print(f + ", ");

    }

}