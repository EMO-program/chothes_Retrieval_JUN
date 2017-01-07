import java.io.File;

/**
 * Created by fulvaz on 16/10/24.
 */
public class Test {
    public static void main(String[] args) {
        File uploadDir = new File("upload");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist
    }
}
