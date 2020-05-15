package sys;

import java.io.IOException;
import java.nio.*;
import java.util.*;

import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.common.nio.Buffers;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

import graphics.Unit;
import gameplay.GameplayModel;

import static com.jogamp.opengl.GL4.*;

public class Renderer implements GLEventListener 
{
	private float[] res=new float[] {0,0}; //width and height of screen in pixels
    public HashSet<Unit> drawUnits=new HashSet<>();

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(3),
    				  IndirectBufferName = GLBuffers.newDirectIntBuffer(1), IndirectBuffer,
    				  vertexArrayName = GLBuffers.newDirectIntBuffer(1),
    				  FrameBufferName = GLBuffers.newDirectIntBuffer(1);
    private ByteBuffer instanceAttribPointer;
    public FloatBuffer instanceAttribBuffer;
	int[] frameTexture=new int[1]; //texture to render and postprocess
    boolean vsync=false;
    long framebufferTexture_handle;
    
    private int render_prog, post_prog; //shaders
    
    public Renderer() 
    {
    	
    }
    public void display(GLAutoDrawable gLDrawable) 
    {
        final GL4 gl = gLDrawable.getGL().getGL4();
        
       	gl.glBindFramebuffer(GL_FRAMEBUFFER, FrameBufferName.get(0));
        gl.glUseProgram(render_prog);
        gl.glBindVertexArray(vertexArrayName.get(0));	
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        gl.glClear(GL_COLOR_BUFFER_BIT);

        for(Unit u : drawUnits)
        {
        	instanceAttribBuffer.put(3*u.unitID  ,u.pos[0])
        						.put(3*u.unitID+1,u.pos[1]);
        }
        gl.glDrawArraysIndirect(GL_TRIANGLE_FAN, 0);
        gl.glBindVertexArray(0);	
        
       	gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        gl.glUseProgram(post_prog);
        gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        
        gl.glProgramUniform1i(post_prog, 3, SysSetup.a.getTotalFPSFrames()%3);

        checkErrors(gl);
    }
 
    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) 
    {
    	System.out.println("displayChanged called");
    }
 
    public void init(GLAutoDrawable glDrawable) 
    {
    	System.out.println("init() called");

        GL4 gl = glDrawable.getGL().getGL4();
        float[] fbo_vertices =new float[]{-1, -1,
        								   1, -1,
        								  -1,  1,
        								   1,  1};
        int[] indirectCommand=new int[] {50, drawUnits.size(), 0, 0};

        ///setting up needed features
        gl.glDisable(GL_CULL_FACE);
        gl.glDisable(GL_DEPTH_TEST);
        gl.glDisable(GL_STENCIL_TEST);
        gl.glDisable(GL_DEBUG_OUTPUT_SYNCHRONOUS); //speed up a little bit
        gl.setSwapInterval(1);                     //v sync on
        
        ///creating buffers and filling them
        
        gl.glCreateBuffers(3, bufferName);
        gl.glCreateBuffers(1, IndirectBufferName);
        gl.glGenFramebuffers(1, FrameBufferName);

        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(Unit.vertices);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
        gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBuffer.capacity()*4, vertexBuffer, 0);  //circle vertices
        
        gl.glBindBuffer(GL_DRAW_INDIRECT_BUFFER, IndirectBufferName.get(0));
        gl.glBufferStorage(GL_DRAW_INDIRECT_BUFFER, 16, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        
        IndirectBuffer=gl.glMapBufferRange(GL_DRAW_INDIRECT_BUFFER, 0, 16, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT).asIntBuffer();
        IndirectBuffer.put(indirectCommand);
        
       	gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(2));
       	gl.glBufferStorage(GL_ARRAY_BUFFER, 32, FloatBuffer.wrap(fbo_vertices),0); //to draw post-processed image

        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(1));
        gl.glBufferStorage(GL_ARRAY_BUFFER, 12000000, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT); //buffer for dynamic positions and radiuses of obj
        
        instanceAttribPointer=gl.glMapBufferRange(GL_ARRAY_BUFFER, 0, 12000000, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
        instanceAttribBuffer=instanceAttribPointer.asFloatBuffer();
        
        int i=0;  //filling buffer with info
        for(Unit u : drawUnits)
        {
        	instanceAttribBuffer.put(3*i,u.pos[0])
        	 					.put(3*i+1,u.pos[1])
        	 					.put(3*i+2,u.r);
        	u.unitID=i;
        	i++;
        }

        checkErrors(gl);

        ///Setting VAO
        
        gl.glGenVertexArrays(1, vertexArrayName);
        
        gl.glBindVertexArray(vertexArrayName.get(0));
        
        gl.glBindVertexBuffer(0, bufferName.get(0), 0, 8);  //verticies
        gl.glBindVertexBuffer(1, bufferName.get(1), 0, 12); //pos and radius attribute
        
        gl.glEnableVertexAttribArray(0);
        gl.glEnableVertexAttribArray(1);
        gl.glEnableVertexAttribArray(2);

        gl.glVertexAttribBinding(0, 0);
        gl.glVertexAttribBinding(1, 1);
        gl.glVertexAttribBinding(2, 1);
        
        gl.glVertexAttribFormat(0, 2, GL_FLOAT, false, 0);
        gl.glVertexAttribFormat(1, 2, GL_FLOAT, false, 0);
        gl.glVertexAttribFormat(2, 1, GL_FLOAT, false, 8);
 
        gl.glVertexBindingDivisor(1, 1);  //change pos each instance
        
        gl.glBindVertexArray(0);
        
        ///setting up shader(s)

        ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), "shaders", null, "defaultShader", "vert", null, true);
       	ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), "shaders", null, "defaultShader", "frag", null, true);
       	ShaderProgram shaderProgram = new ShaderProgram();
       	shaderProgram.add(vertShader);
       	shaderProgram.add(fragShader);
       	shaderProgram.init(gl);
       	render_prog = shaderProgram.program();
       	shaderProgram.link(gl, System.err);
       	
       	vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), "shaders", null, "postShader", "vert", null, true);
        fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), "shaders", null, "postShader-aa", "frag", null, true);
       	ShaderProgram shaderProgram2 = new ShaderProgram();
       	shaderProgram2.add(vertShader);
       	shaderProgram2.add(fragShader);
       	shaderProgram2.init(gl);
       	post_prog = shaderProgram2.program();
       	shaderProgram2.link(gl, System.err);

       	checkErrors(gl);

        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(2));
        gl.glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0 );
        gl.glEnableVertexAttribArray(0);
    }
	
    public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) 
    {
    	System.out.println("reshape() called: x = "+x+", y = "+y+", width = "+width+", height = "+height);
    	res[0]=width;
    	res[1]=height;
        GL4 gl = glDrawable.getGL().getGL4();

        gl.glProgramUniform2f(render_prog, 3, width, height);
        gl.glProgramUniform2f(post_prog, 1, (float)(1.0f/width), (float)(1.0f/height));

        gl.glDeleteTextures(1, frameTexture, 0);
        gl.glCreateTextures(GL_TEXTURE_2D, 1, frameTexture, 0);
        
        gl.glBindTexture(GL_TEXTURE_2D, frameTexture[0]);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, (int)(width), (int)(height), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        gl.glBindTexture(GL_TEXTURE_2D, 0);

       	gl.glBindFramebuffer(GL_FRAMEBUFFER, FrameBufferName.get(0));
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, frameTexture[0], 0);
       	gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        framebufferTexture_handle=gl.glGetTextureHandleARB(frameTexture[0]);
        gl.glMakeTextureHandleResidentARB(framebufferTexture_handle);
       	gl.glProgramUniformHandleui64ARB(post_prog, 2, framebufferTexture_handle);

        checkErrors(gl);
    }
 
 
	public void dispose(GLAutoDrawable glDrawable) 
	{
		System.out.println("dispose() called");
        GL4 gl = glDrawable.getGL().getGL4();
        
        gl.glUseProgram(0);
        
        gl.glBindVertexArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(1));
        gl.glUnmapBuffer(GL_ARRAY_BUFFER);

        gl.glDeleteProgram(render_prog);
     	gl.glDeleteProgram(post_prog);
        gl.glDeleteVertexArrays(1, vertexArrayName);
        gl.glDeleteBuffers(3, bufferName);
        gl.glDeleteFramebuffers(1, FrameBufferName);
        gl.glDeleteTextures(1, frameTexture, 0);
        
        checkErrors(gl);
        
		System.exit(0);
	}
	
	void checkErrors(GL4 gl)
	{
		int err,fbo_status;
		if(gl.glGetError()==GL_NO_ERROR)
			return;
		while((err=gl.glGetError())!=GL_NO_ERROR)
		{
			if(err==GL_INVALID_ENUM)
				System.out.println("GL_INVALID_ENUM");
			else if(err==GL_INVALID_VALUE)
				System.out.println("GL_INVALID_VALUE");
			else if(err==GL_INVALID_OPERATION)
				System.out.println("GL_INVALID_OPERATION");
			else if(err==GL_INVALID_FRAMEBUFFER_OPERATION)
				System.out.println("GL_INVALID_FRAMEBUFFER_OPERATION");
			else if(err==GL_OUT_OF_MEMORY)
				System.out.println("GL_OUT_OF_MEMORY");
			else if(err==GL_STACK_UNDERFLOW)
				System.out.println("GL_STACK_UNDERFLOW");
			else if(err==GL_STACK_OVERFLOW)
				System.out.println("GL_STACK_OVERFLOW");
		}
		/*
		fbo_status=gl.glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if(fbo_status==GL_FRAMEBUFFER_COMPLETE)
			System.out.println("GL_FRAMEBUFFER_COMPLETE");
		else if(fbo_status==GL_FRAMEBUFFER_UNDEFINED)
			System.out.println("GL_FRAMEBUFFER_UNDEFINED");
		else if(fbo_status==GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
			System.out.println("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
		else if(fbo_status==GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
			System.out.println("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
		else if(fbo_status==GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER)
			System.out.println("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
		else if(fbo_status==GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER )
			System.out.println("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
		else if(fbo_status==GL_FRAMEBUFFER_UNSUPPORTED)
			System.out.println("GL_FRAMEBUFFER_UNSUPPORTED");
		else if(fbo_status==GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE)
			System.out.println("GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE");
		else if(fbo_status==GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS)
			System.out.println("GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS");
		System.out.println("");*/
	}
}