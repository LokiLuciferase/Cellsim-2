package edu.lexaron.cells;

import com.sun.istack.internal.Nullable;
import edu.lexaron.world.Location;
import edu.lexaron.world.Tile;
import edu.lexaron.world.Trail;
import edu.lexaron.world.World;
import javafx.scene.image.Image;

import java.security.SecureRandom;
import java.util.*;

/**
 *
 * This class contains the basic functionalities of a living organism in this simulation.
 * It provides a number of abstract methods that must be implemented in order to create a new {@link Breed}.
 * Other more general activities, such as finding a unoccupied {@link Location} are share by all {@link Breed}s.
 *
 * Project name: CellSIM/Cell.java
 * Author: Mirza Suljić <mirza.suljic.ba@gmail.com>
 * Date & time: Feb 5, 2016, 8:55:28 PM
 * Refactored: 24.04.2018
 */
public abstract class Cell {

  private static final Random RANDOM    = new SecureRandom();
  private static final double BIRTH_REQ = 100.0;
  private static final int    OFFSPRING_LIMIT = 3;
  private static final int idleDirectionSwitchDivisor = 50;
  private static final double deleteriousMutationRate = 0.8;
  private static final double mutationRate = 1;
  private static final List<Direction> directionList = new ArrayList<>(EnumSet.allOf(Direction.class));

  private final int               movement;
  private final String            geneCode;
  private final Queue<Direction>  path;

  private boolean alive;
  private int    x, y, vision, trailSize, offspring, oppositeRandomStep, lastRandomStep;
  private Direction idleDirection;
  private double energy;
  private double speed;
  private double efficiency;
  private double biteSize;
  private double mutationStepSizeMultiplier;
  private Location food = null;

  /**
   * Creates a new {@link Cell} based on the provided parameters.
   *
   * @param id          unique {@link Breed} ID
   * @param x           horizontal coordinate of birth location
   * @param y           vertical coordinate of birth location
   * @param energy      initial energy level, usually 50
   * @param vision      initial vision range, determines the FoV
   * @param speed       initial speed, determines how fast the {@link Cell} uses it's {@link Cell#path}
   * @param efficiency  initial efficiency, determines how much energy a {@link Cell} expends for each action it takes
   * @param biteSize    initial size of bite, determines how fast the {@link Cell} consumes it's food source
   * @param mutationStepSizeMultiplier initial mutation rate (determines step size upon adaption)
   */
  @SuppressWarnings ({"UnnecessaryThis"})
  protected Cell(String id, int x, int y, double energy, int vision,
                 double speed, double efficiency, double biteSize, double mutationStepSizeMultiplier) {
    this.path = new ArrayDeque<>();
    this.geneCode = id;
    this.x = x;
    this.y = y;
    this.energy     = energy;
    this.vision     = vision;
    this.movement   = 1;
    this.speed      = speed;
    this.efficiency = efficiency;
    shuffleIdleDirection();
    if (energy > 0.0) {
      this.alive = true;
    }
    this.trailSize = 50;
    this.biteSize = biteSize;
    this.mutationStepSizeMultiplier = mutationStepSizeMultiplier;
  }

  /**
   * @return the {@link Image} that represents this {@link Cell} subclass
   */
  public abstract Image getImage();

  /**
   * Handle how this {@link Cell} subclass looks for food.
   *
   * @param w the {@link World} that contains the food
   */
  public abstract void lookForFood(World w);

  /**
   * @return the {@link Breed} this {@link Cell} belongs to
   */
  public abstract Breed getBreed();

  /**
   * Each {@link Breed} might hunt differently. Use this method to determine how your {@link Cell} hunts in the provided
   * {@link World}.
   *
   * @param world where the {@link Cell} hunts
   */
  public abstract void doHunt(World world);

  abstract void eat(World w);

  abstract Cell doGiveBirth(int x, int y);

