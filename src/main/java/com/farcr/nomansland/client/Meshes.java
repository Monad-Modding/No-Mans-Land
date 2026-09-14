package com.farcr.nomansland.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.Mth;

public class Meshes {
    public static MeshData hemisphere(Tesselator tesselator, int resolutionTheta, int resolutionPhi, float maximumTheta, float radius) {
        return hemisphere(tesselator, resolutionTheta, resolutionPhi, maximumTheta, radius,
                1, 1, 1, 1, 1, 1, 1, 1,1);
    }

    public static MeshData hemisphere(Tesselator tesselator, int resolutionTheta, int resolutionPhi, float maximumTheta, float radius,
                                              float maxThetaR, float maxThetaG, float maxThetaB, float maxThetaA,
                                              float poleR, float poleG, float poleB, float poleA,
                                              float power) {
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        float tMultiplier = (1.0F / resolutionTheta) * maximumTheta,
              multiplier = (1.0F / resolutionPhi) * Mth.TWO_PI;
        // poleFan
        float poleFanRadius = Mth.sin(tMultiplier) * radius,
              poleFanY = Mth.cos(tMultiplier),
              poleFanUVScale = (1.0F / resolutionTheta) * 0.5F,
              poleFanFac = (float) Math.pow(1.0F - (1.0F / resolutionTheta), power),
                
              poleFanR = Mth.lerp(poleFanFac, maxThetaR, poleR), 
              poleFanG = Mth.lerp(poleFanFac, maxThetaG, poleG), 
              poleFanB = Mth.lerp(poleFanFac, maxThetaB, poleB), 
              poleFanA = Mth.lerp(poleFanFac, maxThetaA, poleA);
        for (int p = 0; p < resolutionPhi; p++) {
            float phi0 = (p + 0F) * multiplier,
                  phi1 = (p + 1F) * multiplier;
            float x0 = Mth.sin(phi0), z0 = Mth.cos(phi0),
                  x1 = Mth.sin(phi1), z1 = Mth.cos(phi1);

            bufferbuilder.addVertex(0, radius, 0)
                    .setUv(0.5F, 0.5F)
                    .setColor(poleR, poleG, poleB, poleA);
            bufferbuilder.addVertex(x1 * poleFanRadius, poleFanY, z1 * poleFanRadius)
                    .setUv(x1 * poleFanUVScale + 0.5F, z1 * poleFanUVScale + 0.5F)
                    .setColor(poleFanR, poleFanG, poleFanB, poleFanA);
            bufferbuilder.addVertex(x0 * poleFanRadius, poleFanY, z0 * poleFanRadius)
                    .setUv(x0 * poleFanUVScale + 0.5F, z0 * poleFanUVScale + 0.5F)
                    .setColor(poleFanR, poleFanG, poleFanB, poleFanA);
        }

        // outer rings
        for (int t = 1; t < resolutionTheta; t++) {
            float uvScale0 = ((t + 0F) / resolutionTheta) * 0.5F,
                  uvScale1 = ((t + 1F) / resolutionTheta) * 0.5F;
            float theta0 = (t + 0F) * tMultiplier,
                  theta1 = (t + 1F) * tMultiplier;
            float thetaScale0 = Mth.sin(theta0) * radius,
                  thetaScale1 = Mth.sin(theta1) * radius;
            float y0 = Mth.cos(theta0) *  radius,
                  y1 = Mth.cos(theta1) * radius;

            float fac0 = (float) Math.pow(1.0F - ((t + 0F) / resolutionTheta), power),
                  fac1 = (float) Math.pow(1.0F - ((t + 1F) / resolutionTheta), power);
            float r0 = Mth.lerp(fac0, maxThetaR, poleR), g0 = Mth.lerp(fac0, maxThetaG, poleG), b0 = Mth.lerp(fac0, maxThetaB, poleB), a0 = Mth.lerp(fac0, maxThetaA, poleA);
            float r1 = Mth.lerp(fac1, maxThetaR, poleR), g1 = Mth.lerp(fac1, maxThetaG, poleG), b1 = Mth.lerp(fac1, maxThetaB, poleB), a1 = Mth.lerp(fac1, maxThetaA, poleA);
            for (int p = 0; p < resolutionPhi; p++) {
                float phi0 = (p + 0F) * multiplier,
                      phi1 = (p + 1F) * multiplier;
                float x0 = Mth.sin(phi0), z0 = Mth.cos(phi0);
                float x1 = Mth.sin(phi1), z1 = Mth.cos(phi1);

                bufferbuilder.addVertex(x0 * thetaScale0, y0, z0 * thetaScale0)
                        .setUv(x0 * uvScale0 + 0.5F, z0 * uvScale0 + 0.5F)
                        .setColor(r0, g0, b0, a0);
                bufferbuilder.addVertex(x1 * thetaScale0, y0, z1 * thetaScale0)
                        .setUv(x1 * uvScale0 + 0.5F, z1 * uvScale0 + 0.5F)
                        .setColor(r0, g0, b0, a0);
                bufferbuilder.addVertex(x0 * thetaScale1, y1, z0 * thetaScale1)
                        .setUv(x0 * uvScale1 + 0.5F, z0 * uvScale1 + 0.5F)
                        .setColor(r1, g1, b1, a1);

                bufferbuilder.addVertex(x1 * thetaScale1, y1, z1 * thetaScale1)
                        .setUv(x1 * uvScale1 + 0.5F, z1 * uvScale1 + 0.5F)
                        .setColor(r1, g1, b1, a1);
                bufferbuilder.addVertex(x0 * thetaScale1, y1, z0 * thetaScale1)
                        .setUv(x0 * uvScale1 + 0.5F, z0 * uvScale1 + 0.5F)
                        .setColor(r1, g1, b1, a1);
                bufferbuilder.addVertex(x1 * thetaScale0, y0, z1 * thetaScale0)
                        .setUv(x1 * uvScale0 + 0.5F, z1 * uvScale0 + 0.5F)
                        .setColor(r0, g0, b0, a0);
            }
        }

        return bufferbuilder.buildOrThrow();
    }

