package lab3_integrated.assembler.Passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Pass1 {

	private static ArrayList<String> LitArray = new ArrayList<String>();
	private static ArrayList<String> ExtSyms = new ArrayList<String>();

	/**
	 * Calls methods to fill both the symbol table and literal table
	 * 
	 * @param arr      The array containing all the lines of the file that were
	 *                 parsed down in the validator
	 * @param loc      Initial location counter to be used throughout pass 1
	 * @param SymTable Empty symbol table to be filled
	 * @param LitTable Empty literal table to be filled
	 * 
	 */
	public static int fillTables(ArrayList<List<String>> arr, int loc, HashMap<String, String[]> SymTable,
			HashMap<String, Integer> LitTable) {
		loc = fillSymTable(arr, SymTable, loc);
		loc = fillLitTable(LitTable, loc, LitArray);
		return loc;
	}

	/**
	 * Loops through the array of lines to fill the symbol table
	 * 
	 * @param arr      The array of lines to be looped through
	 * @param SymTable Empty symbol table to be filled
	 * @param loc      The current location counter
	 * 
	 */
	public static int fillSymTable(ArrayList<List<String>> arr, HashMap<String, String[]> SymTable, int loc) {
		String sym, instruct, operand;
		for (int i = 1; i < arr.size() - 1; i++) {
			if (SymTable.size() > 100) {
				System.out.println("Too many symbols");
				System.exit(1);
			}
			// Assigns values for the symbol, instruction and operand from the current line
			sym = arr.get(i).get(0);
			instruct = arr.get(i).get(1);
			operand = arr.get(i).get(2);

			// Fill the symbols array that will become the value for the HashMap
			String[] symArr;
			if (!sym.equals("") && !SymTable.containsKey(sym)) {
				symArr = fillSymArr(sym, instruct, operand, SymTable, loc);
				SymTable.put(sym, symArr);
			} else if (instruct.equals(".EXT")) {
				String[] splitOperand = operand.split(",");
				for (int j = 0; j < splitOperand.length; j++) {
					ExtSyms.add(splitOperand[j]);
				}
			}

			// Add any literals in the line to an ArrayList to later fill the literal table
			if (operand.contains("=") && !instruct.equals(".STRZ")) {
				String lit = operand.substring(operand.indexOf("="));
				LitArray.add(lit);
			}

			// Increment the location counter
			loc = incLoc(instruct, operand, loc);
		}
		return loc;
	}

	/**
	 * Loops through the array of stored literal values and inputs them into the
	 * literal table at the correct location
	 * 
	 * @param LitTable Empty literal table to be filled
	 * @param locCount The current location counter
	 * @param litArray The array of literals to fill the literal table with
	 * 
	 */
	public static int fillLitTable(HashMap<String, Integer> LitTable, int locCount, ArrayList<String> litArray) {
		for (int i = 0; i < litArray.size(); i++) {
			if (LitTable.size() > 50) {
				System.out.println("Too many literals");
				System.exit(1);
			}
			LitTable.put(litArray.get(i), locCount);
			locCount++;
			locCount %= 0x10000;
		}
		return locCount;
	}

	/**
	 * Fills array for a symbol containing the location of the symbol and whether it
	 * is relative or absolute
	 * 
	 * @param sym      String containing the symbol
	 * @param instruct Instruction pertaining to the symbol
	 * @param operand  The operand pertaining to the symbol
	 * @param SymTable SymTable in case there needs to be a check for an absolute
	 *                 symbol
	 * @param locCount The current location counter
	 * 
	 */
	public static String[] fillSymArr(String sym, String instruct, String operand, HashMap<String, String[]> SymTable,
			int locCount) {
		String[] symArr = new String[2];
		if (!instruct.equals(".EQU") && !instruct.equals(".EXT")) {
			symArr[0] = "x" + Integer.toHexString(locCount);
			symArr[1] = "R";
		} else {
			symArr = getEQUVal(sym, operand, SymTable);
		}
		return symArr;

	}

	/**
	 * Gets the final value of any EQU instructions
	 * 
	 * @param sym      Symbol to check the value of
	 * @param operand  Operand containing either the value of the .EQU symbol or
	 *                 another symbol to check the value of
	 * @param SymTable Symbol table to reference if the operand is another symbol
	 */
	public static String[] getEQUVal(String sym, String operand, HashMap<String, String[]> SymTable) {
		String[] val = new String[2];

		if (operand.charAt(0) == '#' || operand.charAt(0) == 'x') {
			// If the operand is a single value
			val[0] = operand;
			val[1] = "A";
		} else {
			// If the operand is a symbol or a series of operands
			if (!SymTable.containsKey(operand) && !ExtSyms.contains(operand)) {
				System.out.println("Forward referencing error");
				System.exit(1);
			} else if (SymTable.containsKey(operand)) {
				String[] symArr = SymTable.get(operand);
				if (symArr[1].equals("A")) {
					val[1] = "A";
				} else {
					val[1] = "R";
				}
				val[0] = symArr[0];
			} else {
				val[0] = operand;
				val[1] = "R";
			}

		}

		return val;
	}

	/**
	 * Increments the location counter according to which instruction is being
	 * checked
	 * 
	 * @param instruct Instruction to be checked for how much to increment the
	 *                 location counter
	 * @param operand  Operand to be parsed to see how mcuh to increment the
	 *                 location counter
	 * @param locCount The current location counter
	 * 
	 */
	public static int incLoc(String instruct, String operand, int locCount) {
		if (instruct.equals(".STRZ")) {
			locCount += operand.length() + 1;
		} else if (instruct.equals(".BLKW")) {
			if (operand.charAt(0) == 'x') {
				locCount += Integer.parseInt(operand.substring(1), 16);
			} else {
				locCount += Integer.parseInt(operand.substring(1));
			}
		} else if (!instruct.equals(".EQU") && !instruct.equals(".ENT") && !instruct.equals(".EXT")) {
			locCount++;
		}
		locCount %= 0x10000;
		return locCount;
	}
}
