package com.farcr.nomansland.common.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class DreamFriendMoonTrigger extends SimpleCriterionTrigger<DreamFriendMoonTrigger.DreamFriendMoonInstance> {
    @Override
    public Codec<DreamFriendMoonInstance> codec() {
        return DreamFriendMoonInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public record DreamFriendMoonInstance() implements SimpleInstance {
        public static final Codec<DreamFriendMoonInstance> CODEC =
            Codec.unit(new DreamFriendMoonInstance());

        @Override
        public void validate(CriterionValidator validator) {
            SimpleInstance.super.validate(validator);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
