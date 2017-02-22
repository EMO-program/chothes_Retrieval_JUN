package yanjun.Counter;

import yanjun.color.similarity.ifs.ISimilarityArray;
import yanjun.properties.Config;
import yanjun.texture.similarity.impl.MinkowskiArrayDistance;
import yanjun.util.DBHelper;
import yanjun.util.MatchUtil;

import java.util.*;

public class Ratecounter {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        long start = System.currentTimeMillis();
        float[] res = getRate();
        //计算提取单个图片的颜色特征的时间
        long end = System.currentTimeMillis();
        System.out.println("检索总时间为： " + (end - start) + "ms");
       System.out.println("检索数据库： " + Config.dbTable);
        float ARR=res[0];//平均检索率
        float ARP=res[1];//平均检索精度
        System.out.println("平均检索率: " + res[0] + "    " + "平均检索精度: " + res[1]);
    }


    public static float[] getRate () {

        ISimilarityArray textureStrategy=new MinkowskiArrayDistance();
        float[] res = new float[2];
        int Ctemp;
        List<Object> list = DBHelper.fetchALLCloth();
        float RR[]=new float[list.size()];
        float RP[]=new float[list.size()];
        Map<String, Double> lbpResultMap = new HashMap<String, Double>();
        int counter;
        float ratecounter;
        List<String> matchUrls = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            lbpResultMap.clear();
            matchUrls.clear();
            Map<String, Object> map = (Map<String, Object>) list.get(i);
            Object SourceData = map.get("lbpFeature");
            double[] lbpSourceFecture = MatchUtil.jsonToArr((String) SourceData);
            for ( int j = 0; j < list.size(); j++) {
                Map<String, Object> INmap = (Map<String, Object>) list.get(j);
                Object candidateLBP = INmap.get("lbpFeature");
                Object candidateid = INmap.get("id");
                // 从快速搜索中选取TOP N结果，继续进行纹理特征匹配
                //获取当前由颜色匹配相似度排序的结果并提取其LBP纹理特征

                double[] lbpTargetFeature = MatchUtil.jsonToArr((String) candidateLBP);
                double lbpDistance = textureStrategy.similarity(lbpTargetFeature, lbpSourceFecture);
                lbpResultMap.put(Objects.toString(candidateid), lbpDistance);
            }
            Map<String, Double> finalResult = MatchUtil.sortByValueAsc(lbpResultMap);
            counter = 0;
            for (Map.Entry<String, Double> finalmap : finalResult.entrySet()) {
                if (counter >= Config.finalResultNumber)
                    break;
                //matchUrls截取只存储finalResultNumber数量的查询结果
                matchUrls.add(finalmap.getKey());
                counter ++;
           //     double TSimilarity=Math.pow(Math.E,-finalmap.getValue());
           //     System.out.println(TSimilarity + " 图片路径 " + finalmap.getKey());
            }
            ratecounter=0f;
            for(String tmp:matchUrls) {

             //   Ctemp=Integer.parseInt((tmp.split("/")[5].split(".")[0]));
               Ctemp=Integer.parseInt(tmp);
                if ((Ctemp - 1) / 4 == i / 4) {
                    ratecounter++;
                }
            }
            RR[i]=ratecounter/4f;
            RP[i]=ratecounter/Config.finalResultNumber;
            System.out.println("图"+i+"检索率: " +  RR[i] + "    " + "图"+i+"检索精度: " +RP[i]);
        }
        float RRSUM=0;
        float RPSUM=0;
        for(int i=0;i<list.size();i++) {
            RRSUM+=RR[i];
            RPSUM+=RP[i];
        }
        res[0] = RRSUM/list.size();
        res[1] = RPSUM/list.size();
        return res;
    }

}
