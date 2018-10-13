package edu.lexaron.cells;

import edu.lexaron.world.Sugar;
import edu.lexaron.world.Tile;
import edu.lexaron.world.World;
import javafx.scene.image.Image;

import java.util.ArrayList;

public class Weed extends Plant {
  private static final Image GFX = new Image("edu/lexaron/gfx/weed.png");
  private static final int MAX_SEED_RADIUS = 10;
  private static final int MAX_SEED_SUGAR_PER_TILE = 10;

  /**
   * Creates a new default {@link Weed} at a random location in the provided {@link World}.
   *
   * @param world where the {@link Weed} is to be created
   */
  public Weed(World world) {
    this("W", getRandom().nextInt(world.getWidth()), getRandom().nextInt(world.getHeight()));
  }

  @SuppressWarnings("MagicNumber")
  private Weed(String id, int x, int y) {
    super(id, x, y, 80.0, 10, 1.0, 1, 0.2, 1.05);
  }

  @Override
  public Image getImage() {
    return GFX;
  }

  @Override
  public Breed getBreed() {
    return Breed.WEED;
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
    return new Weed(getGeneCode() + getOffspring(), x, y);
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
      else {
        resetFoodAndPath();
      }
    }
  }

  /**
   * Create small pool of sugar upon death
   * @param world
   */
  @Override
  protected void die(World world){
    if (getOffspring() > 0) {
      ArrayList<Tile> surroundingTiles = world.getTileEnvironment(getX(), getY(), RANDOM.nextInt(MAX_SEED_RADIUS));
      for (Tile t : surroundingTiles) {
        t.getSugar().setAmount(t.getSugar().getAmount() + RANDOM.nextInt(MAX_SEED_SUGAR_PER_TILE));
      }
    }
    super.die(world);
  }
}