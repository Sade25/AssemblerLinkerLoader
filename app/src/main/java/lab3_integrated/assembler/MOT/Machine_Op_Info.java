package lab3_integrated.assembler.MOT;

public class Machine_Op_Info {
	/*
	 * Enumeration to store the mnemonic name of the instruction.
	 */
	private Machine_Op_Ins_Enum ins;

	/*
	 * Value for the given opcode.
	 */
	private int opcode;

	/*
	 * Length of the instruction.
	 */
	private int size;

	/**
	 * Default constructor to store the opcode value, word size, and specific format
	 * for a given machine instruction.
	 * 
	 * @param ins    the enumerated name of the instruction
	 * @param opcode the opcode's decimal value
	 * @param size   the word size of the instruction
	 * @param format the format of the given instruction
	 */
	public Machine_Op_Info(Machine_Op_Ins_Enum ins, int opcode, int size) {
		this.ins = ins;
		this.opcode = opcode;
		this.size = size;
	}

	/**
	 * Returns the enumerated version of the mnemonic op name.
	 * 
	 * @return mnemonic name
	 * @ensures getInstructionName = this.ins
	 */
	public Machine_Op_Ins_Enum getInstructionName() {
		return this.ins;
	}

	/**
	 * Returns the decimal opcode value.
	 * 
	 * @return opcode value
	 * @ensures getOpcode = this.opcode
	 */
	public int getOpcode() {
		return this.opcode;
	}

	/**
	 * Returns the decimal size value.
	 * 
	 * @return size value
	 * @ensures getSize = this.size
	 */
	public int getSize() {
		return this.size;
	}
}
