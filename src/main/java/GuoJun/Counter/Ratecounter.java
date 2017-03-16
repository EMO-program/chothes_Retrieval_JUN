package GuoJun.Counter;

import GuoJun.color.similarity.ifs.ISimilarityArray;
import GuoJun.properties.Config;
import GuoJun.texture.similarity.impl.MinkowskiArrayDistance;
import GuoJun.texture.similarity.impl.MinkowskiDirDistance;
import GuoJun.util.DBHelper;
import GuoJun.util.MatchUtil;

import java.util.*;

public class Ratecounter {
    static final double CUT = 0;
    static final double CUT2 = 0;
    static final double LBPweight = 0.6;
    static final double GLCMweight = 0;
    static final double DIRweight = 0.4;
    static final int Snum = 4;
    static final boolean USELBPONLY =false;
    static final int IR_UNIPATS[] = { 0, 1, 2, 4, 8, 16, 27, 34, 35 };
    static final int LBPNUM =  58;
    public static void main(String[] args) {
        // TODO Auto-generated method stub
       float TOPN=Config.finalResultNumber;
        int StartIndex=0;
        int EndIndex=1360;
        for(float i=0;i<3;i=i+1) {
            long start = System.currentTimeMillis();
            if(i==0)
            {
                StartIndex=0;EndIndex=284;
            }
            else if(i==1)
            {
                StartIndex=284;EndIndex=976;
            }
            else if(i==2)
            {
                StartIndex=976;EndIndex=1360;
            }
            float[] res = getRate(TOPN,StartIndex,EndIndex);
            //计算提取单个图片的颜色特征的时间
            long end = System.currentTimeMillis();
            System.out.println("检索总时间为： " + (end - start) + "ms");
            System.out.println("检索数据库： " + Config.dbTable);
            float ARR = res[0];//平均检索率
            float ARP = res[1];//平均检索精度
            System.out.println("平均检索率: " + res[0] + "    " + "平均检索精度: " + res[1]);

        }
    }

