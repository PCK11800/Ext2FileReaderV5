import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class Ext2File {
	
	/**
	 * This class contains all methods and variables required
	 * to interact with this specific Ext2 file.
	 * 
	 * It includes and uses file specific variables unique to 
	 * this file only.
	 */

	RandomAccessFile raf;
	Scanner scanner;
	
	ArrayList<Directory> volumeDirectory = new ArrayList<>();
	ArrayList<Superblock> superblockList = new ArrayList<>();
	ArrayList<GroupDescriptor> descriptorList = new ArrayList<>();
	
	//File Specific Variables
	private int NUMBEROFBLOCKGROUPS;
	private int NUMBEROFBLOCKSPERGROUP;
	private int LENGTHOFBLOCKGROUPS;
	private int BLOCKSIZE;
	private int INODESIZE;
	private int NUMBEROFINODESPERGROUP;
	private int INODEPOINTERS[];
	
	public Ext2File(Volume volume) {
		this.raf = volume.getRandomAccessFile();
		setup();
	}
	
	/**
	 * Returns series of bytes given a starting position
	 * in bit. 
	 * 
	 * @param startBit the starting bit where the method will start reading bytes
	 * @param length   the amount of bytes the method will read
	 * @return 		   the bytes of length 'length' starting from startBit
	 * @see			   byte[]
	 */
	public byte[] read(long startBit, long length) {
		byte[] data = new byte[(int) length];
		try {
			raf.seek(startBit);
			raf.read(data);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Set the file specific variables and immediately loads the
	 * base directory from Inode 2.
	 * 
	 * This function is called immediately this class is called. 
	 * 
	 * Function logic:
	 * 1. Read and load Superblock
	 * 2. Read and load GroupDescriptors for all block groups
	 * 3. Load base directory from Inode 2
	 */
	private void setup() {
		superblockList.add(new Superblock(this));
		
		for(int i = 0; i < NUMBEROFBLOCKGROUPS; i++) {
			descriptorList.add(new GroupDescriptor(this, i));
		}
		
		Inode inode2 = new Inode(this, 0, 2);
		loadDirectories(0, inode2.getBlockPointer(0));
		
		run();
	}
	
	/**
	 * Loads all the adjacent directories/files at this filesystem
	 * level.
	 * @param BLOCKGROUPNUMBER The block group number of the directory
	 * @param DIRECTORYPOINTER The directory pointer for this block group
	 */
	
	private void loadDirectories(int BLOCKGROUPNUMBER, int DIRECTORYPOINTER) {
		
		volumeDirectory.clear();
		
		boolean moreDirectories = true;
		int currentDirectoryLength = 0;
		
		while(moreDirectories) {
			volumeDirectory.add(new Directory(this, DIRECTORYPOINTER, currentDirectoryLength));
			currentDirectoryLength = currentDirectoryLength + volumeDirectory.get(volumeDirectory.size() - 1).getLENGTH();
			if(currentDirectoryLength >= 1024) {
				moreDirectories = false;
			}
		}
		
		printDirectories();
	}
	
	/**
	 * Prints all the current accessible directories 
	 * in an UNIX like format
	 * 
	 * FILENAME, LAST ACCESSED TIME, FILETYPE
	 * Each fields is fieldLength spaces/character long
	 * 
	 * hopefully :(
	 */
	private void printDirectories() {
		int fieldLength = 20;
		StringBuilder sb = new StringBuilder(fieldLength);
		
		for(int i = 0; i < volumeDirectory.size(); i++) {

			String name = volumeDirectory.get(i).getNAME();
			String fileType = volumeDirectory.get(i).getFILETYPE_STR();
			String creationDate = volumeDirectory.get(i).getINODE().getCREATIONTIME_STR();
			
			int namePaddingLength =  fieldLength - volumeDirectory.get(i).getNAMELENGTH();
			int fileTypePaddingLength = fieldLength - fileType.length();
			int creationDatePaddingLength = fieldLength - creationDate.length();
			
			sb.append(name);
			for(int n = 0; n < namePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append(fileType);
			for(int n = 0; n < fileTypePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append(creationDate);
			for(int n = 0; n < creationDatePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append("\n");
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * Print out all the datablocks of this particular file
	 * @param DATABLOCKPOINTERS
	 */
	private void getDirectFile(int[] DATABLOCKPOINTERS) {
		for (int i : DATABLOCKPOINTERS) {
			System.out.print(new Datablock(this, i, "file").getDATABLOCKCONTENT_STR());
		}
	}
	
	private void getIndirectFile(int DATABLOCKPOINTER) {
		int[] datapointers = new Datablock(this, DATABLOCKPOINTER, "directory").getDATABLOCKCONTENT_POINTERS();
		getDirectFile(datapointers);
	}
	
	private void getDoubleIndirectFile(int DATABLOCKPOINTER) {
		int[] datapointers = new Datablock(this, DATABLOCKPOINTER, "directory").getDATABLOCKCONTENT_POINTERS();
		for(int i = 0; i < datapointers.length; i++) {
			getIndirectFile(datapointers[i]);
		}
	}
	
	private void getTripleIndirectFile(int DATABLOCKPOINTER) {
		int[] datapointers = new Datablock(this, DATABLOCKPOINTER, "directory").getDATABLOCKCONTENT_POINTERS();
		for(int i = 0; i < datapointers.length; i++) {
			getDoubleIndirectFile(datapointers[i]);
		}
	}
	
	/**
	 * Loads a file given it's inode datablock pointers
	 * 
	 * @param DATABLOCKPOINTERS Inode datablock pointers
	 */
	private void loadFile(int[] DATABLOCKPOINTERS) {
		if(DATABLOCKPOINTERS[12] != 0) {
			getIndirectFile(DATABLOCKPOINTERS[12]);
		}
		else if(DATABLOCKPOINTERS[13] != 0) {
			getDoubleIndirectFile(DATABLOCKPOINTERS[13]);
		}
		else if(DATABLOCKPOINTERS[14] != 0) {
			getTripleIndirectFile(DATABLOCKPOINTERS[14]);
		}
		else {
			getDirectFile(DATABLOCKPOINTERS);
		}
	}
	
	/**
	 * Loads a file/directory given an input.
	 * 
	 * @param input Name of file/directory found in current directory
	 */
	private void loadByName(String input) {
		Directory thisDirectory = null;
		boolean containsDirectory = false;
		
		//Look for file/directory with name "input"
		for(int i = 0; i < volumeDirectory.size(); i++) {
			if(volumeDirectory.get(i).getNAME().equals(input)) {
				thisDirectory = volumeDirectory.get(i);
				containsDirectory = true;
				break;
			}
		}
		
		//If found, either load next level directory or file
		if(containsDirectory) {
			Inode thisInode = thisDirectory.getINODE();
			int[] DATABLOCKPOINTERS = thisInode.getBlockPointers();
			
			if(thisDirectory.getFILETYPE() == 1) {
				loadFile(DATABLOCKPOINTERS);
			}
			else if(thisDirectory.getFILETYPE() == 2) {
				loadDirectories(thisDirectory.getBLOCKGROUPNUMBER(), DATABLOCKPOINTERS[0]);
			}
		}
		else {
			System.out.println("No such file/directory exists");
		}
	}
	
	private void run() {
		scanner = new Scanner(System.in);
		String input = scanner.next();
		loadByName(input);
		run();
	}
	
	public int getNUMBEROFBLOCKGROUPS() {
		return LENGTHOFBLOCKGROUPS;
	}

	public int getNUMBEROFBLOCKSPERGROUP() {
		return NUMBEROFBLOCKSPERGROUP;
	}

	public int getLENGTHOFBLOCKGROUPS() {
		return LENGTHOFBLOCKGROUPS;
	}

	public int getBLOCKSIZE() {
		return BLOCKSIZE;
	}

	public int getINODESIZE() {
		return INODESIZE;
	}

	public int getNUMBEROFINODESPERGROUP() {
		return NUMBEROFINODESPERGROUP;
	}

	public int getINODEPOINTER(int BLOCKGROUPNUMBER) {
		return INODEPOINTERS[BLOCKGROUPNUMBER];
	}
	
	public void setNUMBEROFBLOCKGROUPS(int NUMBEROFBLOCKGROUPS) {
		this.NUMBEROFBLOCKGROUPS = NUMBEROFBLOCKGROUPS;
	}

	public void setNUMBEROFBLOCKSPERGROUP(int NUMBEROFBLOCKSPERGROUP) {
		this.NUMBEROFBLOCKSPERGROUP = NUMBEROFBLOCKSPERGROUP;
	}

	public void setLENGTHOFBLOCKGROUPS(int LENGTHOFBLOCKGROUPS) {
		this.LENGTHOFBLOCKGROUPS = LENGTHOFBLOCKGROUPS;
	}

	public void setBLOCKSIZE(int BLOCKSIZE) {
		this.BLOCKSIZE = BLOCKSIZE;
	}

	public void setINODESIZE(int INODESIZE) {
		this.INODESIZE = INODESIZE;
	}

	public void setNUMBEROFINODESPERGROUP(int NUMBEROFINODESPERGROUP) {
		this.NUMBEROFINODESPERGROUP = NUMBEROFINODESPERGROUP;
	}

	public void setINODEPOINTERSSIZE(int size) {
		this.INODEPOINTERS = new int[size];
	}
	
	public void setINODEPOINTERS(int BLOCKGROUP, int POINTER) {
		INODEPOINTERS[BLOCKGROUP] = POINTER;
	}
}
