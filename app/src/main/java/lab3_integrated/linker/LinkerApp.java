/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lab3_integrated.linker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class LinkerApp {
	public static void main(String[] args) throws Exception {
		// Currently all args are inputs, so like run as ./gradlew
		// args="../examples/main.o ../examples/lib.o" and it'll output object file to
		// stdout
		ArrayList<BufferedReader> inputs = new ArrayList<>();
		System.out.print("Input files: ");
		for (int i = 0; i < args.length - 1; i++) { // all but last value (output file path)
			System.out.print(args[i] + " ");
			try {
				FileReader in = new FileReader(args[i]);
				inputs.add(new BufferedReader(in));
			} catch (IOException e) {
				System.err.println("Error: " + e.getMessage());
				cleanAndExit(inputs, -1);
			}
		}

		int ipla = 0x3600;  // java, fantastic language that it is, doesn't support unsigned shorts, so we
						    // start with an int
		String output = args[args.length - 1];
		System.out.println("\nOutput file: " + output);
		System.out.println("Running Pass One");
		PassOne passOne = new PassOne(ipla);
		PassOne.PassOneResult passOneResult = null;
		try {
			System.out.println("Please enter the initial program load address");
			Scanner scanner = new Scanner(System.in);
			ipla = Integer.parseInt(scanner.nextLine(), 16);
			passOne.ipla = ipla;
			passOneResult = passOne.executePassOne(inputs);
			int ipla_after_page = (ipla + passOneResult.totalSize) >>> 9;
			int ipla_page = ipla >>> 9; // page is represented in the top 7 bits, by removing the bottom 9 we keep only
										// what page it's in.
			if (ipla_page != ipla_after_page) {
				throw new RuntimeException("Program must fit in one page, it currently does not.");
			}
			System.out.println("Pass one done");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			cleanAndExit(inputs, -1);
		}

		try {
			System.out.println("Running pass two");
			PassTwo passTwo = new PassTwo(passOneResult, output);
			passTwo.executePassTwo(output, ipla);
			System.out.println("Pass two done");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			cleanAndExit(inputs, -1);
		}
		System.out.println("Linker done");
		clean(inputs);
	}

	static void printSymbolTable(HashMap<String, Integer> symTable) {
		symTable.forEach((sym, val) -> {
			System.out.println("sym: " + sym + " val: " + Integer.toHexString(val));
		});
	}

	static void cleanAndExit(ArrayList<BufferedReader> inputs, int status) {
		clean(inputs);
		throw new RuntimeException("Linker exit: " + status);
	}

	static void clean(ArrayList<BufferedReader> inputs) {
		for (BufferedReader input : inputs) {
			try {
				input.close();
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
				System.exit(-1);
			}
		}
	}

}