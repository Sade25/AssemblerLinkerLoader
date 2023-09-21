package lab3_integrated.simulator.machine;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * 
 * @author Jeremy Bogen
 * @author Giridhar Srikanth
 *
 */
public class Machine implements Machine_Interface {
	/**
	 * Variable for the name of the machine.
	 */
	public String name;

	/**
	 * Variable to hold the address of the next instruction.
	 */
	public int PC;

	/**
	 * Variable that hold the address of the previous instruction held by the PC.
	 */
	public int prev_pc;

	/**
	 * Array that holds each of the condition code variables: N: if the register
	 * value is negative Z: if the register value is zero P: if the register value
	 * is positive.
	 */
	public boolean[] ccr;

	/**
	 * Array that holds the register from R0 to R7.
	 */
	public short[] registers;

	/**
	 * Two-Dimensional array that holds the memory of the machine that has 128
	 * pages, each containing 512 words, which are 16-bits long.
	 */
	public short[][] memory;

	/**
	 * Constants for size declared as final variables (ccr, register, word, pages,
	 * and unsigned short sizes).
	 */
	private final int CCR_SIZE = 3;
	private final int REGISTER_SIZE = 8;
	private final int WORD_SIZE = 512;
	private final int PAGE_SIZE = 128;
	private final int UPPER_LIMIR = 0x10000;

	/**
	 * Constants for integers declared as final variables.
	 */
	private final int ZERO = 0;
	private final int ONE = 1;
	private final int TWO = 2;
	private final int SEVEN = 7;
	private final int NINE = 9;
	private final int MSB_FIVE_BIT = 0x10;
	private final int ZERO_SIGN_EXTEND = 0b0000000000011111;
	private final int ONE_SIGN_EXTEND = 0b1111111111100000;

	/**
	 * Constants for the lower 8 bits of the TRAP instructors as final variables.
	 */
	private final int OUT_VECT = 0x21;
	private final int PUTS_VECT = 0x22;
	private final int IN_VECT = 0x23;
	private final int HALT_VECT = 0x25;
	private final int OUTN_VECT = 0x31;
	private final int INN_VECT = 0x33;
	private final int RND_VECT = 0x43;

	/**
	 * Constants for sub-instructions in TRAP
	 */
	private final int LOWER_BITS = 0xff;

	/**
	 * Default constructor.
	 */
	public Machine() {
		this.createNewRep();
	}

	/**
	 * Constructor with the name as the parameter
	 * 
	 * @param name contains the name of the machine
	 */
	public Machine(String name) {
		this.createNewRep();
		this.name = name;
	}

	/**
	 * Private representation of the constructor and initializing all the variables.
	 */
	private void createNewRep() {
		// Initializing the name of the machine
		this.name = "MachineName";

		// Initializing the program counter(s)
		this.PC = 0x0;
		this.prev_pc = this.PC;

		// Initializing the condition-code registers
		this.ccr = new boolean[CCR_SIZE];
		for (int i = 0; i < CCR_SIZE; i++) {
			this.ccr[i] = false;
		}

		// Initializing the registers
		this.registers = new short[REGISTER_SIZE];
		for (int i = 0; i < REGISTER_SIZE; i++) {
			this.registers[i] = (short) 0x0;
		}

		// Initializing memory
		this.memory = new short[PAGE_SIZE][WORD_SIZE];
		for (int i = 0; i < PAGE_SIZE; i++) {
			for (int j = 0; j < WORD_SIZE; j++) {
				this.memory[i][j] = (short) 0x0;
			}
		}
	}

	@Override
	public int getPC() {
		this.updatePC();
		return this.prev_pc;
	}

	/**
	 * Updates the program counter to the hold the address of the next instruction.
	 * 
	 * @updates PC
	 * @ensures PC = address of(#PC + 1)
	 */
	private void updatePC() {
		this.prev_pc = this.PC;

		// Making sure the PC is within bounds of memory size
		this.PC = (this.PC + 1) % UPPER_LIMIR;
	}

	@Override
	public void setPC(int start) {
		this.PC = start % UPPER_LIMIR;
	}

