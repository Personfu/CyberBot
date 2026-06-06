package org.dreambot.api.methods.magic;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.interactive.Character;
public class Magic {
  public static boolean canCast(Spell s){return true;}
  public static boolean castSpell(Spell s){return true;}
  public static boolean castSpellOn(Spell s, Character<?> e){return true;}
  public static boolean castSpellOnItem(Spell s, Item i){return true;}
  public static boolean isSpellSelected(){return false;}
}
