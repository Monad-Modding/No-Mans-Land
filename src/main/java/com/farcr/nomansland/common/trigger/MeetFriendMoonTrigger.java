package com.farcr.nomansland.common.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class MeetFriendMoonTrigger extends SimpleCriterionTrigger<MeetFriendMoonTrigger.MeetFriendMoonInstance> {
    @Override
    public Codec<MeetFriendMoonInstance> codec() {
        return MeetFriendMoonInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public record MeetFriendMoonInstance() implements SimpleInstance {
        public static final Codec<MeetFriendMoonInstance> CODEC =
            Codec.unit(new MeetFriendMoonInstance());

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
