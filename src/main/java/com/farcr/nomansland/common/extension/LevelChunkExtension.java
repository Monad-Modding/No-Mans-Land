package com.farcr.nomansland.common.extension;

import org.apache.commons.lang3.NotImplementedException;

public interface LevelChunkExtension {
    default boolean nml$shouldIgnoreBuddyAnchor() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default void nml$setIgnoreBuddyAnchor() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default void nml$clearIgnoreBuddyAnchor() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default boolean nml$toxicGasScanned() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default boolean nml$mayHaveToxicGas() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default void nml$setMayHaveToxicGas(boolean value) throws NotImplementedException {
        throw new NotImplementedException();
    }
}