  @SuppressWarnings ({"MagicCharacter"})
  private void tryBirth(World world) {
    if (energy >= BIRTH_REQ) {
      Location birthPlace = findBirthplace(world);
      Cell child = doGiveBirth(birthPlace.getX(), birthPlace.getY());
      child.inheritFrom(this);
      if ((double) RANDOM.nextInt(100) / 100 <= mutationRate){
        child.evolve();
      }
      world.getNewBornCells().add(child);
      offspring += 1;
      energy /= 3.0;
    }
  }

  /**
   * Handles upkeep, includes the {@link Cell#doHunt(World)} method, tries to produce offspring and handles death.
   *
   * @param world where it all takes place
   */
  @SuppressWarnings ("MagicNumber")
  public final void live(World world) {
    upkeep(world);
    if (alive) {
      doHunt(world);
      if (path.isEmpty() && food == null) {
        behave(world);
        move(world, idleDirection);
        randomStep(world);
      }
    }
    tryBirth(world);
  }

  /**
   * At 0.0 energy, a {@link Cell} dies. At over 100, a {@link Cell} tries to divide.
   *
   * @return the amount of energy this {@link Cell} has
   */
  public final double getEnergy() {
    return energy;
  }

  /**
   * @return the horizontal coordinate of this {@link Cell}
   */
  public final int getX() {
    return x;
  }

  /**
   * @return the vertical coordinate of this {@link Cell}
   */
  public final int getY() {
    return y;
  }

  /**
   * @param x new horizontal coordinate of this {@link Cell}
   */
  public final void setX(int x) {
    this.x = x;
  }

  /**
   * @param y new vertical coordinate of this {@link Cell}
   */
  public final void setY(int y) {
    this.y = y;
  }

  public final Direction getIdleDirection() { return idleDirection; }

  public final void shuffleIdleDirection() {
    if (idleDirection == null || (RANDOM.nextInt(idleDirectionSwitchDivisor) == 0)) {
      idleDirection = directionList.get(RANDOM.nextInt(directionList.size() -1));
    }
  }

  /**
   * Determines a {@link Cell}'s field of vision.
   *
   * @return this {@link Cell}'s range of sight
   */
  public final int getVision() {
    return vision;
  }

  /**
   * Determines a {@link Cell}'s step maginude during evolution.
   *
   * @return this {@link Cell}'s step magnitude during evolution.
   */
  public final double getMutationStepSizeMultiplier() { return mutationStepSizeMultiplier; }

  /**
   * Determines how fast a {@link Cell} uses it's path.
   *
   * @return this {@link Cell}'s speed
   */
  public final double getSpeed() {
    return speed;
  }

  /**
   * Determines a {@link Cell}'s cost of living.
   *
   * @return a coefficient which influences how much each energy is used in each activity
   */
  public final double getEfficiency() {
    return efficiency;
  }

  /**
   * Determines a {@link Cell}'s rate of consuming a food source.
   *
   * @return the amount of energy a {@link Cell} consumes in one go
   */
  public double getBiteSize() {
    return biteSize;
  }

  /**
   * @return whether or not this {@link Cell} is alive
   */
  public final boolean isAlive() {
    return alive;
  }

  /**
   * @return the {@link Location} where this {@link Cell} detected a food source
   */
  @Nullable
  public Location getFood() {
    return food;
  }

  static Random getRandom() {
    return RANDOM;
  }

  public void behave(World world) { shuffleIdleDirection(); }

  void findPathTo(Location target) {
    if (target != null) {
      int difY = target.getY() - y;
      int difX = target.getX() - x;
      if (difX > 0) {
        for (int i = 0; i < Math.abs(difX); i++) {
          path.offer(Direction.RIGHT);
        }
      }
      if (difX < 0) {
        for (int i = 0; i < Math.abs(difX); i++) {
          path.offer(Direction.LEFT);
        }
      }
      if (difY > 0) {
        for (int i = 0; i < Math.abs(difY); i++) {
          path.offer(Direction.DOWN);
        }
      }
      if (difY < 0) {
        for (int i = 0; i < Math.abs(difY); i++) {
          path.offer(Direction.UP);
        }
      }
    }
    else {
      resetFoodAndPath();
    }
  }

