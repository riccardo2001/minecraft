#version 330

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 texCoord;

out vec2 outTexCoord;

uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main() {
    gl_Position = projectionMatrix * modelMatrix * vec4(position, 0.0, 1.0);
    outTexCoord = texCoord;
}