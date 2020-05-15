#version 450

precision lowp float;
precision lowp int;

layout (location = 0) in vec2 v_coord;
layout (location = 1) uniform vec2 inverseVP;
layout (location = 2) uniform sampler2D fbo_texture;
layout (location = 3) uniform int n;

out vec2 f_texcoord;

void main(void)
{
  gl_Position = vec4(v_coord, 1.0, 1.0);
  f_texcoord = (v_coord + 1.0) / 2.0;
}
