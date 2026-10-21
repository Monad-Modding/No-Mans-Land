#version 150
//#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D DiffuseSampler;
uniform sampler2D PreviousSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float zoomOut;
uniform float fadeOut;

out vec4 fragColor;

// https://www.shadertoy.com/view/Xltfzj
#define TAU 6.283

void main() {
    vec2 previousCoord = ((texCoord - vec2(.5)) * zoomOut) + vec2(.5);
    vec4 previousDiffuse = texture(PreviousSampler, previousCoord);

    vec4 currentDiffuse = texture(DiffuseSampler, texCoord);
//    if (currentDiffuse.a < 0.003) currentDiffuse = vec4(0.0);
//    if (previousDiffuse.a < 0.003) previousDiffuse = vec4(0.0);

    float previousAlpha = previousDiffuse.a * fadeOut;
    float currentAlpha = currentDiffuse.a;
    float compositeAlpha = clamp(currentAlpha + previousAlpha * (1.0 - currentAlpha), 0.0, 1.0);

    vec4 compositeColor = vec4(vec3(currentDiffuse.rgb * currentAlpha
            + previousDiffuse.rgb * previousAlpha * (1.0 - currentAlpha)) / compositeAlpha,
        compositeAlpha
    );

//    float directions = 16.0;
//    float quality = 3.0;
//    float size = 8.0;
//
//    // GL code that uses texture size is always cursed on some older computers
//    vec2 radius = textureSize2D(DiffuseSampler, 1) / size;
//
//    for (float d = 0.0; d < TAU; d += TAU / directions){
//        for (float i = 1.0 / quality; i <= 1.0; i += 1.0 / quality)
//            compositeColor += texture(DiffuseSampler, texCoord + vec2(cos(d), sin(d)) * radius * i);
//    }
//
//    // Output to screen
//    compositeColor /= quality * directions - 15.0;
    fragColor = compositeColor;
}
