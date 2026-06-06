package org.dreambot.api.methods.map;
public class Tile {
  private int x,y,z;
  public Tile(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
  public int getX(){return x;}
  public int getY(){return y;}
  public int getZ(){return z;}
  public double distance(){return 0;}
  public double distance(Tile t){return 0;}
  public Tile derive(int dx,int dy){return new Tile(x+dx,y+dy,z);}
}
