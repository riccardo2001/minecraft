#version 330

in vec2 outTextCoord;
in vec3 vertexPos;      // Posizione del vertice per calcolare la nebbia

out vec4 fragColor;

uniform sampler2D txtSampler;
uniform vec3 fogColor;
uniform vec3 cameraPosition;
uniform float fogDensity;
uniform float fogGradient;

void main()
{
    vec4 textureColor = texture(txtSampler, outTextCoord);
    
    if(textureColor.a < 0.1) {
        discard;
    }
    
    float distance = length(vertexPos - cameraPosition);
    float fogFactor = exp(-pow(distance * fogDensity, fogGradient));
    fogFactor = clamp(fogFactor, 0.0, 1.0);
    
    fragColor = mix(vec4(fogColor, 1.0), textureColor, fogFactor);
}