	@Override
	public int getMemoryPageLocation(int address) {
		return (address / WORD_SIZE) % PAGE_SIZE;
	}

	@Override
	public int getMemoryWordLocation(int address) {
		return address % WORD_SIZE;
	}

	@Override
	public void ADD_REG(int DR, int SR1, int SR2) {
		// Performing the method instruction
		short result = (short) (this.registers[SR1] + this.registers[SR2]);
		this.registers[DR] = result;
		updateCCR(result);

	}

	@Override
	public void ADD_IMM(int DR, int SR, int imm5) {
		// Performing the method instruction
		short immediate = this.signExtend(imm5);
		short result = (short) (this.registers[SR] + immediate);
		this.registers[DR] = result;
		updateCCR(result);

	}

	@Override
	public void AND_REG(int DR, int SR1, int SR2) {
		// Performing the method instruction
		short result = (short) (this.registers[SR1] & this.registers[SR2]);
		this.registers[DR] = result;
		updateCCR(result);

	}

	@Override
	public void AND_IMM(int DR, int SR, int imm5) {
		// Performing the method instruction
		short immediate = this.signExtend(imm5);
		short result = (short) (this.registers[SR] & immediate);
		this.registers[DR] = result;
		updateCCR(result);

	}

	@Override
	public void BRx(int n, int z, int p, int pgoffset9) {
		// Performing the method instruction
		boolean check = (n == 1 && this.ccr[ZERO]) || (z == 1 && this.ccr[ONE]) || (p == 1 && this.ccr[TWO]);
		if (check) {
			int address = this.getAddressPC(this.PC, pgoffset9);
			this.PC = address;
		}

	}

	@Override
	public void DBUG() {
		/* Outputting is done by the simulator */
	}

	@Override
	public void JSR(int L, int pgoffset9) {
		// Performing the method instruction
		int address = this.getAddressPC(this.PC, pgoffset9);
		if (L == 1) {
			this.savePC();
		}
		this.PC = address;

	}

	@Override
	public void JSRR(int L, int BaseR, int index6) {
		// Performing the method instruction
		int address = this.getAddressReg(BaseR, index6);
		if (L == 1) {
			this.savePC();
		}
		this.PC = address;

	}

	@Override
	public void LD(int DR, int pgoffset9) {
		// Performing the method instruction
		int address = this.getAddressPC(this.PC, pgoffset9);
		int row = this.getMemoryPageLocation(address);
		int col = this.getMemoryWordLocation(address);

		short op_add = this.memory[row][col];
		this.registers[DR] = op_add;
		this.updateCCR(op_add);

	}

	@Override
	public void LDI(int DR, int pgoffset9) {
		// Performing the method instruction
		int address = this.getAddressPC(this.PC, pgoffset9);
		int r = this.getMemoryPageLocation(address);
		int c = this.getMemoryWordLocation(address);

		short new_address = this.memory[r][c];
		int u_new_address = Short.toUnsignedInt(new_address);
		int row = this.getMemoryPageLocation(u_new_address);
		int col = this.getMemoryWordLocation(u_new_address);

		short op_address = this.memory[row][col];
		this.registers[DR] = op_address;
		this.updateCCR(op_address);

	}

	@Override
	public void LDR(int DR, int BaseR, int index6) {
		// Performing the method instruction
		int address = this.getAddressReg(BaseR, index6);
		int row = this.getMemoryPageLocation(address);
		int col = this.getMemoryWordLocation(address);

		short op_add = this.memory[row][col];
		this.registers[DR] = op_add;
		this.updateCCR(op_add);

	}

	@Override
	public void LEA(int DR, int pgoffset9) {
		// Performing the method instruction
		int address = this.getAddressPC(this.PC, pgoffset9);
		short op_add = (short) address;
		this.registers[DR] = op_add;
		this.updateCCR(op_add);

	}

	@Override
	public void NOT(int DR, int SR) {
		// Performing the method instruction
		short result = (short) (~this.registers[SR]);
		this.registers[DR] = result;
		updateCCR(result);

	}

	@Override
	public void RET() {
		// Performing the method instruction
		this.PC = this.registers[SEVEN];
	}

