package lab3_integrated.assembler.lab2;

import java.util.ArrayList;
import java.util.List;

import lab3_integrated.assembler.lab2.Exceptions.CommentLineNoSemicolon;
import lab3_integrated.assembler.lab2.Exceptions.InvalidOperandException;
import lab3_integrated.assembler.MOT.Machine_Op_Table;
 
public class Validate {
	Machine_Op_Table mot;

	Validate(Machine_Op_Table mot) {
		this.mot = mot;
	}

	enum OpType {
		COMMENT, ORIG, END, EQU, FILL, STRZ, BLKW, INSTRUCTION, UNKNOWN, ENT, EXT
	}
	
	enum OperandType {
		REGISTER, IMMEDIATE, INDEX, ADDRESS, NONE, PSEUDOOP, LITERAL
	}

	public static class Locations {
		public static final int LABEL = 0;
		public static final int OPERATION = 1;
		public static final int OPERANDS = 2;
		public static final int LINE = 3;
	}
	
	private OpType psudeoOP;
	
	private List<String> fileContainsOrigAndEnd = new ArrayList<>();;
	/**
	 * Line number.
	 */
	
	private int commentOrEntExtCounter = 1;
	/**
	 * Validates the given line of code and returns a list that represents the line
	 * containing the a symbol (if applicable), operation, operand, and line number
	 * @param line - The line to validate
	 * @param lines - List of all lines in the program
	 * @return - A list representing the line in the following order 
	 * {Symbol, Operation, Operand, Line Number}
	 * @throws Exception - If an error occurs during validation
	 */
	List<String> validate(String line, ArrayList<List<String>> lines, int lineCount) throws Exception {
		
		if(line.isBlank()) {
			throw new IllegalArgumentException("An Empty Line is NOT valid");
		}
		
		OpType type = getLineType(line);
		this.psudeoOP = type;
		
		if (type == OpType.COMMENT) {
			this.commentOrEntExtCounter++;
			return null;
		} else if (type == OpType.UNKNOWN) {
			if (!containsAnyKey(line)) {
				throw new IllegalArgumentException("LINE MUST HAVE PSEUDO OP OR MACHINE OP");
			}
			validateInstruction(line);
		}
		
		line = stripComment(line);
		List<String> result = validateAndSplitLine(line, lines);
		
		if (type == OpType.ENT || type == OpType.EXT) {
			// validate line content
			// make sure line above it is .ORIG or .EXT or .ENT
			if(lineCount - this.commentOrEntExtCounter != 2) {
				throw new IllegalArgumentException("ENT and EXT must be declared right after ORIG");
			}
			validateEntOrExt(line,result);
			this.commentOrEntExtCounter++;
		}else if(type != OpType.INSTRUCTION && type != OpType.UNKNOWN){
			boolean isValidOperand = checkOperands(result.get(Locations.OPERANDS), new OperandType[] {OperandType.PSEUDOOP});
			if(!isValidOperand) {
				throw new IllegalArgumentException("Invalid Operand");
			}
			
		}
		
		if (result.get(Locations.OPERANDS).contains("\"")) {
			String tempString = result.remove(Locations.OPERANDS);
			tempString = tempString.substring(1, tempString.length() - 1);
			result.add(Locations.OPERANDS, tempString);
		}
		
		result.add(Integer.toString(lineCount));
		validateOperation(result.get(Locations.OPERATION), type);
		

		if(type == OpType.END || type == OpType.ORIG){
			if(lineCount - this.commentOrEntExtCounter != 1 && type == OpType.ORIG) {
				System.out.println(lineCount - this.commentOrEntExtCounter);
				throw new IllegalArgumentException("ORIG must be the first pseudo op in the file ");
			}
			fileContainsOrigAndEnd.add(type.toString());
		}
		
		if(type == OpType.EQU || type == OpType.ORIG){
			try {
				checkSymbol(result.get(Locations.LABEL).trim());
			}catch(Exception e) {
				throw new IllegalArgumentException("EQU and ORIG must have a label ");
			}
			 
		}
		return result;
	}

	/**
	 * Removes the semicolon from a comment
	 * @param line - the comment
	 * @return - updated comment without semicolon
	 */
	String stripComment(String line) {
		return line.split(";")[0];
	}
	
	/**
	 * Checks if the Input line contains any Machine OP Table Symbol
	 * @param input - line to validate
	 * @return true if Input contains Machine OP Table Symbol
	 */
	boolean containsAnyKey(String input) {
		input = input.trim();
		String[] inputArr = input.split(" ");

		for (String s : inputArr) {
			s = s.trim();
			if (mot.containsOp(s)) {
				return true;
			}
		}

		return false;
	}
	
