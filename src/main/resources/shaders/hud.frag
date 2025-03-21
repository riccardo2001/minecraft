#version 330

in vec2 outTexCoord;
out vec4 fragColor;

uniform sampler2D txtSampler;
uniform vec4 color;
uniform vec4 texAtlasCoords;

void main() {
    vec2 finalUV = vec2(
        texAtlasCoords.x + outTexCoord.x * (texAtlasCoords.z - texAtlasCoords.x),
        texAtlasCoords.y + (1.0 - outTexCoord.y) * (texAtlasCoords.w - texAtlasCoords.y)
    );
    
    vec4 texColor = texture(txtSampler, finalUV);
    fragColor = texColor * color;
    
    if(fragColor.a < 0.1)
        discard;
}