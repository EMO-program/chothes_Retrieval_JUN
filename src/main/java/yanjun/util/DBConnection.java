package yanjun.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yanjun.properties.Config;

public class DBConnection {

	private static final String dbDriver = Config.dbDriver;
	private static final String dbUrl = Config.dbUrl;
	private static final String dbUser = Config.dbUser;
	private static final String dbPass = Config.dbPass;
	
	private Connection conn = null;
	private PreparedStatement preStmt = null;
	private ResultSet rs = null;
	
	public Connection getConn() {
		
		try {
	      Class.forName(dbDriver);
      } catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		
		try {
	      conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
      } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		
		return conn;
	}
	
	public int executeUpdate(String sql, Object[] params) {
		int affectedLine = 0;

		try {
			conn = getConn();
	      preStmt = conn.prepareStatement(sql);
	      
	      if (params != null) {
	      	for (int i = 0; i < params.length; i++)
	      		preStmt.setObject(i+1, params[i]);
	      }
	      
	      affectedLine = preStmt.executeUpdate();
	      
      } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      } finally {
      	closeAll();
      }
		
		return affectedLine;
	}
	
	
	public List<Object> excuteQuery(String sql, Object[] params) {
		
		ResultSet rs = executeQueryRS(sql, params);
		ResultSetMetaData rsmd = null;
		
		int colCount = 0;
		try {
	      rsmd = rs.getMetaData();
	      colCount = rsmd.getColumnCount();

      } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
				
		List<Object> list = new ArrayList<Object>();
		
		try {
	      while (rs.next()) {
	      	Map<String, Object> map = new HashMap<String, Object>();
	      	for (int i = 1; i <= colCount; i++)
	      		map.put(rsmd.getColumnLabel(i), rs.getObject(i));
	      	
	      	list.add(map);
	      }
	      
      } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      } finally {
      	closeAll();
      }
		
		return list;
		
	}
	
	
	private ResultSet executeQueryRS(String sql, Object[] params) {
		
		try {
			conn = getConn();
	      preStmt = conn.prepareStatement(sql);
	      
	      if (params != null) {
	      	for (int i = 0; i < params.length; i++)
	      		preStmt.setObject(i+1, params[i]);
	      }
	      
	      rs = preStmt.executeQuery();
	      
      } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		
		return rs;
	}
	
	
	private void closeAll() {
		
		if (rs != null) {
			try {
	         rs.close();
         } catch (SQLException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
		}
		
		if (preStmt != null) {
			try {
	         preStmt.close();
         } catch (SQLException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
		}
		
		if (conn != null) {
			try {
	         conn.close();
         } catch (SQLException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
		}
	}
	
}
