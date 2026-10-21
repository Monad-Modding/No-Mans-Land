package com.farcr.nomansland.common.mixin;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(AlterGroundDecorator.class)
public class AlterGroundDecoratorMixin {
    @Shadow @Final private BlockStateProvider provider;

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void place(TreeDecorator.Context context, CallbackInfo ci) {
        List<BlockPos> startPositions = Lists.newArrayList();
        List<BlockPos> roots = context.roots();
        List<BlockPos> logs = context.logs();

        if (roots.isEmpty()) {
            startPositions.addAll(logs);
        } else if (!logs.isEmpty() && roots.getFirst().getY() == logs.getFirst().getY()) {
            startPositions.addAll(logs);
            startPositions.addAll(roots);
        } else {
            startPositions.addAll(roots);
        }

        if (!startPositions.isEmpty()) {
            BlockStateProvider provider = this.provider;
            AlterGroundEvent.StateProvider eventProvider = EventHooks.alterGround(context, startPositions, provider::getState);

            int maxDistance = 6;
            int maxSpread = 200;

            Set<BlockPos> visited = new HashSet<>();
            Queue<Pair<BlockPos, Integer>> queue = new LinkedList<>();

            for (BlockPos root : startPositions) {
                queue.add(Pair.of(root, 0));
                visited.add(root);
            }

            int spreadCount = 0;
            RandomSource random = context.random();

            while (!queue.isEmpty() && spreadCount < maxSpread) {
                Pair<BlockPos, Integer> pair = queue.poll();
                BlockPos current = pair.getFirst();
                int distance = pair.getSecond();

                if (distance > maxDistance) continue;

                if (Feature.isGrassOrDirt(context.level(), current) && context.level().isStateAtPosition(current.above(), state -> !state.isSolid())) {
                    context.setBlock(current, eventProvider.getState(random, current));
                    spreadCount++;
                }

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos neighbor = current.relative(direction).above(dy);

                        if (!visited.contains(neighbor) && Feature.isGrassOrDirt(context.level(), neighbor) && random.nextFloat() < 0.8f) {
                            visited.add(neighbor);
                            queue.add(Pair.of(neighbor, distance + 1));
                        }
                    }
                }
            }
        }

        ci.cancel();
    }
}
