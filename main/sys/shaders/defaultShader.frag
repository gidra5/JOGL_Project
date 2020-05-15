#version 450

precision lowp float;
precision lowp int;

in vec4 color;

void main()
{
    gl_FragColor = color;
}
