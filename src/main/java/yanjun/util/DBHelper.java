package yanjun.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import yanjun.properties.Config;

public class DBHelper {

	private static final String CLOTHBASE_TB = Config.dbTable;

	private static DBConnection dbcon = new DBConnection();

	public static List<Object> fetchALLCloth() {

		String sql = "select * from " + CLOTHBASE_TB;

		return dbcon.excuteQuery(sql, null);
	}
	
	
	public static List<Object> fetchClothLimit(int N) {
		String sql = "select * from " + CLOTHBASE_TB + " limit " + N;
		return dbcon.excuteQuery(sql, null);
	}

	public static int updateData(String sql, Object[] data) {

		return dbcon.executeUpdate(sql, data);
	}

	public static List<Object> fetchCloth(int N) {

		String sql = "select * from " + CLOTHBASE_TB + " limit " + N;

		return dbcon.excuteQuery(sql, null);
	}

	public static List<Object> getLBPFecture(String path) {
		String sql = "select lbpFeature from " + CLOTHBASE_TB + " where path = ?";
		Object[] data = { path };
		return dbcon.excuteQuery(sql, data);
	}
	
	public static List<Object> getFabric(int id) {
		String sql = "select id, path from " + CLOTHBASE_TB + " where id = ?";
		Object[] data = {id};
		return dbcon.excuteQuery(sql, data);
	}

	public static void createTB() {

		String sql = "create table " + Config.dbTable
		      + " (id int not null auto_increment, " + "path varchar(120), "
		      + "histogram longtext, " + "primary key(id))";

		try {
			PreparedStatement pstmt = (PreparedStatement) dbcon.getConn()
			      .prepareStatement(sql);
			pstmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	public static void createTextureTB() {

		String sql = "create table " + Config.dbTable
		      + " (id int not null auto_increment, " + "path varchar(120), "
		      + "lbpFeature longtext, " + "primary key(id))";

		try {
			PreparedStatement pstmt = (PreparedStatement) dbcon.getConn()
			      .prepareStatement(sql);
			pstmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
