package hiRSaFTL;

import java.util.*;

public class GarbageCollectorFTL {

	Ram ram = new Ram();
	AllocatorFTL allocator = new AllocatorFTL();

	// Greedy GC=> smallest valid page per Block, FIFO Block
	public void garbageCollectorFTL(int writingBlock) {
		int currentPage = 0;
		int PageInfo = 0;

		int victimBlock = 0;

		int validCount = 0;
		int minValidCount = 0;

		int flagPartial = 0;
		int flagFull = 0;
		int flagGC = 0;

		// *************** Searching a Victim Block *******************
		// searching minimum Valid in Blocks
		minValidCount = Config.LOGICAL_PAGE_NUM;

		for (int i = 0; i < Config.ALL_BLOCK_NUM; i++) {

			validCount = Ram.blockInfoValid[i];// Check Block valid
			currentPage = i * Config.LOGICAL_PAGE_NUM;// first page number in
														// the Block.
			PageInfo = Ram.ppnTable[currentPage];

			if ((0 == validCount)// Full Merge -> all Invalid
					&& (-2 == PageInfo)) {
				// put a Full Merge victim in Q
				ram.getVictimQue().offer(i);
				flagFull++; // Full merge Count
				//break; 
			} else if ((0 != validCount) && (validCount < minValidCount)
					&& (writingBlock != i)) {
				// partial Merge
				victimBlock = i;
				minValidCount = validCount;
				flagPartial++;
			}
		}

		// if no victim Block -> put a Partial Merge victim in Q
		if ((0 < flagPartial) && (0 == flagFull)
				&& (0 == ram.getVictimQue().size())) {
			ram.getVictimQue().offer(victimBlock);// save 1 victim on Queue
		}

		// *************** Erasing a Victim Block *******************
		// FIFO victim Block to be erased
		while (null != ram.getVictimQue().peek()) {// true => repeat again
			flagGC++;
			victimBlock = ram.getVictimQue().poll();
			validCount = Ram.blockInfoValid[victimBlock];

			if (0 == validCount) { // Full Merge
				eraseBlock(victimBlock);
				Data.fullmerge++;
				//System.out.println("> Erase Block (Full Merge)= " + victimBlock);
			} else if (0 != validCount) { // Partial Merge
				migratedValidPageFromVictimBlkToFreeBlk(victimBlock);
				eraseBlock(victimBlock);

				Data.partialmerge++;
				/*System.out.println("> Erase Block (Partial Merge)="
						+ victimBlock + " ,Moving page= " + validCount);*/
			}
		}

		if (flagGC == 0) {
			System.out.println(" Error: GC do not operate - GC");
		}
		return;
	}

	// RSa GC for Log Buffer
	public void gCLogBuf(int eraseBlock) {

		int currentLPN = 0;
		int startPPN = 0;
		int endPPN = 0;
		int ppn = 0;
		int flagPartial = 0;

		startPPN = eraseBlock * Config.PAGE_NUM;// start_Victim_PPN
		endPPN = startPPN + Config.PAGE_NUM;// end_Victim_PPN

		for (int i = startPPN; i < endPPN; i++) {
			if (0 == Ram.ppnTable[i]) {// if valid page(0) is
				flagPartial++;
				currentLPN = extractedLPNFromMapTableByPPN(i);

				do {// select free_ppn
					ppn = allocator.allocFTL(Ram.ppnTable);
				} while (eraseBlock == (ppn / Config.PAGE_NUM));

				linkedLPNToPPN(currentLPN, ppn);
				Data.all_total_write_pages++;
				Data.validMoveCount++;
				Data.validMoveCountLog++;
			}
		}

		printMergeType(eraseBlock, flagPartial);
		eraseBlock(eraseBlock);
		Data.eraseCountLog++;
		return;
	}
	
	public void gCLLogBuf(int eraseBlock) {//Garbage Collector LogLog Buffer 

		int currentLPN = 0;
		int currentPBN = 0;
		int startPPN = 0;
		int endPPN = 0;
		int ppn = 0;
		int flagPartial = 0;

		startPPN = eraseBlock * Config.PAGE_NUM;// start_Victim_PPN
		endPPN = startPPN + Config.PAGE_NUM;// end_Victim_PPN -1

		for (int i = startPPN; i < endPPN; i++) {
			if (0 == Ram.ppnTable[i]) {// if valid page(0) is
				flagPartial++;
				currentLPN = extractedLPNFromMapTableByPPN(i);

				do {// select free_ppn in Logbuf - YDB
					ppn = allocator.allocLogBuf(Ram.ppnTable);
					//YDB
					currentPBN = mappingLPNToPPN(currentLPN, ppn);
					switchGCLogBuf(ppn, currentPBN);
					
				} while (eraseBlock == (ppn / Config.PAGE_NUM));

				linkedLPNToPPN(currentLPN, ppn);
				Data.all_total_write_pages++;
				Data.validMoveCount++;
				Data.validMoveCountLog++;
			}
		}

		printMergeType(eraseBlock, flagPartial);
		eraseBlock(eraseBlock);
		Data.eraseCountLog++;
		return;
	}
	
