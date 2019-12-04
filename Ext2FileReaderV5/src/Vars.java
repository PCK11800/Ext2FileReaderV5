
public class Vars {
	
	/**
	 * Vars is a set of fixed variables consistent throughout this ext2 file system.
	 * 
	 * These includes the offsets of specific values in the superblocks, group descriptors
	 * and other components which makes up the file system.
	 */
	
	static final int ALL_SUPERBLOCKLENGTH = 1024;
	static final int ALL_GROUPDESCRIPTORLENGTH = 32;
	
	static final int SUPERBLOCK_MAGICNUMBER_OFFSET = 56;
	static final int SUPERBLOCK_INODETOTAL_OFFSET = 0;
	static final int SUPERBLOCK_BLOCKTOTAL_OFFSET = 4;
	static final int SUPERBLOCK_BLOCKSIZE_OFFSET = 24;
	static final int SUPERBLOCK_NUMBEROFBLOCKSPERGROUP_OFFSET = 32;
	static final int SUPERBLOCK_NUMBEROFINODESPERGROUP_OFFSET = 40;
	static final int SUPERBLOCK_INODESIZE_OFFSET = 88;
	static final int SUPERBLOCK_VOLUMELABEL_OFFSET = 120;
	static final int SUPERBLOCK_VOLUMELABEL_LENGTH = 16;
	
	static final int GROUPDESCRIPTOR_INODETABLEPOINTER_OFFSET = 8;
	
	static final int INODE_FILEMODE_OFFSET = 0;
	static final int INODE_USERID_OFFSET = 2;
	static final int INODE_FILESIZELOWER_OFFSET = 4;
	static final int INODE_LASTACCESSTIME_OFFSET = 8;
	static final int INODE_CREATIONTIME_OFFSET = 12;
	static final int INODE_LASTMODIFIEDTIME_OFFSET = 16;
	static final int INODE_GROUPIDLOWER_OFFSET = 24;
	static final int INODE_NUMOFHLREFFILE_OFFSET = 26;
	static final int INODE_BLOCKPOINTERS_OFFSET = 40;
	static final int INODE_INDIRECTPOINTER_OFFSET = 88;
	static final int INODE_DOUBLEINDIRECTPOINTER_OFFSET = 92;
	static final int INODE_TRIPLEINDIRECTPOINTER_OFFSET = 96;
	static final int INODE_FILESIZEUPPER_OFFSET = 108;
	
	static final int DIRECTORY_INODE_OFFSET = 0;
	static final int DIRECTORY_LENGTH_OFFSET = 4;
	static final int DIRECTORY_NAMELENGTH_OFFSET = 6;
	static final int DIRECTORY_FILETYPE_OFFSET = 7;
	static final int DIRECTORY_FILENAME_OFFSET = 8;
}
