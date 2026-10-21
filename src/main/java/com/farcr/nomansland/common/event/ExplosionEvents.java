package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.SulfurOreBlock;
import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.entity.bombs.Explosive;
import com.farcr.nomansland.common.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class ExplosionEvents {
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Level level = event.getLevel();

        if (level instanceof ServerLevel windLevel && explosion.getDirectSourceEntity() instanceof AbstractWindCharge) {
            ToxicGasBlock.clearArea(windLevel, explosion.center(), explosion.radius() + 2.0);
        }

        Set<BlockPos> sulfurOres = new HashSet<>();
        if (level instanceof ServerLevel serverLevel) {
            Iterator<BlockPos> iterator = event.getAffectedBlocks().iterator();
            Explosion.BlockInteraction interaction = explosion.getBlockInteraction();
            boolean destroysBlocks = interaction == Explosion.BlockInteraction.DESTROY || interaction == Explosion.BlockInteraction.DESTROY_WITH_DECAY;
            while (iterator.hasNext()) {
                BlockPos pos = iterator.next();
                if (destroysBlocks) {
                    if (SulfurOreBlock.isSulfurOre(level.getBlockState(pos))) {
                        sulfurOres.add(pos.immutable());
                        for (Direction direction : Direction.values()) {
                            BlockPos edge = pos.relative(direction);
                            if (SulfurOreBlock.isSulfurOre(level.getBlockState(edge))) sulfurOres.add(edge.immutable());
                        }
                    }
                }

                BlockState state = level.getBlockState(pos);

                for (ExtinguishableBlockPairing block : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                    if (state.is(block.litBlock())) {
                        level.gameEvent(explosion.getDirectSourceEntity(), GameEvent.BLOCK_CHANGE, pos);
                        level.setBlock(pos, block.extinguishedBlock().withPropertiesOf(state), 11);
                        level.playSound(null, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        break;
                    }
                }

                if (event.getExplosion().getDirectSourceEntity() instanceof Explosive explosive && explosive.getOwner() instanceof ServerPlayer serverPlayer && state.is(Tags.Blocks.ORES)) {
                    NMLCriteriaTriggers.MINE_ORE_WITH_EXPLOSIVE.get().trigger(serverPlayer, pos);
                }
            }

            if (!sulfurOres.isEmpty()) {
                for (BlockPos sulfurOre : sulfurOres) SulfurOreBlock.prime(serverLevel, sulfurOre);
            }
        }
    }
}
