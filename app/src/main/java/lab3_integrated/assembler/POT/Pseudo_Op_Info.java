package lab3_integrated.assembler.POT;

public class Pseudo_Op_Info {
	/*
	 * Enumeration to store the mnemonic name of the Pseudo-op.
	 */
	private Pseudo_Op_Ins_Enum ins;

	/*
	 * Length of the instruction.
	 */
	private int length;

	/*
	 * Enumeration to store the type of format (page offset, index offset, or none).
	 */
	private Pseudo_Op_Format format;

	/**
	 * Default constructor to store the length and the format for a given Pseudo-op.
	 * 
	 * @param ins    enumerated name of the pseudo-op
	 * @param length definite/variable size for the pseudo-op
	 * @param format format in the type of format to define the length of the
	 *               pseudo-op
	 */
	public Pseudo_Op_Info(Pseudo_Op_Ins_Enum ins, int length, Pseudo_Op_Format format) {
		this.ins = ins;
		this.length = length;
		this.format = format;
	}

	/**
	 * Returns the enumerated version of the mnemonic op name.
	 * 
	 * @return mnemonic name
	 * @ensures getInstructionName = this.ins
	 */
	public Pseudo_Op_Ins_Enum getInstructionName() {
		return this.ins;
	}

	/**
	 * Returns the decimal size value.
	 * 
	 * @return size value
	 * @ensures getSize = this.length
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Returns the format specified under the enumeration.
	 * 
	 * @return format value
	 * @ensures getFormat = this.format
	 */
	public Pseudo_Op_Format getFormat() {
		return this.format;
	}
}
