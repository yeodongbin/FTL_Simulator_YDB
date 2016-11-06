package hiRSaFTL;

public class Data {

	static int eraseCount = 0; // erase Count(block) in Data area
	static int eraseCountLog = 0; // erase Count(block) in Log Buffer V

	static int overWriteCount = 0; // overwriting Count
	static int overWritePage = 0; // overwriting Pages

	static int validMoveCount = 0;// valid page move in partial Merging
	static int validMoveCountLog = 0;// valid 1 page move in Log Buffer V

	static int write_req = 0;// external write request No.
	static int write_req_1page = 0;// External 1 page write request No.

	static int ex_write_pages = 0;// External write pages No.
	static int count_internal_moving_pages = 0; //(YDB) Internal pages moveing count
	static int total_write_pages = 0; // External + Internal total page
											// writing//

	static int fullmerge = 0; // Full erase count
	static int partialmerge = 0; // Partial erase count

	static int count_total_read = 0;// External read request No.
}
