package yanjun.model;

import java.util.ArrayList;
import java.util.List;

public class Result {

	List<String> resList = new ArrayList<String>();
	String queryPath = null;
	String queryName = null;
	
	public List<String> getResList() {
		return resList;
	}
	public void setResList(List<String> resList) {
		this.resList = resList;
	}
	public String getQueryPath() {
		return queryPath;
	}
	public void setQueryPath(String queryPath) {
		this.queryPath = queryPath;
	}
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	
}
