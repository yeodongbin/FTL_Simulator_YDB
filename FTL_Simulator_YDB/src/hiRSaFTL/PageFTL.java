package hiRSaFTL;

import java.io.*;

public class PageFTL extends GarbageCollectorFTL {

	public void pageFTL() throws IOException {

		int lpn = 0;
		int currentLPN = 0;
		int ppn = 0;
		int block = 0;
		int size = 0;
		long counter = 0;

		String readWrite = null;

		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_SYSTEM_OUTPUT))) {
			InitTables(); // initializing Tables in RAM class
			
			System.out.println("=> Completed initialization");

			for (String line; (line = br.readLine()) != null; counter++) {

				if (counter % 100000 == 0) {
					System.out.println("Reading Line Number " + counter);
				}

				String[] elements = line.split(",");

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

							// *** 1. When LPN have not been linked with PPN,
							if (-1 == ram.getRAM().get(currentLPN)) {
								// System.out.println("mappingFreLPNtoPPN");
								block = mappingFreeLPNtoPPN(currentLPN);

							} else if (-1 < ram.getRAM().get(currentLPN)) {// valid

								block = DetachLPN(currentLPN);
								ppn = allocator.allocFTL(Ram.ppnTable);

								if (-1 == ppn) {// Checking Error
									System.out.println(" allocatiorFTL Error ");
									break;
								} else {
									// System.out.println("mappingValidLPNToPPN");
									block = mappingValidLPNToPPN(currentLPN,
											ppn);
								}
							}
							// System.out.println("switchGC");
							switchGC(block);// GC switch
						}
					} else {
						System.out.println("Error : Overflow LPN");
					}
				} else if ("Read".equals(readWrite)) {
					Data.count_total_read++;
				}
			}

			HashMapToTxt();// record Hash Map to .txt file
		} catch (IOException e) {
			System.out.println("Exception while reading txt file: " + e);
		}
	}

	private void switchGC(int block) {
		// GC switch
		if (true == freePageCount()) {// Free page Check
			// block = writing block
			garbageCollectorFTL(block);
		}
	}

	private int mappingValidLPNToPPN(int currentLPN, int ppn) {
		int block;
		int validNum;
		block = ppn / Config.PAGE_NUM;
		ram.getRAM().put(currentLPN, ppn);
		Ram.ppnTable[ppn] = 0; // 0 valid

		// Counting valid page per Block
		validNum = Ram.blockInfoValid[block] + 1;
		Ram.blockInfoValid[block] = validNum;
		return block;
	}

	private int DetachLPN(int currentLPN) {
		int currPPN;
		int currPBN;
		int validNum;
		// when LPN has valid page,...
		// Changing currentLPN' PPN -> Invalid Page -2
		// valid page decreased =>get(block) - 1;
		currPPN = ram.getRAM().get(currentLPN);
		Ram.ppnTable[currPPN] = -2;// Invalid

		currPBN = currPPN / Config.PAGE_NUM;
		validNum = Ram.blockInfoValid[currPBN] - 1;
		Ram.blockInfoValid[currPBN] = validNum;
		return currPBN;
	}

	private int mappingFreeLPNtoPPN(int currentLPN) {
		int ppn;
		int pbn;
		int validNum;
		// Allocator Operation
		ppn = allocator.allocFTL(Ram.ppnTable);

		// Write mapping information
		ram.getRAM().put(currentLPN, ppn);
		Ram.ppnTable[ppn] = 0;

		// Write Block of the Number of Valid page
		pbn = ppn / Config.PAGE_NUM;// physical Block
		validNum = Ram.blockInfoValid[pbn] + 1;
		Ram.blockInfoValid[pbn] = validNum;
		return pbn;
	}

	private void countWrited1page(int size) {
		if (size == 1) {
			Data.count_write_ex_1page++;
		}
	}

	private void HashMapToTxt() throws IOException {
		ram.hashMapToTxt(ram.getRAM(), Config.RAM_OUTPUT);
		ram.hashMapToTxt(Ram.ppnTable, Config.PAGE_TABLE_OUTPUT);
		ram.hashMapToTxt(Ram.blockInfoValid, Config.BLOCK_TABLE_OUTPUT);
	}

	private void InitTables() throws FileNotFoundException {
		ram.initial_mappingTable();// initializing mapping Table
		ram.initial_blockValid();// initializing block NumValid
		ram.initial_pageTable();// initializing Physical page Table
	}
}
