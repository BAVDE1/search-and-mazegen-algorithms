//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in float wobbleStrength;
layout(location = 2) in float wobbleIndex;
layout(location = 3) in float alpha;

uniform mat4 projectionMatrix;
uniform float time;

out vec4 v_colour;

void main() {
    // wobble
    float t = time * (5 * wobbleStrength) + wobbleIndex;
    vec2 wobble = vec2(sin(t));
    wobble.y += cos(t * 1.5);
    wobble *= 6 * int(wobbleStrength > 0.001);  // yes or no

    gl_Position = vec4(pos.xy + wobble, 1, 1) * projectionMatrix;
    v_colour = vec4(wobbleIndex * .25, wobbleIndex * .25, 1, alpha);
}

//--- FRAG
#version 450 core

in vec4 v_colour;

out vec4 colour;

void main() {
    colour = v_colour;
}