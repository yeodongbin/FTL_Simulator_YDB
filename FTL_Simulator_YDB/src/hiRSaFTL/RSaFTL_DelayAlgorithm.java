package hiRSaFTL;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RSaFTL_DelayAlgorithm extends GarbageCollectorFTL {
	public void rSaFTL_delay() throws IOException {
		int lpn = 0;
		int currentLPN = 0;
		int ppn = 0;
		int block = 0;
		int size = 0;
		long counter = 0;
		String readWrite = null;
		
		Boolean gCState = false;
		Boolean gCLogBufState = false;

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_SYSTEM_OUTPUT))) {
			InitTables(); // initializing Tables in RAM class

			for (String line; (line = br.readLine()) != null; counter++) {
				String[] elements = line.split(",");

				if (counter % 100000 == 0) {
					System.out.println("Reading Line Number " + counter);
				}

				// Save the LPNs in RAM
				readWrite = elements[0];// read or write operation
				lpn = Integer.parseInt(elements[1]); // LPN
				size = Integer.parseInt(elements[2]); // SIZE of the writing

				if ("Write".equals(readWrite)) {
					// External write request No.
					Data.write_req++;
					countWrited1page(size);// Count 1 page

					if (true == ram.getRAM().containsKey(lpn)) {// key-check
						for (int i = 0; i < size; i++) {
							// External write pages No.
							Data.ex_write_pages++;
							Data.total_write_pages++;
							currentLPN = lpn + i;

							// *** 1. When LPN have not been linked with PPN,
							// -1 = free LPN
							if (-1 == ram.getRAM().get(currentLPN)) {
								block = mappingFreeLPNtoPPN(currentLPN);

								// *** 2. When LPN has a valid page,...
							} else if (((false == checkSequentialAddr(currentLPN)))
									&& (1 == size) && (true == gCLogBufState)) {// YDB

								block = detachLPN(currentLPN);
								ppn = allocator.allocLogBuf(Ram.ppnTable);

								if (-1 == ppn) {// Checking Error
									System.out.println("Error - PageFTLBuffer");
									System.exit(999);// Error code
									break;
								} else {
									block = mappingLPNToPPN(currentLPN, ppn);
									switchGCLogBuf(ppn, block);
								}

								// *** 3. when LPN has many valid pages,...
							} else if (-1 < ram.getRAM().get(currentLPN)) {
								// allocation
								block = detachLPN(currentLPN);
								ppn = allocator.allocFTL(Ram.ppnTable);

								if (-1 == ppn) {// Checking Error
									System.out.println(" allocatiorFTL Error ");
									break;
								} else {
									block = mappingLPNToPPN(currentLPN, ppn);
								}
							}

							gCState = switchGC(block);// GC switch
							//gcLog needs 2 Condition to operate its GC
							//turnOn, switchGCLogBuf
							gCLogBufState = turnOnGCLogbuf(gCState,gCLogBufState);
											
							if ((true==gCState)&&(false==gCLogBufState)){
							System.out.println("gCstate = "+ gCState);
							System.out.println("gCLogBufState = "+ gCLogBufState);
							System.exit(999);}
						}

					} else {
						System.out.println("Error : Overflow LPN");
					}
				} else if ("Read".equals(readWrite)) {
					Data.count_total_read++;
				}
			}

			hashMapToTxt();// record Hash Map to .txt file
		} catch (IOException e) {
			System.out.println("Exception while reading txt file: ");
		}
	}

	private Boolean turnOnGCLogbuf(Boolean gCState, Boolean gCLogBufState) {
		if ((true == gCState) && (false == gCLogBufState)) {
			System.out.println("***  TURN ON GC LOGBUF  ***");
			gCLogBufState = true;
		}
		return gCLogBufState;
	}

	private boolean switchGC(int writingBlock) {
		// GC switch
		boolean gCState = false;

		if (true == countFreePages()) {// Free page Check
			// block = writing block
			garbageCollectorFTL(writingBlock);
			gCState = true;
		}

		return gCState;
	}

	private void switchGCLogBuf(int ppn, int block) {
		// switch GC Log-Buffer //YEO
		if ((Config.TOTAL_PAGE_NUM - 1) == ppn) {
			gCLogBuf(Config.ALL_BLOCK_NUM);
		} else if (((Config.LOG_PAGE_NUM - 1) == (ppn % Config.LOG_PAGE_NUM))
				&& (-1 != Ram.ppnTable[ppn + 1])) {
			// GC=>NextBlock
			gCLogBuf(block + 1);
		}
	}

	private int detachLPN(int currentLPN) {
		int currPPN;
		int currPBN;
		int validNum;

		// when LPN has valid page,...
		// Changing currentLPN' PPN -> Invalid Page -2
		// valid page decreased =>get(block) - 1;
		currPPN = ram.getRAM().get(currentLPN);
		Ram.ppnTable[currPPN] = -2;// -2 Invalid

		currPBN = currPPN / Config.PAGE_NUM;
		validNum = Ram.blockInfoValid[currPBN] - 1;// Valid
		Ram.blockInfoValid[currPBN] = validNum;
		return currPBN;
	}

	private int mappingLPNToPPN(int currentLPN, int ppn) {
		int pbn;

		pbn = ppn / Config.PAGE_NUM;
		ram.getRAM().put(currentLPN, ppn);
		Ram.ppnTable[ppn] = 0; // 0 valid
		writeNumValidonBlockTable(pbn);
		return pbn;
	}

	private int mappingFreeLPNtoPPN(int currentLPN) {
		int freePPN; // physical Page Number
		int pBN; // Logical Block Number

		freePPN = allocator.allocFTL(Ram.ppnTable);
		pBN = mappingLPNToPPN(currentLPN, freePPN);

		return pBN;
	}

	private void writeNumValidonBlockTable(int block) {
		int validNum;
		// Write Block of the Number of Valid page
		validNum = Ram.blockInfoValid[block] + 1;
		Ram.blockInfoValid[block] = validNum;
	}

	private void hashMapToTxt() throws IOException {
		ram.hashMapToTxt(ram.getRAM(), Config.RAM_OUTPUT);
		ram.hashMapToTxt(Ram.ppnTable, Config.PAGE_TABLE_OUTPUT);
		ram.hashMapToTxt(Ram.blockInfoValid, Config.BLOCK_TABLE_OUTPUT);
	}

	private void InitTables() throws FileNotFoundException {
		ram.initial_mappingTable();// initializing mapping Table
		ram.initial_blockValid();// initializing block NumValid
		ram.initial_pageTable();// initializing Physical page Table
	}

	private void countWrited1page(int size) {
		if (size == 1) {
			Data.write_req_1page++;
		}
	}

	private boolean checkSequentialAddr(int currentLPN) {

		boolean output = false;
		int frontCurrLPN = (0 > (currentLPN - 1)) ? Config.TOTAL_LOGICAL_PAGE_NUM - 1
				: (currentLPN - 1);
		int rearCurrLPN = (currentLPN + 1) % Config.TOTAL_LOGICAL_PAGE_NUM;

		// i fixed it when inital value = -1
		if ((-1 == ram.getRAM().get(currentLPN) - 1)
				&& (ram.getRAM().get(rearCurrLPN) != ram.getRAM().get(
						currentLPN) + 1)) {
			output = false;
		} else if ((ram.getRAM().get(frontCurrLPN) == ram.getRAM().get(
				currentLPN) - 1)
				|| (ram.getRAM().get(rearCurrLPN) == ram.getRAM().get(
						currentLPN) + 1)) {
			output = true;
		}

		// log buffer
		if (ram.getRAM().get(currentLPN) >= Config.TOTAL_LOGICAL_PAGE_NUM) {
			output = false;
		}

		return output;
	}
}
