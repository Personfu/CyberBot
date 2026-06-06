package org.dreambot.api.methods.container.impl;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.wrappers.items.Item;
public class Inventory {
  public static boolean contains(String n){return false;}
  public static int count(String n){return 0;}
  public static int count(int id){return 0;}
  public static Item get(String n){return null;}
  public static Item get(Filter<Item> f){return null;}
  public static boolean interact(Item i,String a){return true;}
  public static boolean interact(String n,String a){return true;}
  public static boolean isFull(){return false;}
}