	//YDB
	private void switchGCLogBuf(int ppn, int block) {
		// switch GC Log-Buffer //YEO
		if ((Config.ALL_LOG_PAGE_NUM - 1) == ppn) {
			gCLogBuf(Config.ALL_BLOCK_NUM);
		} else if (((Config.LOG_PAGE_NUM - 1) == (ppn % Config.LOG_PAGE_NUM))
				&& (-1 != Ram.ppnTable[ppn + 1])) {
			// GC=>NextBlock
			gCLogBuf(block + 1);
		}
	}
	
	//YDB
	private int mappingLPNToPPN(int currentLPN, int ppn) {
		int pbn;
		pbn = ppn / Config.PAGE_NUM;
		ram.getRAM().put(currentLPN, ppn);
		Ram.ppnTable[ppn] = 0; // 0 valid
		writeNumValidonBlockTable(pbn);
		return pbn;
	}
	
	//YDB
	private void writeNumValidonBlockTable(int block) {
		int validNum;
		// Write Block of the Number of Valid page
		validNum = Ram.blockInfoValid[block] + 1;
		Ram.blockInfoValid[block] = validNum;
	}
	
	
	private void migratedValidPageFromVictimBlkToFreeBlk(int victimBlock) {
		int currPPN;
		int currentLPN;
		int ppn;
		currPPN = victimBlock * Config.PAGE_NUM;// start_VictimPPN

		for (int i = currPPN; i < currPPN + Config.PAGE_NUM; i++) {
			if (0 == Ram.ppnTable[i]) {// if valid page(0) is
				currentLPN = extractedLPNFromMapTableByPPN(i);

				do {// Do not choice free Block on equal with victimBlock
					ppn = allocator.allocFTL(Ram.ppnTable);
				} while ((ppn / Config.PAGE_NUM) == victimBlock);

				linkedLPNToPPN(currentLPN, ppn);
				Data.all_total_write_pages++;
				Data.validMoveCount++;
			}
		}
	}

	private int extractedLPNFromMapTableByPPN(int currPPN) {
		int currentLPN;
		int currPBN;

		// Write mapping information
		currentLPN = getLPNFromPPN(ram.getRAM(), currPPN);

		Ram.ppnTable[currPPN] = -2; // 0 valid=> -2Invalid
		currPBN = currPPN / Config.PAGE_NUM; // physical Block
		subtractedValidNumOnBlockTable(currPBN);

		return currentLPN;
	}

	private void subtractedValidNumOnBlockTable(int currPBN) {
		int validNum;
		validNum = Ram.blockInfoValid[currPBN] - 1;// Valid Block -1
		Ram.blockInfoValid[currPBN] = validNum;
	}

	private void linkedLPNToPPN(int currentLPN, int ppn) {
		int pbn;

		ram.getRAM().put(currentLPN, ppn);
		Ram.ppnTable[ppn] = 0;
		pbn = ppn / Config.PAGE_NUM;
		addedValidNumOnBlockTable(pbn);
	}

	private void addedValidNumOnBlockTable(int currPBN) {
		int validNum;

		// Write Block of the Number of Valid page
		validNum = Ram.blockInfoValid[currPBN] + 1;
		Ram.blockInfoValid[currPBN] = validNum;

	}

	private void printMergeType(int eraseBlock, int flagPartial) {
		if (flagPartial > 0) {
			Data.partialmerge++;
			//System.out.println(">> Erase Log Block (Partial Merge)="+ eraseBlock);
		} else {
			Data.fullmerge++;
			//System.out.println(">> Erase Log Block (Full Merge)="+ eraseBlock);
		}
	}

	// GC switch
	public boolean freePageCount() {
		int end = 0;
		int numValue = 0;
		boolean switchGC = false;

		try {// Count free page in mappingTable
			end = Config.ALL_PAGE_NUM;

			for (int i = 0; i < end; i++) {
				if (-1 == Ram.ppnTable[i]) {
					numValue++;
				}
			}

			if (0 == numValue) {// Overflow checking
				System.out
						.println("** There is no space to save in physical area!! **");
				System.exit(999);
			}

			if (numValue < Config.SWITCH_GC_NUM) {
				switchGC = true;
			}

		} catch (Exception e) {
			System.out.println(" Error : freePageCount " + e);
		}
		return switchGC;
	}

	// Select Victim Block on MappingTable
	public void eraseBlock(int Block) {
		int start_page;
		int end_page;

		start_page = Block * Config.PAGE_NUM;
		end_page = start_page + Config.PAGE_NUM;

		for (int i = start_page; i < end_page; i++) {
			Ram.ppnTable[i] = -1; // -1 free page, -2 Invalid page,
		}

		Ram.blockInfoValid[Block] = 0;
		Data.eraseCount++;
		return;
	}

	public static int getLPNFromPPN(HashMap<Integer, Integer> hmap, int value) {
		for (int obj : hmap.keySet()) {
			if (hmap.get(obj).equals(value)) {
				return obj;
			}
		}
		System.out.println("Error getLPNFromPPN - GC");
		System.exit(0);// YEO
		return 0;
	}
}
