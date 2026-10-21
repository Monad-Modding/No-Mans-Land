package com.farcr.nomansland.client.model;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class BuriedAnimation {
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(0)
                .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, -12.5F, -0), AnimationChannel.Interpolations.LINEAR)
                ))
                .build();

    public static final AnimationDefinition CRAWL = AnimationDefinition.Builder.withLength(2).looping()
                .addAnimation("buried", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, 0, -3), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.posVec(0, 0, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 0, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 3), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, 0, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.posVec(0, 0, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, 0, -3), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1667F, KeyframeAnimations.posVec(0, 0, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.posVec(0, 0, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.5F, KeyframeAnimations.posVec(0, 0, 3), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.posVec(0, 0, 1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8333F, KeyframeAnimations.posVec(0, 0, -1.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.posVec(0, 0, -3), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-12, -12.5F, -3.54F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(1, -12.5F, -4.83F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(6, -12.5F, -4.83F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-2, -12.5F, -3.54F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(6, -12.5F, -1.29F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(1, -12.5F, 1.29F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-12, -12.5F, 3.54F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1667F, KeyframeAnimations.degreeVec(1, -12.5F, 4.83F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.degreeVec(6, -12.5F, 4.83F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.5F, KeyframeAnimations.degreeVec(-2, -12.5F, 3.54F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.degreeVec(6, -12.5F, 1.29F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8333F, KeyframeAnimations.degreeVec(1, -12.5F, -1.29F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.degreeVec(-12, -12.5F, -3.54F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-4, 0, -10), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-2, 0, -8.66F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(2, 0, -5), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(4, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(2, 0, 5), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-2, 0, 8.66F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-4, 0, 10), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.1667F, KeyframeAnimations.degreeVec(-2, 0, 8.66F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.degreeVec(2, 0, 5), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.5F, KeyframeAnimations.degreeVec(4, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.degreeVec(2, 0, -5), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.8333F, KeyframeAnimations.degreeVec(-2, 0, -8.66F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.degreeVec(-4, 0, -10), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, 0, -0.85F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 0, -1.16F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, 0, -0.31F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.posVec(0, 0, -0.85F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightLeg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, 0, -0.85F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.posVec(0, 0, -1.16F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.posVec(0, 0, -0.31F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-26.81F, 0, 25), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-32.89F, 0, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-6.08F, 0, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-0, 0, -25), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-0, 0, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.degreeVec(-0, 0, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.degreeVec(-26.81F, 0, 25), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, 2.12F, 2), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 2.9F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, 0.78F, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, -2.12F, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.posVec(0, -2.9F, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.posVec(0, -0.78F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.posVec(0, 2.12F, 2), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, 0, 25), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-0, 0, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-0, 0, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-26.81F, 0, -25), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-32.89F, 0, -12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.degreeVec(-6.08F, 0, 12.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.degreeVec(-0, 0, 25), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, -2.12F, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.posVec(0, -2.9F, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, -0.78F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, 2.12F, 2), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.3333F, KeyframeAnimations.posVec(0, 2.9F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1.6667F, KeyframeAnimations.posVec(0, 0.78F, 0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(2, KeyframeAnimations.posVec(0, -2.12F, 0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

    public static final AnimationDefinition CHASING = AnimationDefinition.Builder.withLength(1).looping()
                .addAnimation("buried", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, -3.54F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-0, -4.83F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0, 3.54F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-0, 4.83F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0, -3.54F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-0, -4.83F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0, 3.54F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-0, 4.83F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-0, 1.29F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-0, -3.54F, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(32.5F, -12.5F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0417F, KeyframeAnimations.degreeVec(33.75F, -12.5F, -1.25F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(37.5F, -12.5F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(32.5F, -12.5F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(37.5F, -12.5F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(32.5F, -12.5F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.625F, KeyframeAnimations.degreeVec(37.5F, -12.5F, -2.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(32.5F, -12.5F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.875F, KeyframeAnimations.degreeVec(37.5F, -12.5F, 2.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(32.5F, -12.5F, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-37.5F, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, 8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-0, 4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0, -4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0, -8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-0, -4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-0, 4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0, 8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-0, 4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-0, -4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0, -8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-0, -4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-0, 4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-0, 8, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, 8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-0, 4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0, -4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0, -8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-0, -4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-0, 4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0, 8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-0, 4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-0, -4, 4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0, -8, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-0, -4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-0, 4, -4.33F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-0, 8, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(107.5F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(133.48F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(133.48F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(81.52F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(81.52F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(133.48F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(133.48F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(81.52F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(81.52F, -10, -20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(107.5F, -10, -20), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, -0.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.posVec(0, 1, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.posVec(0, 2.73F, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.posVec(0, 2.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 1, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.posVec(0, -0.73F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0, -0.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.posVec(0, 1, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, 2.73F, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.posVec(0, 2.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.posVec(0, 1, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.posVec(0, -0.73F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, -0.73F, 1), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(80, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(54.02F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(54.02F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(105.98F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(105.98F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(54.02F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(54.02F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(105.98F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(105.98F, 10, 20), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(80, 10, 20), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, 2.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.0833F, KeyframeAnimations.posVec(0, 1, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.1667F, KeyframeAnimations.posVec(0, -0.73F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.25F, KeyframeAnimations.posVec(0, -0.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 1, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.posVec(0, 2.73F, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.posVec(0, 2.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.posVec(0, 1, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.6667F, KeyframeAnimations.posVec(0, -0.73F, 0.13F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.posVec(0, -0.73F, 1), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.8333F, KeyframeAnimations.posVec(0, 1, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9167F, KeyframeAnimations.posVec(0, 2.73F, 1.87F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.posVec(0, 2.73F, 1), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

    public static final AnimationDefinition LUNGE_START = AnimationDefinition.Builder.withLength(1)
                .addAnimation("buried", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, -7.07F, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.875F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(30, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(31, -12.5F, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2083F, KeyframeAnimations.degreeVec(2.5F, -6, -4.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.875F, KeyframeAnimations.degreeVec(2.5F, -6, -4.5F), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-0, -6, -4.5F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-37.5F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-10, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-10, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-37.5F, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, 25, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.875F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-40, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, 25, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.875F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-40, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(107.5F, -10, -20), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(25, -10, -20), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(25, -10, -20), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-35, -3.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, -2, 1), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.125F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(80, 10, 20), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(25, 10, 20), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(25, 10, 20), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-35, 3.5F, -1.5F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, 2, 1), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.125F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
                ))
                .build();

    public static final AnimationDefinition LUNGE_AIRBORNE = AnimationDefinition.Builder.withLength(1).looping()
                .addAnimation("buried", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(30, 0, -1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.125F, KeyframeAnimations.degreeVec(30, 0, 1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(30, 0, -1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(30, 0, 1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(30, 0, -1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.625F, KeyframeAnimations.degreeVec(30, 0, 1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(30, 0, -1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.875F, KeyframeAnimations.degreeVec(30, 0, 1.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1, KeyframeAnimations.degreeVec(30, 0, -1.5F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, -6, -4.5F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-0, -6, -5.8F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-0, -6, -3.2F), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-0, -6, -4.5F), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-37.5F, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-40, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-40, 0, -0), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("RightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-40, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.0417F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.7917F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-38.27F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9167F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-41.73F, 0, -0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(1, KeyframeAnimations.degreeVec(-40, 0, -0), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-35, -3.5F, 1.5F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-35, 3.5F, -1.5F), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

    public static final AnimationDefinition EMERGE = AnimationDefinition.Builder.withLength(0.75F)
                .addAnimation("buried", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0.0833F, KeyframeAnimations.posVec(0, -24, 0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.2917F, KeyframeAnimations.posVec(0, -3, 0), AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(0.4167F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
                ))
                .addAnimation("Head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.25F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.375F, KeyframeAnimations.degreeVec(-45, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0, 12.5F, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.625F, KeyframeAnimations.degreeVec(-25, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.75F, KeyframeAnimations.degreeVec(-0, -12.5F, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("Body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-85, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-80, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-15, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.4583F, KeyframeAnimations.degreeVec(-80, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.625F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightLeg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-80, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("RightArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-50, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .addAnimation("LeftArm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                        new Keyframe(0.0833F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-50, 0, -0), AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-0, 0, -0), AnimationChannel.Interpolations.CATMULLROM)
                ))
                .build();

}