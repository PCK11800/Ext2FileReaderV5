import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Datablock {

	private Ext2File file;
	private ByteBuffer buffer;
	
	//Datablock Variables
	private int DATABLOCKPOINTER;
	
	//Datablock content
	private int[] DATABLOCKCONTENT_POINTERS;
	private String DATABLOCKCONTENT_STR;
	
	/**
	 * A datablock contains either file content or in the case of 
	 * indirections, more data pointers. This class can return both
	 * depending on the mode.
	 * 
	 * @param file The Ext2 file this datablock is in
	 * @param DATABLOCKPOINTER The pointer of this datablock
	 * @param mode "file" for reading files, "directory" for reading data pointers
	 */
	public Datablock(Ext2File file, int DATABLOCKPOINTER, String mode) {
		this.file = file;
		this.DATABLOCKPOINTER = DATABLOCKPOINTER * file.getBLOCKSIZE();
		
		buffer = ByteBuffer.wrap(file.read(this.DATABLOCKPOINTER, file.getBLOCKSIZE()));
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		if(mode.equals("file")) {
			modeStr();
		}
		else if(mode.equals("directory")) {
			modeDirectory();
		}
	}
	
	/**
	 * Reads the file and returns it as a string.
	 */
	private void modeStr() {
		byte[] datablock = new byte[file.getBLOCKSIZE()];
		
		for(int i = 0; i < file.getBLOCKSIZE(); i++) {
			datablock[i] = buffer.get(i);
		}
		
		DATABLOCKCONTENT_STR = new String(datablock).trim();
	}
	
	/**
	 * Goes through pointers contained inside the datablock and
	 * stores those that are not 0 into an ArrayList.
	 * 
	 * Afterwards, convert that into an integer array for easier 
	 * usage and passing.
	 */
	private void modeDirectory() {
		ArrayList<Integer> list = new ArrayList<>();
		
		int offset = 0;
		for(int i = 0; i < file.getBLOCKSIZE()/4; i++) {
			if(buffer.getInt(offset) != 0) {
				list.add(buffer.getInt(offset));
			}
			offset = offset + 4;
		}
		
		DATABLOCKCONTENT_POINTERS = new int[list.size()];
		for(int i = 0; i < list.size(); i++) {
			DATABLOCKCONTENT_POINTERS[i] = list.get(i);
		}
	}
	
	public int[] getDATABLOCKCONTENT_POINTERS() {
		return DATABLOCKCONTENT_POINTERS;
	}
	
	public String getDATABLOCKCONTENT_STR() {
		return DATABLOCKCONTENT_STR;
	}
	
}
