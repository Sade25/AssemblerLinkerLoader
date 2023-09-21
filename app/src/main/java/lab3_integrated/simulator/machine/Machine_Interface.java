package lab3_integrated.simulator.machine;

import java.util.Scanner;

/**
 * 
 * @author Giridhar Srikanth
 *
 */
public interface Machine_Interface {

	/**
	 * Returns the address of the current instruction PC is pointing to and updates
	 * the PC to point to the next instruction.
	 * 
	 * @return address PC is pointing to
	 * @updates PC
	 * @ensures getPC = address of(PC) and PC = address of(#PC + 1)
	 */
	public int getPC();

	/**
	 * Sets the PC to address specified by the parameter to start execution.
	 * 
	 * @param address address to start execution
	 * @ensures PC = address
	 */
	public void setPC(int address);

	/**
	 * Returns the page number with relation to the address (row number).
	 * 
	 * @param address the address for a given instruction
	 * @return the page in relation to the address
	 * @ensures getMemoryPageLocation = page_number(address)
	 */
	public int getMemoryPageLocation(int address);

	/**
	 * Returns the page number with relation to the address (column number).
	 * 
	 * @param address the address for a given instruction
	 * @return the word in relation to the address
	 * @ensures getMemoryWordLocation = word_number(address)
	 **/
	public int getMemoryWordLocation(int address);

	/**
	 * Adds the value from source register 1 and source register 2 and stores it in
	 * the destination register.
	 * 
	 * @param DR  destination register
	 * @param SR1 source register 1
	 * @param SR2 source register 2
	 * @updates register, CCRs
	 * @ensures register[DR] = register[SR1] + register[SR2] and CCRs are updated
	 *          properly
	 */
	public void ADD_REG(int DR, int SR1, int SR2);

	/**
	 * Adds the value from the source register and the immediate value and stores it
	 * in the destination register.
	 * 
	 * @param DR   destination register
	 * @param SR   source register
	 * @param imm5 immediate
	 * @updates register, CCRs
	 * @ensures register[DR] = register[SR] + imm5 and CCRs are updated properly
	 */
	public void ADD_IMM(int DR, int SR, int imm5);

	/**
	 * Bitwise ands from source register 1 and source register 2 and stores it in
	 * the destination register.
	 * 
	 * @param DR  destination register
	 * @param SR1 source register 1
	 * @param SR2 source register 2
	 * @updates register, CCRs
	 * @ensures register[DR] = register[SR1] & register[SR2] and CCRs are updated
	 *          properly
	 * 
	 */
	public void AND_REG(int DR, int SR1, int SR2);

	/**
	 * Bitwise ands from the source register and the immediate value and stores it
	 * in the destination register.
	 * 
	 * @param DR   destination register
	 * @param SR   source register
	 * @param imm5 immediate
	 * @updates register, CCRs
	 * @ensures register[DR] = register[SR] & imm5 and CCRs are updated properly
	 */
	public void AND_IMM(int DR, int SR, int imm5);

	/**
	 * Conditionally branches (address formed from the upper 7 bits of the PC and
	 * lower 9 bits of the given instruction) based on whether the bits set for the
	 * CCR from the instruction match the bits sets for the current CCR values.
	 * 
	 * @param n         bit for negative flag
	 * @param z         bit for zero flag
	 * @param p         bit for positive flag
	 * @param pgoffset9 bits of the instruction
	 * @updates PC
	 * @ensure if CCRs match, then PC = address of the operand(upper 7 bits of PC +
	 *         pgoffset9)
	 */
	public void BRx(int n, int z, int p, int pgoffset9);

	/**
	 * Prints to the screen the state of the machine (PC, registers, and CCRs).
	 */
	public void DBUG();

	/**
	 * Takes a jump (address formed from the upper 7 bits of the PC and lower 9 bits
	 * of the given instruction) and saves the previous instruction's address to
	 * register R7 if taking a conditional jump.
	 * 
	 * @param L         bit to determine if conditional jump or unconditional jump
	 * @param pgoffset9 bits of the instruction
	 * @updates PC
	 * @ensures PC = address of the operand(upper 7 bits of PC + pgoffset9) and
	 *          saves previous instruction into R7 if L bit is set to 1
	 */
	public void JSR(int L, int pgoffset9);

	/**
	 * Takes a jump (address formed by adding the 6-bit offset to the value stored
	 * in the base register) and saves the previous instruction's address to
	 * register R7 if taking a conditional jump.
	 * 
	 * @param L         bit to determine if conditional jump or unconditional jump
	 * @param pgoffset9 bits of the instruction
	 * @updates PC
	 * @ensures PC = address of the operand(register[BaseR] + index6) and saves
	 *          previous instruction into R7 if L bit is set to 1
	 */
	public void JSRR(int L, int BaseR, int index6);

