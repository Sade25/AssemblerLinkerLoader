package lab3_integrated.assembler.Passes;

import java.util.Map;

import lab3_integrated.assembler.lab2.Exceptions.Pass2Exception;

public class String_Parser {
	/**
	 * Constants defined as finals.
	 */
	// Magic numbers
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int DECIMAL = 10;
	public static final int HEX = 16;
	public static final int INVALID_NUMBER = -0xFFFF;

	// Imm5 sizes
	public static final int IMMEDIATE_HEX_MAX = 0x1F;
	public static final int IMMEDIATE_DEC_MIN = -16;
	public static final int IMMEDIATE_DEC_MAX = 15;

	// Ind6 sizes
	public static final int INDEX_HEX_MAX = 0x3F;
	public static final int INDEX_DEC_MAX = 63;

	// Trapvect8 sizes
	public static final int TRAP_HEX_MAX = 0xFF;
	public static final int TRAP_DEC_MAX = 255;

	// Address and word sizes
	public static final int ADDRESS_HEX_MAX = 0xFFFF;
	public static final int ADDRESS_DEC_MAX = 65535;
	public static final int WORD_MIN = -32768;
	public static final int WORD_MAX = 32767;

	/**
	 * Checks whether the given string can be parsed into an integer if it is within
	 * the range defined in the abstract machine.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return if the given integer can be parsed to an integer if within the range
	 *         of [-32768, 0xFFFF]
	 * @ensures canParseInt = Integer.parseInt(input) and within range is true,
	 *          false otherwise
	 */
	public boolean canParseInt(String input) {
		int length = input.length();

		// Return false if the length does not contain a literal/hex
		if (length < TWO) {
			return false;
		}

		char pos0 = input.charAt(ZERO);
		char pos1 = input.charAt(ONE);

		int index = input.length();

		// When the string is a literal
		if (pos0 == '=' && length > TWO) {

			// When the value is hex or decimal
			if (pos1 == 'x') {
				return isInteger(input.substring(TWO, index), HEX);
			} else if (pos1 == '#') {
				return isInteger(input.substring(TWO, index), DECIMAL);
			}
		}

		// When the value is hex or decimal
		else if (pos0 == 'x') {
			return isInteger(input.substring(ONE, index), HEX);
		} else if (pos0 == '#') {
			return isInteger(input.substring(ONE, index), DECIMAL);
		}

		return false;
	}

	/**
	 * Attempts to parse and return an immediate value if given in the correct
	 * range.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return integer value if within the range of [-16, 15] or [0x0, 0x1F], or
	 *         returns invalid number (-0xFFFF)
	 * @requires input.length() > 1
	 * @ensure parseImmediate = Integer.parseInt(input) within range, -0xFFFF
	 *         otherwise
	 */
	public int parseImmediate(String input) {
		int val = parseInt(input);

		if ((isHex(input, ZERO) && val >= ZERO && val <= IMMEDIATE_HEX_MAX)
				|| (isDecimal(input, ZERO) && val >= IMMEDIATE_DEC_MIN && val <= IMMEDIATE_DEC_MAX)) {
			return val;
		}

		return INVALID_NUMBER;
	}

	/**
	 * Attempts to parse and return an index value if given in the correct range.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return integer value if within the range of [0, 63] or [0x0, 0x3F], or
	 *         returns invalid number (-0xFFFF)
	 * @ensure parseIndex = Integer.parseInt(input) within range, -0xFFFF otherwise
	 */
	public int parseIndex(String input) {
		int val = parseInt(input);

		if ((isHex(input, ZERO) && val >= ZERO && val <= INDEX_HEX_MAX)
				|| (isDecimal(input, ZERO) && val >= ZERO && val <= INDEX_DEC_MAX)) {
			return val;
		}

		return INVALID_NUMBER;
	}

	/**
	 * Attempts to parse and return an trap vector value if given in the correct
	 * range.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return integer value if within the range of [0, 255] or [0x0, 0xFF], or
	 *         returns invalid number (-0xFFFF)
	 * @requires input.length() > 1
	 * @ensure parseTrapvect = Integer.parseInt(input) within range, -0xFFFF
	 *         otherwise
	 */
	public int parseTrapvect(String input) {
		int val = parseInt(input);

		if ((isHex(input, ZERO) && val >= ZERO && val <= TRAP_HEX_MAX)
				|| (isDecimal(input, ZERO) && val >= ZERO && val <= TRAP_DEC_MAX)) {
			return val;
		}

		return INVALID_NUMBER;
	}

