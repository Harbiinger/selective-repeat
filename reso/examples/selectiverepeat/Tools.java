package reso.examples.selectiverepeat;

import java.time.LocalDateTime;
import java.time.format.*;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class Tools {
	
	public static void log(String log) {
		try {
			FileWriter myWriter = new FileWriter("selectiverepeat.log", true);
			BufferedWriter bw = new BufferedWriter(myWriter);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			String time = now.format(formatter);
			bw.write("["+time+"] "+log);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			System.out.println("Could not write log to selectiverepeat.log");
		}
	}
}
