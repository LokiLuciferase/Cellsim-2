package edu.lexaron.cells;

import edu.lexaron.world.Location;
import edu.lexaron.world.Sugar;
import edu.lexaron.world.World;

/**
 * Author: Mirza <mirza.suljic.ba@gmail.com>
 * Date: 23.4.2018.
 */
abstract class Herbivorous extends Cell {

  private Behaviour behaviour;


  /**
   * Creates a new {@link Cell} based on the provided parameters.
   *
   * @param id         unique {@link Breed} ID
   * @param x          horizontal coordinate of birth location
   * @param y          vertical coordinate of birth location
   */
  @SuppressWarnings ("MagicNumber")
  Herbivorous(String id, int x, int y, double energy, int vision, double speed, double efficiency, double biteSize, double mutationStepSizeMultiplier) {
    super(id, x, y, energy, vision, speed, efficiency, biteSize, mutationStepSizeMultiplier);
    this.behaviour = Behaviour.NEUTRAL;
  }

  @Override
  public void behave(World world) {
    switch (behaviour) {
      case TIMID:  //TODO: implement avoidance behaviour towards other breeds
        break;
      case NEUTRAL:
        shuffleIdleDirection();
        break;
      case HERD: //TODO: implement attraction behaviour towards own breed
                  //FIXME: cells do not eat
        findThisBreed(world);
        break;
    }
  }

  @Override
  public void doHunt(World world) {
    if (getPath().isEmpty()) {
      if (getFood() == null) {
        lookForFood(world);
      }
      else {
        eat(world);
      }
    }
    else {
      useWholePath(world);
    }
    if (getFood() == null) randomStep(world);
  }

  @Override
  public void eat(World w) {
    Sugar prey = w.getWorld()[getY()][getX()].getSugar();
    if (prey.getAmount() > 0.0) {
      prey.setAmount(prey.getAmount() - getBiteSize());
      setEnergy(getEnergy() + getBiteSize());
    }
    else {
      resetFoodAndPath();
    }
  }

  private void findThisBreed(World world){
    boolean found = false;
        outterloop:
    for (int v = 0; v <= getVision(); v++) {
      for (int i = (getY() - v); i <= (getY() + v); i++) {
        for (int j = (getX() - v); j <= (getX() + v); j++) {
          if (isValidLocation(world, j, i)) {
            Cell c = world.getWorld()[i][j].getCell();
            if (c != null && c.isAlive() && c.getBreed() == getBreed()){
              findPathTo(new Location(i, j));
              found = true;
              break outterloop;
            }
          }
        }
      }
    }
    if (!found) {
      shuffleIdleDirection();
    }
  }

}

