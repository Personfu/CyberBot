package org.dreambot.api.wrappers.interactive;
public class Player extends Character<Player> {
  public boolean isMoving(){return false;}
  public int getCombatLevel(){return 3;}
  public Character<?> getInteractingCharacter(){return null;}
}
