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
	private long currentPos = 0;
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
	 * Returns series of bytes from currentPos
	 * @param length the amount of bytes the method will read
	 * @return		 the bytes of length 'length' starting from currentPos
	 */
	public byte[] read(long length) {
		byte[] data = new byte[(int) length];
		try {
			raf.seek(currentPos);
			raf.read(data);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * Sets currentPos to a specific number
	 * @param currentPos Move byte to currentPos position in file
	 */
	public void seek(long currentPos) {
		this.currentPos = currentPos;
	}
	
	/**
	 * Gets the current offset in file
	 * @return Current position in file
	 */
	public long position() {
		return currentPos;
	}
	
	/**
	 * Turn a series of bytes into hexadecimal and 
	 * prints it out.
	 * @param bytes
	 */
	public void dumpHexByte(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		int s = 0;
		int l = 0;
		for(int i = 0; i < bytes.length; i++) {
			if(s == 1) {
				sb.append(" ");
				s = 0;
			}
			if(l == 4) {
				sb.append("\n");
				l = 0;
			}
			sb.append(String.format("%02x", bytes[i]));
			l++;
			s++;
		}
		System.out.print(sb.toString());
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
		loadDirectories(0, inode2.getBlockPointers());
		
		run();
	}
	
	/**
	 * Loads all the adjacent directories/files at this filesystem
	 * level.
	 * @param BLOCKGROUPNUMBER The block group number of the directory
	 * @param DIRECTORYPOINTER The directory pointer for this block group
	 */
	
	private void loadDirectories(int BLOCKGROUPNUMBER, int[] DIRECTORYPOINTERS) {
		volumeDirectory.clear();
		volumeDirectory.trimToSize();
		
		for(int i = 0; i < DIRECTORYPOINTERS.length; i++) {
			if(DIRECTORYPOINTERS[i] != 0) {
				boolean moreDirectories = true;
				int currentDirectoryLength = 0;
				
				while(moreDirectories) {
					volumeDirectory.add(new Directory(this, DIRECTORYPOINTERS[i], currentDirectoryLength));
					currentDirectoryLength = currentDirectoryLength + volumeDirectory.get(volumeDirectory.size() - 1).getLENGTH();
					if(currentDirectoryLength >= 1024) {
						moreDirectories = false;
					}
				}
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
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < volumeDirectory.size(); i++) {

			String name = volumeDirectory.get(i).getNAME();
			String fileType = volumeDirectory.get(i).getFILETYPE_STR();
			String fileSize = volumeDirectory.get(i).getINODE().getFileSize_STR();
			String creationDate = volumeDirectory.get(i).getINODE().getCREATIONTIME_STR();
			
			int fileTypePaddingLength = 15 - fileType.length();
			int fileSizePaddingLength = 15 - fileSize.length();
			int creationDatePaddingLength = 28 - creationDate.length();
			int namePaddingLength =  20 - volumeDirectory.get(i).getNAMELENGTH();
			
			sb.append(fileType);
			for(int n = 0; n < fileTypePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append(fileSize);
			for(int n = 0; n < fileSizePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append(creationDate);
			for(int n = 0; n < creationDatePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append(name);
			for(int n = 0; n < namePaddingLength; n++) {
				sb.append(" ");
			}
			
			sb.append("\n");
		}
		
		System.out.print(sb.toString());
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
				for(int i = 0; i < DATABLOCKPOINTERS.length; i++) {
					if(DATABLOCKPOINTERS[i] != 0) {
						loadDirectories(thisDirectory.getBLOCKGROUPNUMBER(), DATABLOCKPOINTERS);
					}
				}
			}
		}
		else {
			System.out.println("No such file/directory exists");
		}
	}
	
	/**
	 * Allows for input of file/directory name. Allows for
	 * traversal of file system via the console.
	 */
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
