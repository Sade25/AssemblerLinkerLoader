package lab3_integrated.assembler.lab2;

public class Exceptions {
	public static class TooShortException extends Exception {
		@Override
		public String getMessage() {
			return "Line too short.";
		}
	}

	public static class CommentLineNoSemicolon extends Exception {
	}

	public static class InvalidOperandException extends Exception {
	}

	public static class Pass2Exception extends Exception {
		/**
		 * Default constructor of the exception class.
		 * 
		 * @param message message to print to the console
		 */
		public Pass2Exception(String message) {
			super(message);
		}
	}
}
