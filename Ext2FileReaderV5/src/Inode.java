import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Date;
import java.util.Arrays;

public class Inode {

	private Ext2File file;
	private ByteBuffer buffer;
	
	//Inode Variables
	private int BLOCKGROUPNUMBER;
	private int INODEPOINTER;
	private int INODENUMBER;
	
	//Inode Data
	private int FILEMODE;
	private int USERID;
	private int FILESIZELOWER;
	private int LASTACCESSTIME;
	private int CREATIONTIME;
	private int LASTMODIFIEDTIME;
	private int GROUPIDLOWER;
	private int NUMOFHLREFFILE;
	private int[] BLOCKPOINTERS;
	private int FILESIZEUPPER;
	
	public Inode(Ext2File file, int BLOCKGROUPNUMBER, int INODENUMBER) {
		this.file = file;
		this.BLOCKGROUPNUMBER = BLOCKGROUPNUMBER;
		this.INODENUMBER = INODENUMBER;
		
		setInodePointer();
		
		buffer = ByteBuffer.wrap(file.read(this.INODEPOINTER, file.getINODESIZE()));
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		setInode();
	}
	
	/**
	 * This method sets the INODEPOINTER for this inode given it's block group
	 * number and inode number.
	 * 
	 * INODEPOINTER points to the block that holds the start of the Inode Table
	 * Each Inode is 128 bytes long
	 * To search for the start of Inode n, use equation
	 * startByte = (INODEPOINTER * 1024) + ( (n-1) * 128)
	 */
	private void setInodePointer() {
		this.INODEPOINTER = ((BLOCKGROUPNUMBER) * (file.getLENGTHOFBLOCKGROUPS())) + (file.getINODEPOINTER(BLOCKGROUPNUMBER) * 1024) + ((INODENUMBER - 1) * file.getINODESIZE());
	}
	
	private void setInode() {
		FILEMODE = buffer.getShort(Vars.INODE_FILEMODE_OFFSET);
		USERID = buffer.getShort(Vars.INODE_USERID_OFFSET);
		GROUPIDLOWER = buffer.getShort(Vars.INODE_GROUPIDLOWER_OFFSET);
		NUMOFHLREFFILE = buffer.getShort(Vars.INODE_NUMOFHLREFFILE_OFFSET);
		
		FILESIZELOWER = buffer.getInt(Vars.INODE_FILESIZELOWER_OFFSET);
		LASTACCESSTIME = buffer.getInt(Vars.INODE_LASTACCESSTIME_OFFSET);
		CREATIONTIME = buffer.getInt(Vars.INODE_CREATIONTIME_OFFSET);
		LASTMODIFIEDTIME = buffer.getInt(Vars.INODE_LASTMODIFIEDTIME_OFFSET);
		FILESIZEUPPER = buffer.getInt(Vars.INODE_FILESIZEUPPER_OFFSET);
		
		BLOCKPOINTERS = new int[15];
		for(int i = 0; i < 15; i++) {
			BLOCKPOINTERS[i] = buffer.getInt(Vars.INODE_BLOCKPOINTERS_OFFSET + (i * 4));
		}
	}

	/**
	 * Turns milliseconds from the epoch
	 * to a date.
	 * 
	 * @param seconds
	 * @return Date in string
	 */
	@SuppressWarnings("deprecation")
	private String getDate(int seconds) {
		Date date = new Date((long)seconds * 1000);
		return date.toGMTString();
	}
	
	public void printData() {
		System.out.println("=================================================");
		System.out.println("File Mode: " + FILEMODE);
		System.out.println("User ID: " + USERID);
		System.out.println("Group ID (Lower): " + GROUPIDLOWER);
		System.out.println("Number of Hard Linked Reference Files: " + NUMOFHLREFFILE);
		System.out.println("File Size (Lower): " + FILESIZELOWER);
		System.out.println("File Size (Upper): " + FILESIZEUPPER);
		System.out.println("Last Access Time: " + getDate(LASTACCESSTIME));
		System.out.println("Creation Time: " + getDate(CREATIONTIME));
		System.out.println("Last Modified Time: " + getDate(LASTMODIFIEDTIME));
		System.out.println("Pointers to all the datablocks: " + Arrays.toString(BLOCKPOINTERS));
		System.out.println("=================================================");
	}
	
	public int[] getBlockPointers() {
		return BLOCKPOINTERS;
	}
	
	public int getBlockPointer(int n) {
		return BLOCKPOINTERS[n];
	}
	
	public String getCREATIONTIME_STR() {
		return getDate(CREATIONTIME);
	}
}
