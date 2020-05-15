package sys;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

//class to process multiple inputs from mouse and keyboard

public class InputListener implements KeyListener, MouseListener
{
	public volatile boolean[] key=new boolean[256]; 
	public volatile boolean[] mouse=new boolean[16]; // left click - 0, middle click (mouse wheel) - 1, right click - 2, wheel up/down - 3/4 and other stuff
	public volatile float[] mousePos=new float[2];

	public InputListener() 
	{}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if(!e.isAutoRepeat())
			key[e.getKeyCode()]=true;
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		if(!e.isAutoRepeat())
			key[e.getKeyCode()]=false;
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		//mouse[e.getButton()]=true;
	}

	@Override
	public void mouseDragged(MouseEvent e) 
	{
		
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		mousePos[0]=(float)e.getX();
		mousePos[1]=(float)e.getY();
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		mouse[e.getButton()]=true;
	}

	@Override
	public void mouseReleased(MouseEvent e) 
	{
		mouse[e.getButton()]=false;
	}
	
	

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
