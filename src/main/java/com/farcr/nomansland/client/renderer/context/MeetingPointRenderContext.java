package com.farcr.nomansland.client.renderer.context;

import net.minecraft.core.BlockPos;

/*
* Stores information about the meeting point for the renderer
 */
public record MeetingPointRenderContext(
    boolean enabled,
    BlockPos meetingPointPosition
) {
    public static MeetingPointRenderContext fromDefault() {
        // additional information doesnt matter so long as we provide an empty one
        return new MeetingPointRenderContext(false,
            new BlockPos(0, 0, 0)
        );
    }
}
