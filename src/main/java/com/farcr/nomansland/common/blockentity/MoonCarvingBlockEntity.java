package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.AncestralCarvingBlock;
import com.farcr.nomansland.common.block.MoonCarvingBlock;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamStorage;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.networking.ClientboundZoomEffectPacket;
import com.farcr.nomansland.common.registry.NMLAttachmentTypes;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class MoonCarvingBlockEntity extends BlockEntity {
    public MoonCarvingBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOON_CARVING.get(), pos, blockState);
    }

    private static final int VISION_RANGE = 24;
    private static final int STARE_AT_TICKS = 40;
    private static final int DREAM_TIME = 24000 * 3;

    private static final DreamType MOONLIGHT_DREAM_TYPE
        = NMLDreamTypes.FRIEND_MOON_DREAM.get();

    private final Map<Player, Integer> playerStareMap = new HashMap<>();
    private boolean playerMeetsCondition(ServerPlayer player) {
        FriendMoon friendMoon = FriendMoon.getOrDefault(player.getServer().overworld());
        DreamStorage storage = DreamManager.getOrDefault(player.getServer()).getPlayerStorage(player);
        return (storage.getTimeRemainingForDream(MOONLIGHT_DREAM_TYPE) <= 0)
            && (!storage.getHasExperiencedDream(MOONLIGHT_DREAM_TYPE))
            || friendMoon.cosmicBodyExpiredForPlayer(player);
    }

    private boolean hitIsPartOfFormation(BlockPos hit, BlockPos center, BlockState state) {
        Direction facing = state.getValue(AncestralCarvingBlock.FACING);
        int rotation = state.getValue(AncestralCarvingBlock.ROTATION);
        Direction right = AncestralCarvingBlock.getPlaneRight(facing, rotation);
        Direction down = AncestralCarvingBlock.getPlaneDown(facing, rotation);
        for (int col = -1; col <= 1; col++) {
            for (int row = -1; row <= 1; row++) {
                if (hit.equals(center.relative(right, col).relative(down, row))) return true;
            }
        }
        return false;
    }

    private boolean blockStateMeetsConditions(BlockHitResult hit, BlockPos pos, BlockState blockState) {
        if (!hit.getDirection().equals(blockState.getValue(BlockStateProperties.FACING))) return false;
        return !((MoonCarvingBlock) blockState.getBlock()).queryPositions(
            level, pos, blockState.getValue(AncestralCarvingBlock.FACING),
            blockState.getValue(AncestralCarvingBlock.ROTATION),
            (blockState1) -> !blockState1.is(NMLBlocks.MOON_CARVING)
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonCarvingBlockEntity blockEntity) {
        if (!level.isClientSide) {
            Vec3 centerPos = Vec3.atCenterOf(pos);
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
            double rangeSq = Mth.square(VISION_RANGE);
            for (Player player : level.players()) {
                Map<Player, Integer> map = blockEntity.playerStareMap;
                if (SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level, centerPos, player.position()) <= rangeSq
                && blockEntity.playerMeetsCondition((ServerPlayer) player)) {
                    Vec3 eyePos = player.getEyePosition();
                    Vec3 viewVec = player.getViewVector(1.0f);
                    if (subLevel != null) {
                        eyePos = subLevel.logicalPose().transformPositionInverse(eyePos);
                        viewVec = subLevel.logicalPose().transformNormalInverse(viewVec);
                    }
                    BlockHitResult cast = level.clip(
                        new ClipContext(
                            eyePos,
                            eyePos.add(viewVec.scale(VISION_RANGE)),
                            ClipContext.Block.VISUAL,
                            ClipContext.Fluid.NONE,
                            CollisionContext.empty()
                        )
                    );
                    if (cast.getType() == HitResult.Type.BLOCK
                    && blockEntity.hitIsPartOfFormation(cast.getBlockPos(), pos, state)
                    && blockEntity.blockStateMeetsConditions(cast, pos, state)) {
                        map.put(player, map.getOrDefault(player, 0) + 1);
                        if (map.get(player) > STARE_AT_TICKS) {
                            DreamStorage dreamStorage = DreamManager.getOrDefault(player.getServer())
                                .getPlayerStorage((ServerPlayer) player);
                            FriendMoon.getOrDefault(level.getServer().overworld())
                                .resetCosmicBodyForPlayer(player);
                            dreamStorage.removeInformationAboutDream(MOONLIGHT_DREAM_TYPE);
                            dreamStorage.setTimeRemainingForDream(
                                MOONLIGHT_DREAM_TYPE,
                                DREAM_TIME
                            );
                            level.playSound(
                                null, pos,
                                NMLSounds.MOON_CARVING_ACTIVATE.get(),
                                SoundSource.AMBIENT
                            );
                            PacketDistributor.sendToPlayer((ServerPlayer) player,
                                new ClientboundZoomEffectPacket(70));
                            player.setData(NMLAttachmentTypes.LAST_MOON_CARVING_INTERACTION.get(), level.getGameTime());
                        }
                    } else
                        map.remove(player);
                } else
                    map.remove(player);
            }
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
