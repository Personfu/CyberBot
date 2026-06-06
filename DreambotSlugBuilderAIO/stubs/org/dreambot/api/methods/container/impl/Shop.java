package org.dreambot.api.methods.container.impl;
import org.dreambot.api.wrappers.items.Item;
public class Shop {
  public static boolean isOpen(){return false;}
  public static boolean open(){return true;}
  public static boolean close(){return true;}
  public static Item get(String n){return null;}
  public static boolean purchase(Item i,int amt){return true;}
}
