package org.dreambot.api.methods.world;
import org.dreambot.api.methods.filter.Filter;
import java.util.List;
public class Worlds {
  public static List<World> all(Filter<World> f){return null;}
  public static World getRandomWorld(List<World> l){return new World();}
  public static int getCurrentWorld(){return 301;}
}
