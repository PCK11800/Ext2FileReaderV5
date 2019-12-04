import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Directory {

	private Ext2File file;
	private ByteBuffer buffer;
	private Inode directoryInode;
	
	//Directory Variables
	private int DIRECTORYPOINTER;
	private int BLOCKGROUPNUMBER = 0; //Initial set to zero. (the first block group)
	
	//Directory Data
	private int INODEPOINTER;
	private int LENGTH;
	private int NAMELENGTH;
	private int FILETYPE;
	private String FILETYPE_STR;
	private String FILENAME;
	
	public Directory(Ext2File file, int DIRECTORYPOINTER, int DIRECTORYOFFSET) {
		this.file = file;
		this.DIRECTORYPOINTER = (DIRECTORYPOINTER * 1024) + DIRECTORYOFFSET;
		buffer = ByteBuffer.wrap(file.read(this.DIRECTORYPOINTER, 1024));
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		setDirectory();
	}
	
	private void setDirectory() {
		INODEPOINTER = buffer.getInt(Vars.DIRECTORY_INODE_OFFSET);
		LENGTH = buffer.getShort(Vars.DIRECTORY_LENGTH_OFFSET);
		NAMELENGTH = buffer.get(Vars.DIRECTORY_NAMELENGTH_OFFSET);
		FILETYPE = buffer.get(Vars.DIRECTORY_FILETYPE_OFFSET);
		
		byte[] fileName = new byte[NAMELENGTH];
		for(int i = 0; i < NAMELENGTH; i++) {
			fileName[i] = buffer.get(Vars.DIRECTORY_FILENAME_OFFSET + i);
		}
		FILENAME = new String(fileName);
		
		setBlockGroup();
		setFileType();
		setInode();
	}
	
	private void setBlockGroup() {
		while(INODEPOINTER > file.getNUMBEROFINODESPERGROUP()) {
			INODEPOINTER = INODEPOINTER - file.getNUMBEROFINODESPERGROUP();
			BLOCKGROUPNUMBER++;
		}
	}
	
	private void setFileType() {
		if(FILETYPE == 1) {
			FILETYPE_STR = "File";
		}
		else if(FILETYPE == 2) {
			FILETYPE_STR = "Directory";
		}
		else {
			FILETYPE_STR = "Unknown";
		}
	}
	
	private void setInode() {
		directoryInode = new Inode(file, BLOCKGROUPNUMBER, INODEPOINTER);
	}
	
	public void printData() {
		System.out.println("=================================================");
		System.out.println("File Name: " + FILENAME);
		System.out.println("In Block Group: " + BLOCKGROUPNUMBER);
		System.out.println("Inode Number: " + INODEPOINTER);
		System.out.println("Length: " + LENGTH);
		System.out.println("File Type: " + FILETYPE_STR);
		System.out.println("=================================================");
	}
	
	public int getLENGTH() {
		return LENGTH;
	}
	
	public int getNAMELENGTH() {
		return NAMELENGTH;
	}
	
	public String getNAME() {
		return FILENAME;
	}
	
	public int getBLOCKGROUPNUMBER() {
		return BLOCKGROUPNUMBER;
	}
	
	public int getINODEPOINTER() {
		return INODEPOINTER;
	}
	
	public String getFILETYPE_STR() {
		return FILETYPE_STR;
	}
	
	public Inode getINODE() {
		return directoryInode;
	}
	
	public int getFILETYPE() {
		return FILETYPE;
	}
}
