#version 330 core

uniform vec3 crosshairColor;
out vec4 fragColor;

void main() {
    fragColor = vec4(crosshairColor, 1.0);
}