/*
 *
 *
 *
 */
package edu.lexaron.cells;

import edu.lexaron.world.Sugar;
import edu.lexaron.world.World;
import javafx.scene.image.Image;

/**
 * A {@link Herbivorous} {@link Cell}, feeds on the first {@link Sugar} it sees.
 *
 * Project name: CellSIM/cell_max.java
 * Author & email: Mirza Suljić <mirza.suljic.ba@gmail.com>
 * Date & time: Jun 1, 2016, 1:51:43 AM
 * Refactored: 24.04.2018
 */
public class HuntFirst extends Herbivorous {
  private static final Image GFX = new Image("edu/lexaron/gfx/huntFirst.png");

  private HuntFirst(String id, int x, int y) {
    super(id, x, y, 50.0, 3, 1.0, 1.0, 1.0, 1.05);
  }

  /**
   * Creates a new default {@link HuntFirst} at a random location in the provided {@link World}.
   *
   * @param world where the {@link HuntFirst} is to be created
   */
  public HuntFirst(World world) {
    this("F", getRandom().nextInt(world.getWidth()), getRandom().nextInt(world.getHeight()));
  }

  @Override
  public Image getImage() {
    return GFX;
  }

  @Override
  public Breed getBreed() {
    return Breed.HUNT_FIRST;
  }

  @Override
  Cell doGiveBirth(int x, int y) {
    return new HuntFirst(getGeneCode() + getOffspring(), x, y);
  }

  @Override
  public void lookForFood(World w) {
        loop:
    for (int i = getY() + getVision(); i >= (getY() - getVision()); i--) {
      for (int j = getX() - getVision(); j <= (getX() + getVision()); j++) {
        if (isValidLocation(w, j, i) && w.getWorld()[i][j].getSugar().getAmount() > 0.0) {
          setFood(j, i);
          findPathTo(getFood());
          break loop;
        }
      }
    }
  }
  }

