
public class Main {

	private Volume volume;
	
	private void run() {
		volume = new Volume("/Ext2FileReaderV5/Resources/ext2fs");
		new Ext2File(volume);
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}
}
