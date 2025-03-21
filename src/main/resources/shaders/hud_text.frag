#version 330 core
out vec4 FragColor;
uniform vec3 color; // Uniform per il colore

void main() {
    FragColor = vec4(color, 1.0);
}