package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public enum FriendMoonState implements StringRepresentable {
    GREETING("greeting", NMLRegistries.GREETING_DIALOGUE_KEY, FriendMoonState::defaultState),
    PASSIVE("passive", NMLRegistries.PASSIVE_DIALOGUE_KEY, FriendMoonState::defaultState),
    OFFERING("offering", NMLRegistries.OFFERING_DIALOGUE_KEY, FriendMoonState::defaultState),

    // Upset
    NEGATIVE("negative", null, FriendMoonState::negativeProceed),
    NEGATIVE_SPEECH("negative_2", NMLRegistries.NEGATIVE_DIALOGUE_KEY, FriendMoonState::defaultStateNoCandleReset),

    UPSET("upset", null, (moon) -> {});

    private final String name;
    private final @Nullable ResourceKey<Registry<DialoguePool>> dialoguePool;
    private final Consumer<FriendMoon> moonConsumer;
    public static final EnumCodec<FriendMoonState> CODEC = StringRepresentable.fromEnum(FriendMoonState::values);
    FriendMoonState(
        String name,
        @Nullable ResourceKey<Registry<DialoguePool>> dialoguePool,
        Consumer<FriendMoon> moonConsumer
    ) {
        this.name = name;
        this.dialoguePool = dialoguePool;
        this.moonConsumer = moonConsumer;
    }

    public static void defaultStateNoCandleReset(FriendMoon moon) {moon.setState(FriendMoonState.PASSIVE);}
    public static void defaultState(FriendMoon moon) {
        moon.setCandleTime(Math.max(moon.getCandleTime() - 1, 0));
        defaultStateNoCandleReset(moon);
    }
    public static void negativeProceed(FriendMoon moon) {moon.setState(FriendMoonState.NEGATIVE_SPEECH);}

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
    public @Nullable ResourceKey<Registry<DialoguePool>> getDialoguePoolType() {
        return dialoguePool;
    }
    public Consumer<FriendMoon> getMoonConsumer() {
        return moonConsumer;
    }
}
