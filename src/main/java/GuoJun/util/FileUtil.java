package GuoJun.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import GuoJun.properties.Config;

public class FileUtil {
	
	/*
	 * return the image path
	 */
	public static String downloadPicture(String urlPath) {
		
		String imagePath = Config.uploadImageFile + "queryImage_" + System.currentTimeMillis() + ".jpg";
		
		try {
	      URL url = new URL(urlPath);
	      System.out.println("url is: " + url);
	      DataInputStream dataInputStream = new DataInputStream(url.openStream());
	      FileOutputStream fileOutputStream = new FileOutputStream(new File(imagePath));
	      
	      byte[] buffer = new byte[1024];
	      int length;
	      
	      while ((length = dataInputStream.read(buffer)) > 0) {
	      	fileOutputStream.write(buffer, 0, length);
	      }
	      
	      dataInputStream.close();
	      fileOutputStream.close();
	      
      } catch (IOException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		
		return imagePath;
	}
}
