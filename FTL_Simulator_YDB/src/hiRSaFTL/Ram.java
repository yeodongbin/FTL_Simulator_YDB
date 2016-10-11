package hiRSaFTL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map.Entry;
import java.io.*;
import java.util.*;

public class Ram {

	// Mapping table
	private static HashMap<Integer, Integer> mappingTable = new HashMap<Integer, Integer>(
			Config.TOTAL_LOGICAL_PAGE_NUM);

	// save Key(LPN) and Value(PPN)

	public HashMap<Integer, Integer> getRAM() {
		return mappingTable;
	}

	public void initial_mappingTable() throws FileNotFoundException {
		if (Config.AGING == false) {
			for (int i = 0; i < Config.TOTAL_LOGICAL_PAGE_NUM; i++) {
				mappingTable.put(i, -1);
			}
		} else {
			for (int i = 0; i < Config.TOTAL_LOGICAL_PAGE_NUM; i++) {
				mappingTable.put(i, -1);
			}

			if (Config.AGING_RAM_INPUT.length() == 0) {
				System.err.println("Input Filename...");
				System.exit(1); // 읽을 파일명을 주지 않았을 때는 종료
			}

			int lpn, ppn;
			try {
				BufferedReader readline = new BufferedReader(new FileReader(
						Config.AGING_RAM_INPUT));

				String line;
				while ((line = readline.readLine()) != null) {
					String[] split = line.split("     ->    ");

					lpn = Integer.valueOf(split[0]);
					ppn = Integer.valueOf(split[1]);

					mappingTable.put(lpn, ppn);
				}
				readline.close();

			} catch (IOException e) {
				// 혹시 입출력 에러가 발생했다면 어떤 에러인지 출력하고 끄자.
				System.err.println(e);
				System.exit(1);
			}
		}
	}

	// physical page table // physical page Array
	// save Key(PPN) and 0 valid, -1 free,-2 Invalid
	static int[] ppnTable = new int[Config.TOTAL_PAGE_NUM];

	public void initial_pageTable() {
		if (Config.AGING == false) {
			for (int i = 0; i < Config.TOTAL_PAGE_NUM; i++) {
				// pageTable.put(i, -1); // -2 Invalid page, -1 free page
				ppnTable[i] = -1;
			}
		} else {
			if (Config.AGING_PAGE_TABLE_INPUT.length() == 0) {
				System.err.println("Input Filename...");
				System.exit(1); // 읽을 파일명을 주지 않았을 때는 종료
			}

			int ppn, stateOfPpn;
			try {
				BufferedReader readline = new BufferedReader(new FileReader(
						Config.AGING_PAGE_TABLE_INPUT));

				String line;
				while ((line = readline.readLine()) != null) {
					String[] split = line.split("     ->    ");

					ppn = Integer.valueOf(split[0]);
					stateOfPpn = Integer.valueOf(split[1]);

					ppnTable[ppn] = stateOfPpn;
				}
				readline.close();

			} catch (IOException e) {
				// 혹시 입출력 에러가 발생했다면 어떤 에러인지 출력하고 끄자.
				System.err.println(e);
				System.exit(1);
			}
			/*
			 * // Saving waste-value 80% for (int i = 0; i <
			 * Config.TOTAL_LOGICAL_PAGE_NUM*0.8; i++) { // pageTable.put(i,
			 * -1); // -2 Invalid page, -1 free page ppnTable[i] = -2; }
			 */
		}

	}

	/*
	 * private static HashMap<Integer, Integer> pageTable = new HashMap<Integer,
	 * Integer>( Config.TOTAL_PAGE_NUM);
	 * 
	 * public HashMap<Integer, Integer> getPageTable() { return pageTable; }
	 */

	// The Valid count of Physical Block
	// save the number of valid page in their Block.
	static int[] blockInfoValid = new int[Config.BLOCK_NUM];

	public void initial_blockValid() {
		if (Config.AGING == false) {
			for (int i = 0; i < Config.BLOCK_NUM; i++) {
				// blockNumValid.put(i, 0);
				blockInfoValid[i] = 0;
			}
		} else {
			if (Config.AGING_BLOCK_TABLE_INPUT.length() == 0) {
				System.err.println("Input Filename...");
				System.exit(1); // 읽을 파일명을 주지 않았을 때는 종료
			}

			int pbn, numOfValid;
			try {
				BufferedReader readline = new BufferedReader(new FileReader(
						Config.AGING_BLOCK_TABLE_INPUT));

				String line;
				while ((line = readline.readLine()) != null) {
					String[] split = line.split("     ->    ");

					pbn = Integer.valueOf(split[0]);
					numOfValid = Integer.valueOf(split[1]);

					blockInfoValid[pbn] = numOfValid;
				}
				readline.close();

			} catch (IOException e) {
				// 혹시 입출력 에러가 발생했다면 어떤 에러인지 출력하고 끄자.
				System.err.println(e);
				System.exit(1);
			}
		}
	}

	/*
	 * private static HashMap<Integer, Integer> blockNumValid = new
	 * HashMap<Integer, Integer>( Config.BLOCK_NUM);
	 * 
	 * public HashMap<Integer, Integer> getBlockValid() { return blockNumValid;
	 * }
	 * 
	 * class BlockInfo { int validPageCount; int freePageCount;
	 * 
	 * public BlockInfo(int validPageCount, int freePageCount) {
	 * this.validPageCount = validPageCount; this.freePageCount = freePageCount;
	 * }
	 * 
	 * public void setBlockInfo(int validPageCount, int freePageCount) {
	 * this.validPageCount = validPageCount; this.freePageCount = freePageCount;
	 * } }
	 */

	// when finding Victim Block, saving Victim Block on Queue
	private static Queue<Integer> victimQue = new LinkedList<Integer>();

	public Queue<Integer> getVictimQue() {
		return victimQue;
	}

	public void hashMapToTxt(HashMap<Integer, Integer> map, String output)
			throws IOException {
		// create your file writer and buffered reader
		FileWriter fstream = new FileWriter(new File(output));
		BufferedWriter bw = new BufferedWriter(fstream);

		// create your iterator for your map
		Iterator<Entry<Integer, Integer>> it = map.entrySet().iterator();

		while (it.hasNext()) {

			// the key/value pair is stored here in pairs
			Map.Entry<Integer, Integer> pairs = it.next();

			// since you only want the value, we only care about
			// pairs.getValue(), which is written to out
			bw.write(pairs.getKey() + "     ->    " + pairs.getValue() + "\n");
		}
		// lastly, close the file and end
		bw.close();
		return;
	}

	public void hashMapToTxt(int[] map, String output) throws IOException {
		// create your file writer and buffered reader
		FileWriter fstream = new FileWriter(new File(output));
		BufferedWriter bw = new BufferedWriter(fstream);

		for (int i = 0; i < map.length; i++) {
			bw.write(i + "     ->    " + map[i] + "\n");
		}
		bw.close();
		return;
	}
}
