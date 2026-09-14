package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.integration.EtchedIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.friend.condition.MoonlightContextualConditions;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions;
import com.farcr.nomansland.common.friend.condition.MoonlightLeavingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialogueLocation;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueTracker;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.friend.offering.OfferingContext;
import com.farcr.nomansland.common.friend.offering.OfferingType;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueResetPacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMeetingPointPacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.farcr.nomansland.NMLConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class FriendMoon extends SavedData {
    @Nullable private final ServerLevel level;
    public static final String NAME = "friend_moon";
    public FriendMoon(@Nullable ServerLevel level) {
        this.level = level;
    }

    public static FriendMoon getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            () -> new FriendMoon(level),
            (tag, provider) -> FriendMoon.create(tag, provider, level)
        ), FriendMoon.NAME);
    }

    public boolean awake = false;
    public ServerPlayer wokenUpBy;
    public boolean isAwake() {
        return awake;
    }
    public boolean isActive() {
        return isAwake() && (getState() != FriendMoonState.UPSET);
    }

    private int dialogueTicks = 10;
    public int getDialogueTicks() { return dialogueTicks; }

    private int candleTimer = 0;
    public int getCandleTime() { return candleTimer; }
    public void setCandleTime(int candleTime) {
        this.candleTimer = candleTime;
    }

    public static final int ASCENSION_DURATION = 300;
    public static final int ASCENSION_TRANSPARENCY_START = 140;
    public static final int ASCENSION_DAMAGE_INTERVAL = 60;
    public static final int ASCENSION_DAMAGE_END = 180;
    public static final int ASCENSION_REWARD_DELAY = 20;

    private int ascensionTicks = -1;
    private int rewardDelayTicks = -1;
    private BlockPos rewardBasinPos = null;
    private UUID ascendingBuddyUUID = null;
    private int ascendingBuddyEntityId = -1;

    public boolean isAscensionActive() { return ascensionTicks >= 0; }

    public void startAscension(Buddy buddy) {
        ascensionTicks = 0;
        ascendingBuddyUUID = buddy.getUUID();
        ascendingBuddyEntityId = buddy.getId();
        setDirty();
    }

    @Nullable
    private Buddy findAscendingBuddy() {
        if (level == null) return null;
        if (ascendingBuddyEntityId >= 0) {
            Entity entity = level.getEntity(ascendingBuddyEntityId);
            if (entity instanceof Buddy b) return b;
        }
        if (ascendingBuddyUUID != null) {
            Entity entity = level.getEntity(ascendingBuddyUUID);
            if (entity instanceof Buddy b) return b;
        }
        return null;
    }

    public void abortAscension() {
        if (!isAscensionActive())
            return;
        Buddy buddy = findAscendingBuddy();
        if (buddy != null) {
            buddy.setAscensionTicks(-1);
            buddy.setHealth(buddy.getMaxHealth());
        }
        ascensionTicks = -1;
        ascendingBuddyUUID = null;
        ascendingBuddyEntityId = -1;
        rewardDelayTicks = -1;
        rewardBasinPos = null;
        setDirty();
    }

    public void abortSpecialInteractions() {
        abortAscension();
        mapInteractionActive = false;
        mapInteractionTicks = -1;
        badOmenInteractionTicks = -1;
        targetJukeboxPos = null;
        jukeboxInteractionTicks = -1;
        setDirty();
    }

    private boolean mapInteractionActive = false;
    private int mapInteractionTicks = -1;
    public static final int MAP_PARTICLE_DURATION = 30;

    public static OfferingType getOfferingType(OfferingContext offeringContext) {
        if (offeringContext.isValid()) {
            if (isSpecialInteraction(offeringContext))
                return OfferingType.SPECIAL;
            // unwrapped this because its technically more optimized or smth idk
            Entity entity = offeringContext.getEntity();
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                if (stack.is(Items.FILLED_MAP) || stack.is(Items.MAP)
                || (Mods.FIELDGUIDE.isLoaded() && stack.is(Mods.FIELDGUIDE.getItem("field_guide"))))
                    return OfferingType.MAP;
                if (stack.is(NMLItems.TRINKET))
                    return OfferingType.BAD_OMEN;
            }
        }
        return OfferingType.REGULAR;
    }

    private int badOmenInteractionTicks = -1;
    public static final int BAD_OMEN_INTERACTION_LENGTH = 40;
    public int getBadOmenInteractionTicks() {
        return this.badOmenInteractionTicks;
    }
    public boolean badOmenInteraction(Level level, Entity entity, BlockPos basinPos) {
        setDirty();
        badOmenInteractionTicks++;
        if (badOmenInteractionTicks >= BAD_OMEN_INTERACTION_LENGTH)
            return level.isClientSide;
        return true;
    }

    public void mapInteraction(Level level, Entity entity, BlockPos basinPos) {
        if (mapInteractionActive && mapInteractionTicks >= MAP_PARTICLE_DURATION)
            return;
        if (!mapInteractionActive) {
            mapInteractionActive = true;
            mapInteractionTicks = 0;
        }

        mapInteractionTicks++;
        setDirty();
        for (int i = 0; i < 2; i++) {
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 0.6;
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 0.6;
            level.addParticle(
                NMLParticleTypes.MOONLIGHT_SPARK.get(),
                entity.getX() + offsetX, entity.getY() + level.getRandom().nextDouble() * 0.5, entity.getZ() + offsetZ,
                0, 0.03, 0
            );
        }

        if (mapInteractionTicks >= MAP_PARTICLE_DURATION)
            completeMapInteraction(level, entity, basinPos);
    }

    private void completeMapInteraction(Level level, Entity entity, BlockPos basinPos) {
        if (entity instanceof ItemEntity itemEntity && level instanceof ServerLevel serverLevel) {
            Holder<MapDecorationType> targetPoint = NMLMapDecorationTypes.MOONLIGHT_BASIN;
            ItemStack stack = itemEntity.getItem();
            if (stack.is(Items.MAP)) {
                ItemStack basinMap = MapItem.create(serverLevel, basinPos.getX(), basinPos.getZ(), (byte) 2, true, true);
                MapItemSavedData.addTargetDecoration(basinMap, basinPos, "+", targetPoint);
                itemEntity.setItem(basinMap);
            } else if (stack.is(Items.FILLED_MAP)) {
                MapItemSavedData.addTargetDecoration(stack, basinPos, "+", targetPoint);
                itemEntity.setItem(stack);
            }
        }
        setDirty();
    }

    private BlockPos targetJukeboxPos;
    private int jukeboxInteractionTicks = -1;
    public static final int JUKEBOX_PARTICLE_DURATION = 40;

    public boolean isJukeboxInteractionActive() { return targetJukeboxPos != null; }
    public BlockPos getTargetJukeboxPos() { return targetJukeboxPos; }
    public int getJukeboxInteractionTicks() { return jukeboxInteractionTicks; }

    public static final ResourceLocation JUKEBOX_DIALOGUE = NoMansLand.location("jukebox");

    public void startJukeboxInteraction(BlockPos pos) {
        targetJukeboxPos = pos;
        jukeboxInteractionTicks = 0;
        resetDialogue(false);
        applyDialogueLength(
            getDialogueFromLocation(NMLRegistries.SPECIAL_DIALOGUE_KEY, JUKEBOX_DIALOGUE)
                .dispatch(level, getFriendshipPlayers())
        );
        setDirty();
    }

    private void completeJukeboxInteraction() {
        if (level != null && targetJukeboxPos != null) {
            BlockEntity blockEntity = level.getBlockEntity(targetJukeboxPos);
            if (blockEntity instanceof JukeboxBlockEntity jukebox) {
                jukebox.popOutTheItem();
            } else if (Mods.ETCHED.isLoaded()) {
                EtchedIntegration.ejectPlayingDisc(level, targetJukeboxPos, blockEntity);
            }
        }
        targetJukeboxPos = null;
        jukeboxInteractionTicks = -1;
        setDirty();
    }

    private final List<BuddyStar> buddyStars = new ArrayList<>();
    public List<BuddyStar> getBuddyStars() { return buddyStars; }

    public void addBuddyStar(BuddyStar star) {
        buddyStars.add(star);
        setDirty();
    }

    private FriendMoonState state = FriendMoonState.GREETING;
    public FriendMoonState getState() { return this.state; }
    public void setState(FriendMoonState newState) {
        if (newState != state) {
            abortSpecialInteractions();
            this.state = newState;
            setDirty();
        }
    }

    public List<UUID> upsetWith = new ArrayList<>();
    // this specifies to play the bad omen animation specifically
    public void setUpsetWith(UUID playerUUID) {
        if (!upsetWith.contains(playerUUID)) {
            upsetWith.add(playerUUID);
            setDirty();
        }
    }

    public void resetValues() {
        abortSpecialInteractions();
        awake = false;
        wokenUpBy = null;
        setState(FriendMoonState.GREETING);
        setCandleTime(-1);

        setDirty();
    }

    boolean pulseUpdate = false;
    @Override public void setDirty() {
        pulseUpdate = true;
        super.setDirty();
    }

    public boolean shouldPulseUpdate() {
        if (pulseUpdate) {
            pulseUpdate = false;
            return true;
        }
        return false;
    }

    public FriendMoon load(CompoundTag tag, HolderLookup.Provider provider) {
        awake = tag.getBoolean("IsAwake");
        candleTimer = tag.getInt("CandleTimer");
        setState(FriendMoonState.CODEC.byName(tag.getString("State"), FriendMoonState.PASSIVE));
        updatedShadow = tag.getBoolean("UpdatedShadow");

        upsetWith.clear();
        for (Tag uuidEntry : tag.getList("UpsetWith", 11))
            upsetWith.add(NbtUtils.loadUUID(uuidEntry));

        buddyStars.clear();
        for (Tag starTag : tag.getList("BuddyStars", 10)) {
            if (starTag instanceof CompoundTag starCompound)
                BuddyStar.CODEC.parse(NbtOps.INSTANCE, starCompound).result().ifPresent(buddyStars::add);
        }

        mapInteractionActive = tag.getBoolean("MapInteractionActive");
        mapInteractionTicks = tag.getInt("MapInteractionTicks");
        badOmenInteractionTicks = tag.getInt("BadOmenInteractionTicks");
        jukeboxInteractionTicks = tag.getInt("JukeboxInteractionTicks");
        if (tag.contains("JukeboxX"))
            targetJukeboxPos = new BlockPos(tag.getInt("JukeboxX"), tag.getInt("JukeboxY"), tag.getInt("JukeboxZ"));
        else targetJukeboxPos = null;

        cosmicBodyStateMap.clear();
        for (Tag positionTag : tag.getList("StoredPlayerPositions", 10)) {
            if (positionTag instanceof CompoundTag stateCompound) {
                CosmicBodyState.CODEC.parse(NbtOps.INSTANCE, stateCompound).result().ifPresent(
                (data) -> cosmicBodyStateMap.put(data.playerUUID(), data));
            }
        }

        if (tag.contains("MeetingPointOverrideX"))
            meetingPointOverride = new ChunkPos(tag.getInt("MeetingPointOverrideX"), tag.getInt("MeetingPointOverrideZ"));
        else meetingPointOverride = null;

        return this;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("IsAwake", isAwake());

        tag.putInt("CandleTimer", getCandleTime());
        tag.putString("State", state.getSerializedName());
        tag.putBoolean("UpdatedShadow", updatedShadow);

        ListTag upsetTag = new ListTag();
        upsetWith.forEach((playerUUID) -> upsetTag.add(NbtUtils.createUUID(playerUUID)));
        tag.put("UpsetWith", upsetTag);

        ListTag starsTag = new ListTag();
        for (BuddyStar star : buddyStars)
            BuddyStar.CODEC.encodeStart(NbtOps.INSTANCE, star).result().ifPresent(starsTag::add);
        tag.put("BuddyStars", starsTag);

        tag.putBoolean("MapInteractionActive", mapInteractionActive);
        tag.putInt("MapInteractionTicks", mapInteractionTicks);
        tag.putInt("BadOmenInteractionTicks", badOmenInteractionTicks);
        tag.putInt("JukeboxInteractionTicks", jukeboxInteractionTicks);
        if (targetJukeboxPos != null) {
            tag.putInt("JukeboxX", targetJukeboxPos.getX());
            tag.putInt("JukeboxY", targetJukeboxPos.getY());
            tag.putInt("JukeboxZ", targetJukeboxPos.getZ());
        }

        ListTag positionTag = new ListTag();
        cosmicBodyStateMap.forEach((uuid, bodyState) ->
            CosmicBodyState.CODEC.encodeStart(NbtOps.INSTANCE, bodyState).result().ifPresent(positionTag::add));
        tag.put("StoredPlayerPositions", positionTag);

        if (meetingPointOverride != null) {
            tag.putInt("MeetingPointOverrideX", meetingPointOverride.x);
            tag.putInt("MeetingPointOverrideZ", meetingPointOverride.z);
        }
        return tag;
    }

    public static FriendMoon create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        FriendMoon moon = new FriendMoon(serverLevel);
        return moon.load(tag, provider);
    }

    /* Friendship */
    public boolean cannotObtainFriendship(Player serverPlayer) {
        return (serverPlayer.getEffect(MobEffects.BAD_OMEN) != null)
            || upsetWith.contains(serverPlayer.getUUID());
    }

    // side agnostic, expects side to validate the basin
    public static boolean appearConditionsMet(Player player, BlockPos basinPosition) {
        if (!isNightTime(player.level()) || basinPosition == null)
            return false;
        AABB boundingBox = new AABB(basinPosition).inflate(MoonlightBasinBlockEntity.FRIENDSHIP_MAX_RANGE);
        return boundingBox.contains(player.position());
    }

    public static void grantPlayerFriendship(FriendMoon friendMoon, ServerPlayer serverPlayer, BlockPos pos) {
        boolean grantedFriendship = false;
        if (friendMoon.getState() != FriendMoonState.UPSET
        && !friendMoon.cannotObtainFriendship(serverPlayer)) {
            friendMoon.lastFriendshipPlayers.put(serverPlayer, 0);
            grantedFriendship = true;
        } else if (serverPlayer.hasEffect(MobEffects.BAD_OMEN))
            friendMoon.setUpsetWith(serverPlayer.getUUID());
        // inform of player regardless but let them know not to update the leave time
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundMoonlightBasinTrackPacket(pos, grantedFriendship));
    }

    public boolean playerHasFriendship(ServerPlayer player) {
        return lastFriendshipPlayers.containsKey(player);
    }

    public static boolean isFriendMoonDimension(ServerLevel candidate) {
        MinecraftServer server = candidate.getServer();
        if (server == null) return false;

        List<? extends String> configured = NMLConfig.FRIEND_MOON_DIMENSIONS.get();
        if (!configured.isEmpty())
            return configured.contains(candidate.dimension().location().toString());

        return candidate.dimensionTypeRegistration().equals(server.overworld().dimensionTypeRegistration());
    }

    public List<ServerPlayer> getFriendshipPlayers() {
        if (level == null) return List.of();
        MinecraftServer server = level.getServer();
        if (server == null) return level.getPlayers(this::playerHasFriendship);

        List<ServerPlayer> players = new ArrayList<>();
        for (ServerLevel candidate : server.getAllLevels())
            if (isFriendMoonDimension(candidate))
                players.addAll(candidate.getPlayers(this::playerHasFriendship));
        return players;
    }

    private final HashMap<ServerPlayer, Integer> lastFriendshipPlayers = new HashMap<>();
    public void forFriendshipPlayers(Consumer<ServerPlayer> consumer) {
        assert level != null;
        for (ServerPlayer serverPlayer : getFriendshipPlayers())
            consumer.accept(serverPlayer);
    }

    public static boolean isSpecialInteraction(OfferingContext offeringContext) {
        return offeringContext.getEntity().getType().equals(NMLEntities.BUDDY.get());
    }

    public boolean specialInteraction(Level level, Entity entity) {
        if (!(entity instanceof Buddy buddy))
            return false;

        if (!isAscensionActive()) {
            startAscension(buddy);
        } else if (ascendingBuddyUUID != null && !ascendingBuddyUUID.equals(buddy.getUUID())) {
            return true;
        }

        ascensionTicks++;
        setDirty();

        buddy.setAscensionTicks(ascensionTicks);

        if (buddy.getHealth() <= 1.5f)
            buddy.setHealth(1.5f);

        if (ascensionTicks % ASCENSION_DAMAGE_INTERVAL == 0 && ascensionTicks > 0 && ascensionTicks <= ASCENSION_DAMAGE_END && !level.isClientSide()) {
            buddy.hurt(level.damageSources().magic(), 1f);
            if (buddy.getHealth() <= 1.5f)
                buddy.setHealth(1.5f);
            level.playSound(null, buddy.blockPosition(), NMLSounds.BUDDY_BONE_BREAK.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }

        for (int i = 0; i < (ascensionTicks > ASCENSION_TRANSPARENCY_START ? 3 : 1); i++) {
            double offsetX = (level.getRandom().nextDouble() - 0.5) * 0.6;
            double offsetZ = (level.getRandom().nextDouble() - 0.5) * 0.6;
            level.addParticle(
                ParticleTypes.MYCELIUM,
                buddy.getX() + offsetX, buddy.getY() + level.getRandom().nextDouble() * 1.8, buddy.getZ() + offsetZ,
                0, 0.05, 0
            );
        }

        if (ascensionTicks >= ASCENSION_TRANSPARENCY_START) {
            int sparkCount = 1 + (ascensionTicks - ASCENSION_TRANSPARENCY_START) / 40;
            for (int i = 0; i < sparkCount; i++) {
                double offsetX = (level.getRandom().nextDouble() - 0.5) * 0.8;
                double offsetY = level.getRandom().nextDouble() * 1.8;
                double offsetZ = (level.getRandom().nextDouble() - 0.5) * 0.8;
                level.addParticle(
                    NMLParticleTypes.MOONLIGHT_SPARK.get(),
                    buddy.getX() + offsetX, buddy.getY() + offsetY, buddy.getZ() + offsetZ,
                    (level.getRandom().nextDouble() - 0.5) * 0.02, 0.05 + level.getRandom().nextDouble() * 0.03, (level.getRandom().nextDouble() - 0.5) * 0.02
                );
            }
        }

        if (ascensionTicks >= ASCENSION_DURATION) {
            completeAscension(buddy);
            return false;
        }

        return true;
    }

    public void completeAscension(Buddy buddy) {
        if (level == null)
            return;

        String variantName = buddy.getVariantName();
        BuddyStar star = BuddyStar.fromVariant(variantName, level.getRandom());
        addBuddyStar(star);

        rewardDelayTicks = ASCENSION_REWARD_DELAY;
        rewardBasinPos = buddy.blockPosition().below(4);

        buddy.discard();
        ascensionTicks = -1;
        ascendingBuddyUUID = null;
        setDirty();
    }

    public void tickAscensionReward() {
        if (rewardDelayTicks < 0 || level == null)
            return;

        rewardDelayTicks--;
        if (rewardDelayTicks <= 0) {
            rewardDelayTicks = -1;

            if (rewardBasinPos != null) {
                ItemEntity discEntity = new ItemEntity(
                    level,
                    rewardBasinPos.getX() + 0.5, rewardBasinPos.getY() + 1.5, rewardBasinPos.getZ() + 0.5,
                    new ItemStack(NMLItems.MUSIC_DISC_GUIDANCE.get())
                );
                discEntity.setDeltaMovement(0, 0, 0);
                discEntity.setPickUpDelay(10);
                level.addFreshEntity(discEntity);

                forFriendshipPlayers(player -> NMLCriteriaTriggers.BUDDY_ASCENSION.get().trigger(player));
                rewardBasinPos = null;
            }
            setDirty();
        }
    }

    public static float NIGHT_TIME_THRESHOLD = 0.3f;
    public static boolean isNightTime(Level level) {
        float time = level.getTimeOfDay(0f);
        return (time > NIGHT_TIME_THRESHOLD && time < (1 - NIGHT_TIME_THRESHOLD));
    }

    private boolean updatedShadow = false;
    private final HashMap<UUID, CosmicBodyState> cosmicBodyStateMap = new HashMap<>();
    public boolean cosmicBodyExpiredForPlayer(Player player) {
        return cosmicBodyStateMap.getOrDefault(player.getUUID(), new CosmicBodyState(player.getUUID(), 0)).exceedsDays();
    }

    public void resetCosmicBodyForPlayer(Player player) {
        cosmicBodyStateMap.remove(player.getUUID());
        setDirty();
    }

    public void updateMeetingPointInformation(ServerLevel level) {
        if (isNightTime(level)) {
            if (!updatedShadow) {
                MinecraftServer server = level.getServer();
                if (server == null) level.players().forEach((player) -> updatePlayerFriendShadow(player));
                else for (ServerLevel candidate : server.getAllLevels())
                    if (isFriendMoonDimension(candidate))
                        candidate.players().forEach((player) -> updatePlayerFriendShadow(player));
                updatedShadow = true;
            }
        } else updatedShadow = false;
    }

    public static BlockPos getMeetingPointPosition(ServerLevel level) {
        ChunkPos meetingPointChunk = level.getChunkSource().getGeneratorState().meetingPointPosition();
        if (meetingPointChunk == null) return null;
        return meetingPointChunk.getMiddleBlockPosition(0);
    }

    @Nullable private ChunkPos meetingPointOverride = null;

    @Nullable
    public ChunkPos getMeetingPointOverride() {
        return meetingPointOverride;
    }

    public void setMeetingPointOverride(@Nullable ChunkPos pos) {
        this.meetingPointOverride = pos;
        setDirty();
    }

    public void updatePlayerFriendShadow(ServerPlayer player) {
        if (DreamManager.getOrDefault(player.getServer()).playerHasExperiencedDream(player, NMLDreamTypes.FRIEND_MOON_DREAM.get())) {
            int days = 0;
            CosmicBodyState state = cosmicBodyStateMap.get(player.getUUID());
            if (state != null) {
                // avoid updating at all if it exceeds days
                if (state.exceedsDays()) return;
                days = (cosmicBodyStateMap.get(player.getUUID()).daysCounted()) + 1;
            }
            cosmicBodyStateMap.put(player.getUUID(), new CosmicBodyState(player.getUUID(), days));
            setDirty();
        }
        playerSendShadowPacket(player);
    }

    // decoupled from above function as to not update unnecessarily . only use above function to UPDATE position of shadow
    public void playerSendShadowPacket(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        CosmicBodyState state = cosmicBodyStateMap.get(playerUUID);
        if (state != null) {
            Optional<BlockPos> meetingPointPosition = Optional.ofNullable(
                getMeetingPointPosition(level)
            );
            PacketDistributor.sendToPlayer(player,
                new ClientboundMeetingPointPacket(
                    meetingPointPosition,
                    state.exceedsDays()
                )
            );
        }
    }

    public static boolean hasMetWithPlayer(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (DialogueTracker.getOrDefault(server).hasHeardFrom(player.getUUID(), NMLRegistries.GREETING_DIALOGUE_KEY.location())) return true;
        AdvancementHolder meetAdvancement = server.getAdvancements().get(MEET_MOON_ADVANCEMENT);
        return (meetAdvancement != null && player.getAdvancements().getOrStartProgress(meetAdvancement).isDone());
    }

    /* Behavior */
    private int lastTotalPlayers = 0;
    public static float LEAVE_TIME_THRESHOLD = 60F;
    public void tick() {
        assert level != null;
        updateMeetingPointInformation(level);
        tickAscensionReward();

        if (isAscensionActive()) {
            Buddy buddy = findAscendingBuddy();
            if (buddy == null || !buddy.isAlive())
                abortAscension();
        }

        // I forget why this isn't just in the resetValues method but I'm going to look away because it's too late to change it right now
        if (!isNightTime(level) && !upsetWith.isEmpty()) {
            upsetWith.clear();
            setDirty();
        }

        if (this.isAwake()) {
            if (!isNightTime(level)) {
                resetValues();
                return;
            }

            if (getState() != FriendMoonState.UPSET) {
                // query players that had friendship
                boolean someoneLeft = false, someoneDied = false;
                ArrayList<ServerPlayer> withRemovedPlayers = new ArrayList<>(lastFriendshipPlayers.keySet());
                boolean silentlyRemove = ((getState() == FriendMoonState.OFFERING || getState() == FriendMoonState.GREETING)
                    || isJukeboxInteractionActive()) && (lastTotalPlayers > 1);
                for (ServerPlayer player : withRemovedPlayers) {
                    lastFriendshipPlayers.put(player, lastFriendshipPlayers.get(player) + 1);
                    if (lastFriendshipPlayers.get(player) >= LEAVE_TIME_THRESHOLD || player.isDeadOrDying() || cannotObtainFriendship(player)) {
                        if (!silentlyRemove && !cannotObtainFriendship(player)) {
                            setState(FriendMoonState.PASSIVE);
                            if (player.isDeadOrDying()) someoneDied = true;
                            someoneLeft = true;
                        } else if (silentlyRemove)
                            PacketDistributor.sendToPlayer(player, new ClientboundDialogueResetPacket());
                        lastFriendshipPlayers.remove(player);
                    }
                }
                if (someoneLeft) {
                    boolean finalSomeoneDied = someoneDied;
                    applyDialogueLength(
                        getDialogueFromStream(NMLRegistries.LEAVING_DIALOGUE_KEY,
                            (registry) -> leavingFilter(registry, finalSomeoneDied)
                        ).dispatch(level, withRemovedPlayers)
                    );

                    if (!withRemovedPlayers.isEmpty() && lastFriendshipPlayers.isEmpty())
                        resetValues();
                    return;
                }
            }

            // Grant players advancement if they do not have it
            AtomicInteger playerTracker = new AtomicInteger();
            forFriendshipPlayers((player) -> playerTracker.getAndIncrement());

            // Ensure players are listening to the Moon
            int totalPlayers = playerTracker.get();
            if (totalPlayers > 0) {
                if (getState() == FriendMoonState.GREETING) {
                    if (wokenUpBy != null) {
                        applyDialogueLength(
                            getDialogueFromStream(NMLRegistries.GREETING_DIALOGUE_KEY,
                                (registry) -> greetingFilter(registry, wokenUpBy)
                            ).setTargetPlayer(wokenUpBy).dispatch(level, getFriendshipPlayers())
                        );
                        forFriendshipPlayers((player) ->
                            NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(player));
                        getState().getMoonConsumer().accept(this);
                        // just in case I dont want it softlocking players if they log off please
                    } else if (awake) {
                        awake = false;
                        setDirty();
                    }
                    return;
                }

                // Passive Dialogue
                if (dialogueTicks >= 0) {
                    dialogueTicks = Math.max(dialogueTicks - 1, 0);
                    if (dialogueTicks == 0 && !isAscensionActive()) {
                        getState().getMoonConsumer().accept(this);
                        randomDialogueOrGreeting(getState().getDialoguePoolType(), totalPlayers);
                        forFriendshipPlayers((player) ->
                            NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(player));
                    }
                }

                if (jukeboxInteractionTicks >= 0) {
                    jukeboxInteractionTicks++;
                    if (jukeboxInteractionTicks >= JUKEBOX_PARTICLE_DURATION)
                        completeJukeboxInteraction();
                    setDirty();
                }
            } else if (getState() == FriendMoonState.OFFERING)
                setState(FriendMoonState.PASSIVE);
        } else lastFriendshipPlayers.clear();
    }

    int NEGATIVE_TIME = 40;
    public void negative() {
        setCandleTime(3);
        setState(FriendMoonState.NEGATIVE);

        // Dialogue Reset
        resetDialogue(false);
        dialogueTicks = NEGATIVE_TIME;
    }

    public void packetUpdateEvent(FriendMoonUpdate.ToServer packetType, ServerPlayer player) {
        packetType.getConsumer().accept(this, player);
        this.setDirty();
    }

    private static final int DIALOGUE_PADDING = 100;
    public void applyDialogueLength(int dialogueLength) {
        if (dialogueLength > 0) {
            dialogueTicks = (dialogueLength + DIALOGUE_PADDING);
            setDirty();
        }
    }

    private DialogueLocation getDialogueFromStream(
        ResourceKey<Registry<DialoguePool>> registryKey,
        Function<Registry<DialoguePool>, DialoguePool> consumer
    ) {
        Registry<DialoguePool> registry = DialogueUtil.getDialogueRegistry(level, registryKey);
        DialoguePool resultingPool = consumer.apply(registry);
        return new DialogueLocation(
            (resultingPool != null ? registry.getKey(resultingPool) : null),
            registryKey.location(),
            level.getRandom()
        );
    }

    public DialogueLocation getDialogueFromLocation(
        ResourceKey<Registry<DialoguePool>> registryKey,
        ResourceLocation dialogueLocation
    ) {
        return new DialogueLocation(
            dialogueLocation,
            registryKey.location(),
            level.getRandom()
        );
    }

    /*
    * Dialogue Queries: Dialogue Queries provide different conditions for sending dialogue.
    * The way I've set the system up is done in a way where you provide the query and it should
    * allow you to sort through any kind of condition you wish to for dialogues and dispatch them automatically
    * without having to write the same redundant code that gets the registry and resourcelocation
    */
    public static final ResourceLocation DREAM_MOON_ADVANCEMENT = NoMansLand.location("main/dream_friend_moon");
    public static final ResourceLocation MEET_MOON_ADVANCEMENT = NoMansLand.location("main/meet_friend_moon");
    private DialoguePool greetingFilter(Registry<DialoguePool> registry, ServerPlayer serverPlayer) {
        List<DialoguePool> filteredDialogue = registry.stream().filter(
            (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

        // Grant Advancement
        AdvancementHolder dreamAdvancement = level.getServer().getAdvancements().get(DREAM_MOON_ADVANCEMENT);
        if (!hasMetWithPlayer(serverPlayer)) {
            filteredDialogue = MoonlightGreetingConditions.FirstTimeGreetingConditional.FIRST_TIME_ARRAY;
            if (dreamAdvancement != null && serverPlayer.getAdvancements().getOrStartProgress(dreamAdvancement).isDone())
                filteredDialogue = MoonlightGreetingConditions.DreamGreetingConditional.DREAM_ARRAY;
        }
        return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
    }

    private DialoguePool leavingFilter(Registry<DialoguePool> registry, boolean someoneDied) {
        List<DialoguePool> filteredDialogue = registry.stream().filter(
            (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();
        if (someoneDied) filteredDialogue = MoonlightLeavingConditions.OnDeathConditional.ON_DEATH_ARRAY;
        return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
    }

    private DialoguePool addToCommuneFilter(Registry<DialoguePool> registry) {
        List<DialoguePool> filteredDialogue = MoonlightGreetingConditions.AdditionToCommuneConditional.ADDITION_TO_COMMUNE_ARRAY;
        return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
    }

    // Simple dialogue filter used in most cases, just selects a random dialogue based on weight
    private DialoguePool weightedFilter(Registry<DialoguePool> registry) {
        return DialogueUtil.getWeightedEntry(registry.stream().toList(), level.getRandom());
    }

    public ServerPlayer getContextualPlayer() {
        for (ServerPlayer serverPlayer : getFriendshipPlayers()) {
            if (level.getBlockState(serverPlayer.blockPosition()).is(NMLBlocks.MOONLIGHT_BASIN))
                return serverPlayer;
        }
        return null;
    }

    public boolean contextualDialogue(ServerPlayer serverPlayer) {
        DialogueLocation dialogueLocation = getDialogueFromStream(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY, (registry) -> {
            List<DialoguePool> filteredDialogue = registry.stream().filter(
                (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

            // Talk about more interesting things if theyre available
            ArrayList<DialoguePool> conditionalDialogue = new ArrayList<>();

            // Check Equipment
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = serverPlayer.getItemBySlot(slot);
                if (!item.isEmpty()) {
                    DialogueUtil.appendTags(
                        item.getItem(), level.registryAccess(), Registries.ITEM,
                        MoonlightContextualConditions.EquipmentContextualConditional.COMPILED_MAP,
                        MoonlightContextualConditions.EquipmentContextualConditional.KEY_MAP,
                        conditionalDialogue
                    );
                }
            }

            // Check Effects
            serverPlayer.getActiveEffects().forEach((effect) -> {
                DialogueUtil.appendTags(
                    effect.getEffect().value(), level.registryAccess(), Registries.MOB_EFFECT,
                    MoonlightContextualConditions.EffectContextualCondition.COMPILED_MAP,
                    MoonlightContextualConditions.EffectContextualCondition.KEY_MAP,
                    conditionalDialogue
                );
            });

            if (!conditionalDialogue.isEmpty())
                filteredDialogue = conditionalDialogue;

            return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
        }).setTargetPlayer(serverPlayer);
        int dialogueLength = dialogueLocation.dispatch(level, getFriendshipPlayers());
        applyDialogueLength(dialogueLength);
        // if the dialogue length is greater than 0 it succeeded
        return (dialogueLength > 0);
    }

    public void randomDialogueOrGreeting(ResourceKey<Registry<DialoguePool>> registryKey, int totalPlayers) {
        if (registryKey == NMLRegistries.PASSIVE_DIALOGUE_KEY || registryKey == NMLRegistries.GREETING_DIALOGUE_KEY) {
            if (((totalPlayers > 1) && (totalPlayers > lastTotalPlayers)) && registryKey == NMLRegistries.PASSIVE_DIALOGUE_KEY) {
                applyDialogueLength(
                    getDialogueFromStream(NMLRegistries.GREETING_DIALOGUE_KEY, this::addToCommuneFilter)
                        .dispatch(level, getFriendshipPlayers())
                );
                lastTotalPlayers = totalPlayers;
                return;
            }
            lastTotalPlayers = totalPlayers;
        }
        randomDialogue(registryKey);
    }

    public void randomDialogue(ResourceKey<Registry<DialoguePool>> registryKey) {
        if (registryKey == null)
            return;
        // Contextual Dialogue
        if (registryKey == NMLRegistries.PASSIVE_DIALOGUE_KEY) {
            ServerPlayer contextualPlayer = getContextualPlayer();
            if (contextualPlayer != null && contextualDialogue(contextualPlayer))
                return;
        }
        // Passive / Otherwise
        applyDialogueLength(
            getDialogueFromStream(registryKey, this::weightedFilter)
                .dispatch(level, getFriendshipPlayers())
        );
    }

    public void resetDialogue(boolean clientSide) {
        if (!clientSide)
           forFriendshipPlayers((serverPlayer) -> PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialogueResetPacket()));
        dialogueTicks = -1;
    }
}
