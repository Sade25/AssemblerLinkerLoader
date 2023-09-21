package lab3_integrated.assembler.Passes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lab3_integrated.assembler.MOT.Machine_Op_Ins_Enum;
import lab3_integrated.assembler.MOT.Machine_Op_Table;
import lab3_integrated.assembler.POT.Pseudo_Op_Format;
import lab3_integrated.assembler.POT.Pseudo_Op_Ins_Enum;
import lab3_integrated.assembler.POT.Pseudo_Op_Table;
import lab3_integrated.assembler.lab2.Exceptions.Pass2Exception;
import lab3_integrated.assembler.lab2.Validate.Locations;

public class Pass2 {

	/**
	 * Location counter for tracking the address of the current assembly line.
	 */
	private int LC;

	/**
	 * Machine-Ops table containing each machine instruction.
	 */
	private Machine_Op_Table mot;

	/**
	 * Pseudo-Ops table containing each assembler instructions.
	 */
	private Pseudo_Op_Table pot;

	/**
	 * Writing the contents of the object file.
	 */
	private FileWriter outFile;

	/**
	 * Writing the contents of the listing file.
	 */
	private FileWriter listingFile;

	/**
	 * Parser for parsing strings into integer values.
	 */
	private String_Parser sp;

	/**
	 * The size of the object file.
	 */
	private int segmentSize;

	/**
	 * Instance of the symbol table.
	 */
	private Map<String, String[]> symbolTable;

	/**
	 * Instance of the literal table.
	 */
	private Map<String, Integer> literalTable;

	/**
	 * Flag to indicate whether a program is relocatable or not.
	 */
	private boolean isRelocatable;

	/**
	 * The initial load address.
	 */
	private String initialAddress;

	/**
	 * Temporary external symbol table to contain the external symbols.
	 */
	private Map<String, String> tempExternalTable;

	/**
	 * Storing the name of the segment.
	 */
	private String segName;

	/**
	 * Default constructor for the pass 2 class.
	 * 
	 * @param mot          machine ops table
	 * @param pot          pseudo ops table
	 * @param symbolTable  symbol table
	 * @param literalTable literal table
	 * @param objectFile   file writer for the object file
	 * @param listingFile  file writer for the listing file
	 * @param segmentSize  the total size of the segment
	 */
	public Pass2(Machine_Op_Table mot, Pseudo_Op_Table pot, Map<String, String[]> symbolTable,
			Map<String, Integer> literalTable, FileWriter objectFile, FileWriter listingFile, int segmentSize) {
		// Initializing the tables
		this.mot = mot;
		this.pot = pot;

		this.symbolTable = symbolTable;
		this.literalTable = literalTable;
		this.tempExternalTable = new HashMap<>();

		// Other elements of the pass 2
		this.LC = 0;
		this.sp = new String_Parser();
		this.segmentSize = segmentSize;
		this.isRelocatable = false;
		this.initialAddress = "0000";
		this.segName = "";

		// Trying to create the object file and listing file
		this.outFile = objectFile;
		this.listingFile = listingFile;
	}

