package reso.examples.selectiverepeat;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class Tools {
	
	public static void log(double time, String actor, String log) {
		try {
			FileWriter myWriter = new FileWriter("selectiverepeat.log", true);
			BufferedWriter bw = new BufferedWriter(myWriter);
			bw.write("["+(int) time+" ms] <"+actor+"> ~> "+log);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			System.out.println("Could not write log to selectiverepeat.log");
		}
	}

	public static void plot(double time, int windowSize){
		try {
			FileWriter myWriter = new FileWriter("congestionControl.csv", true);
			BufferedWriter bw = new BufferedWriter(myWriter);
			bw.write((int) time+ ","+windowSize);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			System.out.println("Could not write log to congestionControl.csv");
		}
	}

}