  void setFood(int x, int y) {
    food = new Location(x, y);
  }

  void resetFoodAndPath() {
    path.clear();
    food = null;
  }

  void useWholePath(World w) {
    for (int i = 0; i < speed; i++) {
      if (!path.isEmpty()) {
        move(w, path.poll());
      }
    }
  }

  // Take a random step; avoid opposite direction of last step
  void randomStep(World w) {
    int roll = RANDOM.nextInt(directionList.size());
    while (roll == oppositeRandomStep && roll == lastRandomStep) {
      roll = RANDOM.nextInt(directionList.size());
    }
    oppositeRandomStep = directionList.size() - 1 - roll;
    lastRandomStep = roll;
    move(w, directionList.get(roll));
  }

  @SuppressWarnings ({"ImplicitNumericConversion", "ProhibitedExceptionCaught"})
  void move(World world, Direction dir) {
      if (isValidLocation(world, x + dir.getDeltaX(), y + dir.getDeltaY())) {
        Tile targetLocation = world.getWorld()[y + dir.getDeltaY()][x + dir.getDeltaX()];
        if (targetLocation.getCell() == null) {
          if ((energy - (movement * efficiency)) > 0) {
            energy -= (movement * efficiency);
            world.getWorld()[y][x].setCell(null);
            y += dir.getDeltaY();
            x += dir.getDeltaX();
            world.getWorld()[y][x].setTrail(new Trail(trailSize, this));
            world.getWorld()[y][x].setCell(this);
          }
          else {
            die(world);
          }

        }
        else {
          randomStep(world);
        }
      }
      else {
        circumnavigate(world, x + dir.getDeltaX(), y + dir.getDeltaY());
      }
  }

  private void circumnavigate(World world, int x, int y) {
    x = x >= world.getWidth()  ? 0 : x < 0 ? world.getWidth() -1 : x;
    y = y >= world.getHeight() ? 0 : y < 0 ? world.getHeight() -1 : y;
    world.getWorld()[this.y][this.x].setCell(null);
    world.getWorld()[y][x].setTrail(new Trail(trailSize, this));
    world.getWorld()[y][x].setCell(this);
    this.x = x;
    this.y = y;
    resetFoodAndPath();
  }

  void setEnergy(double energy) {
    this.energy = energy;
  }

  boolean isValidLocation(World world, int x, int y) {
    return x >= 0 && x < world.getWidth()
        && y >= 0 && y < world.getHeight();
  }

  @SuppressWarnings ("AssignmentOrReturnOfFieldWithMutableType")
  Queue<Direction> getPath() {
    return path;
  }

  Location findBirthplace(World w) { // todo Mirza : can cause app to hang if no suitable place is available
    Location birthplace = null;
    boolean found = false;
    while (!found) {
      int rx = RANDOM.nextInt(((x + vision) - (x - vision)) + 1) + (x - vision);
      int ry = RANDOM.nextInt(((y + vision) - (y - vision)) + 1) + (y - vision);
      if (!(ry < 0 || rx < 0 || ry >= w.getHeight() || rx >= w.getWidth())) {
        if (w.getWorld()[ry][rx].getCell() == null && w.getWorld()[ry][rx].getDeadCell() == null) {
          birthplace = new Location(rx, ry);
          found = true;
        }
      }
    }
    return birthplace;
  }

  String getGeneCode() {
    return geneCode;
  }

  int getOffspring() {
    return offspring;
  }

