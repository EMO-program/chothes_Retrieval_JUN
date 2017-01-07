package yanjun.properties;

public class Config {
	/**
	 * 数据库配置信息
	 */
	
	public static final String dbDriver = "com.mysql.jdbc.Driver";
	public static final String dbUrl = "jdbc:mysql://localhost/FarbicDB";
	public static final String dbUser = "root";
	public static final String dbPass = "";
	
	public static final String dbTable = "16H10S10V85fabricMA";
	
//	public static final String dbTable = "LBP16AND2fabric300";
	
	/**
	 * 资源路径
	 */
	public static final String resourcePath = "http://0.0.0.0:4567/"; //localhost:8000/";//"src/test/resources/";
	
	public static final String uploadImageFile = "upload/";
	
	public static final String fileName = "Farbic-MA/";
	
	public static final String resultImagePath = resourcePath + "static/" + fileName;
	
	public static final int PC_SIZE = 740;
	public static final int CT_SIZE = 492;
	public static final int ST_SIZE = 424;
	public static final int MA_SIZE = 1628;
	
	public static final int LIMITNUMBER = MA_SIZE;
	
	/**
	 *  图像及颜色配置信息 COMMIT TEST
	 */
	
	public final static int H_DIMENSION = 16;
	public final static int S_DIMENSION = 10;
	public final static int I_DIMENSION = 10;
	
	public final static int Hbit = (int) Math.ceil(Math.log(H_DIMENSION)/Math.log(2));
	public final static int Sbit = (int) Math.ceil(Math.log(S_DIMENSION)/Math.log(2));
	public final static int Vbit = (int) Math.ceil(Math.log(I_DIMENSION)/Math.log(2));

	public final static int SIZE = (int) Math.pow(2, (Hbit + Sbit + Vbit));
	
	// 直方图排序后取TOP_N的值
	public static final int TopNValueOfHis = 128; 
	
	// 取排序后的白分量
	public static final float TopPercentage = 0.85f;
	
	// 误差调整
	public static final double FixError = 0.00000001;
	
	// 伸缩比例限制
	public static double scaleRatio = 4;
	
	
	// 计算相似度
	public static final boolean SIMILARITY = false;
	
	// 检索返回结果数
	public static float resultNumber = 100;
	
	public static final float finalResultNumber = 24;
	
	public static final float MAX_DISTANCE = 100;
	
	// 相同颜色bin百分比
	public static float sameColorNumPercent = 0.3f;
	// 相同颜色统计量百分比
	public static float sameColorQuantityPercent = 0.85f;
	
}
