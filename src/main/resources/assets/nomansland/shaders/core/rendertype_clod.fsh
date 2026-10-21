#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

const float BAYER[16] = float[16](
    0.0625, 0.5625, 0.1875, 0.6875,
    0.8125, 0.3125, 0.9375, 0.4375,
    0.25,   0.75,   0.125,  0.625,
    1.0,    0.5,    0.875,  0.375
);

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    int px = int(mod(gl_FragCoord.x, 4.0));
    int py = int(mod(gl_FragCoord.y, 4.0));
    if (color.a < 0.1 || color.a < BAYER[py * 4 + px] - 0.001) {
        discard;
    }
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color.rgb *= lightMapColor.rgb;
    fragColor = linear_fog(vec4(color.rgb, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