	/**
	 * Attempts to parse and return an address value if given in the correct range.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return integer value if within the range of [0, 65535] or [0x0, 0xFFFF], or
	 *         returns invalid number (-0xFFFF)
	 * @requires input.length() > 1
	 * @ensure parseAddress = Integer.parseInt(input) within range, -0xFFFF
	 *         otherwise
	 */
	public int parseAddress(String input) {
		int val = parseInt(input);

		if ((isHex(input, ZERO) && val >= ZERO && val <= ADDRESS_HEX_MAX)
				|| (isDecimal(input, ZERO) && val >= ZERO && val <= ADDRESS_DEC_MAX)) {
			return val;
		}

		return INVALID_NUMBER;
	}

	/**
	 * Checks whether the given string can be parsed into an literal if it is within
	 * the range.
	 * 
	 * @param input potential string to be parsed into a literal value
	 * @return if the given integer can be parsed to an integer if within the range
	 *         of [-32768, 32767] or [0x0, 0xFFFF]
	 * @ensures isLiteral = Integer.parseInt(input) and within range is true, false
	 *          otherwise
	 */
	public boolean isLiteral(String input) {
		boolean b = input.length() > TWO && input.charAt(ZERO) == '=' && canParseInt(input);
		if (b) {
			int val = parseInt(input);
			return (isHex(input, ONE) && val >= ZERO && val <= ADDRESS_HEX_MAX)
					|| (isDecimal(input, ONE) && val >= WORD_MIN && val <= WORD_MAX);
		}
		return false;
	}

	/**
	 * Attempts to parse and return a literal value as a string if given in the
	 * correct range.
	 * 
	 * @param input potential string to be parsed into a literal value
	 * @return if the given integer can be parsed to an integer if within the range
	 *         of [-32768, 32767] or [0x0, 0xFFFF], -0xFFFF otherwise
	 * @ensures isLiteral = Integer.parseInt(input) and within range, null otherwise
	 */
	public String parseLiteralString(String input) {
		int val = parseInt(input);

		if ((isHex(input, ONE) && val >= ZERO && val <= ADDRESS_HEX_MAX)
				|| (isDecimal(input, ONE) && val >= WORD_MIN && val <= WORD_MAX)) {
			String s = Integer.toHexString(val);

			if (s.length() <= 4) {
				while (s.length() < 4) {
					s = "0" + s;
				}
			} else {
				s = s.substring(s.length() - 4, s.length());
			}

			return s;
		}

		return null;
	}

	/**
	 * Attempts to parse and return an union range value if given in the correct
	 * range.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return integer value if within the range of [-32678, 32677] or [0x0,
	 *         0xFFFF], or returns invalid number (-0xFFFF)
	 * @requires input.length() > 1
	 * @ensure parseUnionRange = Integer.parseInt(input) within range, -0xFFFF
	 *         otherwise
	 */
	public int parseUnionRange(String input) {
		int val = parseInt(input);

		if ((isHex(input, ZERO) && val >= ZERO && val <= ADDRESS_HEX_MAX)
				|| (isDecimal(input, ZERO) && val >= WORD_MIN && val <= WORD_MAX)) {
			return val;
		}

		return INVALID_NUMBER;
	}

	/**
	 * Returns the binary string version of the index6 value.
	 * 
	 * @param ind  the index value to be parsed
	 * @param line the current line of assembly code
	 * @return the index value as a binary string
	 * @throws Pass2Exception
	 */
	public String indexToBinaryString(String ind, String line) throws Pass2Exception {
		if (!canParseInt(ind)) {
			throw new Pass2Exception("Line " + line + ": Invalid index6 value");
		}

		int index = parseIndex(ind);

		if (index < -0x8000) {
			throw new Pass2Exception(
					"Line " + line + ": Index6 value is not within the specified range [#0 - #63] or [0x0 - 0x3F]");
		}

		String s = Integer.toBinaryString(index);
		while (s.length() < 6) {
			s = "0" + s;
		}

		return s;
	}

	/**
	 * Returns if a given address is within the page range as the location counter.
	 * 
	 * @param lc      the location counter
	 * @param address the address
	 * @param line    the current line of assembly code
	 * @return Parses the given address and ensures the address is within the same
	 *         page range as location counter
	 * @throws Pass2Exception
	 */
	public String pageRangeAndOffset(String lc, String address, String line) throws Pass2Exception {
		if (!canParseInt(address)) {
			throw new Pass2Exception("Line " + line + ": Invalid address value");
		}

		int add_val = parseAddress(address);

		if (add_val < -0x8000) {
			throw new Pass2Exception("Line " + line
					+ ": Address value is not within the specified range [#0 - #65535] or [0x0 - 0xFFFF]");
		}

		// Interpret the LC address as a hex
		int lc_val = (parseAddress("x" + lc) + 1) % 0x10000;

		int page_lc = (lc_val / 512) % 128;
		int page_address = (add_val / 512) % 128;

		if (page_lc != page_address) {
			throw new Pass2Exception(
					"Line " + line + ": Address value is not within the same page number as PC (PC at page: #" + page_lc
							+ ", Defined Address at page: #" + page_address + ")");

		}

		String s = Integer.toBinaryString(add_val);
		while (s.length() < 16) {
			s = "0" + s;
		}

		return s.substring(7, s.length());
	}

