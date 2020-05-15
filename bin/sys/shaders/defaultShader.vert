#version 450

precision lowp float;
precision lowp int;

layout (location = 0) in vec2 vertexPos;
layout (location = 1) in vec2 pos;
layout (location = 2) in float r;

layout (location = 3) uniform vec2 res;

flat out vec4 color;

void main()
{ 
	vec2 p=(vertexPos*r+pos)/res;
    gl_Position = vec4(p, 1, 1);
    color=vec4(0, 0.5, 0.5, 0);
}
