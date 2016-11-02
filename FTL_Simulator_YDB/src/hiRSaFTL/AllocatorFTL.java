package hiRSaFTL;

import java.io.IOException;

public class AllocatorFTL {

	static int currentPoint = 0;

	public int allocFTL(int[] ppnTable) {
		int result = -1;
		int searchCount = 0;

		Ram ram1 = new Ram();

		while (searchCount < Config.ALL_PAGE_NUM) {
			searchCount++;

			// -1 Free page
			currentPoint = currentPoint % Config.ALL_PAGE_NUM;
			if (-1 == ppnTable[currentPoint]) {
				result = currentPoint;
				currentPoint++;
				break;
			} else {
				currentPoint++;
			}
		}

		ErrorCheck(result, ram1);// Error Check = result == -1
		return result;
	}

	static int currentBufPoint = -1;
	static int currentBufBufPoint = -1;

	public int allocLogBuf(int[] LogBufferTable) {
		int result = -1;
		int lastNum = Config. ALL_LOG_PAGE_NUM ;

		// initialize currentBufPoint
		if (-1 == currentBufPoint) {
			for (int i = Config.ALL_PAGE_NUM; i < Config.ALL_LOG_PAGE_NUM; i++) {
				if (-1 == LogBufferTable[i]) {
					currentBufPoint = i;
					break;
				} 
			}
		}

		if (-1 == LogBufferTable[currentBufPoint]) {// -1 Free page
			result = currentBufPoint;
			currentBufPoint++;

			if (lastNum == currentBufPoint) {
				currentBufPoint = Config.ALL_PAGE_NUM;
			}
		} else {
			System.out.println("YDB");
			System.out.println("currentBufPoint Error!! - allocatorLogBuf");
			System.exit(0);
		}

		return result;
	}
	
	public int allocLLogBuf(int[] LogLogBufferTable) {
		int result = -1;
		int lastNum = Config.TOTAL_PAGE_NUM;

		// initialize currentBufBufPoint
		if (-1 == currentBufBufPoint) {
			for (int i = Config.ALL_LOG_PAGE_NUM; i < Config.TOTAL_PAGE_NUM; i++) {
				if (-1 == LogLogBufferTable[i]) {
					currentBufBufPoint = i;
					break;
				} 
			}
		}

		if (-1 == LogLogBufferTable[currentBufBufPoint]) {// -1 Free page
			result = currentBufBufPoint;
			currentBufBufPoint++;

			if (lastNum == currentBufBufPoint) {
				currentBufBufPoint = Config.ALL_LOG_PAGE_NUM;
			}
		} else {
			System.out.println("current Buf BufPoint Error!! - allocatorLogLogBuf ");
			System.exit(0);
		}

		return result;
	}

	private void ErrorCheck(int result, Ram ram) {
		if (-1 == result) { // allocator Error Check
			System.out.println(" Error : Physical Memory Full - allocatorFTL");

			try {
				ram.hashMapToTxt(ram.getRAM(), Config.RAM_OUTPUT);
				ram.hashMapToTxt(Ram.ppnTable, Config.PAGE_TABLE_OUTPUT);
				ram.hashMapToTxt(Ram.blockInfoValid, Config.BLOCK_TABLE_OUTPUT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
}
