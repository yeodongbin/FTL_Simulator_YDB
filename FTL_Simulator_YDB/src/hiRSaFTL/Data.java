package hiRSaFTL;

public class Data {

	static int eraseCount = 0; // erase Count in Data area
	static int eraseCountLog = 0; // erase Count in Log Buffer V

	static int overWriteCount = 0; // overwriting Count
	static int overWritePage = 0; // overwriting Pages

	static int validMoveCount = 0;// valid page move in partial Merging
	static int validMoveCountLog = 0;// valid 1 page move in Log Buffer V

	static int count_total_ex_write = 0;// External write request No.
	static int count_write_ex_1page = 0;// External 1 page write request No.

	static int count_total_ex_write_pages = 0;// External write pages No.
	static int all_total_write_pages = 0; // External + Internal total page
											// writing//

	static int fullmerge = 0; // Full erase count
	static int partialmerge = 0; // Partial erase count

	static int count_total_read = 0;// External read request No.
}
