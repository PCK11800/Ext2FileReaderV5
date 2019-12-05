import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Superblock {
	
	/**
	 * The superblock is a 'table' which holds information
	 * about this specific Ext2 file. This class contains methods
	 * which are required to read and calculate variables.
	 */
	
	private Ext2File file;
	private ByteBuffer buffer;
	
	//Variables
	private int BLOCKGROUPNUMBER;
	private int SUPERBLOCK_OFFSET;
	private int MAGICNUMBER;
	private int INODETOTAL;
	private int BLOCKTOTAL;
	private int BLOCKSIZE;
	private int NUMBEROFBLOCKSPERGROUP;
	private int NUMBEROFINODESPERGROUP;
	private int INODESIZE;
	private String VOLUMELABEL;
	private int NUMBEROFBLOCKGROUPS;
	private int LENGTHOFBLOCKGROUPS;
	
	public Superblock(Ext2File file) {
		this.file = file;
		SUPERBLOCK_OFFSET = 1024;
		buffer = ByteBuffer.wrap(file.read(SUPERBLOCK_OFFSET, Vars.ALL_SUPERBLOCKLENGTH));
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		setSuperblock();
		setFileVariables();
		printData();
	}
	
	/**
	 * Read the superblock of the file and gets the data from it.
	 */
	private void setSuperblock() {
		MAGICNUMBER = buffer.getShort(Vars.SUPERBLOCK_MAGICNUMBER_OFFSET);
		INODETOTAL = buffer.getInt(Vars.SUPERBLOCK_INODETOTAL_OFFSET);
		BLOCKTOTAL = buffer.getInt(Vars.SUPERBLOCK_BLOCKTOTAL_OFFSET);
		BLOCKSIZE = (int) Math.pow(2,buffer.getInt(Vars.SUPERBLOCK_BLOCKSIZE_OFFSET)) * 1024;
		NUMBEROFBLOCKSPERGROUP = buffer.getInt(Vars.SUPERBLOCK_NUMBEROFBLOCKSPERGROUP_OFFSET);
		NUMBEROFINODESPERGROUP = buffer.getInt(Vars.SUPERBLOCK_NUMBEROFINODESPERGROUP_OFFSET);
		INODESIZE = buffer.getInt(Vars.SUPERBLOCK_INODESIZE_OFFSET);
		
		byte[] volumeLabel = new byte[Vars.SUPERBLOCK_VOLUMELABEL_LENGTH];
		for(int i = 0; i < volumeLabel.length; i++) {
			volumeLabel[i] = buffer.get(Vars.SUPERBLOCK_VOLUMELABEL_OFFSET + i);
		}
		VOLUMELABEL = new String(volumeLabel);
	}
	
	/**
	 * Using the data obtained from the superblock, calculates
	 * file specific variables required in other cases and passes
	 * it to the this file's Ext2File class.
	 */
	private void setFileVariables() {
		//Calculate some variables
		NUMBEROFBLOCKGROUPS = (int) Math.ceil((double) BLOCKTOTAL / NUMBEROFBLOCKSPERGROUP);
		LENGTHOFBLOCKGROUPS = NUMBEROFBLOCKSPERGROUP * BLOCKSIZE;
		
		//Set file specific variables
		file.setBLOCKSIZE(BLOCKSIZE);
		file.setINODESIZE(INODESIZE);
		file.setNUMBEROFBLOCKGROUPS(NUMBEROFBLOCKGROUPS);
		file.setLENGTHOFBLOCKGROUPS(LENGTHOFBLOCKGROUPS);
		file.setNUMBEROFBLOCKSPERGROUP(NUMBEROFBLOCKSPERGROUP);
		file.setNUMBEROFINODESPERGROUP(NUMBEROFINODESPERGROUP);
		file.setINODEPOINTERSSIZE(NUMBEROFBLOCKSPERGROUP);
	}
	
	/**
	 * Returns Magic Number byte value into hexadecimal
	 * @param value
	 * @return Magic Number in hexadecimal
	 */
	private String convertMagicNumber(int value) {
		String hex = Integer.toHexString(value - 0xffff0000);
		return "0x" + hex;
	}
	
	public void printData() {
		System.out.println("=================================================");
		System.out.println("Volume Name: " + VOLUMELABEL);
		System.out.println("Magic Number: " + convertMagicNumber(MAGICNUMBER));
		System.out.println("Inode Count: " + INODETOTAL);
		System.out.println("Block Count: " + BLOCKTOTAL);
		System.out.println("Block Size: " + BLOCKSIZE);
		System.out.println("Blocks Per Group: " + NUMBEROFBLOCKSPERGROUP);
		System.out.println("Inodes Per Group: " + NUMBEROFINODESPERGROUP);
		System.out.println("Inode Size: " + INODESIZE);
		System.out.println("Number of Block Groups: " + NUMBEROFBLOCKGROUPS);
		System.out.println("=================================================");
	}

	public int getBLOCKGROUPNUMBER() {
		return BLOCKGROUPNUMBER;
	}

	public int getSUPERBLOCK_OFFSET() {
		return SUPERBLOCK_OFFSET;
	}

	public int getMAGICNUMBER() {
		return MAGICNUMBER;
	}

	public int getINODETOTAL() {
		return INODETOTAL;
	}

	public int getBLOCKTOTAL() {
		return BLOCKTOTAL;
	}

	public int getBLOCKSIZE() {
		return BLOCKSIZE;
	}

	public int getNUMBEROFBLOCKSPERGROUP() {
		return NUMBEROFBLOCKSPERGROUP;
	}

	public int getNUMBEROFINODESPERGROUP() {
		return NUMBEROFINODESPERGROUP;
	}

	public int getINODESIZE() {
		return INODESIZE;
	}

	public String getVOLUMELABEL() {
		return VOLUMELABEL;
	}

	public int getNUMBEROFBLOCKGROUPS() {
		return NUMBEROFBLOCKGROUPS;
	}

	public int getLENGTHOFBLOCKGROUPS() {
		return LENGTHOFBLOCKGROUPS;
	}
	
}
