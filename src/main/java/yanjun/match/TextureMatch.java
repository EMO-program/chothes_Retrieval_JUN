package yanjun.match;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import yanjun.color.similarity.ifs.ISimilarityArray;
import yanjun.color.similarity.ifs.ISimilarityMap;
import yanjun.properties.Config;
import yanjun.texture.LBP;
import yanjun.texture.LTxXORP;
import yanjun.util.ColorUtil;
import yanjun.util.DBHelper;
import yanjun.util.MatchUtil;

public class TextureMatch {


    private ISimilarityArray textureStrategy;

    public TextureMatch( ISimilarityArray textureStrategy) {
        this.textureStrategy = textureStrategy;
    }

    @SuppressWarnings({ "unchecked", "null" })
    public List<String> calcSimilarity(String imgPath) {
        // TODO Auto-generated method stub

        FileInputStream imageFile;
        double minDistance = Double.MAX_VALUE;

        // 返回查询结果
        List<String> matchUrls = new ArrayList<String>();


        try {

            imageFile = new FileInputStream(imgPath);
            BufferedImage bufferImage;

            bufferImage = ImageIO.read(imageFile);
            LTxXORP.setRotaIvaPats();//设定旋转模式值

            //得到图片的纹理一维数组特征向量（各颜色分量频率（统计量））
            double[] lbpSourceFecture = LTxXORP.getLBPFeature(imgPath);

            // 提取数据库数据
            List<Object> list = DBHelper.fetchALLCloth();

            // long startTime = System.currentTimeMillis();

            // 把每个条数据的路径和最短距离特征提取出来并存储在lbpResultMap的键值对中。
            Map<String, Double> lbpResultMap = new HashMap<String, Double>();

            for (int i = 0; i < list.size(); i++) {

                Map<String, Object> map = (Map<String, Object>) list.get(i);

                Object candidatePath = map.get("path");
                Object candidateLBP = map.get("lbpFeature");

                // 从快速搜索中选取TOP N结果，继续进行纹理特征匹配
                //获取当前由颜色匹配相似度排序的结果并提取其LBP纹理特征

                double[] lbpTargetFeature = MatchUtil.jsonToArr((String) candidateLBP);
                double lbpDistance = textureStrategy.similarity(lbpTargetFeature, lbpSourceFecture);
                lbpResultMap.put((String) candidatePath, lbpDistance);

                // 判断衡量标准选取距离还是相似度
                if (lbpDistance < minDistance)
                    minDistance = lbpDistance;

            }

            Map<String, Double> tempResultMap;

            System.out.println("Min Distance : " + (float) minDistance);



            System.out.println("============== finish Texture =================");

            Map<String, Double> finalResult = MatchUtil.sortByValueAsc(lbpResultMap);

            int counter = 0;
            for (Map.Entry<String, Double> map : finalResult.entrySet()) {
                if (counter >= Config.finalResultNumber)
                    break;
                //matchUrls截取只存储finalResultNumber数量的查询结果
                matchUrls.add(map.getKey());
                counter ++;
                double TSimilarity=Math.pow(Math.E,-map.getValue());
                System.out.println(TSimilarity + " 图片路径 " + map.getKey());

            }

            System.out.println();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return matchUrls;
    }
}
