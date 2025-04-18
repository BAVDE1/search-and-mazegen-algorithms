//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec4 colour;

uniform mat4 projectionMatrix;

out vec4 v_colour;

void main() {
    gl_Position = vec4(pos.xy, 1, 1) * projectionMatrix;
    v_colour = colour;
}

//--- FRAG
#version 450 core

in vec4 v_colour;

out vec4 colour;

void main() {
    colour = v_colour;
}