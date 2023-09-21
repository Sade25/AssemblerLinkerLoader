package lab3_integrated.assembler.POT;

import java.util.HashMap;

import lab3_integrated.assembler.Passes.String_Parser;

public class Pseudo_Op_Table {
	/*
	 * Constants for the instruction size.
	 */
	public static final int ZERO = 0; // Pseudo-Ops with size 0/variable length (initial value)
	public static final int ONE = 1; // Pseudo-Ops with size 1
	public static final int INVALID = -1; // Invalid values for when a requirement is not met

	/**
	 * Map to represent the machine op table.
	 */
	private HashMap<String, Pseudo_Op_Info> table;

	/**
	 * Parser specialized to deal with parsing immediates in the assembly language.
	 */
	private String_Parser sp;

	/**
	 * Default constructor.
	 */
	public Pseudo_Op_Table() {
		this.table = new HashMap<>();
		sp = new String_Parser();
		loadTable();
	}

	/**
	 * Loads the 6 Pseudo-Ops for the assembler to the Pseudo-Ops table, which
	 * contains the mnemonic name, size defined by the pseudo-ops behavior.
	 * 
	 * @ensures Pseudo_Op_Table = <"pseudo-op name", "size", <definite length,
	 *          variable length>>
	 */
	public void loadTable() {
		// Ops with size 0
		// Regular ops
		this.table.put(".ORIG", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.ORIG, ZERO, Pseudo_Op_Format.DEFINITE));
		this.table.put(".EQU", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.EQU, ZERO, Pseudo_Op_Format.DEFINITE));
		this.table.put(".END", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.END, ZERO, Pseudo_Op_Format.DEFINITE));

		// Linking/Loader ops
		this.table.put(".ENT", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.ENT, ZERO, Pseudo_Op_Format.DEFINITE));
		this.table.put(".EXT", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.EXT, ZERO, Pseudo_Op_Format.DEFINITE));

		// Ops with size 1
		this.table.put(".FILL", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.FILL, ONE, Pseudo_Op_Format.DEFINITE));

		// Ops with variable size length
		this.table.put(".BLKW", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.BLKW, ZERO, Pseudo_Op_Format.VARIABLE));
		this.table.put(".STRZ", new Pseudo_Op_Info(Pseudo_Op_Ins_Enum.STRZ, ZERO, Pseudo_Op_Format.VARIABLE));
	}

	/**
	 * Returns if the given instruction is in the Pseudo-Ops Table
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return the boolean value for if the given instruction is in the Pseudo-Ops
	 *         Table or not
	 * @ensures containsOp = Pseudo_Op_Table contains key (true), or false
	 */
	public boolean containsOp(String key) {
		return this.table.containsKey(key);
	}

	/**
	 * Returns the enumerated version of the instruction name, or null if it is
	 * invalid.
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return enumerated version of the key or null for invalid instructions
	 * @ensures getInstructionName = enum_name(key) or null
	 */
	public Pseudo_Op_Ins_Enum getInstructionName(String key) {
		if (!this.table.containsKey(key)) {
			return null;
		}

		Pseudo_Op_Info op = this.table.get(key);
		return op.getInstructionName();
	}

	/**
	 * Returns the word size for the instruction, or -1 if the instruction is
	 * invalid.
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return word size of the key instruction or -1 if the instruction is invalid
	 * @ensures getLength = word_size(key) or -1
	 */
	public int getLength(String key) {
		if (!this.table.containsKey(key)) {
			return INVALID;
		}

		Pseudo_Op_Info op = this.table.get(key);
		return op.getLength();
	}

	/**
	 * Returns the format of the instruction, or null if the instruction is invalid.
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return format of the key instruction or null if the instruction is invalid
	 * @ensures getFormat = format(key) or null
	 */
	public Pseudo_Op_Format getFormat(String key) {
		if (!this.table.containsKey(key)) {
			return null;
		}

		Pseudo_Op_Info op = this.table.get(key);
		return op.getFormat();
	}

	/**
	 * Returns the size for the block of words to be allocated based on the operand
	 * size.
	 * 
	 * @param str operand that contains the numerical value of the block of words
	 * @return the number of words to allocate based on the operand, -1 if not valid
	 * @ensures blockLength = numerical_length(str) or -1
	 */
	public int blockLength(String str) {
		// If the block can be parsed
		if (!sp.canParseInt(str)) {
			return INVALID;
		}

		int val = sp.parseAddress(str);

		// If the block value is <= 0
		if (val < ONE) {
			return INVALID;
		}

		return val;
	}
}