	boolean isDebugOrRet(String input) {
		input = input.trim();
		String[] inputArr = input.split(" ");

		for (String s : inputArr) {
			s = s.trim();
			if (mot.containsOp(s) && (s.equals("DBUG") || s.equals("RET"))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if a line is a comment
	 * @param line - Comment line
	 * @throws Exceptions.CommentLineNoSemicolon if line is not a comment
	 */
	void validateComment(String line) throws Exceptions.CommentLineNoSemicolon {
		if (line.length() > 0 && line.trim().charAt(0) != ';') {
			throw new Exceptions.CommentLineNoSemicolon();
		}
	}
	
	/**
	 * Check is an operation is valid meaning it's a Pseudo OP or Machine OP
	 * @param operation - operation to validate
	 * @param type - ENUM of all possible Pseudo OPs
	 * @throws Exception if not a valid operation
	 */
	void validateOperation(String operation, OpType type) throws Exception {
		operation = operation.trim();
		if (!operation.equals("." + type) && !mot.containsOp(operation)) { 
			throw new IllegalArgumentException("WAS EXPECTING " + "." + type + " GOT " + operation);
		}

	}

	/**
	 * Determine if a line containing a Machine OP is syntactically valid
	 * so Symbol, Operation, Operand and Comment where symbol and comment are optional
	 * @param line - line to syntactically validate
	 * @throws InvalidOperandException if validateOperand method fails
	 */
	void validateInstruction(String line) throws InvalidOperandException {
		line = line.trim();
		String[] lineArr = line.split("\s+");
		List<String> arr = stripArray(lineArr);
		if (arr.size() == 3) {
			String sym = arr.remove(0);
			checkSymbol(sym);
			
			String operation = arr.remove(0).trim();
			String operand = arr.remove(0).trim();
			validateOperand(operand, operation);
		} else if (arr.size() == 2) {	
			String operation = arr.remove(0).trim();
			String operand = arr.remove(0).trim();
			validateOperand(operand, operation);
		} else {
			if(!isDebugOrRet(lineArr[0])) {
				throw new IllegalArgumentException("The following line is invalid: " + line);
			}
			
		}
	}
	
	/**
	 * Removes comment from line if there is one and returns a list of all other data
	 * @param arr - String array containing data from line that's split by spaces
	 * @return - ArrayList containing the remaining data without comment
	 */
	List<String> stripArray(String[] arr) {
		List<String> result = new ArrayList<>();
		for (String s : arr) {
			s = s.trim();
			if (s.length() > 0 && s.charAt(0) == ';') {
				break;
			}
			result.add(s);
		}
		return result;
	}
	 
	/**
	 * Determines if operation is a Machine OP then verifies it's operand length and order 
	 * is as expected according machine's description
	 * http://web.cse.ohio-state.edu/~giles.25/3903/assignments/project2.html
	 * @param operand - operand of the operation
	 * @param operation - operation to perform
	 * @throws Exceptions.InvalidOperandException if operation and/or operand are incompatible
	 */
	void validateOperand(String operand, String operation) throws Exceptions.InvalidOperandException {
		operand = operand.trim();
		if (operand.equals("") && !(operation.equals(".ORIG") || operation.equals(".END"))) {
			throw new Exceptions.InvalidOperandException();
		}
	 	
		switch (operation) {
		case "ADD":
		case "AND":
			if (!checkOperands(operand,
					new OperandType[] { OperandType.REGISTER, OperandType.REGISTER, OperandType.REGISTER })
					&& !checkOperands(operand,
							new OperandType[] { OperandType.REGISTER, OperandType.REGISTER, OperandType.IMMEDIATE })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "BR":
		case "BRN":
		case "BRZ":
		case "BRP":
		case "BRNZ":
		case "BRNP":
		case "BRZP":
		case "BRNZP":
		case "JSR":
		case "JMP":
			if (!checkOperands(operand, new OperandType[] { OperandType.ADDRESS })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "DBUG":
		case "RET":
			if (!checkOperands(operand, new OperandType[] { OperandType.NONE })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;

		case "JSRR":
		case "JMPR":
			if (!checkOperands(operand, new OperandType[] { OperandType.REGISTER, OperandType.IMMEDIATE })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "LDI":
		case "LEA":
		case "ST":
		case "STI":
			if (!checkOperands(operand, new OperandType[] { OperandType.REGISTER, OperandType.ADDRESS })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "LD":
			if (!checkOperands(operand, new OperandType[] { OperandType.REGISTER, OperandType.ADDRESS }) 
					&& !checkOperands(operand, new OperandType[] { OperandType.REGISTER, OperandType.LITERAL })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "LDR":
		case "STR":
			if (!checkOperands(operand,
					new OperandType[] { OperandType.REGISTER, OperandType.REGISTER, OperandType.INDEX })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "NOT":
			if (!checkOperands(operand, new OperandType[] { OperandType.REGISTER, OperandType.REGISTER })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			break;
		case "TRAP":
			if (!checkOperands(operand, new OperandType[] { OperandType.IMMEDIATE })) {
				throw new IllegalArgumentException("Invalid Operand " + operand);
			}
			// DO Something
			break;
		default:
			
			throw new IllegalArgumentException("Invalid Operation  " + operation);
			
		}

	}
	/**
	 * Determine if the operand of the Machine OP or Pseudo OP is syntactically correct
	 * http://web.cse.ohio-state.edu/~giles.25/3903/assignments/project2.html
	 * @param operand - to verify
	 * @param desiredOps - Array of ENUMS which determine what the ordering of an operand should be
	 * @return - true if the operand is valid
	 */
	boolean checkOperands(String operand, OperandType[] desiredOps) {
		String[] op = operand.split(",");
		if (op.length != desiredOps.length && desiredOps[0] != OperandType.NONE ) {
			return false;
		}
		
		for (int i = 0; i < desiredOps.length; i++) {
			op[i] = op[i].trim();
			String currentString = op[i];
			char firstCh = currentString.length() > 0 ? currentString.charAt(0): ' ';
			OperandType currentType = desiredOps[i];

			if (desiredOps[i] == OperandType.REGISTER) {
				if (firstCh == 'R') {
					if (currentString.length() > 2 || !Character.isDigit(currentString.charAt(1))
							|| currentString.charAt(1) - '0' > 7) {
						return false;
					}
				} else {
					try {
					checkSymbol(currentString);
					
					}catch(Exception e) {
						return false;
					}
					
				}
			} else if (currentType == OperandType.IMMEDIATE || currentType == OperandType.INDEX
					|| currentType == OperandType.ADDRESS) {
				if (firstCh == 'x' || firstCh == '#') {
					String digits = currentString.substring(1);
					
					if (!digits.matches("^-?\\d+.*")) {
						return false;
					}
				} else if (firstCh == 'R') {
					return false;
				} else {
					try {
						checkSymbol(currentString);
					}catch(Exception e) {
							return false;
						}
				}

			} else if (currentType == OperandType.NONE) {
				if (!currentString.isBlank()) {
					try{
						   checkSymbol(currentString);
					   }catch(Exception e) {
						   return false;
					   }
				}

			} else if (currentType == OperandType.PSEUDOOP){
//				operand = stripComment(operand);
				if(this.psudeoOP == OpType.EQU || this.psudeoOP == OpType.BLKW) {
					
					if (!operand.matches("^x[0-9a-fA-F]+$") && !operand.matches("^#-?[0-9]+$")) {
					   try{
						   checkSymbol(operand);
					   }catch(Exception e) {
						   return false;
					   }
					    
					} 
					
				}else {
					boolean alphaNumericCheck = operand.matches("^x[0-9a-fA-F]+$") || operand.matches("^#-?[0-9]+$");
					if (this.psudeoOP == OpType.ORIG) {
						if (!alphaNumericCheck && !operand.isBlank() && !isComment(operand)){
							return false;
						}
					}else if(this.psudeoOP == OpType.END) {
						if (!alphaNumericCheck && !operand.isBlank() && !isComment(operand)) {
							   try{
								   checkSymbol(operand);
							   }catch(Exception e) {
								   return false;
							   }
								
							} 
						
					}else if(this.psudeoOP == OpType.FILL) {
						if (!alphaNumericCheck) {
							   try{
								   checkSymbol(operand);
							   }catch(Exception e) {
								   return false;
							   }
								
							}
					}
				}
				
				
			}else if(currentType == OperandType.LITERAL) {
				if(currentString.length() >= 3 && currentString.charAt(0) == '=' ) {
					String literal = currentString.substring(1);
					if(!literal.matches("^x[0-9a-fA-F]+$") && !literal.matches("^#-?[0-9]+$")){
						return false;
					}
				}else {
					return false;
				}
				
				
			}else {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Checks if a symbol already exists
	 * @param symbol - to determine if already present
	 * @param lines - of all data parsed where list index 0 is the symbol
	 */
	void checkIfSymExists(String symbol, ArrayList<List<String>> lines) {
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).get(0).equals(symbol)) {
				throw new IllegalArgumentException("Symbol already exists");
			}
		}
	}
	
	boolean isComment(String line) {
		line = line.trim();
		if(line.length() == 0 || line.charAt(0) != ';') {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if a string is a valid symbol based on the machine description
	 * @param symbol - to verify
	 */
	void checkSymbol(String symbol) {
		if (symbol.length() > 6 || symbol.charAt(0) == 'x') {
			throw new IllegalArgumentException("Error symbol");
		}
		
		if(symbol.length() > 0 && !Character.isAlphabetic(symbol.charAt(0))) {
			throw new IllegalArgumentException("Error symbol");
		}
		
		if (!symbol.matches("[A-Za-z0-9]+")) {
			throw new IllegalArgumentException("Error symbol");
		}
	}
	
	/**
	 * Splits line and verify's it has all five parts based in the following order
	 * Lable, White Space, Operation, White Space, Operand/Comment
	 * @param line - line to validate
	 * @param lines - List of all lines in the program
	 * @return - A list representing the line in the following order {Symbol, Operation, Operand}
	 * @throws Exception if line doesn't meet requirements
	 */
	List<String> validateAndSplitLine(String line, ArrayList<List<String>> lines) throws Exception {
		if (line.length() < 17) {
			throw new Exceptions.TooShortException();
		}
		
		String label = line.substring(0, 6);
		String firstWhiteSpace = line.substring(6, 9);
		String operation = line.substring(9, 14);
		String secondWhiteSpace = line.substring(14, 17);
		String operandsOrComment = line.substring(17);

		label = label.trim();
		operation = operation.trim();
		operandsOrComment = operandsOrComment.trim();

		if (label.length() > 0) {
			checkIfSymExists(label, lines);
			char labelFirstChar = label.charAt(0);
			if (!Character.isAlphabetic(labelFirstChar) || labelFirstChar == 'R' || labelFirstChar == 'x') {
				throw new IllegalArgumentException(
						"Label must start with an alphabetic character that is NOT a R OR an x");
			}
		}

		if (!firstWhiteSpace.isBlank()) {
			throw new IllegalArgumentException("White space must NOT contain any characters");
		}

		if (!secondWhiteSpace.isBlank()) {
			throw new IllegalArgumentException("White space must NOT contain any characters");
		}
		List<String> result = new ArrayList<>();

		// operation -> 0, operandsOrComments -> 1 originally
		result.add(Locations.LABEL, label);
		result.add(Locations.OPERATION, operation);
		result.add(Locations.OPERANDS, operandsOrComment);
		
		return result;
	} 
	 
	/**
	 * Returns ENUM describing the type of line passed
	 * @param line - to determine type of
	 * @return - ENUM of pseudo OP or unknown type
	 */
	OpType getLineType(String line) {
		line = line.trim();
		
		if(line.length() < 0) {
			return OpType.UNKNOWN;
		}
		
		if (line.charAt(0) == ';') {
			return OpType.COMMENT;
		} else if (line.contains(OpType.STRZ.toString())) {
			return OpType.STRZ;
		} else if (line.contains(OpType.BLKW.toString())) {
			return OpType.BLKW;
		} else if (line.contains(OpType.ORIG.toString())) {
			return OpType.ORIG;
		} else if (line.contains(OpType.EQU.toString())) {
			return OpType.EQU;
		} else if (line.contains(OpType.END.toString())) {
			return OpType.END;
		} else if (line.contains(OpType.FILL.toString())) {
			return OpType.FILL;
		} else if (line.contains(OpType.ENT.toString())) {
			return OpType.ENT;
		} else if (line.contains(OpType.EXT.toString())) {
			return OpType.EXT;
		} else {
			return OpType.UNKNOWN;
		}
	}

	/**
	 * Determine if ORIG and END exist excatly once
	 * @return true if ORIG and END both appear once false otherwise
	 */
	public boolean doesFileContainsOrigAndEnd() {
		return fileContainsOrigAndEnd.size() == 2? true: false;
	}
	
	void validateEntOrExt(String line, List<String> list) throws CommentLineNoSemicolon {
		line = line.trim();
		
		
		if(!list.get(0).isBlank()) {
			throw new IllegalArgumentException("No Label Allowed for ENT or EXT operations");
		}
		
		if(list.get(1).trim().charAt(0) != ';') {
			
			String[] symbols = list.get(2).split(",");
			
			for(String s: symbols) {
				checkSymbol(s.trim());
			}

		}
	}
	

}
