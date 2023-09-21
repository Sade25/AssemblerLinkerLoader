package lab3_integrated.simulator.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lab3_integrated.simulator.machine.Machine;
import lab3_integrated.simulator.simulator.Simulator;

public class Loader {

	private static int initalLoadAddress;
	private static int upperBound;

	/**
	 * This method will loop until the valid file is inputed then it will load and
	 * set the machine contents
	 * 
	 * @param path - file path to load from
	 */
	public static void parseInputFile(String path, Machine machine, Scanner scan) {

		boolean complete = false;
		boolean hFlag = false;
		boolean eFlag = false;

		while (!complete) {
			int i = 0;
			try {
				File file = new File(path);
				scan = new Scanner(file);
				while (scan.hasNextLine()) {
					String data = scan.nextLine();
					switch (data.charAt(0)) {
					case 'H':
						boolean header = headerLineValidation(data);
						if (header && i == 0) {
							loadHeaderRecordContent(data, machine);
						} else {
							invalidInputErrorNotice("header invalid");
						}
						i++;
						hFlag = true;
						break;
					case 'T':
						boolean text = textLineValidation(data);
						if (text) {
							loadTextRecordContent(data, machine);
						} else {
							invalidInputErrorNotice("text record invalid");
						}
						i++;
						break;
					case 'E':
						boolean end = endLineValidation(data);
						// TODO: check end is the last record
						if (end && !scan.hasNextLine()) {
							loadEndRecordContent(data, machine);
						} else {
							invalidInputErrorNotice("end record invalid");
						}
						i++;
						eFlag = true;
						break;
					default:
						invalidInputErrorNotice("unspecified error");
						break;
					}
				}
				if (!hFlag || !eFlag) {
					invalidInputErrorNotice("missing a header or end record");
				}
				scan.close();
			} catch (FileNotFoundException e) {
				invalidFilePath();
				System.exit(1);
			}
			complete = true;
		}
	}

	/**
	 * This method will validate that the header line meets the machine conditions
	 * 
	 * @param line - header record
	 * @return boolean indicating true if valid and false otherwise
	 * 
	 */
	public static boolean headerLineValidation(String line) {
		String regex = "^H[A-Za-z0-9 _]{6}[0-9A-Fa-f]{4}[0-9A-Fa-f]{4}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		return (matcher.matches());
	}

	/**
	 * This method will set the machine name and determine the maximum length of the
	 * machine
	 * 
	 * @param line - header record
	 */
	public static void loadHeaderRecordContent(String line, Machine machine) {

		machine.name = line.substring(1, 7);
		initalLoadAddress = Integer.parseInt(line.substring(7, 11), 16);
		int machineLength = Integer.parseInt(line.substring(11), 16);
		Loader.upperBound = machineLength + initalLoadAddress;

	}

	/**
	 * This method will validate that the text line meets the machine conditions
	 * 
	 * @param line - text record
	 * @return boolean indicating true if valid and false otherwise
	 * 
	 */
	public static boolean textLineValidation(String line) {
		String regex = "^T[0-9A-Fa-f]{4}[0-9A-Fa-f]{4}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		return (matcher.matches());
	}

	/**
	 * This method will store the word in the correct location according the the
	 * machine
	 * 
	 * @param line - text record
	 */
	public static void loadTextRecordContent(String line, Machine machine) {
		int dataToStore = Integer.parseInt(line.substring(5), 16);
		int addressToStoreAt = Integer.parseInt(line.substring(1, 5), 16);
		// use getpage method
		int page = machine.getMemoryPageLocation(addressToStoreAt);
		int wordLocation = machine.getMemoryWordLocation(addressToStoreAt);
		machine.memory[page][wordLocation] = (short) dataToStore;

	}

	/**
	 * This method will validate that the header line meets the machine conditions
	 * 
	 * @param line - end record
	 * @return boolean indicating true if valid and false otherwise
	 * 
	 */
	public static boolean endLineValidation(String line) {
		String regex = "^E[0-9A-Fa-f]{4}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);

		return (matcher.matches());
	}

	/**
	 * This method will use the end record to set the program counter
	 * 
	 * @param line - end record
	 */
	public static void loadEndRecordContent(String line, Machine machine) {
		// use setpc method
		machine.setPC(Short.parseShort(line.substring(1), 16));
	}

	public static void invalidInputErrorNotice(String problem) {
		System.err.println(
				"INVALID FILE CONTENTS, please try a different file that meets the requirements. Requirement failed: "
						+ problem);
		System.exit(1);
	}

	public static void invalidFilePath() {
		System.err.println("FILE NOT FOUND, please try again");
		System.exit(1);
	}

}
