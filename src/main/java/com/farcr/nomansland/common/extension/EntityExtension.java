package com.farcr.nomansland.common.extension;

import org.apache.commons.lang3.NotImplementedException;

public interface EntityExtension {
    default void NML$setInspectionState(boolean isInspecting) throws NotImplementedException {
        throw new NotImplementedException();
    }
    default boolean NML$isBeingInspected() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default boolean NML$wasPreviouslyInspected() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default float NML$getInspectionFade(float partialTick) throws NotImplementedException {
        throw new NotImplementedException();
    }
    default float nml$getVisualTickMultiplier() { return 1f; }
}
