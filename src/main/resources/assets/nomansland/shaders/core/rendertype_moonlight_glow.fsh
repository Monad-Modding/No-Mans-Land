#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;

uniform float ElapsedTime;
uniform float GlintOpacity;

in float vertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

const vec3 fixedColor = vec3(
    147. / 255.,
    157. / 255.,
    107. / 255.
);

void main() {
    vec2 uv = texCoord0;
    float alpha = min(.66, abs(sin(ElapsedTime / 250.)) * GlintOpacity);
    vec4 color = vec4((fixedColor.rgb * alpha), alpha);

    if (color.a < 0.1)
        discard;

    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd);
    fragColor = vec4(color.rgb * fade, color.a);
}