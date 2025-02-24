package src;
// import library
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class IQPuzzlerPro {
    static Board board;
    static List<Puzzle> puzzles = new ArrayList<>();
    static int iterationCount = 0;
    static boolean hasSolution = false;
    static boolean isCustomMode = false;

    // array of colors
    static final Color[] COLORS = {
        Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN,
        Color.YELLOW, Color.ORANGE, Color.PINK, Color.GRAY, Color.DARK_GRAY
    };

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java IQPuzzlerPro <input_file.txt>");
            return;
        }

        String inputFile = args[0];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String[] firstLine = reader.readLine().trim().split("\\s+");
            if (firstLine.length < 3) {
                throw new IllegalArgumentException("Format input tidak valid. Harap berikan tiga angka dipisah oleh spasi (N, M, P).");
            }
            int N = Integer.parseInt(firstLine[0]);
            int M = Integer.parseInt(firstLine[1]);
            int P = Integer.parseInt(firstLine[2]);

            if (N <= 0 || M <= 0 || P <= 0) {
                throw new IllegalArgumentException("Ukuran papan dan jumlah blok harus lebih dari 0.");
            }

            String config = reader.readLine().trim();
            if (!config.equals("DEFAULT") && !config.equals("CUSTOM")) {
                throw new IllegalArgumentException("Hanya konfigurasi 'DEFAULT' atau 'CUSTOM' yang diperbolehkan.");
            }

            if (config.equals("DEFAULT")) {
                board = new Board(N, M, false, puzzles);
                isCustomMode = false;
            } else {
                char[][] customGrid = new char[N][M];
                for (int i = 0; i < N; i++) {
                    String line = reader.readLine().trim();
                    if (line.length() != M) {
                        throw new IllegalArgumentException("Baris konfigurasi tidak sesuai dengan ukuran papan.");
                    }
                    customGrid[i] = line.toCharArray();
                }
                board = new Board(N, M, true, customGrid, puzzles);
                isCustomMode = true;
            }

            List<char[]> blockLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    break;
                }
                blockLines.add(line.toCharArray());
            }

            List<List<char[]>> blocks = new ArrayList<>();
            List<char[]> currentBlock = new ArrayList<>();
            char previousChar = '\0';

            for (char[] lineChars : blockLines) {
                char firstChar = '\0';
                for (char c : lineChars) {
                    if (c != ' ') {
                        firstChar = c;
                        break;
                    }
                }
                if (firstChar != previousChar && !currentBlock.isEmpty()) {
                    blocks.add(currentBlock);
                    currentBlock = new ArrayList<>();
                }
                currentBlock.add(lineChars);
                previousChar = firstChar;
            }

            if (!currentBlock.isEmpty()) {
                blocks.add(currentBlock);
            }

            if (blocks.size() != P) {
                throw new IllegalArgumentException("Jumlah puzzle yang dibaca tidak sesuai dengan nilai P.");
            }

            for (List<char[]> block : blocks) {
                puzzles.add(new Puzzle(block));
            }

            // time
            long startTime = System.nanoTime();
            hasSolution = bruteForce(0);
            long endTime = System.nanoTime();
            long durationInMillis = (endTime - startTime) / 1_000_000;

            if (!hasSolution) {
                System.out.println("Tidak ada solusi!");
            } else {
                System.out.println("Solusi ditemukan:");
                board.print();
            }

            // iteration
            System.out.println("Waktu pencarian: " + durationInMillis + " ms");
            System.out.print("\n");
            System.out.println("Banyak kasus yang ditinjau: " + iterationCount);

            // solution
            System.out.println("\nApakah Anda ingin menyimpan solusi dalam file .txt? (ya/tidak)");
            try (Scanner scanner = new Scanner(System.in)) {
                String saveTxtChoice = scanner.nextLine().trim().toLowerCase();
                if (saveTxtChoice.equals("ya")) {
                    saveSolutionToFile(inputFile);
                } else if (!saveTxtChoice.equals("tidak")) {
                    System.out.println("Error: Pilihan jawaban hanya ya atau tidak.");
                }
                System.out.println("\nApakah Anda ingin menyimpan solusi dalam file .png? (ya/tidak)");
                String savePngChoice = scanner.nextLine().trim().toLowerCase();
                if (savePngChoice.equals("ya")) {
                    saveSolutionToImage(inputFile);
                } else if (!savePngChoice.equals("tidak")) {
                    System.out.println("Error: Pilihan jawaban hanya ya atau tidak.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: File tidak ditemukan atau tidak dapat dibaca.");
        } catch (InputMismatchException e) {
            System.out.println("Error: Input awal harus berupa angka (baris, kolom, jumlah blok).");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
        }
    }

    // brute-force algorithm
    public static boolean bruteForce(int index) {
        if (index == puzzles.size()) {
            if (board.isFull()) {
                return true;
            }
            return false;
        }

        Puzzle currentPuzzle = puzzles.get(index);

        for (int row = 0; row < board.N; row++) {
            for (int col = 0; col < board.M; col++) {
                List<char[][]> variations = currentPuzzle.generateVariations();
                for (char[][] variation : variations) {
                    iterationCount++;
                    if (board.canPlace(variation, row, col, isCustomMode)) {
                        board.place(variation, row, col, currentPuzzle.character);
                        if (bruteForce(index + 1)) {
                            return true;
                        }
                        board.remove(variation, row, col);
                    }
                }
            }
        }
        return false;
    }

    public static void saveSolutionToFile(String inputFileName) {
        File outputDir = new File("test");
        if (!outputDir.exists()) {
            if (!outputDir.mkdir()) {
                System.out.println("Error: Gagal membuat folder 'test'.");
                return;
            }
        }

        String outputFileName = "test/solution_" + new File(inputFileName).getName();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFileName))) {
            if (hasSolution) {
                for (char[] row : board.grid) {
                    for (char cell : row) {
                        if (cell == '.' || cell == 'X') {
                            writer.print(' ');
                        } else {
                            writer.print(cell);
                        }
                    }
                    writer.println();
                }
                System.out.println("Solusi berhasil disimpan di: " + outputFileName);
            } else {
                writer.println("Tidak ada solusi!");
                System.out.println("Solusi berhasil disimpan di: " + outputFileName);
            }
        } catch (IOException e) {
            System.out.println("Error: Gagal menyimpan solusi ke file.");
        }
    }

    public static void saveSolutionToImage(String inputFileName) {
        File outputDir = new File("test");
        if (!outputDir.exists()) {
            if (!outputDir.mkdir()) {
                System.out.println("Error: Gagal membuat folder 'test'.");
                return;
            }
        }

        String outputFileName = "test/solution_" + new File(inputFileName).getName().replace(".txt", ".png");
        int cellSize = 50;
        int width = board.M * cellSize;
        int height = board.N * cellSize;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        for (int row = 0; row < board.N; row++) {
            for (int col = 0; col < board.M; col++) {
                char cell = board.grid[row][col];
                if (cell != '.' && cell != 'X') {
                    int colorIndex = cell - 'A';
                    g2d.setColor(COLORS[colorIndex % COLORS.length]);
                    g2d.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 20));
                    g2d.drawString(String.valueOf(cell), col * cellSize + 20, row * cellSize + 30);
                }
            }
        }
        try {
            ImageIO.write(image, "PNG", new File(outputFileName));
            System.out.println("Solusi berhasil disimpan di: " + outputFileName);
        } catch (IOException e) {
            System.out.println("Error: Gagal menyimpan solusi sebagai gambar.");
        }
    }
}