	@Override
	public void ST(int SR, int pgoffset9) {
		// Performing the method instruction
		int address = this.getAddressPC(this.PC, pgoffset9);
		int row = this.getMemoryPageLocation(address);
		int col = this.getMemoryWordLocation(address);

		this.memory[row][col] = this.registers[SR];

	}

	@Override
	public void STI(int SR, int pgoffset9) {
		// Performing the method instruction
		int address = this.getAddressPC(this.PC, pgoffset9);
		int r = this.getMemoryPageLocation(address);
		int c = this.getMemoryWordLocation(address);

		short new_address = this.memory[r][c];
		int u_new_address = Short.toUnsignedInt(new_address);
		int row = this.getMemoryPageLocation(u_new_address);
		int col = this.getMemoryWordLocation(u_new_address);

		this.memory[row][col] = this.registers[SR];

	}

	@Override
	public void STR(int SR, int BaseR, int index6) {
		// Performing the method instruction
		int address = this.getAddressReg(BaseR, index6);
		int row = this.getMemoryPageLocation(address);
		int col = this.getMemoryWordLocation(address);

		this.memory[row][col] = this.registers[SR];

	}

	@Override
	public boolean TRAP(int trapvec8, Scanner in) {
		boolean end = false;

		// Performing the method instruction
		switch (trapvec8) {
		case OUT_VECT:
			this.OUT();
			break;
		case PUTS_VECT:
			this.PUTS();
			break;
		case IN_VECT:
			this.IN(in);
			break;
		case HALT_VECT:
			this.HALT();
			end = true;
			break;
		case OUTN_VECT:
			this.OUTN();
			break;
		case INN_VECT:
			this.INN(in);
			break;
		case RND_VECT:
			this.RND();
			break;
		default:
			this.defaultCase(trapvec8);
			break;
		}

		this.savePC();

		return end;
	}

	/**
	 * Displays the character formed from the lower 8 bits of R0
	 */
	private void OUT() {
		char char_val = (char) (this.registers[ZERO] & LOWER_BITS);
		System.out.println("\nThe character formed in R0 in ASCII: " + char_val + "\n");
	}

	/**
	 * Printing the characters (string) from the given address in the registers,
	 * till the end of memory.
	 */
	private void PUTS() {
		int address = Short.toUnsignedInt(this.registers[ZERO]);
		char char_val;
		System.out.print("\nThe null-terminated String formed from the address in R0 in ASCII: ");

		short more_char = memory[getMemoryPageLocation(address)][getMemoryWordLocation(address)];
		// System.out.println(more_char);
		while (more_char != 0) {

			// Get the address in memory
			int temp_address = Short.toUnsignedInt(more_char);
			// System.out.println(address + " " + more_char);
			char_val = (char) (temp_address & LOWER_BITS);
			// System.out.println(address + " " + char_val);

			// Print the character
			System.out.print(char_val);

			// Update to the next address
			address = (address + 1) % this.UPPER_LIMIR;
			more_char = memory[getMemoryPageLocation(address)][getMemoryWordLocation(address)];
			// System.out.println(address + " " + more_char);
		}

		System.out.println("\n");
	}

	/**
	 * Getting user input on the character to be stored in R0 and clearing out the
	 * upper bits after storing the character.
	 */
	private void IN(Scanner in) {
		// Getting the character input
		System.out.print("\nEnter a character: ");
		char input = in.next().charAt(0);
		System.out.println();

		// Putting the input into the register and zero'ing out the upper bits
		this.registers[ZERO] = (short) input;
		this.registers[ZERO] &= LOWER_BITS;
		this.updateCCR(this.registers[ZERO]);
	}

	/**
	 * Displays a message saying execution is halted.
	 */
	private void HALT() {
		System.out.println("\nA HALT instruction was encountered. Terminating executing instructions.\n");
	}

	/**
	 * Displaying the current value stored in R0 as a decimal.
	 */
	private void OUTN() {
		System.out.println("\nThe current value in R0 in decimal: " + this.registers[ZERO] + "\n");
	}

