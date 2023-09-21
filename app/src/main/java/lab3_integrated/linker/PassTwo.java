package lab3_integrated.linker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PassTwo {
    PassOne.PassOneResult passOneResult;
    BufferedWriter writer;

    PassTwo(PassOne.PassOneResult passOneResult, String outputFilePath) throws IOException {
        this.passOneResult = passOneResult;
        this.writer = new BufferedWriter(new FileWriter(outputFilePath));
    }

    void executePassTwo(String outputFile, int ipla) throws RuntimeException, IOException {
        writer.write("HMain  " + String.format("%04X", ipla) + String.format("%04X\n", passOneResult.totalSize));
        for (PassOne.Segment segment : passOneResult.segments) {
            segment.input.lines().forEachOrdered((line) -> {
                if (line.charAt(0) == 'T') {
                    //TODO: maybe clean up these parseInt calls into a method
                    int loc = Integer.parseInt(line.substring(1, 5), 16);
                    loc = loc + segment.pla;
                    short val = (short) Integer.parseInt(line.substring(5, 9), 16);
                    if (line.length() > 9 && line.charAt(9) == 'X' && line.charAt(10) == '9') {
                        //val >>>= 9;
                        //val <<= 9; // in THEORY this resets the lower nine bits to 0s (val >> 9) << 9
                        /*
                        Example:
                        1111 1111 1111 1111 >>> 9 = 0000 0000 0111 1111
                        0000 0000 0111 1111 << 9 = 1111 1110 0000 0000
                         */
                        String sym = line.substring(11);
                        try {
                            short symVal = (short) passOneResult.symTable.get(sym).intValue();
                            symVal &= 0x1FF;
                            val += symVal;
                        } catch(Exception e) {
                            throw new RuntimeException("Symbol \"" + sym + "\" not defined");
                        }
                    } else if (line.length() > 9 && line.charAt(9) == 'X' && line.charAt(10) == '1' && line.charAt(11) =='6') {
                        String sym = line.substring(12);
                        try {
                            val = (short) passOneResult.symTable.get(sym).intValue();
                        } catch(Exception e) {
                            throw new RuntimeException("Symbol \"" + sym + "\" not defined");
                        }
                    }
                    try {
                        writer.write("T" + String.format("%04X", loc) + String.format("%04x\n", val));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        try {
            int main = passOneResult.symTable.get("Main  ");
            writer.write("E" + String.format("%04X", main));
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Need a \"Main  \" segment");
        }
    }
}
