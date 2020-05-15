package gameplay;

import com.jogamp.opengl.math.VectorUtil;

import graphics.Unit;
import sys.SysSetup;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

public class Player extends Unit
{
	public Player(float r, float x, float y) 
	{
		super(r, x, y);
	}
	
	@Override
	public void move(float[] dir, double dt)
	{
		super.move(dir, dt);
	}
}
