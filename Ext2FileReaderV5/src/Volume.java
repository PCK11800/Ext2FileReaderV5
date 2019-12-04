import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Volume {

	private RandomAccessFile raf;
	
	public Volume(String filePath) {
		Path path = FileSystems.getDefault().getPath("..", filePath);
		File file = new File(path.toString());
		try {
			raf = new RandomAccessFile(file, "r");
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	RandomAccessFile getRandomAccessFile() {
		return raf;
	}
}
