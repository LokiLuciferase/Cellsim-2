/*
 *  Project name: CellSIM/Wrold.java
 *  Author & email: Mirza Suljić <mirza.suljic.ba@gmail.com>
 *  Date & time: Feb 5, 2016, 8:54:35 PM
 */
package edu.lexaron.world;

import edu.lexaron.cells.Cell;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Mirza Suljić <mirza.suljic.ba@gmail.com>
 */
public class World {

  private static final int MAX_SUGAR_PER_TILE = 20;
  private final int height;
  private final int width;
  private final Random random = new SecureRandom();
  private Tile[][] world;
  private volatile Set<Cell> allCells = new HashSet<>();
  private Set<Cell> newBornCells = new HashSet<>();
  private Set<Cell> eatenCorpses = new HashSet<>();

  /**
   * @param width
   * @param height
   */
  public World(int width, int height) {
    this.height = height;
    this.width = width;
  }

  /**
   * @param sugarFactor
   * @return
   */
  public Tile[][] generateWorld(double sugarFactor) {
    // sf, 0 to 100 in %

    System.out.println("Generating world...");
    world = new Tile[height][width];

    int sugarTiles = (int) (((width * height)) * (sugarFactor / 100));
    System.out.println(String.format("Setup:%sx%s, SF=%s, ST=%s", width, height, sugarFactor, sugarTiles));
    int tileID = 1;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        world[j][i] = new Tile(tileID, null, new Sugar(j, i, 0), new Trail(0, null));
        tileID++;
      }
    }
    int x;
    int y;
    for (int i = 0; i < sugarTiles; i++) {
      do {
        x = random.nextInt(width);
        y = random.nextInt(height);
      }
      while (hasSugar(y, x));
      world[y][x].setSugar(new Sugar(x, y, random.nextInt(MAX_SUGAR_PER_TILE + 1)));
    }
    System.out.println("Done generating world!");

    return world;
  }

  /**
   * @param x
   * @param y
   * @return
   */
  public boolean hasSugar(int x, int y) {
    return world[x][y].getSugar().getAmount() != 0;
  }

//  /**
//   *
//   */
//  public void newFood() {
//    int x, y;
////        x = r.nextInt(((width / 4) * 3) - (width / 4)) + (width / 4);
////        y = r.nextInt(((height / 4) * 3) - (height / 4)) + (height / 4);
//    x = random.nextInt(width - 2) + 1;
//    y = random.nextInt(height - 2) + 1;
//    if (world[y][x].getSugar().getAmount() <= 0) {
//      world[y][x].getSugar().setAmount(random.nextInt(9) + 1);
//    }
//    else {
//      //if (world[y][x].getSugar().getAmount() <= 18)
//      world[y][x].getSugar().setAmount(world[y][x].getSugar().getAmount() + 2);
//    }
//
//  }

  /**
   * @return
   */
  public Tile[][] getWorld() {
    return world;
  }

  /**
   * @return
   */
  public int getHeight() {
    return height;
  }

  /**
   * @return
   */
  public int getWidth() {
    return width;
  }

  /**
   *
   * @param x
   * @param y
   * @param radius
   * @return
   */
  public ArrayList<Tile> getTileEnvironment(int x, int y, int radius) {
    ArrayList<Tile> surroundingTiles = new ArrayList<>();
    for (int i = x - radius; i < x + radius; i++){
      for (int j = y - radius; j < y + radius; j++){
        int validX = i >= getWidth()  ? 0 - (getWidth() -1 - i) : i < 0 ? getWidth() -1 + i : i;
        int validY = j >= getHeight()  ? 0 - (getHeight() - 1 - j) : j < 0 ? getHeight() -1 + j: j;
        surroundingTiles.add(world[validY][validX]);
      }
    }
    return surroundingTiles;
  }

  /**
   * @return
   */
  public Set<Cell> getAllCells() {
    return allCells;
  }

  /**
   * @return
   */
  public Set<Cell> getNewBornCells() {
    return newBornCells;
  }

  public Set<Cell> getEatenCorpses() {
    return eatenCorpses;
  }

  @SuppressWarnings ("ImplicitNumericConversion")
  public int getTotalSugar() {
    int result = 0;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        result += world[j][i].getSugar().getAmount();
      }
    }
    return result;
  }
}
