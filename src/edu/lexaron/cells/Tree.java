package edu.lexaron.cells;

import edu.lexaron.world.Sugar;
import edu.lexaron.world.World;
import javafx.scene.image.Image;

/**
 * A {@link Tree} is a {@link Cell} subclass that cannot move. However, they can generate small amounts of food  within
 * their FoV when they look at an empty {@link edu.lexaron.world.Tile}. This makes it possible for the {@link Tree}s
 * to support each other, thus turning them into a forest.
 *
 * Project name: CellSIM/Tree.java
 * Author & email: Mirza Suljić <mirza.suljic.ba@gmail.com>
 * Date & time: Jun 19, 2016, 12:57:57 AM
 * Refactored: 24.04.2018
 */
public class Tree extends Plant {
  private static final Image GFX = new Image("edu/lexaron/gfx/tree.png");

  /**
   * Creates a new default {@link Tree} at a random location in the provided {@link World}.
   *
   * @param world where the {@link Tree} is to be created
   */
  public Tree(World world) {
    this("T", getRandom().nextInt(world.getWidth()), getRandom().nextInt(world.getHeight()));
  }

  @SuppressWarnings ("MagicNumber")
  private Tree(String id, int x, int y) {
    super(id, x, y, 50.0, 5, 1.0, 0.1, 0.2, 1.05);
  }

  @Override
  public Image getImage() {
    return GFX;
  }

  @Override
  public Breed getBreed() {
    return Breed.TREE;
  }

  @Override
  public void doHunt(World world) {
    if (getFood() == null) {
      lookForFood(world);
    }
    else {
      for (int i = 0; (double) i < getSpeed(); i++) {
        eat(world);
      }
    }
  }

  @Override
  Cell doGiveBirth(int x, int y) {
    return new Tree(getGeneCode() + getOffspring(), x, y);
  }

  @SuppressWarnings ("MethodDoesntCallSuperMethod")
  @Override
  public void eat(World w) {
    if (getFood() != null) {
      Sugar sugar = w.getWorld()[getFood().getY()][getFood().getX()].getSugar();
      if (sugar != null && sugar.getAmount() > 0.0) {
        sugar.setAmount(sugar.getAmount() - getBiteSize());
        setEnergy(getEnergy() + getBiteSize());
      }
      else if (w.getWorld()[getFood().getY()][getFood().getX()].getCell() != null) {
        Cell anotherCell = w.getWorld()[getFood().getY()][getFood().getX()].getCell();
        if (getFood() != null && anotherCell != null && anotherCell.getBreed() == getBreed()) {
          anotherCell.setEnergy(anotherCell.getEnergy() - getBiteSize());
          setEnergy(getEnergy() + getBiteSize());
        }
      }
      else {
        resetFoodAndPath();
      }
    }
  }

}
