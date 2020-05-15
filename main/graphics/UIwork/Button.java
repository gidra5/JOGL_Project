package graphics.UIwork;

public class Button 
{
	public float x,y,w,h;
	boolean pressed=false, flocused=false, blocked=false; //1.is button pressed, 2.is mouse on button, 3.is button available for press action
	
	
	public Button(float x, float y, float w, float h) 
	{
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;
	}

}