    public static MeshData texturelessHemisphere(Tesselator tesselator, int resolutionTheta, int resolutionPhi, float maximumTheta, float radius,
                                                  float maxThetaR, float maxThetaG, float maxThetaB, float maxThetaA,
                                                  float poleR, float poleG, float poleB, float poleA,
                                                  float power, float offset) {
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        float tMultiplier = (1.0F / resolutionTheta) * maximumTheta,
              multiplier = (1.0F / resolutionPhi) * Mth.TWO_PI;
        // poleFan
        float poleFanRadius = Mth.sin(tMultiplier) * radius,
              poleFanY = Mth.cos(tMultiplier),

              poleFanFac = (float) Math.pow(Mth.smoothstep(Mth.clampedMap(1.0F - (1.0F / resolutionTheta), offset > 0 ? offset : 0, offset > 0 ? 1 : (1 + offset), 0.0F, 1.0F)), power),
              poleFanR = Mth.lerp(poleFanFac, maxThetaR, poleR),
              poleFanG = Mth.lerp(poleFanFac, maxThetaG, poleG),
              poleFanB = Mth.lerp(poleFanFac, maxThetaB, poleB),
              poleFanA = Mth.lerp(poleFanFac, maxThetaA, poleA);
        for (int p = 0; p < resolutionPhi; p++) {
            float phi0 = (p + 0F) * multiplier,
                    phi1 = (p + 1F) * multiplier;
            float x0 = Mth.sin(phi0), z0 = Mth.cos(phi0),
                    x1 = Mth.sin(phi1), z1 = Mth.cos(phi1);

            bufferbuilder.addVertex(0, radius, 0)
                    .setColor(poleR, poleG, poleB, poleA);
            bufferbuilder.addVertex(x1 * poleFanRadius, poleFanY, z1 * poleFanRadius)
                    .setColor(poleFanR, poleFanG, poleFanB, poleFanA);
            bufferbuilder.addVertex(x0 * poleFanRadius, poleFanY, z0 * poleFanRadius)
                    .setColor(poleFanR, poleFanG, poleFanB, poleFanA);
        }

        // outer rings
        for (int t = 1; t < resolutionTheta; t++) {
            float theta0 = (t + 0F) * tMultiplier,
                  theta1 = (t + 1F) * tMultiplier;
            float thetaScale0 = Mth.sin(theta0) * radius,
                  thetaScale1 = Mth.sin(theta1) * radius;
            float y0 = Mth.cos(theta0) *  radius,
                  y1 = Mth.cos(theta1) * radius;
            float fac0 = (float) Math.pow(Mth.smoothstep(Mth.clampedMap(1.0F - ((t + 0F) / resolutionTheta), offset > 0 ? offset : 0, offset > 0 ? 1 : (1 + offset), 0.0F, 1.0F)), power),
                  fac1 = (float) Math.pow(Mth.smoothstep(Mth.clampedMap(1.0F - ((t + 1F) / resolutionTheta), offset > 0 ? offset : 0, offset > 0 ? 1 : (1 + offset), 0.0F, 1.0F)), power);
            float r0 = Mth.lerp(fac0, maxThetaR, poleR), g0 = Mth.lerp(fac0, maxThetaG, poleG), b0 = Mth.lerp(fac0, maxThetaB, poleB), a0 = Mth.lerp(fac0, maxThetaA, poleA);
            float r1 = Mth.lerp(fac1, maxThetaR, poleR), g1 = Mth.lerp(fac1, maxThetaG, poleG), b1 = Mth.lerp(fac1, maxThetaB, poleB), a1 = Mth.lerp(fac1, maxThetaA, poleA);
            for (int p = 0; p < resolutionPhi; p++) {
                float phi0 = (p + 0F) * multiplier,
                      phi1 = (p + 1F) * multiplier;
                float x0 = Mth.sin(phi0), z0 = Mth.cos(phi0);
                float x1 = Mth.sin(phi1), z1 = Mth.cos(phi1);

                bufferbuilder.addVertex(x0 * thetaScale0, y0, z0 * thetaScale0)
                        .setColor(r0, g0, b0, a0);
                bufferbuilder.addVertex(x1 * thetaScale0, y0, z1 * thetaScale0)
                        .setColor(r0, g0, b0, a0);
                bufferbuilder.addVertex(x0 * thetaScale1, y1, z0 * thetaScale1)
                        .setColor(r1, g1, b1, a1);

                bufferbuilder.addVertex(x1 * thetaScale1, y1, z1 * thetaScale1)
                        .setColor(r1, g1, b1, a1);
                bufferbuilder.addVertex(x0 * thetaScale1, y1, z0 * thetaScale1)
                        .setColor(r1, g1, b1, a1);
                bufferbuilder.addVertex(x1 * thetaScale0, y0, z1 * thetaScale0)
                        .setColor(r0, g0, b0, a0);
            }
        }
        return bufferbuilder.buildOrThrow();
    }
}