	/**
	 * Gets the address of the operand (formed from the upper 7 bits of the PC and
	 * lower 9 bits of the given instruction), retrieves the instruction from
	 * memory, and loads it into the destination register.
	 * 
	 * @param DR        destination register
	 * @param pgoffset9 bits of the instruction
	 * @updates registers, CCRs
	 * @ensures register[DR] = memory[address of the operand(upper 7 bits of PC +
	 *          pgoffset9)] and CCRs are updated properly
	 */
	public void LD(int DR, int pgoffset9);

	/**
	 * Gets the address of the address of the operand (formed from the upper 7 bits
	 * of the PC and lower 9 bits of the given instruction), retrieves the address
	 * of the operand from memory, goes to memory again to retrieve the instruction,
	 * and then loads it into the destination register.
	 * 
	 * @param DR        destination register
	 * @param pgoffset9 bits of the instruction
	 * @updates registers, CCRs
	 * @ensures register[DR] = memory[memory[address of the operand(upper 7 bits of
	 *          PC + pgoffset9)]] and CCRs are updated properly
	 */
	public void LDI(int DR, int pgoffset9);

	/**
	 * Gets the address of the operand (formed by adding the 6-bit offset to the
	 * value stored in the base register), goes to memory, and store it into the
	 * destination register.
	 * 
	 * @param DR     destination register
	 * @param BaseR  base register with operand address
	 * @param index6 6-bit offset to be added onto the base register
	 * @updates registers, CCRs
	 * @ensures register[DR] = memory[address of the operand(register[BaseR] +
	 *          index6)]
	 */
	public void LDR(int DR, int BaseR, int index6);

	/**
	 * Using math to compute the address formed from the upper 7 bits of the PC and
	 * the 9 bits from the given instruction.
	 * 
	 * @param DR        destination register
	 * @param pgoffset9 bits of the instruction
	 * @updates register, CCRs
	 * @ensures register[DR] = address formed from(upper 7 bits of PC + pgoffset9)
	 *          and CCRs are updated properly
	 */
	public void LEA(int DR, int pgoffset9);

	/**
	 * Finds the bitwise compliment (2s complement) of the source register and
	 * stores it in the destination register.
	 * 
	 * @param DR destination register
	 * @param SR source register
	 * @updates register, CCRs
	 * @ensures register[DR] = ~register[SR] and CCRs are updated properly
	 */
	public void NOT(int DR, int SR);

	/**
	 * Copies the contents from R7 onto PC to return back from the subroutine.
	 * 
	 * @updates PC
	 * @ensure PC = register[7]
	 */
	public void RET();

	/**
	 * Loads the value from the source register into memory, given by the address of
	 * operand (formed from the upper 7 bits of the PC and lower 9 bits of the given
	 * instruction).
	 * 
	 * @param SR        source register
	 * @param pgoffset9 bits of the instruction
	 * @updates memory
	 * @ensures memory[address of the operand(upper 7 bits of PC + pgoffset9)] =
	 *          register[SR]
	 */
	public void ST(int SR, int pgoffset9);

	/**
	 * Loads the value from the source register into memory specified by the address
	 * of the operand stored in memory given by the address of operand (formed from
	 * the upper 7 bits of the PC and lower 9 bits of the given instruction).
	 * 
	 * @param SR        source register
	 * @param pgoffset9 bits of the instruction
	 * @updates memory
	 * @ensures memory[memory[address of the operand(upper 7 bits of PC +
	 *          pgoffset9)]] = register[SR]
	 */
	public void STI(int SR, int pgoffset9);

	/**
	 * Loads the value from the source register to memory at the the address of the
	 * operand (formed by adding the 6-bit offset to the value stored in the base
	 * register).
	 * 
	 * @param SR     source register
	 * @param BaseR  base register with operand address
	 * @param index6 6-bit offset to be added onto the base register
	 * @updates memory
	 * @ensures memory[address of the operand(register[BaseR] + index6)] =
	 *          register[SR]
	 */
	public void STR(int SR, int BaseR, int index6);

	/**
	 * Executes a system call based on the value provided by lower 8 bits of the
	 * TRAP instruction. Below is a list of systems calls that can occur based on
	 * the value (in hexadecimal) given by the lower 8 bits:
	 * Executes a system call based on the value provided by lower 8 bits of the
	 * TRAP instruction. Below is a list of systems calls that can occur based on
	 * the value (in hexadecimal) given by the lower 8 bits:
	 * 0x21: output the character formed by the lower 8 bits of R0
	 * 0x22: output the null-terminated string of R0 
	 * 0x23: input a character and zero-extend that to R0
	 * 0x25: stopping execution and printing the message to the screen
	 * 0x31: output the value in R0 as a decimal (base 10 number system)
	 * 0x33: input a decimal value and put it into R0
	 * 0x43: store a random random in R0
	 * 
	 * @param trapvec8 determine which system call to execute
	 * @return boolean stating whether the TRAP instruction made a system call to
	 *         HALT instruction flow
	 * @updates registers, CCRs
	 * @ensures the required system call is executed with the boolean on whether
	 *          HALT occurred or not
	 */
	public boolean TRAP(int trapvec8, Scanner in);
}
