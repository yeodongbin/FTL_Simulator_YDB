/*
 * Config is setting class. 
 * if you want to change the setting of FTL
 * you should change option on Config.class
 */

package hiRSaFTL;

public class Config {

	public static boolean AGING = true;// false true
	public static int FTL = 2; 
	// 0:���ÿ� �ǽ�(�̿ϼ�) 
	// 1: PageFTL, 2:RSaFTL, 3:HiRSaFTL, 
	// 4:RSaFTL_DelayAlgorithm 5:HiRSaFTL_DelayAlgorithm

	static final int PAGE_BYPT_SIZE = 4096; // 4KB = 4096byte

	
/*
	// Logical Area
	static final int LOGICAL_BLOCK_NUM = 64; // plane size = 4096
	static final int LOGICAL_PAGE_NUM = 4;   // The number of pages in a block
	static final int TOTAL_LOGICAL_PAGE_NUM = LOGICAL_BLOCK_NUM * LOGICAL_PAGE_NUM;

	// Overflow provision
	static final int OP_BLOCK_NUM = (FTL == 1) ? 32
									: (((FTL == 2)||(FTL == 4)) ? 16
									: (((FTL == 3)||(FTL == 5)) ? 16
									: 0));
	static final int OP_PAGE_NUM = LOGICAL_PAGE_NUM;
	static final int TOTAL_OP_PAGE_NUM = OP_BLOCK_NUM * OP_PAGE_NUM;

	// *Log Buffer Area // page
	static final int LOG_BLOCK_NUM = (FTL == 1) ? 0
									: (((FTL == 2)||(FTL == 4)) ? 16
									: (((FTL == 3)||(FTL == 5)) ? 12
									: 0));
	static final int LOG_PAGE_NUM = LOGICAL_PAGE_NUM;
	static final int TOTAL_LOG_PAGE_NUM = LOG_BLOCK_NUM * LOG_PAGE_NUM;

	// **LogLog Buffer Area
	static final int LLOG_BLOCK_NUM = (FTL == 1) ? 0
									: (((FTL == 2)||(FTL == 4)) ? 0
									: (((FTL == 3)||(FTL == 5)) ? 4
									: 0));
	static final int LLOG_PAGE_NUM = LOGICAL_PAGE_NUM;
	static final int TOTAL_LLOG_PAGE_NUM = LLOG_BLOCK_NUM * LLOG_PAGE_NUM;
*/

	// Logical Area
		static final int LOGICAL_BLOCK_NUM = 4096; // plane size = 4096
		static final int LOGICAL_PAGE_NUM = 256;// The number of pages in a block
		static final int TOTAL_LOGICAL_PAGE_NUM = LOGICAL_BLOCK_NUM * LOGICAL_PAGE_NUM;

		// Overflow provision
		static final int OP_BLOCK_NUM = (FTL == 1) ? 512
										: (((FTL == 2)||(FTL == 4)) ? 384
										: (((FTL == 3)||(FTL == 5)) ? 256
										: 0));
		static final int OP_PAGE_NUM = LOGICAL_PAGE_NUM;
		static final int TOTAL_OP_PAGE_NUM = OP_BLOCK_NUM * OP_PAGE_NUM;

		// *Log Buffer Area // page
		static final int LOG_BLOCK_NUM = (FTL == 1) ? 0
										: (((FTL == 2)||(FTL == 4)) ? 128
										: (((FTL == 3)||(FTL == 5)) ? 25
										: 0));
		static final int LOG_PAGE_NUM = LOGICAL_PAGE_NUM;
		static final int TOTAL_LOG_PAGE_NUM = LOG_BLOCK_NUM * LOG_PAGE_NUM;

		// **LogLog Buffer Area
		static final int LLOG_BLOCK_NUM = (FTL == 1) ? 0
										: (((FTL == 2)||(FTL == 4)) ? 0
										: (((FTL == 3)||(FTL == 5)) ? 231
										: 0));
		static final int LLOG_PAGE_NUM = LOGICAL_PAGE_NUM;
		static final int TOTAL_LLOG_PAGE_NUM = LLOG_BLOCK_NUM * LLOG_PAGE_NUM;
		
	//*/
	// ALL ��쿡 ���� �����.
	static final int ALL_BLOCK_NUM = LOGICAL_BLOCK_NUM + OP_BLOCK_NUM;
	static final int ALL_PAGE_NUM = (ALL_BLOCK_NUM * LOGICAL_PAGE_NUM);
	
	// ALL LOG ��쿡 ���� �����.
	static final int ALL_LOG_BLOCK_NUM = LOGICAL_BLOCK_NUM + OP_BLOCK_NUM 
										+ LOG_BLOCK_NUM;
	static final int ALL_LOG_PAGE_NUM = (ALL_LOG_BLOCK_NUM * LOGICAL_PAGE_NUM);

	// Physical total Area
	static final int BLOCK_NUM = LOGICAL_BLOCK_NUM + OP_BLOCK_NUM 
								+ LOG_BLOCK_NUM 
								+ LLOG_BLOCK_NUM ;
	static final int PAGE_NUM = LOGICAL_PAGE_NUM;
	static final int TOTAL_PAGE_NUM = BLOCK_NUM * PAGE_NUM;

	// GC Switch// more than OP area 
	//(ex) 4 op block*page num =16 page)
	//static final int SWITCH_GC_NUM = OP_BLOCK_NUM * LOGICAL_PAGE_NUM;
	static final int SWITCH_GC_NUM = 2 * LOGICAL_PAGE_NUM;

	static final String FILE_SYSTEM_INPUT = "D:/���� �ڷ�/0. ���� �ڷ�/Traces_MSR/"
			+ "msr-cambridge1/MSR-Cambridge/hm_0.csv/CAMRESHMSA01-lvm0.csv";
	static final String FILE_SYSTEM_OUTPUT = "./FileOut_hm_0.txt";
	// static final String FILE_SYSTEM_OUTPUT = "test"+".txt";

	// Aging File
	static final String AGING_BLOCK_TABLE_INPUT = "./aging_Physical_Block.txt";
	static final String AGING_PAGE_TABLE_INPUT = "./aging_Physical_Page.txt";
	static final String AGING_RAM_INPUT = "./aging_LPN_PPN_Mapping_Table.txt";
	
	// Test��	
	static final String BLOCK_TABLE_OUTPUT = "./Physical_Block" + FTL
				+ ".txt";
	static final String PAGE_TABLE_OUTPUT = "./Physical_Page" + FTL
				+ ".txt";
	static final String RAM_OUTPUT = "./LPN_PPN_Mapping_Table" + FTL
				+ ".txt";
	
	static final String LOG_TABLE_OUTPUT = "./Log_Table_Output.txt";
}
