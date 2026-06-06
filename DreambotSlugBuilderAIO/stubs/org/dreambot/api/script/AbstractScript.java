package org.dreambot.api.script;
import java.awt.Graphics;
public abstract class AbstractScript {
  public void onStart(){}
  public int onLoop(){return 0;}
  public void onPaint(Graphics g){}
  public void onExit(){}
  public void stop(){}
  public void log(String s){}
  public void log(Object o){}
}