	/**
	 * Returns the opcode's value as a binary string.
	 * 
	 * @param opcode_value the decimal value for the machine instructions
	 * @return the binary string of the opcode value
	 */
	public String opcodeBinaryString(int opcode_value) {
		String value = Integer.toBinaryString(opcode_value);

		while (value.length() < 4) {
			value = "0" + value;
		}

		return value;
	}

	/**
	 * Returns the binary string version of the register value.
	 * 
	 * @param reg  the register value to be parsed
	 * @param line the current line of assembly code
	 * @return the register value as a binary string
	 * @throws Pass2Exception
	 */
	public String registerToBinaryString(String reg, String line) throws Pass2Exception {
		// Treat the register value as a assembler decimal
		if (!canParseInt(reg)) {
			throw new Pass2Exception("Line " + line + ": Invalid register value");
		}

		int reg_val = parseIndex(reg);
		if (reg_val < 0 || reg_val > 7) {
			throw new Pass2Exception("Line " + line + ": Register value not within range (decimal and hex: [0 - 7])");
		}

		String value = Integer.toBinaryString(reg_val);

		while (value.length() < 3) {
			value = "0" + value;
		}

		return value;
	}

	/**
	 * Parses the binary string into a 4 characters hex string.
	 * 
	 * @param binString the binary string
	 * @param line      the current line of assembly code
	 * @return the hex version of the entire, 16-bit, binary string
	 */
	public String convertBinaryToHexString(String binString, String line) {
		int bin_val = Integer.parseUnsignedInt(binString, 2);
		String hexValue = Integer.toHexString(bin_val).toUpperCase();

		while (hexValue.length() < 4) {
			hexValue = "0" + hexValue;
		}

		return hexValue;
	}

	/**
	 * Returns the binary string version of the immediate value.
	 * 
	 * @param imm  the immediate value to be parsed
	 * @param line the current line of assembly code
	 * @return the immediate value as a binary string
	 * @throws Pass2Exception
	 */
	public String immediateToBinaryString(String imm, String line) throws Pass2Exception {
		if (!canParseInt(imm)) {
			throw new Pass2Exception("Line " + line + ": Invalid immediate value");
		}

		int immediate = parseImmediate(imm);

		if (immediate < -0x8000) {
			throw new Pass2Exception(
					"Line " + line + ": Imm5 value is not within the specified range [#-16 - #15] or [0x0 - 0x1F]");
		}

		String s = Integer.toBinaryString(immediate);
		if (s.length() < 5) {
			while (s.length() < 5) {
				s = "0" + s;
			}
		} else {
			s = s.substring(s.length() - 5, s.length());
		}

		return s;
	}

	/**
	 * Returns the binary string version of the trapvect8 value.
	 * 
	 * @param trap the index value to be parsed
	 * @param line the current line of assembly code
	 * @return the trap vector value as a binary string
	 * @throws Pass2Exception
	 */
	public String trapToBinaryString(String trap, String line) throws Pass2Exception {
		if (!canParseInt(trap)) {
			throw new Pass2Exception("Line " + line + ": Invalid trapvect8 value");
		}

		int trap_vect = parseTrapvect(trap);

		if (trap_vect < -0x8000) {
			throw new Pass2Exception(
					"Line " + line + ": Trapvect8 value is not within the specified range [#0 - #255] or [0x0 - 0xFF]");
		}

		String s = Integer.toBinaryString(trap_vect);
		while (s.length() < 8) {
			s = "0" + s;
		}

		return s;
	}

	/**
	 * Returns the value of the literal as a hex string.
	 * 
	 * @param literalTable table containing information about the literal table
	 * @param address      the given address to compare (operand)
	 * @param line         the current line of assembly code
	 * @return the literal value as a hex string
	 * @throws Pass2Exception
	 */
	public String literalValue(Map<String, Integer> literalTable, String address, String line) throws Pass2Exception {
		if (!literalTable.containsKey(address)) {
			throw new Pass2Exception(
					"Line " + line + ": The literal " + address + " is not found in the literal table");
		}

		if (!isLiteral(address)) {
			throw new Pass2Exception("Line " + line
					+ ": The given literal value is not within range [#-32768 - #32767] or [0x0 - 0xFFFF]");
		}

		int litVal = literalTable.get(address);
		String litHex = Integer.toHexString(litVal);

		while (litHex.length() < 4) {
			litHex = "0" + litHex;
		}

		litHex = litHex.toUpperCase();
		litHex = "x" + litHex;
		return litHex;
	}