	/**
	 * Main method for parsing through each line of assembly code and displaying to
	 * the object file and listing file.
	 * 
	 * @param list List containing each line of assembly to be parsed, without the
	 *             comments or junk value
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public void parseInput(List<List<String>> list) throws Pass2Exception {
		for (int i = 0; i < list.size(); i++) {
			// Getting the four parts of the assembly code
			String symbol = list.get(i).get(Locations.LABEL);
			String opcode = list.get(i).get(Locations.OPERATION);
			String operand = list.get(i).get(Locations.OPERANDS);
			String line = list.get(i).get(Locations.LINE);

			// Adding any literals to the object file (before .END)
			if (i == list.size() - 1) {
				for (Map.Entry<String, Integer> entry : literalTable.entrySet()) {
					String textRecord = literalObjectFile(entry.getKey(), entry.getValue(), line);

					// Writing the contents to the output file
					try {
						this.outFile.write(textRecord);
					} catch (IOException e) {
						throw new Pass2Exception(
								"Line " + line + ": Unable to add assembly line code to the object file");

					}
				}
			}

			// Opcode defined in the Pseudo_Ops_Table
			String assembleLine;
			if (pot.containsOp(opcode)) {
				assembleLine = assemblePseudoOpLine(symbol, opcode, operand, line);
			}

			// Opcode contained in the Machine_Ops_Table
			else {
				assembleLine = assembleMachineOpLine(opcode, operand, line);
			}

			// Writing the contents to the output file
			try {
				this.outFile.write(assembleLine);
			} catch (IOException e) {
				throw new Pass2Exception("Line " + line + ": Unable to add assembly line code to the object file");

			}

			String listFileOutput = outputToListingFile(symbol, opcode, operand, assembleLine, line);

			// Writing the contents to the listing file
			try {
				this.listingFile.write(listFileOutput);
			} catch (IOException e) {
				throw new Pass2Exception("Line " + line + ": Unable to add assembly line code to the listing file");

			}

			// Adding any literals to the listing file (after .END)
			if (i == list.size() - 1) {
				for (Map.Entry<String, Integer> entry : literalTable.entrySet()) {
					String listFile = literalListingFile(entry.getKey(), entry.getValue(), line);

					// Writing the contents to the listing file
					try {
						this.listingFile.write(listFile);
					} catch (IOException e) {
						throw new Pass2Exception(
								"Line " + line + ": Unable to add assembly line code to the listing file");

					}
				}
			}
		}

		// Closing the output and listing file streams
		try {
			this.outFile.close();
		} catch (IOException e) {
			throw new Pass2Exception("Unable to close object file");

		}

		try {
			this.listingFile.close();
		} catch (IOException e) {
			throw new Pass2Exception("Unable to close listing file");

		}
	}

	/**
	 * Creating the text records for the literal table before the .END operation
	 * executes.
	 * 
	 * @param literal the literal value to be added to text records
	 * @param value   the corresponding address for a given literal
	 * @param line    the current line of assembly code
	 * @return returns the text record string
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public String literalObjectFile(String literal, Integer value, String line) throws Pass2Exception {
		String textRecord = "T";

		String literalAddress = sp.literalValue(literalTable, literal, line);
		textRecord += literalAddress.substring(1);

		String litVal = sp.parseLiteralString(literal);

		if (litVal == null) {
			throw new Pass2Exception("Line " + line + ": Invalid literal value");
		}

		textRecord += litVal + "\n";
		return textRecord;
	}

	/**
	 * Creating the text records for the literal table before the .END operation
	 * executes.
	 * 
	 * @param literal the literal value to be added to text records
	 * @param value   the corresponding address for a given literal
	 * @param line    the current line of assembly code
	 * @return returns the listing file record string
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public String literalListingFile(String literal, Integer value, String line) throws Pass2Exception {
		String assemblyLine = literalObjectFile(literal, value, line);

		// Address value
		String address = assemblyLine.substring(1, 5);
		String listFileOutput = "(" + address + ")";

		// Hex value
		String hexVal = assemblyLine.substring(5, 9);
		listFileOutput += (" " + hexVal);

		// Binary value
		int hexInt = Integer.parseInt(hexVal, 16);
		String binVal = Integer.toBinaryString(hexInt);
		while (binVal.length() < 16) {
			binVal = "0" + binVal;
		}

		listFileOutput += (" " + binVal + " (");
		listFileOutput += " lit) \n";

		return listFileOutput;
	}

	/**
	 * Going through the possible machine instructions and creating the text
	 * records.
	 * 
	 * @param opcode  the operation to execute for the line of assembly
	 * @param operand the operands of the instruction
	 * @param line    the assembly line number
	 * @return the text record with the location, instruction, and modification
	 *         record (if applicable)
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public String assembleMachineOpLine(String opcode, String operand, String line) throws Pass2Exception {
		// Getting the enumerated name of the instruction, the value, and the size
		Machine_Op_Ins_Enum ins_name = mot.getInstructionName(opcode);
		int value = mot.getOpcode(opcode);
		int size = mot.getSize(opcode);

		// Splitting the operands by the elements they have
		String[] splitOperand = operand.split(",");
		String record = "T";

		// Using a switch-case block for each of the machine instructions present.
		switch (ins_name) {
		case ADD:
			record += addOp(splitOperand, value, size, line) + "\n";
			break;
		case AND:
			record += andOp(splitOperand, value, size, line) + "\n";
			break;
		case BR:
			record += brxOp(splitOperand, "0", "0", "0", value, size, line) + "\n";
			break;
		case BRN:
			record += brxOp(splitOperand, "1", "0", "0", value, size, line) + "\n";
			break;
		case BRZ:
			record += brxOp(splitOperand, "0", "1", "0", value, size, line) + "\n";
			break;
		case BRP:
			record += brxOp(splitOperand, "0", "0", "1", value, size, line) + "\n";
			break;
		case BRNZ:
			record += brxOp(splitOperand, "1", "1", "0", value, size, line) + "\n";
			break;
		case BRNP:
			record += brxOp(splitOperand, "1", "0", "1", value, size, line) + "\n";
			break;
		case BRZP:
			record += brxOp(splitOperand, "0", "1", "1", value, size, line) + "\n";
			break;
		case BRNZP:
			record += brxOp(splitOperand, "1", "1", "1", value, size, line) + "\n";
			break;
		case DBUG:
			record += dbugOp(value, size, line) + "\n";
			break;
		case JSR:
			record += jsrOp(splitOperand, "1", value, size, line) + "\n";
			break;
		case JMP:
			record += jsrOp(splitOperand, "0", value, size, line) + "\n";
			break;
		case JSRR:
			record += jsrrOp(splitOperand, "1", value, size, line) + "\n";
			break;
		case JMPR:
			record += jsrrOp(splitOperand, "0", value, size, line) + "\n";
			break;
		case LD:
			record += ldOp(splitOperand, value, size, line) + "\n";
			break;
		case LDI:
			record += ldiOp(splitOperand, value, size, line) + "\n";
			break;
		case LDR:
			record += ldrOp(splitOperand, value, size, line) + "\n";
			break;
		case LEA:
			record += leaOp(splitOperand, value, size, line) + "\n";
			break;
		case NOT:
			record += notOp(splitOperand, value, size, line) + "\n";
			break;
		case RET:
			record += retOp(value, size, line) + "\n";
			break;
		case ST:
			record += stOp(splitOperand, value, size, line) + "\n";
			break;
		case STI:
			record += stiOp(splitOperand, value, size, line) + "\n";
			break;
		case STR:
			record += strOp(splitOperand, value, size, line) + "\n";
			break;
		case TRAP:
			record += trapOp(splitOperand, value, size, line) + "\n";
			break;
		default:
			// Error?
			break;
		}

		return record;
	}

	/**
	 * Going through the possible psueodo ops instructions and creating the text
	 * records if necessary.
	 * 
	 * 
	 * @param symbol  any symbol if present in the assembly line
	 * @param opcode  the operation to execute for the line of assembly
	 * @param operand the operands of the instruction
	 * @param line    the assembly line number
	 * @return the text record (if applicable) with the location, instruction, and
	 *         modification record (if applicable)
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public String assemblePseudoOpLine(String symbol, String opcode, String operand, String line)
			throws Pass2Exception {
		// Getting the enumerated name of the instruction
		Pseudo_Op_Ins_Enum ins_name = pot.getInstructionName(opcode);

		// Splitting the operands by the elements they have (for .ENT and .EXT)
		String[] splitOperand = operand.split(",");
		String record = "";

		// Using a switch-case block for each of the assembler instructions present.
		switch (ins_name) {
		case BLKW:
			record += blkwOp(symbol, opcode, operand, line);
			break;
		case END:
			record += endOp(operand, line);
			break;
		case ENT:
			record += entOp(splitOperand, line);
			break;
		case EXT:
			record += extOp(splitOperand, line);
			break;
		case EQU:
			record += equOp(operand, line);
			break;
		case FILL:
			record += fillOp(operand, line);
			break;
		case ORIG:
			record += origOp(symbol, operand, line);
			break;
		case STRZ:
			record += strzOp(symbol, opcode, operand, line);
			break;
		default:
			// Error?
			break;
		}

		return record;
	}

	/**
	 * Parsing the line of assembly code into a listing file.
	 * 
	 * @param symbol       any symbol if present in the assembly line
	 * @param opcode       the operation to execute for the line of assembly
	 * @param operand      the operands of the instruction
	 * @param assemblyLine the line of assembly code that contains the text record
	 * @param line         the assembly line number
	 * @return the string following the format for the listing file
	 */
	public String outputToListingFile(String symbol, String opcode, String operand, String assemblyLine, String line) {
		String listFile = "";

		if (this.pot.containsOp(opcode) && this.pot.getFormat(opcode) == Pseudo_Op_Format.DEFINITE
				&& this.pot.getLength(opcode) == 0) {
			// Account for record that don't occupy memory
			listFile = "\t\t\t\t\t\t\t (";

			// Line number
			while (line.length() < 4) {
				line = " " + line;
			}

			// Symbol value
			while (symbol.length() < 16) {
				symbol += " ";
			}

			listFile += (line + ") " + symbol);

			// Opcode and operand
			while (opcode.length() < 6) {
				opcode += " ";
			}

			listFile += (opcode + operand + "\n");
		}

		else if (this.mot.containsOp(opcode) || (this.pot.containsOp(opcode) && this.pot.getLength(opcode) == 1)) {
			// Address value
			String address = assemblyLine.substring(1, 5);
			listFile = "(" + address + ")";

			// Hex value
			String hexVal = assemblyLine.substring(5, 9);
			listFile += (" " + hexVal);

			// Binary value
			int hexInt = Integer.parseInt(hexVal, 16);
			String binVal = Integer.toBinaryString(hexInt);
			while (binVal.length() < 16) {
				binVal = "0" + binVal;
			}

			listFile += (" " + binVal + " (");

			// Line number
			while (line.length() < 4) {
				line = " " + line;
			}

			// Symbol value
			while (symbol.length() < 16) {
				symbol += " ";
			}

			listFile += (line + ") " + symbol);

			// Opcode and operand
			while (opcode.length() < 6) {
				opcode += " ";
			}

			listFile += (opcode + operand + "\n");
		}

		return listFile;
	}

