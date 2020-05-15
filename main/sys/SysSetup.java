package sys;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;

import gameplay.GameplayModel;
import graphics.*;

public class SysSetup
{
	public static Animator a; //runs renderer
	public static GameplayModel gpm=new GameplayModel(); //thread that processes all in-game communication
	public static Renderer r=new Renderer(); 
	
	public static void main(String[] args) {
		
		GLProfile profile = GLProfile.get(GLProfile.GL4);
    	GLCapabilities capabilities = new GLCapabilities(profile);
    	capabilities.setStereo(false);

    	GLWindow w = GLWindow.create(capabilities);

    	a = new Animator(w); 
    	w.addGLEventListener(r);
    	w.addKeyListener(gpm.input);
    	w.addMouseListener(gpm.input);
    	
    	final float pi=(float)(6.28318530718/25);
    	for(int i=0;i<25;i++)
    	{
    		Unit.elements[i]=(short)i;
    		Unit.vertices[2*i]=FloatUtil.cos(pi*i);
    		Unit.vertices[2*i+1]=FloatUtil.sin(pi*i);
    	}
    	
    	for(int i=1; i<0;i++)
    		r.drawUnits.add(new Unit(20+i/75,2f*1920*((float)Math.random()-0.5f),2f*1080*((float)Math.random()-0.5f)));
	    System.out.println("OK");
    	r.drawUnits.add(gpm.p);

        w.setFullscreen(true);
        a.setRunAsFastAsPossible(true);
        a.start();
        w.setVisible(true);
        gpm.start();
	}
    
}
