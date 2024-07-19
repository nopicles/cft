package org.example;

import java.io.*;
import java.util.*;

 public class DataFilter {
    private static final String DEFAULT_OUTPUT_PATH = "./";
    private static final String DEFAULT_PREFIX = "";
    private static final String[] DEFAULT_FILE_NAMES = {"integers.txt", "floats.txt", "strings.txt"};

    private String outputPath = DEFAULT_OUTPUT_PATH;
    private String prefix = DEFAULT_PREFIX;
    private boolean appendMode = false;
    private boolean fullStats = false;

    private Map<String, BufferedWriter> writers =  new HashMap<>();
    private Map<String, List<String>> stats = new HashMap<>();

    public static void main(String[] args) {
        DataFilter filter = new DataFilter();
        if (!filter.parseArgs(args)) {
            System.exit(1);
        }
        filter.processFiles(Arrays.asList(args));
        filter.closeWriters();
        filter.printStats();
    }

    private boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    } else {
                        System.err.println("Missing argument for -o option");
                        return false;
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length) {
                        prefix = args[++i];
                    } else {
                        System.err.println("Missing argument for -p option");
                        return false;
                    }
                    break;
                case "-a":
                    appendMode = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                case "-s":
                    fullStats = false;
                    break;
                default:
                    if (args[i].startsWith("-")) {
                        System.err.println("Unknown option: " + args[i]);
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private void processFiles(List<String> fileNames) {
        for (String fileName : fileNames) {
            if (fileName.startsWith("-")) {
                continue;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading file " + fileName + ": " + e.getMessage());
            }
        }
    }

    private void processLine(String line) throws IOException {
        if (line.trim().isEmpty()) {
            return;
        }
        try {
            int intValue = Integer.parseInt(line);
            writeToFile("integers", line);
            updateStats("integers", line);
        } catch (NumberFormatException e1) {
            try {
                double floatValue = Double.parseDouble(line);
                writeToFile("floats", line);
                updateStats("floats", line);
            } catch (NumberFormatException e2) {
                writeToFile("strings", line);
                updateStats("strings", line);
            }
        }
    }

    private void writeToFile(String type, String line) throws IOException {
        BufferedWriter writer = writers.get(type);
        if (writer == null) {
            String filePath = outputPath + "/" + prefix + DEFAULT_FILE_NAMES[getTypeIndex(type)];
            writer = new BufferedWriter(new FileWriter(filePath, appendMode));
            writers.put(type, writer);
        }
        writer.write(line);
        writer.newLine();
    }

    private void updateStats(String type, String line) {
        List<String> stat = stats.get(type);
        if (stat == null) {
            stat = new ArrayList<>();
            stats.put(type, stat);
        }
        if (type.equals("strings")) {
            stat.add(line);
        } else {
            double value = Double.parseDouble(line);
            if (stat.size() == 0) {
                stat.add("1");
                stat.add(line);
                stat.add(line);
                stat.add(line);
                stat.add(line);
            } else {
                int count = Integer.parseInt(stat.get(0)) + 1;
                double min = Math.min(Double.parseDouble(stat.get(1)), value);
                double max = Math.max(Double.parseDouble(stat.get(2)), value);
                double sum = Double.parseDouble(stat.get(3)) + value;
                double avg = sum / count;
                stat.set(0, String.valueOf(count));
                stat.set(1, String.valueOf(min));
                stat.set(2, String.valueOf(max));
                stat.set(3, String.valueOf(sum));
                stat.set(4, String.valueOf(avg));
            }
        }
    }

    private void closeWriters() {
        for (BufferedWriter writer : writers.values()) {
            try {
                writer.close();
            } catch (IOException e) {
                System.err.println("Error closing writer: " + e.getMessage());
            }
        }
    }

    private void printStats() {
        for (Map.Entry<String, List<String>> entry : stats.entrySet()) {
            String type = entry.getKey();
            List<String> stat = entry.getValue();
            System.out.println("Stats for " + type + ":");
            if (type.equals("strings")) {
                int count = stat.size();
                int minLength = Integer.MAX_VALUE;
                int maxLength = 0;
                for (String s : stat) {
                    int length = s.length();
                    if (length < minLength) minLength = length;
                    if (length > maxLength) maxLength = length;
                }
                System.out.println("Count: " + count);
                System.out.println("Min Length: " + minLength);
                System.out.println("Max Length: " + maxLength);
            } else {
                int count = Integer.parseInt(stat.get(0));
                double min = Double.parseDouble(stat.get(1));
                double max = Double.parseDouble(stat.get(2));
                double sum = Double.parseDouble(stat.get(3));
                double avg = Double.parseDouble(stat.get(4));
                System.out.println("Count: " + count);
                if (fullStats) {
                    System.out.println("Min: " + min);
                    System.out.println("Max: " + max);
                    System.out.println("Sum: " + sum);
                    System.out.println("Avg: " + avg);
                }
            }
        }
    }

    private int getTypeIndex(String type) {
        switch (type) {
            case "integers":
                return 0;
            case "floats":
                return 1;
            case "strings":
                return 2;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}