  private void evolve() {
    Boolean isDeleterious = RANDOM.nextInt(10) < 10 * deleteriousMutationRate;
    double fuzzFactor = RANDOM.nextGaussian();
    if (fuzzFactor < 0) {fuzzFactor *= -1;}
    fuzzFactor += 1;
    fuzzFactor /= 20;
    fuzzFactor += 1; //TODO: fix this mess
    switch (RANDOM.nextInt(5)) {  // do not allow increasing of step size multiplier
      case 0:
        mutateVision(isDeleterious, fuzzFactor);
        break;
      case 1:
        mutateEfficiency(isDeleterious, fuzzFactor);
        break;
      case 2:
        mutateSpeed(isDeleterious, fuzzFactor);
        break;
      case 3:
        mutateTrailSize(isDeleterious, fuzzFactor);
        break;
      case 4:
        mutateBiteSize(isDeleterious, fuzzFactor);
        break;
      case 5:
        mutateMutationStepSizeMultiplier(isDeleterious, fuzzFactor);
        break;
    }
  }

  private void inheritFrom(Cell parent) {
    energy      = parent.getEnergy() / 3.0;
    vision      = parent.getVision();
    speed       = parent.getSpeed();
    efficiency  = parent.getEfficiency();
    biteSize    = parent.getBiteSize();
    mutationStepSizeMultiplier = parent.getMutationStepSizeMultiplier();
  }

  private void upkeep(World w) {
//    energy -= biteSize * efficiency / 10.0; // todo Mirza : think of a tax
    if (!alive || energy <= 0.0 || offspring >= OFFSPRING_LIMIT) {
      die(w);
    }
  }

  void die(World world) {
    alive = false;
    world.getWorld()[y][x].setDeadCell(this);
    world.getWorld()[y][x].setCell(null);
  }

  // TODO: let vision evolve in other increments dependent on mutationRate
  private void mutateVision(Boolean isDeleterious, double fuzzFactor) {
    double baseVisionChange = 1;
    int cumulativeVisionChange = (int) Math.round(baseVisionChange * mutationStepSizeMultiplier * fuzzFactor);
    if (isDeleterious) {
      vision -= cumulativeVisionChange;
    } else {
      vision += cumulativeVisionChange;
    }
    if (vision < 1) { vision = 1; }
  }

  private void mutateEfficiency(Boolean isDeleterious, double fuzzFactor) {
    double baseEfficiencyChange = 1.05;
    double cumulativeEfficiencyChange = baseEfficiencyChange * mutationStepSizeMultiplier * fuzzFactor;
    if (isDeleterious) {
      efficiency *= cumulativeEfficiencyChange;
    } else {
      efficiency /= cumulativeEfficiencyChange;
    }
  }

  private void mutateSpeed(Boolean isDeleterious, double fuzzFactor) {
    double baseSpeedChange = 0.25;
    double cumulativeSpeedChange = baseSpeedChange * mutationStepSizeMultiplier * fuzzFactor;
    if (isDeleterious) {
      speed -= cumulativeSpeedChange;
    } else {
      speed += cumulativeSpeedChange;
    }
    if (speed <= 0.01 ) { speed = 0.01; }
  }

  private void mutateTrailSize(Boolean isDeleterious, double fuzzFactor) {
    double baseTrailSizeChange = 1;
    int cumulativeTrailSizeChange = (int) Math.round(baseTrailSizeChange * mutationStepSizeMultiplier * fuzzFactor);
    if (isDeleterious) {
      trailSize -= cumulativeTrailSizeChange;
    } else {
      trailSize += cumulativeTrailSizeChange;
    }
    if (trailSize < 2) {trailSize = 2;}
  }

  private void mutateBiteSize(Boolean isDeleterious, double fuzzFactor) {
    double baseBiteSizeChange = 1.05;
    double cumulativeBiteSizeChange = baseBiteSizeChange * mutationStepSizeMultiplier * fuzzFactor;
    if (isDeleterious) {
      biteSize /= cumulativeBiteSizeChange;
    } else {
      biteSize *= cumulativeBiteSizeChange;
    }
  }

  private void mutateMutationStepSizeMultiplier(Boolean isDeleterious, double fuzzFactor) {
    mutationStepSizeMultiplier *= 1.5;
  }

}