	/**
	 * Get the user input on any value within the range of signed short and store it
	 * into R0.
	 */
	private void INN(Scanner in) {
		// Specifying the rules for input
		System.out.println("\nRequirments for input");
		System.out.println("\t1. Must be written as a decimal number");
		System.out.println("\t2. Must be within the range of [" + Short.MIN_VALUE + ", " + Short.MAX_VALUE + "]\n");
		int input = Short.MIN_VALUE - 1;

		// Trying to parse the proper input
		do {
			try {
				System.out.print("Enter a valid input adhering to the rules: ");
				input = in.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input.");
			}

			// Clearing the buffer
			in.nextLine();
		} while (input < Short.MIN_VALUE || input > Short.MAX_VALUE);

		System.out.println("\nValue of the given input: " + input + "\n");

		// Updating the necessary registers and CCRs
		short val = (short) input;
		this.registers[ZERO] = val;
		this.updateCCR(val);
	}

	/**
	 * Assigning a random value to R0.
	 */
	private void RND() {
		// Calculating random value
		short min = Short.MIN_VALUE;
		short max = Short.MAX_VALUE;
		short random_short = (short) (Math.random() * (max - min + 1) + min);

		this.registers[ZERO] = random_short;
		this.updateCCR(random_short);

	}

	/**
	 * Outputs saying the given trap vector is invalid.
	 * 
	 * @param val invalid trap vector
	 */
	private void defaultCase(int val) {
		System.err.println("\nThe given entry (" + val + ") does not match a value from the TRAP vector table\n");
	}

	/**
	 * Updates the CCR value based on the values modified in the registers.
	 * 
	 * @param num the short stored in a register
	 * @updates ccr
	 * @ensures sets the correct CCRs based on the passed short value
	 */
	private void updateCCR(short num) {
		// Setting the N flag (negative number) and others to false
		if (num < 0) {
			// True case
			this.ccr[ZERO] = true;

			// False case(s)
			this.ccr[ONE] = false;
			this.ccr[TWO] = false;
		}

		// Setting the Z flag (zero number) and others to false
		else if (num == 0) {
			// True case
			this.ccr[ONE] = true;

			// False case(s)
			this.ccr[ZERO] = false;
			this.ccr[TWO] = false;
		}

		// Setting the P flag (positive number) and others to false
		else {
			// True case
			this.ccr[TWO] = true;

			// False case(s)
			this.ccr[ZERO] = false;
			this.ccr[ONE] = false;
		}
	}

	/**
	 * Forming an address from the PC and offset.
	 * 
	 * @param PC     program counter
	 * @param offset the lower bits to be added to the PC
	 * @return the address formed from concatenating the bits of PC and offset
	 * @ensures address = 7 bits of PC + offset (9 bits)
	 */
	public int getAddressPC(int pc, int offset) {
		int upper_half = pc >> NINE;
		int address = (upper_half << NINE) + offset;
		return address % UPPER_LIMIR;
	}

	/**
	 * Forming an address from the base register and offset.
	 * 
	 * @param r     register
	 * @param index the value to be added to the register
	 * @return the address formed from adding the offset to the specified register's
	 *         value
	 * @ensures address = register[r] + index
	 */
	public int getAddressReg(int r, int index) {
		short rval = this.registers[r];
		int u_rval = Short.toUnsignedInt(rval);
		int address = u_rval + index;
		return address % UPPER_LIMIR;
	}

	/**
	 * Signs extends a 5-bit number into a 16 bits quantity.
	 * 
	 * @param imm5 the immediate value to be sign extended
	 * @return the sign-extended 16-bit
	 * @ensures signExtend = 16-bit sign-extension of imm5 based on the MSB
	 */
	private short signExtend(int imm5) {
		int msb = MSB_FIVE_BIT & imm5;
		short result;

		if (msb == ZERO) {
			result = (short) (ZERO_SIGN_EXTEND & imm5);
		} else {
			result = (short) (ONE_SIGN_EXTEND | imm5);
		}

		return result;
	}

	/**
	 * Copying the current instruction address PC points to into R7.
	 */
	private void savePC() {
		this.registers[SEVEN] = (short) this.PC;
	}
}
