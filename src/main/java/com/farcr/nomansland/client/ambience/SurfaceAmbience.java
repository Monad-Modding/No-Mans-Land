package com.farcr.nomansland.client.ambience;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector3f;

public class SurfaceAmbience {
    private static final String SURFACE_AMBIENCE_PROFILER = NoMansLand.MODID + ".surfaceAmbienceTick";

    private final Minecraft minecraft;
    private float aboveGroundFactor, prevAboveGroundFactor = 1.0F;

    public SurfaceAmbience(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private final BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos iteratorPos = new BlockPos.MutableBlockPos();
    public void tick(boolean shouldUpdate) {
        minecraft.getProfiler().push(SURFACE_AMBIENCE_PROFILER);

        prevAboveGroundFactor = aboveGroundFactor;
        if (!shouldUpdate) return;

        ClientLevel level = minecraft.level;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vector3f cameraLook = camera.getLookVector();
        boolean aboveGround = true;

        BlockHitResult cast = level.clip(
                new ClipContext(
                        camera.getPosition(),
                        camera.getPosition().add(cameraLook.x * 8, cameraLook.y * 8, cameraLook.z * 8),
                        ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty())
        );

        // our sample positions are randomly set.
        // making sure that it's not inside a solid block.
        for (int i = 0; i < 3; i++) {
            // offset in look direction
            samplePos.set(cast.getBlockPos());

            // some random offset
            int x = (int) (level.random.nextGaussian() * 5);
            int z = (int) (level.random.nextGaussian() * 5);

            samplePos.move(x, 2, z);
            if (!isSolidBlock(level, samplePos)) {
                break;
            }
        }

        int heightY = level.getHeight(Heightmap.Types.WORLD_SURFACE, samplePos.getX(), samplePos.getZ());
        // if we're above the height map and sea level, we're always considered outside.
        boolean aboveHeightMap = camera.getPosition().y > heightY;
        if (!aboveHeightMap) {
            // count the number of fully solid blocks above us.
            int solidBlocksEncountered = 0;
            iteratorPos.set(samplePos);
            while (iteratorPos.getY() < heightY) {
                BlockState state = level.getBlockState(iteratorPos);
                if (state.isCollisionShapeFullBlock(level, iteratorPos) && state.blocksMotion() && state.canOcclude()) {
                    solidBlocksEncountered++;
                }

                if (solidBlocksEncountered >= 12) {
                    aboveGround = false;
                    break;
                }
                iteratorPos.move(Direction.UP);
            }
        }

        float mixFactor = 1.0F / 64.0F;
        aboveGroundFactor = aboveGroundFactor * (1.0F - mixFactor) + (aboveGround ? 1.0F : -1.0F) * mixFactor;
        minecraft.getProfiler().pop();
    }

    public float getAboveGroundFactor(double partialTick) {
        return (float) (Mth.lerp(partialTick, prevAboveGroundFactor, aboveGroundFactor) + 1.0F) / 2.0F;
    }

    private static boolean isSolidBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isCollisionShapeFullBlock(level, pos) && state.blocksMotion() && state.canOcclude();
    }
}
