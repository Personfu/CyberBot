package org.dreambot.api.methods.map;
import org.dreambot.api.wrappers.interactive.Character;
public class Area {
  public Area(int x1,int y1,int x2,int y2){}
  public boolean contains(Character<?> c){return false;}
  public boolean contains(Tile t){return false;}
  public Tile getRandomTile(){return null;}
}
