package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.entity.*;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.bombs.AcridBomb;
import com.farcr.nomansland.common.entity.bombs.Explosive;
import com.farcr.nomansland.common.entity.bombs.Firebomb;
import com.farcr.nomansland.common.entity.bombs.InkBomb;
import com.farcr.nomansland.common.entity.bombs.LivingUrn;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.farcr.nomansland.common.entity.goose.Goose;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NoMansLand.MODID);

//    public static final Supplier<EntityType<Boat>> BOAT =
//            ENTITIES.register("maple_boat", () -> EntityType.Builder.<Boat>of(Boat::new, MobCategory.MISC)
//                    .sized(1.375f, 0.5625f).build("maple_boat"));
//    public static final Supplier<EntityType<ChestBoat>> CHEST_BOAT =
//            ENTITIES.register("maple_chest_boat", () -> EntityType.Builder.<ChestBoat>of(ChestBoat::new, MobCategory.MISC)
//                    .sized(1.375f, 0.5625f).build("maple_chest_boat"));

    public static final Supplier<EntityType<Firebomb>> FIREBOMB =
            ENTITIES.register("firebomb", () -> EntityType.Builder.<Firebomb>of(Firebomb::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("firebomb"));

    public static final Supplier<EntityType<InkBomb>> INK_BOMB =
            ENTITIES.register("ink_bomb", () -> EntityType.Builder.<InkBomb>of(InkBomb::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("ink_bomb"));

    public static final Supplier<EntityType<Explosive>> EXPLOSIVE =
            ENTITIES.register("explosive", () -> EntityType.Builder.<Explosive>of(Explosive::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(4).updateInterval(20).build("explosive"));

    public static final Supplier<EntityType<AcridBomb>> ACRID_BOMB =
            ENTITIES.register("acrid_bomb", () -> EntityType.Builder.<AcridBomb>of(AcridBomb::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("acrid_bomb"));

    public static final Supplier<EntityType<ThrownAquaRegiaBottle>> AQUA_REGIA =
            ENTITIES.register("aqua_regia", () -> EntityType.Builder.<ThrownAquaRegiaBottle>of(ThrownAquaRegiaBottle::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("aqua_regia"));

    public static final Supplier<EntityType<LivingUrn>> LIVING_URN =
            ENTITIES.register("living_urn", () -> EntityType.Builder.<LivingUrn>of(LivingUrn::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("living_urn"));

    public static final Supplier<EntityType<FallingPotEntity>> FALLING_POT =
            ENTITIES.register("falling_pot", () -> EntityType.Builder.<FallingPotEntity>of(FallingPotEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20).build("falling_pot"));

    public static final Supplier<EntityType<LivingPot>> LIVING_POT =
            ENTITIES.register("living_pot", () -> EntityType.Builder.<LivingPot>of(LivingPot::new, MobCategory.MISC)
                    .fireImmune().clientTrackingRange(10).build("living_pot"));


    public static final Supplier<EntityType<IncendiaryArrow>> INCENDIARY_ARROW =
            ENTITIES.register("incendiary_arrow", () -> EntityType.Builder.<IncendiaryArrow>of(IncendiaryArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20).build("incendiary_arrow"));

    public static final Supplier<EntityType<Ember>> EMBER =
            ENTITIES.register("ember", () -> EntityType.Builder.<Ember>of(Ember::new, MobCategory.MISC)
                    .fireImmune().sized(1, 1).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("ember"));

    public static final Supplier<EntityType<LingeringCloud>> LINGERING_CLOUD =
            ENTITIES.register("lingering_cloud", () -> EntityType.Builder.<LingeringCloud>of(LingeringCloud::new, MobCategory.MISC)
                    .fireImmune().sized(6, 6).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("lingering_cloud"));

    public static final Supplier<EntityType<InkCloud>> INK_CLOUD =
            ENTITIES.register("ink_cloud", () -> EntityType.Builder.<InkCloud>of(InkCloud::new, MobCategory.MISC)
                    .fireImmune().sized(6, 6).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("ink_cloud"));

    public static final Supplier<EntityType<PacifiedCloud>> PACIFIED_CLOUD =
            ENTITIES.register("pacified_cloud", () -> EntityType.Builder.<PacifiedCloud>of(PacifiedCloud::new, MobCategory.MISC)
                    .fireImmune().sized(6, 6).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("pacified_cloud"));


    public static final Supplier<EntityType<BillhookBass>> BILLHOOK_BASS = register("billhook_bass", BillhookBass::new, MobCategory.WATER_CREATURE, 0.7F, 0.5F);

    public static final Supplier<EntityType<Deer>> DEER = register("deer", Deer::new, MobCategory.CREATURE, 0.8F, 1.4F);

    public static final Supplier<EntityType<Goose>> GOOSE = register("goose", Goose::new, MobCategory.CREATURE, 0.6F, 0.9F);

//    public static final Supplier<EntityType<BuriedEntity>> BURIED =
//            ENTITIES.register("buried", () -> EntityType.Builder.of(BuriedEntity::new, MobCategory.MONSTER)
//                    .sized(1.0f, 1.0f).clientTrackingRange(8).build("buried"));

    // TODO: moose
//    public static final Supplier<EntityType<Moose>> MOOSE =
//            ENTITIES.register("moose", () -> EntityType.Builder.of(Moose::new, MobCategory.CREATURE)
//                    .sized(1.5F, 1.75F)
//                    .eyeHeight(2.0F)
//                    .passengerAttachments(new Vec3(0.0F, 2.05F, -0.5F))
//                    .build("moose"));

    public static final Supplier<EntityType<Tortoise>> TORTOISE =
            ENTITIES.register("tortoise", () -> EntityType.Builder.of(Tortoise::new, MobCategory.CREATURE)
                    .sized(1.25F, 1.25F)
                    .eyeHeight(1.0F)
                    .passengerAttachments(new Vec3(0.0F, 1.15F, 0.0F))
                    .build("tortoise"));

    public static final Supplier<EntityType<Buddy>> BUDDY =
        ENTITIES.register("buddy", () -> EntityType.Builder.of(Buddy::new, MobCategory.MISC)
            .sized(.75f, 2.05f)
            .ridingOffset(-0.7F)
            .build("buddy"));

    public static final Supplier<EntityType<Frienderman>> FRIENDERMAN =
            ENTITIES.register("frienderman", () -> EntityType.Builder.of(Frienderman::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.9F)
                    .eyeHeight(2.55F)
                    .clientTrackingRange(8)
                    .build("frienderman"));

    public static final Supplier<EntityType<DreamingPlayer>> DREAMING_PLAYER =
        ENTITIES.register("dreaming_player", () -> EntityType.Builder.of(DreamingPlayer::new, MobCategory.MISC)
            .noSummon().build("dreaming_player"));

    public static <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.EntityFactory<T> entity, MobCategory category, float width, float height) {
        return ENTITIES.register(name, () -> EntityType.Builder.of(entity, category).sized(width, height).build(name));
    }
}
