package graphics;

import sys.SysSetup;

public class Unit
{
	/*public static final float[] vertices_lod3=new float[100]; //shape, r>=500
	public static final short[] elements_lod3=new short[50];  //indicies if needed, r>=500
	public static final float[] vertices_lod2=new float[50]; //shape, r>=100
	public static final short[] elements_lod2=new short[25];  //indicies if needed, r>=100
	public static final float[] vertices_lod1=new float[16]; //shape, r>=50
	public static final short[] elements_lod1=new short[8];  //indicies if needed, r>=50
	public static final float[] vertices_lod0=new float[12]; //shape, r<=10
	public static final short[] elements_lod0=new short[6];  //indicies if needed, r<=10
	*/
	public static final float[] vertices=new float[50]; //shape, r>=100
	public static final short[] elements=new short[25]; //indicies if needed, r>=100
	public volatile float[] pos;
	public volatile float r; //r is radius of circle
	public int unitID=-1; //id in renderer's buffer, if -1 then isnt there
	protected float vel=1000;
	
	public Unit(float r,float x, float y) 
	{ 
		this.r=r;
		pos=new float[] {x,y};
	}
	
	public void move(float[] dir, double dt) //dir should be normalized!!!
	{
		dir[0]*=vel*dt;
		dir[1]*=vel*dt;
		
		pos[0]+=dir[0];
		pos[1]+=dir[1];
	}
}
