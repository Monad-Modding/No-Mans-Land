package com.farcr.nomansland.datagen.sound.lodestone;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.util.DeferredSoundType;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public abstract class LodestoneBlockSoundEventSystem extends LodestoneSoundEventSystem {

    public static LodestoneBlockSoundEventSystem INSTANCE;

    public LodestoneBlockSoundEventSystem(PackOutput packOutput, String modId, ExistingFileHelper existingFileHelper) {
        super(packOutput, modId, existingFileHelper);
        INSTANCE = this;
    }

    public void add(DeferredSoundType soundType, String path) {
        add(soundType, path, c -> {
        });
    }

    public void add(DeferredSoundType soundType, String path, Consumer<BlockSoundEventBuilder> modifier) {
        var builder = BlockSoundEventBuilder.create(path, soundType);
        modifier.accept(builder);
        builder.addSounds();
    }

    public SoundEventBuilderBlueprint blueprint(String path) {
        return blueprint(path, c -> {
        });
    }

    public SoundEventBuilderBlueprint blueprint(String path, Consumer<BlockSoundEventBuilder> modifier) {
        return new SoundEventBuilderBlueprint(path, modifier);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class SoundEventBuilderBlueprint {
        protected final String path;
        protected Consumer<BlockSoundEventBuilder> modifier;

        public SoundEventBuilderBlueprint(String path, Consumer<BlockSoundEventBuilder> modifier) {
            this.path = path;
            this.modifier = modifier;
        }

        public SoundEventBuilderBlueprint modify(Consumer<BlockSoundEventBuilder> extraModifier) {
            modifier = modifier.andThen(extraModifier);
            return this;
        }

        public SoundEventBuilderBlueprint addAll(DeferredSoundType... soundTypes) {
            for (DeferredSoundType soundType : soundTypes) {
                add(soundType);
            }
            return this;
        }

        public SoundEventBuilderBlueprint add(DeferredSoundType soundType) {
            INSTANCE.add(soundType, path, modifier);
            return this;
        }

        public SoundEventBuilderBlueprint add(DeferredSoundType soundType, Consumer<BlockSoundEventBuilder> extraModifier) {
            INSTANCE.add(soundType, path, modifier.andThen(extraModifier));
            return this;
        }
    }
}