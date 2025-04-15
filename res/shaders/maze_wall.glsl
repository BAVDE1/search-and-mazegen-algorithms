//--- VERT
#version 450 core

layout(location = 0) in vec2 pos;
layout(location = 1) in float wobbleSpeed;
layout(location = 2) in float wobbleIndex;

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

// use nonsense values lol
vec2 random(vec2 uv){
    uv = vec2(dot(uv, vec2(127.1, 311.7)), dot(uv, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(uv) * 43758.5453123);
}

float noise(vec2 uv) {
    vec2 uv_index = floor(uv);
    vec2 uv_fract = fract(uv);

    vec2 blur = smoothstep(0.0, 1.0, uv_fract);

    return mix( mix( dot( random(uv_index + vec2(0.0,0.0) ), uv_fract - vec2(0.0,0.0) ),
                dot( random(uv_index + vec2(1.0,0.0) ), uv_fract - vec2(1.0,0.0) ), blur.x),
                mix( dot( random(uv_index + vec2(0.0,1.0) ), uv_fract - vec2(0.0,1.0) ),
                dot( random(uv_index + vec2(1.0,1.0) ), uv_fract - vec2(1.0,1.0) ), blur.x), blur.y) + 0.5;
}

void main() {
    float n1 = noise((gl_FragCoord.xy + vec2(-time, cos(time)) * 10) * (.01 * scale));
    float n2 = noise((gl_FragCoord.xy + sin(time)) * (.02 * scale));
    float n = (n1 + n2) * .5;
    float mix1 = mix(.4, .5, smoothstep(.3, .4, n));
    float mix2 = mix(mix1, .65, smoothstep(.5, .6, n));
    float mix3 = mix(mix2, .8, smoothstep(.65, .75, n));
    colour = vec4(1, 1, 1, mix3 * .2);
}