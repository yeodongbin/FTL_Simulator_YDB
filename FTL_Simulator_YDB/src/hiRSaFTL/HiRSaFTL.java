/**
 * @author YEO, DONGBIN
 * 2016-04-06 Hierarchy Request-Size aware FTL coding start!
 *  
 */
package hiRSaFTL;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class HiRSaFTL extends GarbageCollectorFTL {

	public void hiRSaFTL() throws IOException {
		int lpn = 0;
		int currentLPN = 0;
		int ppn = 0;
		int currentPBN = 0;
		int size = 0;
		int temp = -3;
		long counter = 0;
		String readWrite = null;

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
					Data.count_total_ex_write++;
					countWrited1page(size);// Count 1 page

					if (true == ram.getRAM().containsKey(lpn)) {// key-check
						for (int i = 0; i < size; i++) {
							// External write pages No.
							Data.count_total_ex_write_pages++;
							Data.all_total_write_pages++;
							currentLPN = lpn + i;

							// ****** Size Classification (Sequential , Random)
							// // Sequential Written LPN
							if (1 < size) {
								// mapping table entry: empty
								if (-1 == ram.getRAM().get(currentLPN)) {
									currentPBN = mappingFreeLPNtoPPN(currentLPN);
								} else {
									// mapping table entry: filled
									currentPBN = detachLPN(currentLPN);
									ppn = allocator.allocFTL(Ram.ppnTable);

									if (-1 == ppn) {
										System.out
												.println(" allocatiorFTL Error ");
										break;
									} else {
										currentPBN = mappingLPNToPPN(
												currentLPN, ppn);
									}
								}
								// Random Written LPN
							} else if (1 == size) {

								ppn = ram.getRAM().get(currentLPN);
								temp = ppn;
								// // mapping table entry: empty
								if (-1 == ram.getRAM().get(currentLPN)) {
									currentPBN = mappingFreeLPNtoPPN(currentLPN);
								} else {
									// mapping table entry: filled
									// // in Data Blocks
									if (ppn < Config.ALL_PAGE_NUM) {
										// // Previous data is stored in several
										if (false == checkSequentialAddr(currentLPN)) {
											// saved in Log buffer
											currentPBN = detachLPN(currentLPN);
											ppn = allocator
													.allocLogBuf(Ram.ppnTable);

											if (temp == ppn) {// Checking Error
												System.out
														.println("Error!-PageFTLBuffer");
												System.exit(999);// Error code
												break;
											} else {
												currentPBN = mappingLPNToPPN(
														currentLPN, ppn);
												switchGCLogBuf(ppn, currentPBN);
											}

											// Previous data is stored in a row
										} else {// saved in Data Blocks
											currentPBN = detachLPN(currentLPN);
											ppn = allocator
													.allocFTL(Ram.ppnTable);

											if (-1 == ppn) {
												System.out
														.println(" allocatiorFTL Error ");
												break;
											} else {
												currentPBN = mappingLPNToPPN(
														currentLPN, ppn);
											}

										}
										// in Random Log or LLog buffer
									} else if (ppn >= Config.ALL_PAGE_NUM) {
										currentPBN = detachLPN(currentLPN);
										ppn = allocator
												.allocLLogBuf(Ram.ppnTable);

										if (temp == ppn) {// Checking Error
											System.out
													.println("Error - //in Random Log buffer");
											System.exit(999);// Error code
											break;
										} else {
											currentPBN = mappingLPNToPPN(
													currentLPN, ppn);
											switchGCLLogBuf(ppn, currentPBN);
										}

									} else {
										System.out
												.println("Error - ppn number error");
										System.exit(999);// Error code
										break;
									}
								}
							} else {
								System.out.println(" allocatior Error ");
								break;
							}
							switchGC(currentPBN);// GC switch
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

	private void switchGC(int block) {
		// GC switch
		if (true == freePageCount()) {// Free page Check
			// block = writing block
			garbageCollectorFTL(block);
		}
	}

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

	// check point!!-YDB**
	private void switchGCLLogBuf(int ppn, int block) {
		// switch GC LogLog-Buffer //YEO
		if ((Config.TOTAL_PAGE_NUM - 1) == ppn) {
			gCLLogBuf(Config.ALL_LOG_BLOCK_NUM);
		} else if (((Config.LLOG_PAGE_NUM - 1) == (ppn % Config.LLOG_PAGE_NUM))
				&& (-1 != Ram.ppnTable[ppn + 1])) {
			// GC=>NextBlock
			gCLLogBuf(block + 1);
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
		validNum = Ram.blockInfoValid[currPBN] - 1;// subtraction Valid-score
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
		int ppn;
		int block;

		ppn = allocator.allocFTL(Ram.ppnTable);
		block = mappingLPNToPPN(currentLPN, ppn);
		return block;
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
			Data.count_write_ex_1page++;
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
