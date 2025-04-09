//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in vec2 texturePos;
layout(location = 2) in vec4 colour;
layout(location = 3) in float isWobbling;
layout(location = 4) in float wobbleIndex;

uniform mat4 projectionMatrix;
uniform float time;

out vec2 v_texturePos;
out vec4 v_colour;
out float v_isMouseHovering;

void main() {
    // hovering wobble
    float t = time * (5 * isWobbling) + wobbleIndex;
    vec2 wobble = vec2(sin(t)) * 6;
    wobble.y += cos(t * 1.5) * 6;
    wobble *= isWobbling;

    // regular
    gl_Position = vec4(pos.xy + wobble, 1, 1) * projectionMatrix;
    v_texturePos = texturePos;
    v_colour = colour;
    v_isMouseHovering = isWobbling;
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
}