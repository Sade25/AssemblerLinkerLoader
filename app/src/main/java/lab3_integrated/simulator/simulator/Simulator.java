package lab3_integrated.simulator.simulator;
import java.util.Scanner;
import lab3_integrated.simulator.machine.Machine;
import lab3_integrated.simulator.loader.Loader;


public class Simulator {

	public static enum Mode{QUIET, TRACE, STEP};
	public static Mode mode;
	public static Machine machine;
	private static int REGISTER_SIZE = 8;
	private static int SHORT_MAX_VALUE = 0x10000;
	private static int defaultLoopNum = 1000;
	private static Scanner read;
	
	/**
	 * Constants for the lower 8 bits of certain TRAP instructors
	 */
	private static int IN_VECT = 0x23;
	private static int INN_VECT = 0x33;
	private static int RND_VECT = 0x43;

	
	public static void simulator_main(String fileName) {
		int pc, loopNum;
		String state, numInstructs;
		boolean halt;
		read = new Scanner(System.in);
		machine = new Machine();
		
		Loader.parseInputFile(fileName, machine, read);
		
		System.out.println("Please enter 1 for Quiet mode, 2 for Trace mode, or 3 for Step mode");
		state = read.nextLine();
		switch (state) {
			case "2" -> mode = Mode.TRACE;
			case "3" -> mode = Mode.STEP;
			default -> mode = Mode.QUIET;
		}
		
		System.out.println("Please enter the maximum number of instructions you'd like the program to run for");
		numInstructs = read.nextLine();
		if (isNum(numInstructs)) {
			loopNum = Integer.parseInt(numInstructs);
		} else {
			loopNum = defaultLoopNum;
		}
		
		for (int i = 0; i < loopNum; i++) {
			pc = machine.getPC();
			short instruction = machine.memory[machine.getMemoryPageLocation(pc)][machine.getMemoryWordLocation(pc)];
			halt = ParseInstruction(instruction);
			if (halt) {
				break;
			}
			if (mode == Mode.STEP) {
				System.out.println("Press enter when you want to continue");
				read.nextLine();
			}
		}
		read.close();
	}
	/**
	 * Parses the instruction that is taken from the space in memory and runs said instruction
	 * 
	 * @param instruction taken from the space in memory assigned by the PC
	 * @return returns whether the program should be halted by the HALT instruction
	 */
	public static boolean ParseInstruction(short instruction) {
		boolean halt = false;
		String instructString = Integer.toBinaryString(instruction);
		if (instructString.length() > 16) {
			instructString = instructString.substring(16);
		}
		instructString = addLeadingZeros(instructString);
		short instruct = Short.parseShort(instructString.substring(0,4));
		// Destination register
		int dr = Integer.parseInt(instructString.substring(4,7), 2);
		// Source register 1
		int sr1 = Integer.parseInt(instructString.substring(7,10), 2);
		// Source register 2
		int sr2 = Integer.parseInt(instructString.substring(13), 2);
		// nzp flags in the ccrs
		int n = Integer.parseInt(instructString.substring(4, 5), 2);
		int z = Integer.parseInt(instructString.substring(5, 6), 2);
		int p = Integer.parseInt(instructString.substring(6, 7), 2);
		int L = Integer.parseInt(instructString.substring(4, 5), 2);
		int imm5 = Integer.parseInt(instructString.substring(11), 2);
		int pgoffset = Integer.parseInt(instructString.substring(7), 2);
		int BaseR = Integer.parseInt(instructString.substring(7, 10), 2);
		int index = Integer.parseInt(instructString.substring(10), 2);
		// Trap vector to indicate which trap method to run
		int trapVect = Integer.parseInt(instructString.substring(8), 2);
		switch (instruct) {
			case 1:
				outputMachineStateBeforeCheck("ADD");
				if (instructString.charAt(10) == '0') {
					machine.ADD_REG(dr, sr1, sr2);
				} else {
					machine.ADD_IMM(dr, sr1, imm5);
				}
				outputAffectedRegisters(dr, sr1, sr2);
				outputMachineStateAfterCheck("ADD");
				break;
			case 101:
				outputMachineStateBeforeCheck("AND");
				if (instructString.charAt(10) == '0') {
					machine.AND_REG(dr, sr1, sr2);
				} else {
					machine.AND_IMM(dr, sr1, imm5);
				}
				outputAffectedRegisters(dr, sr1, sr2);
				outputMachineStateAfterCheck("AND");
				break;
			case 0:
				outputMachineStateBeforeCheck("BRx");
				machine.BRx(n, z, p, pgoffset);
				outputAffectedPC(machine.PC, 0, -1);
				outputMachineStateAfterCheck("BRx");
				break;
			case 1000:
				outputMachineStateBeforeCheck("DBUG");
				outputMachineState();
				outputMachineStateAfterCheck("DBUG");
				break;
			case 100:
				outputMachineStateBeforeCheck("JSR");
				machine.JSR(L, pgoffset);
				outputAffectedPC(machine.PC, L, -1);
				outputMachineStateAfterCheck("JSR");
				break;
			case 1100:
				outputMachineStateBeforeCheck("JSRR");
				machine.JSRR(L, BaseR, index);
				outputAffectedPC(machine.PC, L, BaseR);
				outputMachineStateAfterCheck("JSRR");
				break;
			case 10:
				outputMachineStateBeforeCheck("LD");
				machine.LD(dr, pgoffset);
				outputLoadStore(machine.prev_pc, -1, dr, -1, false);
				outputMachineStateAfterCheck("LD");
				break;
			case 1010:
				outputMachineStateBeforeCheck("LDI");
				machine.LDI(dr, pgoffset);
				outputLoadStore(machine.prev_pc, machine.PC, dr, -1, false);
				outputMachineStateAfterCheck("LDI");
				break;
			case 110:
				outputMachineStateBeforeCheck("LDR");
				machine.LDR(dr, BaseR, index);
				outputLoadStore(machine.prev_pc, -1, dr, BaseR, false);
				outputMachineStateAfterCheck("LDR");
				break;
			case 1110:
				outputMachineStateBeforeCheck("LEA");
				machine.LEA(dr, pgoffset);
				outputLoadStore(-1, -1, dr, -1, false);
				outputMachineStateAfterCheck("LEA");
				break;
			case 1001:
				outputMachineStateBeforeCheck("NOT");
				machine.NOT(dr, sr1);
				outputOtherReg(dr, sr1);
				outputMachineStateAfterCheck("NOT");
				break;
			case 1101:
				outputMachineStateBeforeCheck("RET");
				machine.RET();
				outputOtherReg(7, -1);
				outputMachineStateAfterCheck("RET");
				break;
			case 11:
				outputMachineStateBeforeCheck("ST");
				machine.ST(dr, pgoffset);
				outputLoadStore(machine.getAddressPC(machine.PC, pgoffset), -1, sr1, -1, true);
				outputMachineStateAfterCheck("ST");
				break;
			case 1011:
				int address = machine.getAddressPC(machine.PC, pgoffset);
				int new_address = machine.memory[machine.getMemoryPageLocation(address)][machine.getMemoryWordLocation(address)];
				outputMachineStateBeforeCheck("STI");
				machine.STI(dr, pgoffset);
				outputLoadStore(address, new_address, sr1, -1, true);
				outputMachineStateAfterCheck("STI");
				break;
			case 111:
				outputMachineStateBeforeCheck("STR");
				machine.STR(dr, BaseR, index);
				outputLoadStore(machine.getAddressReg(BaseR, index), -1, sr1, BaseR, true);
				outputMachineStateAfterCheck("STR");
				break;
			case 1111:
				outputMachineStateBeforeCheck("TRAP");
				halt = machine.TRAP(trapVect, read);
				if (trapVect == IN_VECT || trapVect == INN_VECT || trapVect == RND_VECT) {
					outputAffectedRegisters(0, -1, -1);
				}
				outputMachineStateAfterCheck("TRAP");
				break;
		}
		return halt;
	}
	
