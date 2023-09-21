package lab3_integrated.linker;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PassOne {

    int ipla;
    PassOne(int ipla) {
        this.ipla = ipla;
    }

    static class PassOneResult {
        HashMap<String, Integer> symTable;
        int totalSize;
        ArrayList<Segment> segments;

        PassOneResult(HashMap<String, Integer> symTable, int totalSize, ArrayList<Segment> segments) {
            this.symTable = symTable;
            this.totalSize = totalSize;
            this.segments = segments;
        }
    }

    static class Segment {
        String segmentName;
        BufferedReader input;
        int size;
        int pla;
        HashMap<String, Integer> extSymbols;

        Segment(String segmentName, BufferedReader input, int size, int pla, HashMap<String, Integer> extSymbols) {
            this.segmentName = segmentName;
            this.input = input;
            this.size = size;
            this.pla = pla;
            this.extSymbols = extSymbols;
        }
    }

    PassOneResult executePassOne(ArrayList<BufferedReader> inputs) throws Exception {
        HashMap<String, Integer> extSymbolTable = new HashMap<>();
        ArrayList<Segment> segments = new ArrayList<>();
        int pla = ipla;
        for (BufferedReader input : inputs) {
            Segment seg = processSegment(input, pla);
            segments.add(seg);
            pla += seg.size;
            /* Example:
             ipla: 3600
             segment size: 3
             segment: 3600, 3601, 3602
             pla = 3600 + 3 = 3603
             segment size: 2
             segment: 3603, 3604
             etc.
             */
            extSymbolTable.putAll(seg.extSymbols);
        }
        int totalSize = pla - ipla;

        return new PassOneResult(extSymbolTable, totalSize, segments);
    }
    Segment processSegment(BufferedReader input, int pla) throws IOException,RuntimeException {
        HashMap<String, Integer> extSymbols = new HashMap<>();
        String line = input.readLine();
        String name = line.substring(1, 7); // H*Lib   *0003 (in hex)
        extSymbols.put(name, pla);
        int size = Integer.parseInt(line.substring(11), 16); // HLib *0003* (in hex)

        input.mark(300); // 300 bytes ahead, we're only doing one line so this should be more than enough
        line = input.readLine();
        while (line.charAt(0) == 'N') { // Assume N records until first T record
            int end = line.indexOf('=');
            String symbolName = line.substring(1, end);
            int symbolVal = Integer.parseInt(line.substring(end+1), 16) + pla;
            if (symbolVal > pla + size) {
                throw new RuntimeException("Symbol defined outside segment: " + symbolName);
            }
            extSymbols.put(symbolName, symbolVal); // TODO this seems fragile

            input.mark(300);
            line = input.readLine();
        }
        input.reset(); // Reset so the first line is a T record
        return new Segment(name, input, size, pla, extSymbols);
    }
}