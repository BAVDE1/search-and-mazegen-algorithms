//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in float status;
layout(location = 2) in float wobbleSpeed;
layout(location = 3) in float wobbleIndex;

uniform mat4 projectionMatrix;
uniform float wobbleFrequency;
uniform float time;

void main() {
    // wobble
    float t = time * (5 * wobbleSpeed) + wobbleIndex;
    vec2 wobble = vec2(sin(t));
    wobble.y += cos(t * 1.5);
    wobble *= wobbleFrequency * int(wobbleSpeed > 0.001);  // yes or no

    gl_Position = vec4(pos.xy + wobble, 1, 1) * projectionMatrix;
}

//--- FRAG
#version 450 core

uniform float time;
uniform float scale;

out vec4 colour;

void main() {
    float lineScale = max(5, 25 * (1.4 - scale));
    float x = -time + gl_FragCoord.x / lineScale;
    float y = gl_FragCoord.y / lineScale;
    float sum = x - y;

    float alpha = .5 + (.2 * int(mod(float(sum), 2.)));
    colour = vec4(1, 1, 1, alpha);
}