	/**
	 * Parsing the line of .BLKW op into a listing file.
	 * 
	 * @param symbol  any symbol if present in the assembly line
	 * @param address the address specified by the location counter
	 * @param opcode  the operation to execute for the line of assembly
	 * @param operand the operands of the instruction
	 * @param line    the assembly line number
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public void blkwListingFile(String symbol, String address, String opcode, String operand, String line)
			throws Pass2Exception {
		// Writing the contents to the listing file
		String listFileOutput = "(" + address + ")";
		listFileOutput += " \t \t\t\t\t\t (";

		// Line number
		while (line.length() < 4) {
			line = " " + line;
		}

		// Symbol value
		while (symbol.length() < 16) {
			symbol += " ";
		}

		listFileOutput += (line + ") " + symbol);

		// Opcode and operand
		while (opcode.length() < 6) {
			opcode += " ";
		}

		listFileOutput += (opcode + operand + "\n");

		try {
			this.listingFile.write(listFileOutput);
		} catch (IOException e) {
			throw new Pass2Exception("Line " + line + ": Unable to add assembly line code to the listing file");
		}
	}

	/**
	 * Parsing the line of .STRZ op into a listing file.
	 * 
	 * @param symbol       any symbol if present in the assembly line
	 * @param opcode       the operation to execute for the line of assembly
	 * @param operand      the operands of the instruction
	 * @param i            index to determine if the operand should be printed or
	 *                     not
	 * @param assemblyLine the line of assembly code that contains the text record
	 * @param line         the assembly line number
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	public void strzListingFile(String symbol, String opcode, String operand, int i, String assemblyLine, String line)
			throws Pass2Exception {
		// Address value
		String address = assemblyLine.substring(1, 5);
		String listFileOutput = "(" + address + ")";

		// Hex value
		String hexVal = assemblyLine.substring(5, 9);
		listFileOutput += (" " + hexVal);

		// Binary value
		int hexInt = Integer.parseInt(hexVal, 16);
		String binVal = Integer.toBinaryString(hexInt);
		while (binVal.length() < 16) {
			binVal = "0" + binVal;
		}

		listFileOutput += (" " + binVal + " (");

		// Line number
		while (line.length() < 4) {
			line = " " + line;
		}

		listFileOutput += line + ") ";

		if (i == 0) {
			// Symbol value
			while (symbol.length() < 16) {
				symbol += " ";
			}

			listFileOutput += symbol;

			// Opcode and operand
			while (opcode.length() < 6) {
				opcode += " ";
			}

			listFileOutput += (opcode + "\"" + operand + "\"\n");
		} else {
			listFileOutput += "\n";
		}

		// Writing the contents to the listing file
		try {
			this.listingFile.write(listFileOutput);
		} catch (IOException e) {
			throw new Pass2Exception("Line " + line + ": Unable to add assembly line code to the listing file");
		}
	}

	/**
	 * Forming the text record for the ADD instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the ADD instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String addOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Iterating through the operand length
		for (int i = 0; i < operand.length; i++) {
			String pos = operand[i];

			// If the operand is a register
			if (pos.charAt(0) == 'R') {
				// Adding the don't care values if the operand only involved registers
				if (i == 2) {
					binaryString += "000";
				}

				// Convert the register value to fit the binary
				String regVal = sp.registerToBinaryString("#" + pos.substring(1), line);
				binaryString += regVal;
			}

			// If the operand is an immediate
			else if (pos.charAt(0) == '#' || pos.charAt(0) == 'x') {
				binaryString += "1";
				String immVal = sp.immediateToBinaryString(pos, line);
				binaryString += immVal;
			}

			// If the operand is an absolute symbol
			else {
				// Ensure the symbol is in the symbol table
				absoluteSymbolTable(pos, line);

				// If there is a symbol at the end, it is treated as an immediate
				if (i == 2) {
					binaryString += "1";
					String immVal = sp.immediateToBinaryString(symbolTable.get(pos)[0], line);
					binaryString += immVal;
				}

				// Otherwise, treat it is a register
				else {
					// Convert the register value to fit the binary
					String regVal = sp.registerToBinaryString(symbolTable.get(pos)[0], line);
					binaryString += regVal;
				}
			}
		}

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the AND instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the AND instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String andOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		// Calling the instruction with the same procedure
		return addOp(operand, opcode_value, size, line);
	}

	/**
	 * Forming the text record for the BRx instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param n            the N (negative) bit
	 * @param z            the Z (zero) bit
	 * @param p            the P (positive) bit
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the BRx instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String brxOp(String[] operand, String n, String z, String p, int opcode_value, int size, String line)
			throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Adding the N, Z, P bits to the binary string
		binaryString += (n + z + p);

		String address = operand[0];

		int relative = 0;

		// name of the external symbol for the modification record
		String externalSym = address;

		// If the given address is a symbol
		if (!(address.charAt(0) == '#' || address.charAt(0) == 'x')) {
			/*
			 * Ensure the symbol is present in the table and check if the symbol is absolute
			 * or relative
			 */
			relative = isRelativeSymbol(address, line);