	/**
	 * Returns the hex version of the address in the String.
	 * 
	 * @param operand the operand containing the address
	 * @param line    the current line of assembly code
	 * @return the given address as a hex value
	 * @throws Pass2Exception
	 */
	public String addressToHex(String operand, String line) throws Pass2Exception {
		if (!canParseInt(operand)) {
			throw new Pass2Exception("Line " + line + ": Invalid address value");
		}

		int address = parseAddress(operand);

		if (address < -0x8000) {
			throw new Pass2Exception("Line " + line
					+ ": Address value is not within the specified range [#0 - #65535] or [0x0 - 0xFFFF]");
		}

		String s = Integer.toHexString(address).toUpperCase();
		while (s.length() < 4) {
			s = "0" + s;
		}

		return s;
	}

	/**
	 * Returns the hex version of the address in the String (specific for .FILL).
	 * 
	 * @param operand the operand containing the address
	 * @param line    the current line of assembly code
	 * @return the given address as a hex value (specific for .FILL)
	 * @throws Pass2Exception
	 */
	public String fillHexString(String operand, String line) throws Pass2Exception {
		if (!canParseInt(operand)) {
			throw new Pass2Exception("Line " + line + ": Invalid decimal/hex value for .FILL");
		}

		int address = parseUnionRange(operand);

		if (address < -0x8000) {
			throw new Pass2Exception("Line " + line + ": .FILL value not within range");
		}

		String s = Integer.toHexString(address);

		if (s.length() <= 4) {
			while (s.length() < 4) {
				s = "0" + s;
			}
		} else {
			s = s.substring(s.length() - 4, s.length());
		}

		return s.toUpperCase();
	}

	/**
	 * Returns the integer value if a given String can be parsed into an integer.
	 * 
	 * @param input potential string to be parsed into an integer value
	 * @return the parsed integer if within a specified range, -0xFFFF if not
	 *         possible
	 * @requires input.length() > 1
	 * @ensure parseInt(input) = Integer.parseInt(input) if within range, -0xFFFF
	 *         otherwise
	 */
	private int parseInt(String input) {
		int length = input.length();
		int index = input.length();

		char pos0 = input.charAt(ZERO);
		char pos1 = input.charAt(ONE);

		// When the String is a literal
		if (pos0 == '=' && length > TWO) {

			// When the value is hex or decimal
			if (pos1 == 'x') {
				return Integer.parseInt(input.substring(TWO, index), HEX);
			} else if (pos1 == '#') {
				return Integer.parseInt(input.substring(TWO, index), DECIMAL);
			}
		}

		// When the value is hex or decimal
		else if (pos0 == 'x') {
			return Integer.parseInt(input.substring(ONE, index), HEX);
		} else if (pos0 == '#') {
			return Integer.parseInt(input.substring(ONE, index), DECIMAL);
		}

		return INVALID_NUMBER;
	}

	/**
	 * Checks whether the integer can be parsed into a string, with the given
	 * base-representation is within range.
	 * 
	 * @param input the string containing the number to be parsed into an integer
	 * @param radix the base representation of the integer
	 * @return whether the string is in the range defined for radix ([0x0, 0xFFFF]
	 *         for base-16 and [-32768, 65535] for base-10)
	 * @ensures isInteger = Integer.parseInt(input) within range is true, false
	 *          otherwise
	 */
	private boolean isInteger(String input, int radix) {
		try {
			int val = Integer.parseInt(input, radix);

			// Hex must be positive in the abstract state machine
			if (radix == HEX) {
				return val >= ZERO && val <= ADDRESS_HEX_MAX;
			}

			return val >= WORD_MIN && val <= ADDRESS_DEC_MAX;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Checks whether the given input is a hex number.
	 * 
	 * @param input the potential string that is a hex value
	 * @param index the index position at which the character is denoted as a hex
	 *              ('x' character)
	 * @return if the string is a hex value or not
	 * @ensures isDecimal = input.charAt(index) is 'x' is true, false otherwise
	 */
	private boolean isHex(String input, int index) {
		return input.length() > index && input.charAt(index) == 'x';
	}

	/**
	 * Checks whether the given input is a decimal number.
	 * 
	 * @param input the potential string that is a decimal value
	 * @param index the index position at which the character is denoted as a
	 *              decimal ('#' character)
	 * @return if the string is a decimal value or not
	 * @ensures isDecimal = input.charAt(index) is '#' is true, false otherwise
	 */
	private boolean isDecimal(String input, int index) {
		return input.length() > index && input.charAt(index) == '#';
	}
}
