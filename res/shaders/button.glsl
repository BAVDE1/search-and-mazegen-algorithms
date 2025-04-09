//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 texturePos;
layout(location = 2) in vec4 colour;
layout(location = 3) in float isMouseHovering;
layout(location = 4) in float wobbleIndex;

uniform mat4 projectionMatrix;
uniform float time;

out vec2 v_texturePos;
out vec4 v_colour;
out float v_isMouseHovering;

void main() {
    // hovering wobble
    float t = time * 5 + (wobbleIndex * .8);
    vec2 wobble = vec2(sin(t), cos(t)) * 8 * isMouseHovering;

    // regular
    gl_Position = vec4(pos.xy + wobble, 1, 1) * projectionMatrix;
    v_texturePos = texturePos;
    v_colour = colour;
    v_isMouseHovering = isMouseHovering;
}

//--- FRAG
#version 450 core

uniform sampler2D fontTexture;

in vec2 v_texturePos;
in vec4 v_colour;
in float v_isMouseHovering;

out vec4 colour;

void main() {
    float alpha = v_texturePos.x > -1 ? texture(fontTexture, v_texturePos.xy).a : 1;
    colour = (v_colour * alpha) / 255;
//    if (v_isMouseHovering > .5) {
//        colour.a = 0.;
//    }
//    colour -= vec4(1, 1, 1, 0) * v_isMouseHovering;
//    if (v_isMouseHovering > .5) {
//        colour -= vec4(1, 1, 1, 0);
//    }
}