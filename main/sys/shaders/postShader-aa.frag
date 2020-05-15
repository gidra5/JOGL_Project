#version 450

precision highp float;
precision highp int;

uniform sampler2D fbo_texture;
uniform int n;
uniform vec2 inverseVP;
in vec2 f_texcoord;

void main(void)
{
	vec2 d;
	if(n==1)
		d=inverseVP*vec2(-0.5,-0.866025);
	else if(n==0)
		d=inverseVP*vec2(-0.866025,0.5);
	else if(n==2)
		d=inverseVP*vec2(-1,0);/*
	else if(n==3)
		d=inverseVP*vec2(-1,0);
	else if(n==4)
		d=inverseVP*vec2(0.866025,0.5);
	else if(n==5)
		d=inverseVP*vec2(-0.5,0.866025);*/
	gl_FragColor = texture2D(fbo_texture,f_texcoord+d);
}
