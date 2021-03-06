package GuoJun.model;

import java.util.List;
import java.util.Map;

import GuoJun.util.DBHelper;

public class Model {

	public Fabric getFabric (int fabricId) {
		List<Object> res = DBHelper.getFabric(fabricId);
		Map<String, Object> map = (Map<String, Object>) res.get(0);
		
		String storePath = (String)map.get("path");
		int index = storePath.indexOf("Farbic-MA");
		int end = storePath.indexOf(".");
		String imgPath = storePath.substring(index);
		String fabricName = storePath.substring(index+10, end);

		
		Fabric fabric = new Fabric();
		fabric.setFabricId(fabricId);
		fabric.setFabricName(fabricName);
		fabric.setStorePath(imgPath);
        //查看布匹详情（根据id）
		System.out.println("使用了Model的getFabric方法");
		return fabric;
	}
}