class Board {
    int N, M;
    char[][] grid;
    boolean isCustomMode;
    List<Character> placedPuzzles = new ArrayList<>();
    List<Puzzle> puzzles;

    static final String RESET = "\u001B[0m";
    static final String UNDERLINE = "\u001B[4m";

    static final String[] TEXT_COLORS = {
        "\u001B[31m", // A - Red
        "\u001B[32m", // B - Green
        "\u001B[34m", // C - Blue
        "\u001B[35m", // D - Magenta
        "\u001B[36m", // E - Cyan
        "\u001B[33m", // F - Yellow
        "\u001B[90m", // G - Gray
        "\u001B[4;31m", // H - Red (Underlined)
        "\u001B[4;32m", // I - Green (Underlined)
        "\u001B[4;34m", // J - Blue (Underlined)
        "\u001B[4;35m", // K - Magenta (Underlined)
        "\u001B[4;36m", // L - Cyan (Underlined)
        "\u001B[4;33m", // M - Yellow (Underlined)
        "\u001B[4;90m", // N - Gray (Underlined)
        "\u001B[40;30m", // O - Black text, Gray bg
        "\u001B[41;30m", // P - Black text, Red bg
        "\u001B[42;30m", // Q - Black text, Green bg
        "\u001B[43;30m", // R - Black text, Yellow bg
        "\u001B[44;30m", // S - Black text, Blue bg
        "\u001B[45;30m", // T - Black text, Magenta bg
        "\u001B[46;30m", // U - Black text, Cyan bg
        "\u001B[41;93m", // V - Yellow text, Red bg
        "\u001B[42;93m", // W - Yellow text, Green bg
        "\u001B[43;93m", // X - Yellow text, Yellow bg
        "\u001B[44;93m", // Y - Yellow text, Blue bg
        "\u001B[45;93m"  // Z - Yellow text, Magenta bg
    };