	/**
	 * Outputting the state the machine before executing the instructions.
	 * 
	 * @param instName the name of the instruction
	 */
	private static void outputMachineStateBeforeCheck(String instName) {
		if (mode == Mode.TRACE || mode == Mode.STEP) {
			System.out.println("\nState of the machine BEFORE executing the " + instName + " instruction\n");
			outputMachineState();
		}
	}

	/**
	 * Outputting the state the machine after executing the instructions and
	 * affected memory locations and CCRs.
	 * 
	 * @param instName the name of the instruction
	 */
	private static void outputMachineStateAfterCheck(String instName) {
		if (mode == Mode.TRACE || mode == Mode.STEP) {
			System.out.println("\nState of the machine AFTER executing the " + instName + " instruction\n");
			outputMachineState();
		}
	}

	/**
	 * Output the state of the machine (Instruction, memory page, registers, and
	 * CCRs).
	 */
	private static void outputMachineState() {
		System.out.println("Current instruction: 0x" + Integer.toHexString(machine.prev_pc));
		System.out.println("Next instruction: 0x" + Integer.toHexString(machine.PC));

		System.out.println("Memory page: " + machine.getMemoryPageLocation(machine.PC));

		System.out.println("Registers: ");

		for (int i = 0; i < REGISTER_SIZE; i++) {
			System.out.println("R" + i + ": " + machine.registers[i]);
		}

		System.out.println("CCRs: ");

		int n = 0, z = 0, p = 0;

		if (machine.ccr[0]) {
			n = 1;
		}

		if (machine.ccr[1]) {
			z = 1;
		}

		if (machine.ccr[2]) {
			p = 1;
		}

		System.out.println("N: " + n + "\tZ: " + z + "\tP: " + p);
		System.out.println();
	}

