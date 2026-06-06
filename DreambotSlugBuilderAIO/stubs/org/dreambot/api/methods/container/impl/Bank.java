package org.dreambot.api.methods.container.impl;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.wrappers.items.Item;
public class Bank {
  public static boolean isOpen(){return false;}
  public static boolean open(){return true;}
  public static boolean close(){return true;}
  public static boolean contains(String n){return false;}
  public static boolean depositAll(String n){return true;}
  public static boolean depositAllExcept(Filter<Item> f){return true;}
  public static boolean depositAllExcept(String... n){return true;}
  public static boolean withdraw(String n,int amt){return true;}
  public static boolean withdrawAll(String n){return true;}
}
