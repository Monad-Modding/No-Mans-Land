package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.client.renderer.FriendMoonOfferingSoundStarter;
import com.farcr.nomansland.common.block.moonlight.MoonlightCandleBlock;
import com.farcr.nomansland.common.extension.EntityExtension;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonState;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.friend.offering.OfferingContext;
import com.farcr.nomansland.common.friend.offering.OfferingType;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.farcr.nomansland.common.block.moonlight.MoonlightBasinBlock.MULTIBLOCK_SIZE;

public class MoonlightBasinBlockEntity extends BlockEntity {

    public MoonlightBasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOONLIGHT_BASIN.get(), pos, blockState);
        pulseUpdate();
    }

    private void pulseUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(
                getBlockPos(), getBlockState(),
                getBlockState(), 3
            );
        }
    }

    private static final AABB BASIN_BOUNDING_BOX =
        Block.box(-8d, 11d, -8d, 24d, 24d, 24d)
            .toAabbs().getFirst();

    public static OfferingContext loopBasinEntities(Function<Entity, OfferingContext> consumer, Level level, BlockPos pos) {
        AABB aabb = BASIN_BOUNDING_BOX.move(pos);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb, EntitySelector.ENTITY_STILL_ALIVE)) {
            OfferingContext offeringContext = consumer.apply(entity);
            if (offeringContext != null)
                return offeringContext;
        }
        return null;
    }

    public static OfferingContext getOfferingAbove(BlockPos pos, Level level) {
        return loopBasinEntities((entity) -> {
            if (entity.NML$wasPreviouslyInspected())
                return null;

            ArrayList<DialoguePool> list = new ArrayList<>();
            // Item Offering List
            if (entity instanceof ItemEntity itemEntity) {
                Item itemType = itemEntity.getItem().getItem().asItem();
                DialogueUtil.appendTags(
                    itemType, level.registryAccess(), Registries.ITEM,
                    MoonlightOfferingConditions.ItemOfferingConditional.COMPILED_MAP,
                    MoonlightOfferingConditions.ItemOfferingConditional.KEY_MAP,
                    list
                );
            }
            // Entity Offering List
            DialogueUtil.appendTags(
                entity.getType(), level.registryAccess(), Registries.ENTITY_TYPE,
                MoonlightOfferingConditions.EntityOfferingConditional.COMPILED_MAP,
                MoonlightOfferingConditions.EntityOfferingConditional.KEY_MAP,
                list
            );

            if (!list.isEmpty()) {
                DialoguePool pool = list.stream().max(Comparator.comparingInt(p -> p.getWeight().asInt())).get();
                Optional<Registry<DialoguePool>> optionalRegistry = level.registryAccess().registry(NMLRegistries.OFFERING_DIALOGUE_KEY);
                if (optionalRegistry.isPresent()) return new OfferingContext(entity, optionalRegistry.get().getKey(pool));
            }
            return null;
        }, level, pos);
    }

    public static final float FRIENDSHIP_MAX_RANGE = 16;

    private OfferingContext inspectionContext;
    private UUID pendingInspectionUUID;
    private void setInspectionContext(OfferingContext newInspectionContext, FriendMoon friendMoon) {
        // clear previous inspection context
        if (inspectionContext != null)
            inspectionContext.getEntity().NML$setInspectionState(false);

        // assign new inspection context
        inspectionContext = newInspectionContext;
        if (newInspectionContext != null)
            newInspectionContext.getEntity().NML$setInspectionState(true);

        assert level != null;
        if (friendMoon != null && newInspectionContext != null) {
            friendMoon.setState(FriendMoonState.OFFERING);
            friendMoon.resetDialogue(level.isClientSide());
        }

        if (!level.isClientSide())
            pulseUpdate();
    }

    private boolean tryResolveInspection() {
        if (pendingInspectionUUID == null || level == null) return false;
        if (inspectionContext != null && inspectionContext.getEntity() != null
            && pendingInspectionUUID.equals(inspectionContext.getEntity().getUUID()))
            return true;
        UUID uuid = pendingInspectionUUID;
        AABB searchBox = new AABB(getBlockPos()).inflate(FRIENDSHIP_MAX_RANGE);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, searchBox, EntitySelector.ENTITY_STILL_ALIVE)) {
            if (entity.getUUID().equals(uuid)) {
                setInspectionContext(new OfferingContext(entity, null), clientMoon);
                return true;
            }
        }
        return false;
    }

    private float offeringBeamIntensity, previousOfferingBeamIntensity;
    private void updateOfferingBeam() {
        if (!level.isClientSide()) return;

        previousOfferingBeamIntensity = offeringBeamIntensity;
        if (this.clientMoon.getState() == FriendMoonState.OFFERING && this.clientMoon.isActive()) {
            offeringBeamIntensity = Mth.lerp(0.1F, offeringBeamIntensity, 1.0F);
        } else {
            offeringBeamIntensity = Mth.lerp(0.1F, offeringBeamIntensity, 0.0F);
        }
    }
    public float getOfferingBeamIntensity(float partialTick) {
        return Mth.lerp(partialTick, this.offeringBeamIntensity, this.previousOfferingBeamIntensity);
    }

    public float getOfferingPeakHeight(float partialTick) {
        float fallback = 1.5F;
        if (inspectionContext == null || inspectionContext.getEntity() == null) return fallback;
        Entity entity = inspectionContext.getEntity();
        if (!entity.isAlive()) return fallback;
        double entityY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double centerY = entityY + entity.getBbHeight() * 0.5;
        return (float) (centerY - getBlockPos().getY() - 1.0);
    }

    public static final int BLOCK_Y_REACH = 3;
    public static final int BLOCK_EXTEND_REACH = 10;

    public static ArrayList<BlockPos> getCandles(Level level, BlockPos basinPosition) {
        ArrayList<BlockPos> candles = new ArrayList<>();

        int rotation = 270;
        int travelAlong = 0;
        int segmentLength = 1;
        int segmentProgress = 0;
        Vec3i vectorOffset = new Vec3i(0, 0, 0);
        for (int i = 1; i <= (BLOCK_EXTEND_REACH * BLOCK_EXTEND_REACH); i++) {
            for (int j = -BLOCK_Y_REACH; j < BLOCK_Y_REACH; j++) {
                BlockPos elevatedPosition = basinPosition.offset(vectorOffset).offset(0, j, 0);
                BlockState potentialCandle = level.getBlockState(elevatedPosition);
                if (potentialCandle.is(NMLBlocks.MOONLIGHT_CANDLE))
                    candles.add(new BlockPos(elevatedPosition));
            }
            double radians = rotation * (Math.PI / 180.0);
            vectorOffset = vectorOffset.offset((int) Math.round(Math.sin(radians)), 0, (int) Math.round(Math.cos(radians)));

            travelAlong++;
            if (travelAlong >= segmentLength) {
                travelAlong = 0;
                rotation += 90;
                if (rotation >= 360)
                    rotation -= 360;

                segmentProgress++;
                if (segmentProgress % 2 == 0)
                    segmentLength++;
            }
        }
        return candles;
    }

    private int trackedCandles = -1;
    private boolean queryNegativeInteraction(ArrayList<BlockPos> candleList) {
        int litCandles = queryLitCandles(candleList);
        boolean returnValue = (litCandles < trackedCandles);
        trackedCandles = litCandles;
        return returnValue;
    }

    private int queryLitCandles(ArrayList<BlockPos> candleList) {
        int litCandles = 0;
        for (BlockPos candlePos : candleList) {
            assert level != null;
            if (level.getBlockState(candlePos).getValue(MoonlightCandleBlock.CANDLE_LIT))
                litCandles++;
        }
        return litCandles;
    }

    private HashMap<UUID, Boolean> quickSparkHash = new HashMap<>();
    private int jukeboxScanTimer = 0;

    private static BlockPos findPlayingJukebox(Level level, BlockPos basinPos) {
        int range = (int) FRIENDSHIP_MAX_RANGE;
        for (int x = -range; x <= range; x++) {
            for (int y = -BLOCK_Y_REACH; y < BLOCK_Y_REACH; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = basinPos.offset(x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.hasProperty(BlockStateProperties.HAS_RECORD)
                    && checkState.getValue(BlockStateProperties.HAS_RECORD))
                        return checkPos;
                }
            }
        }
        return null;
    }

    private boolean basinIsLit = false;
    public void setBasinLight(boolean shouldLight) {
        if (shouldLight == basinIsLit) return;
        BlockPos centerPosition = this.getBlockPos();
        for (int i = 0; i < MULTIBLOCK_SIZE; i++) {
            for (int j = 0; j < MULTIBLOCK_SIZE; j++) {
                BlockPos newPosition = centerPosition.offset(new Vec3i(i - 1, 0, j - 1));
                Level level = this.getLevel();
                BlockState blockState = level.getBlockState(newPosition);
                if (blockState.is(NMLBlocks.MOONLIGHT_BASIN))
                    level.setBlock(newPosition, blockState.setValue(MoonlightCandleBlock.CANDLE_LIT, shouldLight), 3);
            }
        }
        basinIsLit = shouldLight;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonlightBasinBlockEntity blockEntity) {
        @Nullable FriendMoon friendMoon = (!level.isClientSide() ? FriendMoon.getOrDefault(level.getServer().overworld()) : blockEntity.clientMoon);
        if (friendMoon == null) {
            blockEntity.setBasinLight(false);
            return;
        }

        blockEntity.setBasinLight(friendMoon.isActive());

        // Client may have received the inspection UUID before the entity was synced (or before it
        // entered render distance). Retry the lookup until the entity shows up.
        if (level.isClientSide() && blockEntity.pendingInspectionUUID != null
            && blockEntity.inspectionContext == null)
            blockEntity.tryResolveInspection();

        AABB aabb = new AABB(pos).inflate(FRIENDSHIP_MAX_RANGE);
        if (!level.isClientSide()) {
            if (FriendMoon.isNightTime(level)) {
                for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, aabb))
                    FriendMoon.grantPlayerFriendship(friendMoon, serverPlayer, pos);
                if (friendMoon.shouldPulseUpdate())
                    blockEntity.pulseUpdate();
            }
        }

        blockEntity.updateOfferingBeam();

        if (level.isClientSide()) {
            boolean shouldLoop = friendMoon.isActive() && friendMoon.getState() == FriendMoonState.OFFERING;
            if (shouldLoop && (blockEntity.offeringSoundInstance == null
                || !FriendMoonOfferingSoundStarter.isActive(blockEntity.offeringSoundInstance))) {
                blockEntity.offeringSoundInstance = FriendMoonOfferingSoundStarter.start(pos);
            } else if (!shouldLoop) {
                blockEntity.offeringSoundInstance = null;
            }
        }

        if (!friendMoon.isActive()) {
            blockEntity.trackedCandles = 0;
            blockEntity.setInspectionContext(null, friendMoon);
            if (!level.isClientSide()) {
                friendMoon.abortAscension();
                ArrayList<BlockPos> candleList = getCandles(level, pos);
                candleList.forEach((blockPos) -> {
                    BlockState blockState = level.getBlockState(blockPos);
                    if (blockState.getValue(MoonlightCandleBlock.CANDLE_LIT)
                    && blockState.getBlock() instanceof MoonlightCandleBlock candleBlock) {
                        candleBlock.extinguish(null, blockState, level, blockPos);
                        candleBlock.triggerSparkAnimation(blockState, level, blockPos, level.getRandom());
                    }
                });
            }
            return;
        }

        // Offerings
        if (friendMoon.getState() != FriendMoonState.OFFERING)
            blockEntity.setInspectionContext(null, friendMoon);
        else {
            OfferingContext inspectionContext = blockEntity.inspectionContext;
            if (inspectionContext != null && inspectionContext.getEntity() != null) {
                Entity entity = inspectionContext.getEntity();
                if (!entity.isAlive()) {
                    blockEntity.setInspectionContext(null, friendMoon);
                    if (!level.isClientSide) friendMoon.negative();
                    friendMoon.abortAscension();
                } else {
                    Vec3 newPosition = new Vec3(pos.getCenter().x, entity.position().y, pos.getCenter().z);
                    entity.setDeltaMovement(new Vec3(0, 0, 0));

                    Vec3 approachSpeed = newPosition.subtract(
                        entity.position()).multiply(new Vec3(new Vector3f(1 / 15f)));

                    entity.addDeltaMovement(approachSpeed);
                    if (approachSpeed.lengthSqr() <= 0.001f) {
                        int raiseDistance = inspectionContext.getOfferingType().equals(OfferingType.SPECIAL) ? 4 : 2;
                        Vec3 raisedPosition = pos.above(raiseDistance).getCenter();
                        Vec3 dist = raisedPosition.subtract(entity.position());

                        float speed = 1 / 20f;
                        entity.setDeltaMovement(
                            dist.multiply(new Vec3(new Vector3f(speed))));

                        if (dist.lengthSqr() <= 0.1f) {
                            if (inspectionContext.getOfferingType().shouldContinueRegularInteraction()
                            && (!level.isClientSide() && friendMoon.getDialogueTicks() < 0)) {
                                int dialogueLength = friendMoon.getDialogueFromLocation(
                                    NMLRegistries.OFFERING_DIALOGUE_KEY, inspectionContext.getDialogueLocation())
                                    .dispatch(level, friendMoon.getFriendshipPlayers());
                                friendMoon.applyDialogueLength(dialogueLength - 80);
                            }

                            switch (inspectionContext.getOfferingType()) {
                                case SPECIAL: {
                                    if (!friendMoon.specialInteraction(level, entity))
                                        blockEntity.setInspectionContext(null, friendMoon);
                                    break;
                                }
                                case MAP: {
                                    if (friendMoon.getDialogueTicks() >= 0 && friendMoon.getDialogueTicks() <= FriendMoon.MAP_PARTICLE_DURATION)
                                        friendMoon.mapInteraction(level, entity, pos);
                                    break;
                                }
                                case BAD_OMEN: {
                                    if (!friendMoon.badOmenInteraction(level, entity, pos)) {
                                        friendMoon.setState(FriendMoonState.UPSET);
                                        friendMoon.forFriendshipPlayers((player) -> friendMoon.setUpsetWith(player.getUUID()));
                                        blockEntity.setInspectionContext(null, friendMoon);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!level.isClientSide()) {
            // Determine offering context
            if (blockEntity.inspectionContext == null) {
                OfferingContext context = getOfferingAbove(pos, level);
                if (context != null && context.isValid()
                && (context.getEntity().onGround() || context.getEntity() instanceof FlyingMob
                || context.getEntity().getDeltaMovement().lengthSqr() < 1.0E-3))
                    blockEntity.setInspectionContext(context, friendMoon);
            }

            if (friendMoon.getState() == FriendMoonState.PASSIVE && !friendMoon.isJukeboxInteractionActive()) {
                if (blockEntity.jukeboxScanTimer++ >= 20) {
                    blockEntity.jukeboxScanTimer = 0;
                    BlockPos jukeboxPos = findPlayingJukebox(level, pos);
                    if (jukeboxPos != null)
                        friendMoon.startJukeboxInteraction(jukeboxPos);
                }
            }

            ArrayList<BlockPos> candleList = getCandles(level, pos);
            if (blockEntity.queryNegativeInteraction(candleList)) {
                friendMoon.negative();
                if (blockEntity.trackedCandles <= 0)
                    friendMoon.setState(FriendMoonState.UPSET);
            } else if (friendMoon.getCandleTime() <= 0) {
                candleList.forEach((blockPos) -> {
                    BlockState blockState = level.getBlockState(blockPos);
                    if (!blockState.getValue(MoonlightCandleBlock.CANDLE_LIT)
                    && blockState.getBlock() instanceof MoonlightCandleBlock candleBlock)
                        candleBlock.lightSpark(blockState, level, blockPos, level.getRandom());
                });
            }
        } else {
            if (friendMoon.isJukeboxInteractionActive() && friendMoon.getJukeboxInteractionTicks() >= 0) {
                BlockPos jukeboxPos = friendMoon.getTargetJukeboxPos();
                int ticks = friendMoon.getJukeboxInteractionTicks();
                if (jukeboxPos != null) {
                    double cx = jukeboxPos.getX() + 0.5;
                    double cy = jukeboxPos.getY() + 0.5;
                    double cz = jukeboxPos.getZ() + 0.5;
                    for (int i = 0; i < 2; i++) {
                        double burstX = (level.getRandom().nextDouble() - 0.5) * 0.15;
                        double burstY = 0.05 + level.getRandom().nextDouble() * 0.1;
                        double burstZ = (level.getRandom().nextDouble() - 0.5) * 0.15;
                        level.addParticle(
                            NMLParticleTypes.MOONLIGHT_SPARK.get(),
                            cx, cy + 0.3, cz,
                            burstX, burstY, burstZ
                        );
                    }
                }
            }

            if (friendMoon.getState() != FriendMoonState.OFFERING) {
                for (Entity entity : level.getEntitiesOfClass(LivingEntity.class, aabb, (entity) -> {
                    UUID uuid = entity.getUUID();
                    if (!blockEntity.quickSparkHash.containsKey(uuid)) {
                        ArrayList<DialoguePool> list = new ArrayList<>();
                        DialogueUtil.appendTags(
                                entity.getType(), level.registryAccess(), Registries.ENTITY_TYPE,
                                MoonlightOfferingConditions.EntityOfferingConditional.COMPILED_MAP,
                                MoonlightOfferingConditions.EntityOfferingConditional.KEY_MAP,
                                list
                        );
                        blockEntity.quickSparkHash.put(uuid, !list.isEmpty());
                    }
                    return blockEntity.quickSparkHash.get(uuid);
                })) {
                    if (level.getRandom().nextFloat() <= 0.25
                    && !((EntityExtension) entity).NML$wasPreviouslyInspected()) {
                        level.addParticle(
                                NMLParticleTypes.MOONLIGHT_SPARK.get(),
                                entity.getX(), entity.getY() + 0.5f, entity.getZ(),
                                0f, 0f, 0f
                        );
                    }
                }
            }
        }
    }

    public FriendMoon clientMoon;
    private Object offeringSoundInstance;
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("InspectionUUID") && level != null) {
            pendingInspectionUUID = UUID.fromString(tag.getString("InspectionUUID"));
            tryResolveInspection();
        } else if (pendingInspectionUUID != null) {
            pendingInspectionUUID = null;
            if (level != null && level.isClientSide() && inspectionContext != null)
                setInspectionContext(null, clientMoon);
        }

        if (clientMoon == null)
            clientMoon = new FriendMoon(null);
        clientMoon.load(tag, registries);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (inspectionContext != null && inspectionContext.isValid())
            tag.putString("InspectionUUID", inspectionContext.getEntity().getStringUUID());

        // Store Friend Moon information in BlockEntity
        if (level != null && !level.isClientSide()) {
            FriendMoon friendMoon = FriendMoon.getOrDefault(level.getServer().overworld());
            friendMoon.save(tag, registries);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