    public static float[] getRate ( float TOPN,int Start,int End) {

        ISimilarityArray textureStrategy=new MinkowskiArrayDistance();
        ISimilarityArray distanceStrategy=new MinkowskiDirDistance();
        float[] res = new float[2];
        int Ctemp;
        List<Object> list = DBHelper.fetchALLCloth();
       //定义遍历统计的始末序号
        int StartIndex=Start;
        int EndIndex=End;

        int listNum=EndIndex-StartIndex;
        float RR[]=new float[list.size()];
        float RP[]=new float[list.size()];
        Map<String, Double> lbpResultMap = new HashMap<String, Double>();
        int counter;
        double[] StempDir,StempGLCM,StempLBP,SiruLBP9,SiruLBP27,TtempDir,TtempGLCM,TtempLBP,TiruLBP9,TiruLBP27;
        SiruLBP9=new double[9];
        TiruLBP9=new double[9];
        SiruLBP27=new double[27];
        TiruLBP27=new double[27];
           double tempDdistance,tempGdistance,tempLdistance,tempL9distance,tempL27distance;

        float ratecounter;
        double meanSet=0;
        double minSet=0;
        List<String> matchUrls = new ArrayList<String>();
        for (int i = StartIndex; i <EndIndex; i++) {
            lbpResultMap.clear();
            matchUrls.clear();
            Map<String, Object> map = (Map<String, Object>) list.get(i);
           Object SourceData = map.get("lbpFeature");
          // Object SourceData = map.get("glcmFeature");
            double[] lbpSourceFecture = MatchUtil.jsonToArr((String) SourceData);
            StempLBP=Arrays.copyOfRange(lbpSourceFecture,0,LBPNUM + 1 );
            if(!USELBPONLY) {
                StempDir = Arrays.copyOfRange(lbpSourceFecture, LBPNUM + 1, LBPNUM + 6);
                if(GLCMweight!=0)
                StempGLCM = Arrays.copyOfRange(lbpSourceFecture, LBPNUM + 6, LBPNUM + 10);
            }
            int uriCount=0,less_uriCount=0;

            //将LBP两个特征进行猜分
//            int h;
//            for(int w=0;w<36;w++){
//                for( h=0;h<9;h++){
//                    if(w==IR_UNIPATS[h]){
//                        SiruLBP9[uriCount++]=StempLBP[w];
//                        break;
//                    }
//                }
//                if(h==9)
//                SiruLBP27[less_uriCount++]=StempLBP[w];
//            }



//            for (double f : StempLBP)
//                System.out.print(f + ", ");
//            System.out.println();
//            for (double f : SiruLBP9)
//                System.out.print(f + ", ");
//            System.out.println();
//            for (double f : SiruLBP27)
//                System.out.print(f + ", ");
//            System.out.println();

            for ( int j = 0; j < list.size(); j++) {
                Map<String, Object> INmap = (Map<String, Object>) list.get(j);
               Object candidateLBP = INmap.get("lbpFeature");
               //Object candidateLBP = INmap.get("glcmFeature");
                Object candidateid = INmap.get("id");

                double[] lbpTargetFeature = MatchUtil.jsonToArr((String) candidateLBP);
                double lbpDistance;
               if(!USELBPONLY) {
                   TtempDir = Arrays.copyOfRange(lbpTargetFeature, LBPNUM + 1, LBPNUM + 6);
                   if(GLCMweight!=0)
                   TtempGLCM=Arrays.copyOfRange(lbpTargetFeature,LBPNUM + 6,LBPNUM + 10);
                 //  tempDdistance = distanceStrategy.similarity(StempDir, TtempDir);
                  tempDdistance = textureStrategy.similarity(StempDir, TtempDir);
                   if(GLCMweight!=0)
                   tempGdistance = textureStrategy.similarity(StempGLCM, TtempGLCM);
                   else tempGdistance=0;
                  // tempGdistance+=tempDdistance;
                   if (Math.pow(Math.E, -(tempGdistance+tempDdistance)) < CUT) {
                       lbpDistance = Config.MAX_DISTANCE;
                   }//如果GLCM+方向相似度大于阈值CUT，则继续对大维数的LBP特征进行比较，否则不比较。
                   else {
                       TtempLBP = Arrays.copyOfRange(lbpTargetFeature, 0,LBPNUM + 1 );
                       tempLdistance = textureStrategy.similarity(TtempLBP, StempLBP);

                       //将LBP两个特征进行拆分
//                        int k;
//                       uriCount=0;less_uriCount=0;
//                       for(int w=0;w<36;w++){
//                           for( k=0;k<9;k++){
//                               if(w==IR_UNIPATS[k]){
//                                   TiruLBP9[uriCount++]=TtempLBP[w];
//                                   break;
//                               }
//                           }
//                           if(k==9)
//                               TiruLBP27[less_uriCount++]=TtempLBP[w];
//                       }
//                       tempL9distance= textureStrategy.similarity(TiruLBP9, SiruLBP9);
//                       if (Math.pow(Math.E, -tempL9distance) < CUT2) {
//                           lbpDistance = Config.MAX_DISTANCE;
//                       }
//                       else{
//                           tempL27distance= textureStrategy.similarity(TiruLBP27, SiruLBP27);
//                           lbpDistance = tempL9distance +tempL27distance + tempGdistance+tempDdistance;
//                           //double lbpDistance = textureStrategy.similarity(lbpTargetFeature, lbpSourceFecture);
//                       }
                       lbpDistance = LBPweight*tempLdistance + GLCMweight*tempGdistance + DIRweight*tempDdistance;

                   }
               }
                else {
                   TtempLBP = Arrays.copyOfRange(lbpTargetFeature, 0,LBPNUM + 1);
                   lbpDistance = textureStrategy.similarity(TtempLBP, StempLBP);
               }
                lbpResultMap.put(Objects.toString(candidateid), lbpDistance);
            }
            Map<String, Double> finalResult = MatchUtil.sortByValueAsc(lbpResultMap);
            counter = 0;
            for (Map.Entry<String, Double> finalmap : finalResult.entrySet()) {
                if (counter >= TOPN)
                    break;
                //matchUrls截取只存储finalResultNumber数量的查询结果
                matchUrls.add(finalmap.getKey());
                counter ++;
           //     double TSimilarity=Math.pow(Math.E,-finalmap.getValue());
                if(counter==TOPN-1){
              // System.out.println(Math.pow(Math.E,-finalmap.getValue()) + "   图片路径 " + finalmap.getKey());
                if(Math.pow(Math.E,-finalmap.getValue())>minSet) minSet=Math.pow(Math.E,-finalmap.getValue());
                    meanSet+=Math.pow(Math.E,-finalmap.getValue());
                }
            }

            ratecounter=0f;
            for(String tmp:matchUrls) {

             //   Ctemp=Integer.parseInt((tmp.split("/")[5].split(".")[0]));
               Ctemp=Integer.parseInt(tmp);
                if ((Ctemp - 1) / Snum == i / Snum) {
                    ratecounter++;
                }
            }
            RR[i]=ratecounter/(float) Snum;
            RP[i]=ratecounter/TOPN;
            counter=i+1;
          // System.out.println("图"+counter+"检索率: " +  RR[i] + "    " + "图"+counter+"检索精度: " +RP[i]);
        }
        float RRSUM=0;
        float RPSUM=0;
        for(int i=0;i<list.size();i++) {
            RRSUM+=RR[i];
            RPSUM+=RP[i];
        }
        //System.out.println("平均阈值" + meanSet/list.size()+"   最小阈值" + Math.pow(Math.E,-minSet));
        res[0] = RRSUM/(listNum);
        res[1] = RPSUM/(listNum);
        return res;
    }

}
