package exercises;

import algorithms.BruteForce;
import algorithms.CellIndexMethod;
import input.RandomInputGenerator;
import models.Board;
import models.Particle;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Exercise {
  protected static final int BOARD_M = 10; // The amount of cells in the board is M * M
  protected static final int CONVERGENCE_RADIUS = 6;

  protected static final String RANDOM_INPUT_PATH = "files/randomInput.txt";
  protected static final String STATIC_PATH = "files/static100.txt";
  protected static final String DYNAMIC_PATH = "files/dynamic100.txt";

  protected static final String CELL_INDEX_METHOD_OUTPUT = "files/cimOutput.txt";
  protected static final String BRUTE_FORCE_OUTPUT = "files/bfOutput.txt";

  protected static final String CELL_INDEX_METHOD_XYZ_OUTPUT = "files/cimOutput.xyz";
  protected static final String BRUTE_FORCE_XYZ_OUTPUT = "files/bfOutput.xyz";

  protected static Board cellIndexMethodWithEdge(List<Particle> list, int boardSize, int cellSize, int convergenceRadius) {
    CellIndexMethod cellIndexMethod = new CellIndexMethod(list, boardSize, cellSize, convergenceRadius);
    cellIndexMethod.calculateDistanceWithEdge();
    return cellIndexMethod.getBoard();
  }

  protected static Board bruteForceMethodWithEdge(List<Particle> list, int boardSize, int cellSize, int convergenceRadius) {
    BruteForce bruteForce = new BruteForce(list, boardSize, cellSize, convergenceRadius);
    bruteForce.calculateDistanceWithEdge();
    return bruteForce.getBoard();
  }

  protected static void writeTo(String fileName, List<String> lines) {
    Path path = Paths.get(fileName);
    try {
      Files.write(path, lines, Charset.forName("UTF-8"));
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  protected static List<Particle> getParticlesFromRandomInput() {
    // TODO: LOAD PARTICLES FROM FILE
    return new LinkedList<>();
  }

  protected static void generateRandomInput(String path, int particleCount, int boardM) {
    RandomInputGenerator.generateInput(path, particleCount, boardM);
  }

  protected static List<Particle> getParticlesFromNewRandomInput(String path, int particleCount, int boardM) {
    generateRandomInput(path, particleCount, boardM);
    return getParticlesFromRandomInput();
  }
}
