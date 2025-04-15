//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in float status;
layout(location = 2) in float wobbleSpeed;
layout(location = 3) in float wobbleIndex;

uniform mat4 projectionMatrix;
uniform float wobbleFrequency;
uniform float time;

out vec3 v_colour;

void main() {
    // wobble
    float t = time * (5 * wobbleSpeed) + wobbleIndex;
    vec2 wobble = vec2(sin(t));
    wobble.y += cos(t * 1.5);
    wobble *= wobbleFrequency * int(wobbleSpeed > 0.001);  // yes or no

    gl_Position = vec4(pos.xy + wobble, 1, 1) * projectionMatrix;
    int r = 1-int(status == 3 || status == 4);
    int g = 1-int(status == 5);
    int b = 1-int(status == 2 || status == 4 || status == 5);
    v_colour = vec3(r, g, b);
}

//--- FRAG
#version 450 core

uniform float time;
uniform float maxScale;  // set on runtime
uniform float scale;

in vec3 v_colour;

out vec4 colour;

void main() {
    float lineScale = 1 / max(5, 25 * (maxScale - scale));
    float x = -time + gl_FragCoord.x * lineScale;
    float y = gl_FragCoord.y * lineScale;
    float sum = x - y;

    float alpha = .5 + (.2 * int(mod(sum, 2.)));
    colour = vec4(v_colour, alpha);
}