package edu.lexaron.cells;

import edu.lexaron.world.World;
import javafx.scene.image.Image;

/**
 * {@link Leech} are a kind of {@link Carnivorous} {@link Cell} that follows it's prey
 * and slowly steals the prey's energy.
 *
 * Project name: CellSIM/Leech.java
 * Author & email: Mirza Suljić <mirza.suljic.ba@gmail.com>
 * Date & time: Jun 19, 2016, 8:00:46 PM
 * Refactored: 24.04.2018
 */
@SuppressWarnings ("MagicNumber")
public class Leech extends Carnivorous {
  private static final Image GFX = new Image("edu/lexaron/gfx/leech.png");

  private Leech(String ID, int x, int y) {
    super(ID, x, y, 50.0, 5, 3, 0.20,  2.5, 1.05);
  }

  /**
   * Creates a new default {@link Leech} at a random location in the provided {@link World}.
   *
   * @param world where the {@link Leech} is to be created
   */
  public Leech(World world) {
    this("L", getRandom().nextInt(world.getWidth()), getRandom().nextInt(world.getHeight()));
  }

  @Override
  public Image getImage() {
    return GFX;
  }

  @Override
  public Breed getBreed() {
    return Breed.LEECH;
  }

  @Override
  Cell doGiveBirth(int x, int y) {
    return new Leech(getGeneCode() + getOffspring(), x, y);
  }

  @Override
  public boolean isValidPrey(Cell prey) {
    return (prey != null && !prey.equals(this));
  }

  @Override
  public void eat(World world) {
    if (getFood() != null && world.getWorld()[getFood().getY()][getFood().getX()].getCell() != null) {
      Cell hostCell = world.getWorld()[getFood().getY()][getFood().getX()].getCell();
      if (isValidPrey(hostCell)) {
        hostCell.setEnergy(hostCell.getEnergy() - getBiteSize());
        setEnergy(getEnergy() + getBiteSize());
        if (hostCell.getEnergy() < 0) {
          hostCell.die(world);
          world.getWorld()[getFood().getY()][getFood().getX()].setDeadCell(hostCell);
          world.getWorld()[getFood().getY()][getFood().getX()].setCell(null);
        }
      }
    }
    else {
      getPath().clear();
      resetFoodAndPath();
    }

  }
}
