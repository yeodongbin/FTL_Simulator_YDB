/*
 * Author : Dong-Bin Yeo
 * Country : South Korea
 * 
 */

package hiRSaFTL;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FTLSimulator {

	public static void main(String[] args) throws Exception {

		printTodayDate();

		// FileSystem_Output
		FileSystem filesystem = new FileSystem();
		filesystem.csv_To_txt();
		System.out.println("=> .txt is extracted from .csv !!");

		long time1 = System.currentTimeMillis();// start time

		if (Config.FTL == 1) {
			System.out.println("=>Page FTL Simulator Start! ");
			PageFTL pageFTL = new PageFTL();
			pageFTL.pageFTL();
			executionResult();// in console

		} else if (Config.FTL == 2) {
			System.out.println("=>RSaFTL Simulator Start! ");
			RSaFTL rSaFTL = new RSaFTL();
			rSaFTL.rSaFTL();
			executionResult();// in console

		} else if (Config.FTL == 3) {
			System.out.println("=>HiRSaFTL Simulator Start! ");
			HiRSaFTL hiRSaFTL = new HiRSaFTL();
			hiRSaFTL.hiRSaFTL();
			executionResult();// in console

		} else if (Config.FTL == 4) {
			System.out.println("=>RSaFTL_DelayAlgorithm Simulator Start! ");
			RSaFTL_DelayAlgorithm rSaFTL_delay = new RSaFTL_DelayAlgorithm();
			rSaFTL_delay.rSaFTL_delay();
			executionResult();// in console

		} else {
			System.out.println("---!!Config.FTL Error!!---");
		}

		long time2 = System.currentTimeMillis();// end time

		System.out.println("###### Run-Time(sec) : " + (time2 - time1)
				/ 100000.0f + "min");
	}

	private static void printTodayDate() {// Data
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"*** [yyyy-MM-dd,a hh:mm:ss] ***");
		System.out.println(sdf.format(dt).toString());
	}

	private static void executionResult() {// Print out Result in console
		if (Config.FTL == 1) {
			System.out.println("=> Page FTL Simulator");
		} else if (Config.FTL == 2) {
			System.out.println("=> RSaFTL Simulator");
		} else if (Config.FTL == 4) {
			System.out.println("=> RSaFTL Delay Algoritum Simulator");
		} else {
			System.out.println("=> HiRSaFTL Simulator");
		}

		System.out.println("\n*** SIMULATOR CONFIGURATION ***");
		System.out.println(" PAGE NUM          = " + Config.LOGICAL_PAGE_NUM);
		System.out.println(" LOGICAL BLOCK NUM = " + Config.LOGICAL_BLOCK_NUM);
		System.out.println(" OP BLOCK NUM      = " + Config.OP_BLOCK_NUM);
		System.out.println(" LOG BLOCK NUM     = " + Config.LOG_BLOCK_NUM);
		System.out.println(" LOG LOG BLOCK NUM = " + Config.LLOG_BLOCK_NUM);
		System.out.println(" TOTAL BLOCK NUM   = " + Config.BLOCK_NUM);
		System.out.println(" TOTAL PAGE NUM    = " + Config.TOTAL_PAGE_NUM);

		System.out.println("\n*** EXECUTION RESULT DATA ***");
		System.out.println(" Total Writing Count       = "
				+ Data.count_total_ex_write);
		System.out.println(" Total written pages Count = "
				+ Data.count_total_ex_write_pages);
		System.out.println(" Random writing Count      = "
				+ Data.count_write_ex_1page);
		System.out.println(" Sequential writing Count  = "
				+ (Data.count_total_ex_write - Data.count_write_ex_1page));
		System.out.println(" All write Number (inside/outside) = "
				+ (Data.all_total_write_pages));

		System.out.println("\n Erase Count              = " + Data.eraseCount);
		System.out.println(" fullmerge Count          = " + Data.fullmerge);
		System.out.println(" partialmerge Count       = " + Data.partialmerge);
		System.out
				.println(" Valid-page Move          = " + Data.validMoveCount);
	}
}