	/**
	 * Print the affected registers with data movement instructions.
	 * 
	 * @param DR  destination register
	 * @param SR1 source register 1
	 * @param SR2 source register 2/immediate value
	 */
	private static void outputAffectedRegisters(int DR, int SR1, int SR2) {
		if (mode == Mode.TRACE || mode == Mode.STEP) {
			System.out.println("Affected Register: R" + DR);

			if (SR1 >= 1) {
				if (SR2 < 0) {
					System.out.println("Used Register: R" + SR1);
				} else {
					System.out.println("Used Register: R" + SR1 + ", R" + SR2);
				}
			}
		}

	}

	/**
	 * Output the PC value with instructions that change the flow of control.
	 * 
	 * @param PC  program counter
	 * @param L   bit to see if the address was saved or not
	 * @param reg base register (addressing mode)
	 */
	private static void outputAffectedPC(int PC, int L, int reg) {
		if (mode == Mode.TRACE || mode == Mode.STEP) {

			System.out.println("Previous PC value: " + ((machine.prev_pc + 1) % SHORT_MAX_VALUE));
			System.out.println("New PC value: " + PC);

			if (L == 1) {
				System.out.print("Affected Register: R7");
			}

			if (reg >= 0) {
				System.out.print("R" + reg);
			}

			System.out.println();
		}
	}

	/**
	 * Outputs the addresses/registers affected by the load/store instructions.
	 * 
	 * @param address     one memory touch address
	 * @param new_address two memory touch address
	 * @param ds_reg      destination/source register
	 * @param base_reg    base register (addressing mode)
	 * @param b           flag to alter between destination or source register
	 */
	private static void outputLoadStore(int address, int new_address, int ds_reg, int base_reg, boolean b) {
		if (mode == Mode.TRACE || mode == Mode.STEP) {

			if (address >= 0) {
				System.out.print("Address affected: 0x" + Integer.toHexString(address));
			}

			if (new_address >= 0) {
				System.out.print(", 0x" + Integer.toHexString(new_address));
			}

			System.out.println();

			if (!b) {
				System.out.print("Registers affected: R" + ds_reg);
			} else {
				System.out.print("Registers used: R" + ds_reg);
			}

			System.out.println();
		}
	}

	/**
	 * Output the state of registers for the NOT and RET instructions.
	 * 
	 * @param DR destination register
	 * @param SR source register
	 */
	private static void outputOtherReg(int DR, int SR) {
		if (mode == Mode.TRACE || mode == Mode.STEP) {

			System.out.print("Registers affected: R" + DR);

			if (SR >= 0) {
				System.out.print("Registers used: R" + SR);
			}
		}
	}
	
	private static String addLeadingZeros(String instructString) {
		while (instructString.length() < 16) {
			instructString = "0" + instructString;
		}
		
		return instructString;
	}
	
	private static boolean isNum(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) < '0' || str.charAt(i) > '9') {
				return false;
			}
		}
		return true;
	}

}
