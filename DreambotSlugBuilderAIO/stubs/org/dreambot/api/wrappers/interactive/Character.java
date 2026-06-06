package org.dreambot.api.wrappers.interactive;
import org.dreambot.api.methods.map.Tile;
public class Character<T> {
  public boolean exists(){return true;}
  public double distance(){return 0;}
  public String getName(){return "";}
  public boolean hasAction(String a){return true;}
  public int getHealthPercent(){return 100;}
  public boolean isInCombat(){return false;}
  public boolean interact(String a){return true;}
  public Tile getTile(){return null;}
  public int getAnimation(){return -1;}
}