			// Check address if it is a .EXT symbol or from symbol table
			if (relative == 2) {
				address = this.tempExternalTable.get(address);
			} else {
				String val = this.symbolTable.get(address)[0];

				// Check if the address is a value
				if (val.charAt(0) == 'x' || val.charAt(0) == '#') {
					address = val;
				}

				// otherwise, it is a external symbol
				else {

					// make sure the symbol is a external forward referencing
					if (!this.tempExternalTable.containsKey(val)) {
						throw new Pass2Exception("Line: " + line + ": cannot forward reference a non-external symbol");
					}

					// update the value with the value from the external symbol table
					address = this.tempExternalTable.get(val);
					externalSym = val;

					// when creating modification records, make sure it is a external symbol
					relative = 2;
				}
			}
		}

		/*
		 * Check if the given address is within page range (with the location counter)
		 * and if so, return the lower 9 bits of the given address
		 */
		String offset = sp.pageRangeAndOffset(lcAddress, address, line);
		binaryString += offset;

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		// Check if the symbol was a relative symbol to add a modification record
		if (this.isRelocatable) {
			if (relative == 1) {
				textRecord += ("X9" + this.segName);
			} else if (relative == 2) {
				textRecord += ("X9" + externalSym);
			}
		}

		return textRecord;
	}

	/**
	 * Forming the text record for the DBUG instruction.
	 * 
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the DBUG instruction
	 */
	private String dbugOp(int opcode_value, int size, String line) {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Adding the don't cares
		binaryString += "000000000000";

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the JSR/JMP instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param L            the bit to see if the address is to be saved
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the JSR/JMP instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String jsrOp(String[] operand, String L, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Adding the L bits and don't cares to the binary string
		binaryString += (L + "00");

		String address = operand[0];

		int relative = 0;

		// name of the external symbol for the modification record
		String externalSym = address;

		// If the given address is a symbol
		if (!(address.charAt(0) == '#' || address.charAt(0) == 'x')) {
			/*
			 * Ensure the symbol is present in the table and check if the symbol is absolute
			 * or relative
			 */
			relative = isRelativeSymbol(address, line);

			// Update address if it is a .EXT symbol
			if (relative == 2) {
				address = this.tempExternalTable.get(address);
			} else {
				String val = this.symbolTable.get(address)[0];

				// Check if the address is a value
				if (val.charAt(0) == 'x' || val.charAt(0) == '#') {
					address = val;
				}

				// otherwise, it is a external symbol
				else {

					// make sure the symbol is a external forward referencing
					if (!this.tempExternalTable.containsKey(val)) {
						throw new Pass2Exception("Line: " + line + ": cannot forward reference a non-external symbol");
					}

					// update the value with the value from the external symbol table
					address = this.tempExternalTable.get(val);
					externalSym = val;

					// when creating modification records, make sure it is a external symbol
					relative = 2;
				}
			}
		}

		/*
		 * Check if the given address is within page range (with the location counter)
		 * and if so, return the lower 9 bits of the given address
		 */
		String offset = sp.pageRangeAndOffset(lcAddress, address, line);
		binaryString += offset;

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		// Check if the symbol was a relative symbol to add a modification record
		if (this.isRelocatable) {
			if (relative == 1) {
				textRecord += ("X9" + this.segName);
			} else if (relative == 2) {
				textRecord += ("X9" + externalSym);
			}
		}

		return textRecord;
	}

	/**
	 * Forming the text record for the JSRR/JMPR instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param L            the bit to see if the address is to be saved
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the JSRR/JMPR instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String jsrrOp(String[] operand, String L, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Adding the L bit and the don't cares
		binaryString += (L + "00");

		// Iterating through the operand length
		for (int i = 0; i < operand.length; i++) {
			String pos = operand[i];

			// If the operand is a register
			if (pos.charAt(0) == 'R') {
				// Convert the register value to fit the binary
				String regVal = sp.registerToBinaryString("#" + pos.substring(1), line);
				binaryString += regVal;
			}

			// If the operand is an index
			else if (pos.charAt(0) == '#' || pos.charAt(0) == 'x') {
				String indVal = sp.indexToBinaryString(pos, line);
				binaryString += indVal;
			}

			// If the operand is an absolute symbol
			else {
				// Ensure the symbol is in the symbol table
				absoluteSymbolTable(pos, line);

				// If there is a symbol at the end, it is treated as an index
				if (i == 1) {
					String indVal = sp.indexToBinaryString(symbolTable.get(pos)[0], line);
					binaryString += indVal;
				}

				// Otherwise, treat it is a register
				else {
					// Convert the register value to fit the binary
					String regVal = sp.registerToBinaryString(symbolTable.get(pos)[0], line);
					binaryString += regVal;
				}
			}
		}

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the LD instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the LD instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String ldOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Getting the register
		String pos = operand[0];

		// If the operand is a register
		if (pos.charAt(0) == 'R') {
			// Convert the register value to fit the binary
			String regVal = sp.registerToBinaryString("#" + pos.substring(1), line);
			binaryString += regVal;
		}

		// If the operand is an absolute symbol
		else {
			// Ensure the symbol is in the symbol table
			absoluteSymbolTable(pos, line);

			// Convert the register value to fit the binary
			String regVal = sp.registerToBinaryString(symbolTable.get(pos)[0], line);
			binaryString += regVal;
		}

		String address = operand[1];

		int relative = 0;

		// name of the external symbol for the modification record
		String externalSym = address;

		// If the given address is a literal

		if (address.charAt(0) == '=') {
			address = sp.literalValue(this.literalTable, address, line);
			relative = 1;
		}

		// If the given address is a symbol
		else if (!(address.charAt(0) == '#' || address.charAt(0) == 'x')) {
			/*
			 * Ensure the symbol is present in the table and check if the symbol is absolute
			 * or relative
			 */
			relative = isRelativeSymbol(address, line);

			// Update address if it is a .EXT symbol
			if (relative == 2) {
				address = this.tempExternalTable.get(address);
			} else {
				String val = this.symbolTable.get(address)[0];

				// Check if the address is a value
				if (val.charAt(0) == 'x' || val.charAt(0) == '#') {
					address = val;
				}

				// otherwise, it is a external symbol
				else {

					// make sure the symbol is a external forward referencing
					if (!this.tempExternalTable.containsKey(val)) {
						throw new Pass2Exception("Line: " + line + ": cannot forward reference a non-external symbol");
					}

					// update the value with the value from the external symbol table
					address = this.tempExternalTable.get(val);
					externalSym = val;

					// when creating modification records, make sure it is a external symbol
					relative = 2;
				}
			}
		}

		/*
		 * Check if the given address is within page range (with the location counter)
		 * and if so, return the lower 9 bits of the given address
		 */
		String offset = sp.pageRangeAndOffset(lcAddress, address, line);
		binaryString += offset;

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		// Check if the symbol was a relative symbol to add a modification record
		if (this.isRelocatable) {
			if (relative == 1) {
				textRecord += ("X9" + this.segName);
			} else if (relative == 2) {
				textRecord += ("X9" + externalSym);
			}
		}

		return textRecord;
	}

	/**
	 * Forming the text record for the LDI instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the LDI instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String ldiOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		// Calling the instruction with the same procedure
		return ldOp(operand, opcode_value, size, line);
	}

	/**
	 * Forming the text record for the LDR instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the LDR instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String ldrOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Iterating through the operand length
		for (int i = 0; i < operand.length; i++) {
			String pos = operand[i];

			// If the operand is a register
			if (pos.charAt(0) == 'R') {
				// Convert the register value to fit the binary
				String regVal = sp.registerToBinaryString("#" + pos.substring(1), line);
				binaryString += regVal;
			}

			// If the operand is an index
			else if (pos.charAt(0) == '#' || pos.charAt(0) == 'x') {
				String indVal = sp.indexToBinaryString(pos, line);
				binaryString += indVal;
			}

			// If the operand is an absolute symbol
			else {
				// Ensure the symbol is in the symbol table
				absoluteSymbolTable(pos, line);

				// If there is a symbol at the end, it is treated as an index
				if (i == 2) {
					String indVal = sp.indexToBinaryString(symbolTable.get(pos)[0], line);
					binaryString += indVal;
				}

				// Otherwise, treat it is a register
				else {
					// Convert the register value to fit the binary
					String regVal = sp.registerToBinaryString(symbolTable.get(pos)[0], line);
					binaryString += regVal;
				}
			}
		}

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the LEA instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the LEA instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String leaOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		// Calling the instruction with the same procedure
		return ldOp(operand, opcode_value, size, line);
	}

	/**
	 * Forming the text record for the NOT instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the NOT instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String notOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Iterating through the operand length
		for (int i = 0; i < operand.length; i++) {
			String pos = operand[i];

			// If the operand is a register
			if (pos.charAt(0) == 'R') {
				// Convert the register value to fit the binary
				String regVal = sp.registerToBinaryString("#" + pos.substring(1), line);
				binaryString += regVal;
			}

			// If the operand is an absolute symbol
			else {
				// Ensure the symbol is in the symbol table
				absoluteSymbolTable(pos, line);

				// Convert the register value to fit the binary
				String regVal = sp.registerToBinaryString(symbolTable.get(pos)[0], line);
				binaryString += regVal;
			}
		}

		// Add the do not care values
		binaryString += "000000";

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the RET instruction.
	 * 
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the RET instruction
	 */
	private String retOp(int opcode_value, int size, String line) {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Adding the don't cares
		binaryString += "000000000000";

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the LD instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the LD instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String stOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		// Calling the instruction with the same procedure
		return ldOp(operand, opcode_value, size, line);
	}

	/**
	 * Forming the text record for the STI instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the LD instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String stiOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		// Calling the instruction with the same procedure
		return ldOp(operand, opcode_value, size, line);
	}

	/**
	 * Forming the text record for the STR instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the STR instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String strOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		// Calling the instruction with the same procedure
		return ldrOp(operand, opcode_value, size, line);
	}

	/**
	 * Forming the text record for the TRAP instruction.
	 * 
	 * @param operand      each of the operand split into a separate string
	 * @param opcode_value the size by which LC is to be updated
	 * @param size         the size by which LC is to be updated
	 * @param line         the current line in the assembly code being parsed
	 * @return the text record for the TRAP instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String trapOp(String[] operand, int opcode_value, int size, String line) throws Pass2Exception {
		String textRecord = "";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(size);
		textRecord += lcAddress;

		String binaryString = sp.opcodeBinaryString(opcode_value);

		// Adding the don't cares
		binaryString += "0000";

		// Getting the register
		String pos = operand[0];

		// If the operand is a register
		if (pos.charAt(0) == '#' || pos.charAt(0) == 'x') {
			String trap = sp.trapToBinaryString(pos, line);
			binaryString += trap;
		}

		// If the operand is an absolute symbol
		else {
			// Ensure the symbol is in the symbol table
			absoluteSymbolTable(pos, line);

			// If there is a symbol at the end, it is treated as a trap vector
			String trap = sp.trapToBinaryString(symbolTable.get(pos)[0], line);
			binaryString += trap;
		}

		// Convert the binary string into a hex string
		String hexString = sp.convertBinaryToHexString(binaryString, line);
		textRecord += hexString;

		return textRecord;
	}

	/**
	 * Forming the text record for the .BLKW instruction and outputting to the
	 * listing file.
	 * 
	 * @param symbol  any symbol pertaining to the given line of assembly
	 * @param opcode  the name of the instruction
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the text record for the LD instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String blkwOp(String symbol, String opcode, String operand, String line) throws Pass2Exception {
		String textRecord = "";
		int blockVal;

		if (!(operand.charAt(0) == 'x' || operand.charAt(0) == '#')) {
			absoluteSymbolTable(operand, line);
			blockVal = pot.blockLength(this.symbolTable.get(operand)[0]);
		}

		// Otherwise, the value is defined as a constant
		else {
			blockVal = pot.blockLength(operand);
		}

		if (blockVal < 0) {
			throw new Pass2Exception(
					"Line: " + line + ": The given BLKW value is not between [1, 65535] or [x1, xFFFF].");
		}

		// Updating the address based on the location counter
		String address = addressFromLC(blockVal);
		blkwListingFile(symbol, address, opcode, operand, line);

		return textRecord;
	}

	/**
	 * Forming the end record for the .END instruction.
	 * 
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the end record for the .END instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String endOp(String operand, String line) throws Pass2Exception {
		String textRecord = "E";

		// If the address does not end with a value
		if (operand.isEmpty()) {
			textRecord += this.initialAddress;
		} else {
			// If the operand is a hex/decimal value
			if (operand.charAt(0) == 'x' || operand.charAt(0) == '#') {
				textRecord += sp.addressToHex(operand, line);
			}

			// If the operand is a symbol
			else {
				// Verifying the symbol is present in the symbol table
				int relative = isRelativeSymbol(operand, line);
				String address;

				// Update address if it is a .EXT symbol or symbol table value
				if (relative == 2) {
					address = this.tempExternalTable.get(operand);
				} else {
					String val = this.symbolTable.get(operand)[0];

					// Check if the address is a value
					if (val.charAt(0) == 'x' || val.charAt(0) == '#') {
						address = val;
					}

					// otherwise, it is a external symbol
					else {

						// make sure the symbol is a external forward referencing
						if (!this.tempExternalTable.containsKey(val)) {
							throw new Pass2Exception(
									"Line: " + line + ": cannot forward reference a non-external symbol");
						}

						// update the value with the value from the external symbol table
						address = this.tempExternalTable.get(val);
					}
				}

				textRecord += sp.addressToHex(address, line);
			}
		}

		return textRecord;
	}

	/**
	 * Parsing through .ENT operands and forming N records.
	 * 
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the end record for the .END instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String entOp(String[] operand, String line) throws Pass2Exception {
		String nRecord = "";

		// Throw an error for > 5 fields in the operand
		if (operand.length > 5) {
			throw new Pass2Exception("Line " + line + ": More than 5 entry symbols defined in the operand field");
		}

		// Parsing through all the operands
		for (int i = 0; i < operand.length; i++) {
			nRecord += "N";
			String pos = operand[i];

			// Making sure the symbol is relative
			if (isRelativeSymbol(pos, line) != 1) {
				throw new Pass2Exception("Line " + line + ": Symbol \"" + pos + "\" is not a relative symbol");
			}

			// Getting the symbol value
			String symVal = this.symbolTable.get(pos)[0].substring(1);

			// Adding the symbol name and the values (without hex/decimal) to the record
			nRecord += (pos + "=" + symVal);
			nRecord += "\n";
		}

		return nRecord;
	}

	/**
	 * Parsing .EXT operations and adding any external symbols to a temporary
	 * storage unit.
	 * 
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the end record for the .END instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String extOp(String[] operand, String line) throws Pass2Exception {
		String textRecord = "";

		// Throw an error for > 5 fields in the operand
		if (operand.length > 5) {
			throw new Pass2Exception("Line " + line + ": More than 5 external symbols defined in the operand field");
		}

		// Parsing through all the operands
		for (int i = 0; i < operand.length; i++) {
			String pos = operand[i];

			// If it is a symbol used in the program, then throw an error
			if (this.symbolTable.containsKey(pos)) {
				throw new Pass2Exception("Line " + line + ": Symbol \"" + pos
						+ "\" is already defined in the symbol table (cannot be used as an external symbol)");
			}

			// Add in the value temporarily with 0
			this.tempExternalTable.put(pos, "x0000");
		}

		return textRecord;
	}

	/**
	 * Ensuring the .EQU instruction values are correct.
	 * 
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the text record for the EQU instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String equOp(String operand, String line) throws Pass2Exception {
		String textRecord = "";

		if (operand.charAt(0) == 'x' || operand.charAt(0) == '#') {
			if (!sp.canParseInt(operand)) {
				throw new Pass2Exception("Line " + line + ": Invalid decimal/hex value");

			}
		} else {
			isRelativeSymbol(operand, line);
		}

		return textRecord;
	}

	/**
	 * Forming the text record for the .FILL instruction
	 * 
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the text record for the .FILL instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String fillOp(String operand, String line) throws Pass2Exception {
		String textRecord = "T";
		int relocatable = 0;
		// name of the external symbol for the modification record
		String externalSym = operand;

		// Forming the address based on location counter
		String lcAddress = addressFromLC(1);
		textRecord += lcAddress;

		// If the operand is a hex/decimal
		if (operand.charAt(0) == 'x' || operand.charAt(0) == '#') {
			textRecord += sp.fillHexString(operand, line);
		}

		// If the operand is a symbol
		else {
			relocatable = isRelativeSymbol(operand, line);
			String address;

			// Update address if it is a .EXT symbol or symbol table value
			if (relocatable == 2) {
				address = this.tempExternalTable.get(operand);
			} else {
				String val = this.symbolTable.get(operand)[0];

				// Check if the address is a value
				if (val.charAt(0) == 'x' || val.charAt(0) == '#') {
					address = val;
				}

				// otherwise, it is a external symbol
				else {

					// make sure the symbol is a external forward referencing
					if (!this.tempExternalTable.containsKey(val)) {
						throw new Pass2Exception("Line: " + line + ": cannot forward reference a non-external symbol");
					}

					// update the value with the value from the external symbol table
					address = this.tempExternalTable.get(val);
					externalSym = val;

					// when creating modification records, make sure it is a external symbol
					relocatable = 2;
				}
			}

			textRecord += sp.fillHexString(address, line);
		}

		// Check if the operand was relocatable
		if (this.isRelocatable) {
			if (relocatable == 1) {
				textRecord += ("X16" + this.segName);
			} else if (relocatable == 2) {
				textRecord += ("X16" + externalSym);
			}
		}

		textRecord += "\n";

		return textRecord;
	}

	/**
	 * Forming the header record for the .ORIG instruction.
	 * 
	 * @param symbol  any symbol pertaining to the given line of assembly
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the header record for the .ORIG instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String origOp(String symbol, String operand, String line) throws Pass2Exception {
		String headerRecord = "H";

		// Making the segment name 6 characters long
		while (symbol.length() < 6) {
			symbol += " ";
		}

		headerRecord += symbol;
		this.segName = symbol;

		// If the origin is loaded at 0 or given
		if (operand.length() < 1) {
			headerRecord += "0000";
			this.isRelocatable = true;
		} else {
			// Making the hex value for the origin
			String originAddress = sp.addressToHex(operand, line);
			this.LC = Integer.parseUnsignedInt(originAddress, 16);

			headerRecord += originAddress;
			this.initialAddress = originAddress;
		}

		// Making sure the segment size is within bounds
		if (this.segmentSize > 0xFFFF) {
			throw new Pass2Exception("Line " + line + ": Segment size is greater than 0xFFFF");

		}

		// Relocatable and > page length
		if (this.isRelocatable && this.segmentSize > 512) {
			throw new Pass2Exception("Line " + line + ": Segment size is greater than a page length");
		}

		// Loading the segment size
		String segmentLength = Integer.toUnsignedString(this.segmentSize, 16).toUpperCase();

		// Making the segment size address is 4 characters long
		while (segmentLength.length() < 4) {
			segmentLength = "0" + segmentLength;
		}

		headerRecord += (segmentLength + "\n");
		return headerRecord;
	}

	/**
	 * Forming the text record(s) for the .STRZ instruction and outputting to the
	 * listing file.
	 * 
	 * @param symbol  any symbol pertaining to the given line of assembly
	 * @param opcode  the name of the instruction
	 * @param operand each of the operand split into a separate string
	 * @param line    the current line in the assembly code being parsed
	 * @return the text record(s) for the .STRZ instruction
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private String strzOp(String symbol, String opcode, String operand, String line) throws Pass2Exception {
		String finalTextRecord = "";
		String textRecord = "";
		int i = 0;

		for (i = 0; i < operand.length(); i++) {
			// Creating the text record
			textRecord = "T";

			// Forming the address based on location counter
			String lcAddress = addressFromLC(1);
			textRecord += lcAddress;

			// Parsing each character with its ASCII values
			int c = (int) operand.charAt(i);
			String hexString = Integer.toHexString(c).toUpperCase();
			while (hexString.length() < 4) {
				hexString = "0" + hexString;
			}

			// Add the a hex string to the text record
			textRecord += hexString;
			textRecord += "\n";

			finalTextRecord += textRecord;

			strzListingFile(symbol, opcode, operand, i, textRecord, line);
		}

		textRecord = "T";

		// Forming the address based on location counter
		String lcAddress = addressFromLC(1);
		textRecord += lcAddress;

		// Adding the null termination to the text record
		textRecord += "0000";
		textRecord += "\n";

		finalTextRecord += textRecord;

		strzListingFile(symbol, opcode, operand, i, textRecord, line);

		return finalTextRecord;
	}

	/**
	 * Check whether a symbol is absolute or not.
	 * 
	 * @param pos  the symbol to check its relativity
	 * @param line the current line in the assembly code being parsed
	 * @return the boolean value for whether the symbol is absolute or not
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private void absoluteSymbolTable(String pos, String line) throws Pass2Exception {
		if (this.tempExternalTable.containsKey(pos)) {
			throw new Pass2Exception(
					"Line " + line + ": External symbol \"" + pos + "\" cannot be used as an absolute symbol");
		}

		if (!this.symbolTable.containsKey(pos)) {
			throw new Pass2Exception("Line " + line + ": Symbol \"" + pos + "\" is not found in the symbol table");
		}

		if (!symbolTable.get(pos)[1].equals("A")) {
			throw new Pass2Exception("Line " + line + ": Symbol \"" + pos + "\" is not an absolute symbol");
		}
	}

	/**
	 * Check whether a symbol is relative or not. Returns 1 if the symbol is in the
	 * symbol table and is an external symbol, return 2 if the symbol is in the
	 * external symbol, otherwise it is absolute symbol.
	 * 
	 * @param pos  the symbol to check its relativity
	 * @param line the current line in the assembly code being parsed
	 * @return the boolean value for whether the symbol is relative or not
	 * @throws Pass2Exception throwing a runtime error when a fatal error is
	 *                        detected in Pass2
	 */
	private int isRelativeSymbol(String pos, String line) throws Pass2Exception {
		// Throw an error if it is not contained in either tables
		if (!this.symbolTable.containsKey(pos) && !this.tempExternalTable.containsKey(pos)) {
			throw new Pass2Exception("Line " + line + ": Symbol \"" + pos + "\" is not found in the symbol table");
		}

		// Symbol table relative
		if (this.tempExternalTable.containsKey(pos)) {
			return 2; // is an external symbol
		} else if (symbolTable.get(pos)[1].equals("R")) {
			return 1; // is a relative symbol
		}

		return 0; // is an absolute symbol
	}

	/**
	 * Returns the address formed by the location counter and increments it by the
	 * size according to the instruction.
	 * 
	 * @param size the size of the instruction
	 * @return the address LC points to
	 */
	private String addressFromLC(int size) {
		String address = Integer.toHexString(this.LC).toUpperCase();

		while (address.length() < 4) {
			address = "0" + address;
		}

		this.LC = (this.LC + size) % 0x10000;
		return address;
	}

}
