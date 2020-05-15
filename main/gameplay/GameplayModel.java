package gameplay;

import static com.jogamp.newt.event.KeyEvent.*;

import sys.InputListener;
import sys.SysSetup;

public class GameplayModel extends Thread 
{
	public GameplayModel() {}
	
	public static InputListener input=new InputListener();
	public volatile static Player p=new Player(500f,0f,0f);

    public double lastTime= System.nanoTime();
    private double dt = 0;
	
	@Override
	public void run()
	{
		System.out.println("running");
		while(true)
		{
			dt=(System.nanoTime()-lastTime)/1e+9;
			
			if(input.key[VK_ESCAPE])
				break;
			if(input.key[VK_W])
				p.move(new float[] {0 ,1}, dt);
			if(input.key[VK_A])
				p.move(new float[] {-1,0}, dt);
			if(input.key[VK_S])
				p.move(new float[] {0,-1}, dt);
			if(input.key[VK_D])
				p.move(new float[] {1 ,0}, dt);
			
			lastTime= System.nanoTime();
		}
		SysSetup.a.stop();
		System.exit(0);
	}
}
