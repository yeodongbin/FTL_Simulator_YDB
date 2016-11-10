/*
 * Author : Dong-Bin Yeo
 * Country : South Korea
 * 
 */

package hiRSaFTL;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FTLSimulator {
//YDB
	public static void main(String[] args) throws Exception {
		
		long time1 = System.currentTimeMillis();// start time
		
		printTodayDate();
/*
		// FileSystem_Output
		FileSystem filesystem = new FileSystem();
		filesystem.csv_To_txt();
		System.out.println("=> .txt is extracted from .csv !!");
*/
		switch (Config.FTL) {
		case 1:
			System.out.println("=>Page FTL Simulator Start! ");
			PageFTL pageFTL = new PageFTL();
			pageFTL.pageFTL();
			executionResult();// in console
			System.out.println("=>Page FTL Simulator End! ");
			break;
		case 2:
			System.out.println("=>RSaFTL Simulator Start! ");
			RSaFTL rSaFTL = new RSaFTL();
			rSaFTL.rSaFTL();
			executionResult();// in console
			System.out.println("=>RSaFTL Simulator End! ");
			break;
		case 3:
			System.out.println("=>HiRSaFTL Simulator Start! ");
			HiRSaFTL hiRSaFTL = new HiRSaFTL();
			hiRSaFTL.hiRSaFTL();
			executionResult();// in console
			System.out.println("=>HiRSaFTL Simulator End! ");
			break;
		case 4:
			System.out.println("=>RSaFTL_DelayAlgorithm Simulator Start! ");
			RSaFTL_DelayAlgorithm rSaFTL_delay = new RSaFTL_DelayAlgorithm();
			rSaFTL_delay.rSaFTL_delay();
			executionResult();// in console
			System.out.println("=>RSaFTL_DelayAlgorithm Simulator End! ");
			break;
		case 5:
			System.out.println("=>HiRSaFTL_DelayAlgorithm Simulator Start! ");
			HiRSaFTL_DelayAlgorithm hiRSaFTL_delay = new HiRSaFTL_DelayAlgorithm();
			hiRSaFTL_delay.hiRSaFTL_delay();
			executionResult();// in console
			System.out.println("=>HiRSaFTL_DelayAlgorithm Simulator End! ");
			break;
		default:
			System.out.println("---!!Config.FTL Error!!---");
			break;
		}

		long time2 = System.currentTimeMillis();// end time
		System.out.println("## Run-Time(sec) : " + (time2 - time1) / 100000.0f);
	}

	private static void printTodayDate() {// Data
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"*** [yyyy-MM-dd,a hh:mm:ss] ***");
		System.out.println(sdf.format(dt).toString());
	}

	private static void executionResult() {// Print out Result in console

		System.out.println("\n*** SIMULATOR CONFIGURATION ***");
		System.out.println(" PAGE NUM          = " + Config.LOGICAL_PAGE_NUM);
		System.out.println(" LOGICAL BLOCK NUM = " + Config.LOGICAL_BLOCK_NUM);
		System.out.println(" OP BLOCK NUM      = " + Config.OP_BLOCK_NUM);
		System.out.println(" LOG BLOCK NUM     = " + Config.LOG_BLOCK_NUM);
		System.out.println(" LOG LOG BLOCK NUM = " + Config.LLOG_BLOCK_NUM);
		System.out.println(" TOTAL BLOCK NUM   = " + Config.BLOCK_NUM);
		System.out.println(" TOTAL PAGE NUM    = " + Config.TOTAL_PAGE_NUM);

		System.out.println("\n*** EXECUTION RESULT DATA ***");
		System.out.println("Write Request           = " + Data.write_req);
		System.out.println("Sequential Write Request= "
				+ (Data.write_req - Data.write_req_1page));
		System.out.println("Random Write Request    = " + Data.write_req_1page);
		System.out.println("");
		System.out.println("Written pages Count (exteral/interal) = "
				+ Data.total_write_pages);
		System.out.println("Written pages Count (external) = "
				+ Data.ex_write_pages);
		System.out.println("Written pages Count (interal)  = "
				+ (Data.total_write_pages - Data.ex_write_pages));
		// valid-page Move 와 동일해야한다.
		System.out.println("");
		System.out.println("Erase Count              = " + Data.eraseCount);
		System.out.println("Fullmerge Count          = " + Data.fullmerge);
		System.out.println("Partialmerge Count       = " + Data.partialmerge);
		System.out
				.println("Fullmerge (data blocks)    = " + Data.fullmerge_data);
		System.out
				.println("Partialmerge (data blocks) = " + Data.partialmerge_data);
		System.out
				.println("Fullmerge (log blocks)     = " + Data.fullmerge_log);
		System.out
				.println("Partialmerge (log blocks)  = " + Data.partialmerge_log);

		System.out.println("Valid-page Move          = " + Data.validMoveCount);
		System.out.println("Valid-page Move Log      = "
				+ Data.validMoveCountLog);
	}
}
