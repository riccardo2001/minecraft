#version 330 core

layout(location = 0) in vec3 aPos;

uniform vec2 uScreenSize;

void main() {
    // Converti coordinate da pixel a spazio NDC (-1 to 1)
    vec2 pos = (aPos.xy * 2.0 / uScreenSize) - 1.0;
    gl_Position = vec4(pos.x, -pos.y, 0.0, 1.0);
}