    // default constructor
    public Board(int N, int M, boolean isCustomMode, List<Puzzle> puzzles) {
        this.N = N;
        this.M = M;
        this.isCustomMode = isCustomMode;
        this.puzzles = puzzles;
        grid = new char[N][M];
        for (char[] row : grid) {
            Arrays.fill(row, '.');
        }
    }

    // custom constructor
    public Board(int N, int M, boolean isCustomMode, char[][] customGrid, List<Puzzle> puzzles) {
        this.N = N;
        this.M = M;
        this.isCustomMode = isCustomMode;
        this.grid = customGrid;
        this.puzzles = puzzles;
    }

    public boolean isFull() {
        if (puzzles.size() != placedPuzzles.size()) {
            return false;
        }
        for (char[] row : grid) {
            for (char cell : row) {
                if (isCustomMode && cell == 'X') {
                    return false;
                } else if (!isCustomMode && cell == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canPlace(char[][] block, int row, int col, boolean isCustomMode) {
        int bN = block.length;
        int bM = block[0].length;
        if (row + bN > N || col + bM > M) return false;
        for (int i = 0; i < bN; i++) {
            for (int j = 0; j < bM; j++) {
                if (block[i][j] != ' ') {
                    if (isCustomMode && grid[row + i][col + j] != 'X') {
                        return false;
                    } else if (!isCustomMode && grid[row + i][col + j] != '.') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void place(char[][] block, int row, int col, char c) {
        int bN = block.length;
        int bM = block[0].length;
        for (int i = 0; i < bN; i++) {
            for (int j = 0; j < bM; j++) {
                if (block[i][j] != ' ') {
                    grid[row + i][col + j] = c;
                }
            }
        }
        placedPuzzles.add(c);
    }

    public void remove(char[][] block, int row, int col) {
        int bN = block.length;
        int bM = block[0].length;
        for (int i = 0; i < bN; i++) {
            for (int j = 0; j < bM; j++) {
                if (block[i][j] != ' ') {
                    grid[row + i][col + j] = isCustomMode ? 'X' : '.';
                }
            }
        }
        placedPuzzles.remove(placedPuzzles.size() - 1);
    }

    public void print() {
        for (char[] row : grid) {
            for (char cell : row) {
                if (cell == '.' || cell == 'X') {
                    System.out.print(' ');
                } else {
                    int index = cell - 'A';
                    String style = (index >= 7 && index <= 13) ? UNDERLINE : "";
                    System.out.print(style + TEXT_COLORS[index] + cell + RESET);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}

class Puzzle {
    char[][] shape;
    char character;

    public Puzzle(List<char[]> blockLines) {
        this.shape = convertListToArray(blockLines);
        this.character = findCharacter(shape);
    }

    private char[][] convertListToArray(List<char[]> list) {
        int maxCols = 0;
        for (char[] row : list) {
            maxCols = Math.max(maxCols, row.length);
        }

        char[][] array = new char[list.size()][maxCols];
        for (int i = 0; i < list.size(); i++) {
            char[] row = list.get(i);
            Arrays.fill(array[i], ' ');
            System.arraycopy(row, 0, array[i], 0, row.length);
        }
        return array;
    }

    private char findCharacter(char[][] block) {
        for (char[] row : block) {
            for (char cell : row) {
                if (cell != ' ') return cell;
            }
        }
        throw new IllegalArgumentException("Puzzle harus memiliki karakter huruf.");
    }

    public List<char[][]> generateVariations() {
        List<char[][]> variations = new ArrayList<>();
        char[][] rotated = shape;

        for (int i = 0; i < 4; i++) {
            rotated = rotateClockwise(rotated);
            variations.add(rotated);
            variations.add(flipHorizontally(rotated));
            variations.add(flipVertically(rotated));
        }
        return variations;
    }

    private char[][] rotateClockwise(char[][] block) {
        int bN = block.length;
        int bM = block[0].length;
        char[][] rotated = new char[bM][bN];
        for (int i = 0; i < bN; i++) {
            for (int j = 0; j < bM; j++) {
                rotated[j][bN - 1 - i] = block[i][j];
            }
        }
        return rotated;
    }

    private char[][] flipHorizontally(char[][] block) {
        int bN = block.length;
        int bM = block[0].length;
        char[][] flipped = new char[bN][bM];
        for (int i = 0; i < bN; i++) {
            for (int j = 0; j < bM; j++) {
                flipped[i][bM - 1 - j] = block[i][j];
            }
        }
        return flipped;
    }

    private char[][] flipVertically(char[][] block) {
        int bN = block.length;
        int bM = block[0].length;
        char[][] flipped = new char[bN][bM];
        for (int i = 0; i < bN; i++) {
            flipped[i] = block[bN - 1 - i];
        }
        return flipped;
    }
}