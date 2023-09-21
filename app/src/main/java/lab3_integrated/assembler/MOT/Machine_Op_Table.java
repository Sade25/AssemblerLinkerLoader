package lab3_integrated.assembler.MOT;

import java.util.HashMap;

public class Machine_Op_Table {
	/**
	 * Constants for the opcode values of each instructions.
	 */
	public static final int BRX_OPCODE = 0; // Opcode value for the BRx instruction
	public static final int ADD_OPCODE = 1; // Opcode value for the ADD instruction
	public static final int LD_OPCODE = 2; // Opcode value for the LD instruction
	public static final int ST_OPCODE = 3; // Opcode value for the ST instruction
	public static final int JSR_OPCODE = 4; // Opcode value for the JSR instruction
	public static final int AND_OPCODE = 5; // Opcode value for the AND instruction
	public static final int LDR_OPCODE = 6; // Opcode value for the LDR instruction
	public static final int STR_OPCODE = 7; // Opcode value for the STR instruction
	public static final int DBUG_OPCODE = 8; // Opcode value for the DBUG instruction
	public static final int NOT_OPCODE = 9; // Opcode value for the NOT instruction
	public static final int LDI_OPCODE = 10; // Opcode value for the LDI instruction
	public static final int STI_OPCODE = 11; // Opcode value for the STI instruction
	public static final int JSRR_OPCODE = 12; // Opcode value for the JSRR instruction
	public static final int RET_OPCODE = 13; // Opcode value for the RET instruction
	public static final int LEA_OPCODE = 14; // Opcode value for the LEA instruction
	public static final int TRAP_OPCODE = 15; // Opcode value for the TRAP instruction

	/*
	 * Constants for the instruction size.
	 */
	public static final int ONE = 1; // Size of each opcode
	public static final int INVALID = -1; // Invalid values for when a requirement is not met

	/**
	 * Map to represent the machine op table.
	 */
	private HashMap<String, Machine_Op_Info> table;

	/**
	 * Default constructor.
	 */
	public Machine_Op_Table() {
		this.table = new HashMap<>();
		loadTable();
	}

	/**
	 * Loads the 16 machine instructions into the Machine-Ops Table with their size,
	 * opcode value, and format they are stored as (page offset, index, or none).
	 * 
	 * @ensures Machine_Op_Table = <"opcode name", <value, size, format>>, for all
	 *          instructions
	 */
	public void loadTable() {
		// Data processing instructions
		this.table.put("ADD", new Machine_Op_Info(Machine_Op_Ins_Enum.ADD, ADD_OPCODE, ONE));
		this.table.put("AND", new Machine_Op_Info(Machine_Op_Ins_Enum.AND, AND_OPCODE, ONE));
		this.table.put("NOT", new Machine_Op_Info(Machine_Op_Ins_Enum.NOT, NOT_OPCODE, ONE));

		// Data movement instructions: Load instructions
		this.table.put("LD", new Machine_Op_Info(Machine_Op_Ins_Enum.LD, LD_OPCODE, ONE));
		this.table.put("LDI",
				new Machine_Op_Info(Machine_Op_Ins_Enum.LDI, LDI_OPCODE, ONE));
		this.table.put("LDR", new Machine_Op_Info(Machine_Op_Ins_Enum.LDR, LDR_OPCODE, ONE));
		this.table.put("LEA",
				new Machine_Op_Info(Machine_Op_Ins_Enum.LEA, LEA_OPCODE, ONE));

		// Data movement instructions: Store instructions
		this.table.put("ST", new Machine_Op_Info(Machine_Op_Ins_Enum.ST, ST_OPCODE, ONE));
		this.table.put("STI",
				new Machine_Op_Info(Machine_Op_Ins_Enum.STI, STI_OPCODE, ONE));
		this.table.put("STR", new Machine_Op_Info(Machine_Op_Ins_Enum.STR, STR_OPCODE, ONE));

		// Flow of control instructions: Branching (all valid combinations)
		this.table.put("BR", new Machine_Op_Info(Machine_Op_Ins_Enum.BR, BRX_OPCODE, ONE));
		this.table.put("BRN",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRN, BRX_OPCODE, ONE));
		this.table.put("BRZ",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRZ, BRX_OPCODE, ONE));
		this.table.put("BRP",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRP, BRX_OPCODE, ONE));
		this.table.put("BRNZ",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRNZ, BRX_OPCODE, ONE));
		this.table.put("BRNP",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRNP, BRX_OPCODE, ONE));
		this.table.put("BRZP",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRZP, BRX_OPCODE, ONE));
		this.table.put("BRNZP",
				new Machine_Op_Info(Machine_Op_Ins_Enum.BRNZP, BRX_OPCODE, ONE));

		// Flow of control instructions: Jumping
		this.table.put("JSR",
				new Machine_Op_Info(Machine_Op_Ins_Enum.JSR, JSR_OPCODE, ONE));
		this.table.put("JMP",
				new Machine_Op_Info(Machine_Op_Ins_Enum.JMP, JSR_OPCODE, ONE));
		this.table.put("JSRR",
				new Machine_Op_Info(Machine_Op_Ins_Enum.JSRR, JSRR_OPCODE, ONE));
		this.table.put("JMPR",
				new Machine_Op_Info(Machine_Op_Ins_Enum.JMPR, JSRR_OPCODE, ONE));

		// Flow of control instructions: General
		this.table.put("RET", new Machine_Op_Info(Machine_Op_Ins_Enum.RET, RET_OPCODE, ONE));
		this.table.put("TRAP", new Machine_Op_Info(Machine_Op_Ins_Enum.TRAP, TRAP_OPCODE, ONE));

		// Miscellaneous instructions
		this.table.put("DBUG", new Machine_Op_Info(Machine_Op_Ins_Enum.DBUG, DBUG_OPCODE, ONE));
	}

	/**
	 * Returns the word size for the instruction, or -1 if the instruction is
	 * invalid.
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return word size of the key instruction or -1 if the instruction is invalid
	 * @ensures getSize = word_size(key) or -1
	 */
	public int getSize(String key) {
		if (!this.table.containsKey(key)) {
			return INVALID;
		}

		Machine_Op_Info op = this.table.get(key);
		return op.getSize();
	}

	/**
	 * Returns if the given instruction is in the Machine-Ops Table
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return the boolean value for if the given instruction is in the Machine-Ops
	 *         Table or not
	 * @ensures containsOp = Machine_Op_Table contains key (true), or false
	 */
	public boolean containsOp(String key) {
		return this.table.containsKey(key);
	}

	/**
	 * Returns the value for the instruction, or -1 if the instruction is invalid.
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return opcode value of the key instruction or -1 if the instruction is
	 *         invalid
	 * @ensures getOpcode = opcode_value(key) or -1
	 */
	public int getOpcode(String key) {
		if (!this.table.containsKey(key)) {
			return INVALID;
		}

		Machine_Op_Info op = this.table.get(key);
		return op.getOpcode();
	}

	/**
	 * Returns the enumerated version of the instruction name, or null if it is
	 * invalid.
	 * 
	 * @param key Mnemonic name for the instruction
	 * @return enumerated version of the key or null for invalid instructions
	 * @ensures getInstructionName = enum_name(key) or null
	 */
	public Machine_Op_Ins_Enum getInstructionName(String key) {
		if (!this.table.containsKey(key)) {
			return null;
		}

		Machine_Op_Info ins = this.table.get(key);
		return ins.getInstructionName();
	}
}
