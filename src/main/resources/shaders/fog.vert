#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

out vec2 outTextCoord;
out vec3 vertexPos;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

void main()
{
    vec4 worldPos = modelMatrix * vec4(position, 1.0);
    vertexPos = worldPos.xyz;
    gl_Position = projectionMatrix * viewMatrix * worldPos;
    outTextCoord = texCoord;
}
