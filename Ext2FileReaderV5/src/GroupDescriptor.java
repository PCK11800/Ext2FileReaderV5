import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GroupDescriptor {
	
	/**
	 * The Group Descriptor contains the Inode table pointer
	 * of this specific block group. 
	 */
	
	private Ext2File file;
	private ByteBuffer buffer;
	
	//Variables
	private int GROUPDESCRIPTOR_OFFSET;
	private int BLOCKGROUPNUMBER;
	private int INODETABLEPOINTER;
	
	public GroupDescriptor(Ext2File file, int BLOCKGROUPNUMBER) {
		this.file = file;
		this.BLOCKGROUPNUMBER = BLOCKGROUPNUMBER;
		
		getOffset();
		
		buffer = ByteBuffer.wrap(file.read(GROUPDESCRIPTOR_OFFSET, Vars.ALL_GROUPDESCRIPTORLENGTH));
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		setGroupDescriptor();
		//printData();
	}
	
	/**
	 * Reads the group descriptor table and gets the offset
	 * from it.
	 */
	private void getOffset() {
		//Add boot block length + superblock
		GROUPDESCRIPTOR_OFFSET = 2048;
		GROUPDESCRIPTOR_OFFSET = GROUPDESCRIPTOR_OFFSET + (BLOCKGROUPNUMBER * Vars.ALL_GROUPDESCRIPTORLENGTH);
	}
	
	/**
	 * Read the Group descriptor of this block group and gets the 
	 * inode table pointer from it.
	 */
	private void setGroupDescriptor() {
		INODETABLEPOINTER = buffer.getInt(Vars.GROUPDESCRIPTOR_INODETABLEPOINTER_OFFSET) - (BLOCKGROUPNUMBER * file.getNUMBEROFBLOCKSPERGROUP());
		//Set File Vars
		file.setINODEPOINTERS(BLOCKGROUPNUMBER, INODETABLEPOINTER);
	}
	
	public void printData() {
		System.out.println("=================================================");
		System.out.println("Block Group: " + BLOCKGROUPNUMBER);
		System.out.println("Inode Table Pointer: " + INODETABLEPOINTER);
		System.out.println("=================================================");
	}
	
	public int getINODETABLEPOINTER() {
		return INODETABLEPOINTER;
	}
}
