package hiRSaFTL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileSystem {

	public void csv_To_txt() throws IOException {
		int skipCount = 0;
		long temp = -1;
		long lPN = 0; // logical page number from the workload
		//long page_offset = 0; // physical page offset from the workload
		long page_num_fold = 0;
		long pageSize = 0; // physical page size from the workload

		// I/O trace file format (FILE_INPUT)
		// .csv:Timestamp,Hostname,DiskNumber,Type,Offset,Size,ResponseTime
		// Read .csv -> Write .txt
		FileWriter fw = new FileWriter(new File(Config.FILE_SYSTEM_OUTPUT));
		
		try (BufferedReader br = new BufferedReader(new FileReader(
				Config.FILE_SYSTEM_INPUT))) {

			for (String line; (line = br.readLine()) != null;) {
				String[] elements = line.split(",");

				temp = Long.valueOf(elements[4]).longValue(); // String to long
				lPN = temp / Config.PAGE_BYPT_SIZE;
				// folding the total data
				page_num_fold = lPN % Config.TOTAL_LOGICAL_PAGE_NUM;

				
				temp = Long.valueOf(elements[5]).longValue(); // String to long
				pageSize = (int)(temp / Config.PAGE_BYPT_SIZE);
				if (pageSize == 0) {
					pageSize = 1;
				} else if (0 < ((int) (temp % Config.PAGE_BYPT_SIZE))) {
					pageSize = pageSize + 1;
				}
				
				
				// folding the total data && Write data
				if ((Config.TOTAL_LOGICAL_PAGE_NUM >= page_num_fold + pageSize)) {
					fw.write(elements[3] + "," + page_num_fold + ","
							+ pageSize + "\n");
				} else {
					skipCount++;
				}
			}
		} catch (IOException e) {
			System.out.println("Exception while reading csv file: " + e);
		}
		
		fw.close();
		System.out.println("Skip writing Count : " + skipCount);
	}
}
