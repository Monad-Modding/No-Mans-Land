#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform float DesaturateAmount;

out vec4 fragColor;

// https://www.shadertoy.com/view/lsdXDH
vec4 desaturate(vec4 color) {
    float bw = (min(color.r, min(color.g, color.b)) + max(color.r, max(color.g, color.b))) * 0.5;
    return vec4(bw, bw, bw, color.a);
}

void main(){
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    vec4 desaturatedColor = desaturate(diffuseColor);
    vec4 outColor = mix(diffuseColor, desaturatedColor, DesaturateAmount);
    fragColor = vec4(outColor.rgb, 1.0);
}