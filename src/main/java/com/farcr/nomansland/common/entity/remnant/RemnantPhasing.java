package com.farcr.nomansland.common.entity.remnant;

import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RemnantPhasing {

    public record PerchSpot(BlockPos feet, Direction facing) {
    }

    public static boolean isDiveable(BlockState state) {
        return state.is(NMLTags.REMNANT_DIVEABLE);
    }

    public static boolean canPhaseThrough(BlockGetter level, BlockPos pos) {
        if (!isDiveable(level.getBlockState(pos))) return false;
        return hasNearbyAir(level, pos);
    }

    public static boolean hasNearbyAir(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (level.getBlockState(cursor.setWithOffset(pos, dx, dy, dz)).isAir()) return true;
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) != 2) continue;
                    if (level.getBlockState(cursor.setWithOffset(pos, dx, dy, dz)).isAir()) return true;
                }
            }
        }
        return false;
    }

    public static boolean isPassable(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }

    public static boolean isWallColumn(BlockGetter level, BlockPos feet) {
        return canPhaseThrough(level, feet) && canPhaseThrough(level, feet.above());
    }

    public static boolean isOpenColumn(BlockGetter level, BlockPos feet) {
        return isPassable(level, feet) && isPassable(level, feet.above());
    }

    @Nullable
    public static Direction findPerchFacing(BlockGetter level, BlockPos feet, RandomSource random) {
        Direction[] directions = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        int offset = random.nextInt(directions.length);
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[(i + offset) % directions.length];
            if (isValidPerchFacing(level, feet, direction)) return direction;
        }
        return null;
    }

    public static boolean isValidPerchFacing(BlockGetter level, BlockPos feet, Direction facing) {
        BlockPos front = feet.relative(facing);
        if (!isOpenColumn(level, front)) return false;
        return level.getBlockState(front.below()).isFaceSturdy(level, front.below(), Direction.UP);
    }

    public static Vec3 halfOutPosition(BlockPos feet, Direction facing) {
        return Vec3.atBottomCenterOf(feet).add(facing.getStepX() * 0.5, 0.0, facing.getStepZ() * 0.5);
    }

    public static Vec3 hiddenPosition(BlockPos feet) {
        return Vec3.atBottomCenterOf(feet);
    }

    public static Vec3 frontPosition(BlockPos feet, Direction facing) {
        return Vec3.atBottomCenterOf(feet.relative(facing));
    }

    @Nullable
    public static BlockPos findStandableNear(BlockGetter level, BlockPos start, int descent) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int i = 0; i <= descent; i++) {
            if (isOpenColumn(level, cursor) && level.getBlockState(cursor.below()).isFaceSturdy(level, cursor.below(), Direction.UP)) {
                return cursor.immutable();
            }
            cursor.move(0, -1, 0);
        }
        return null;
    }

    @Nullable
    public static PerchSpot findPerchAround(BlockGetter level, BlockPos center, RandomSource random, int tries, int spread, boolean avoidSky) {
        for (int i = 0; i < tries; i++) {
            BlockPos base = center.offset(random.nextInt(spread * 2 + 1) - spread, random.nextInt(7) - 3, random.nextInt(spread * 2 + 1) - spread);
            PerchSpot spot = perchAtGround(level, base, avoidSky);
            if (spot != null) return spot;
        }
        return null;
    }

    @Nullable
    public static PerchSpot findAmbushPerch(BlockGetter level, Remnant remnant, LivingEntity target, Vec3 aim, boolean trustView, double minRadius, double maxRadius) {
        RandomSource random = remnant.getRandom();
        double baseAngle;
        double spread;
        if (trustView) {
            Vec3 view = target.getViewVector(1.0F);
            baseAngle = Mth.atan2(view.z, view.x) + Math.PI;
            spread = 2.8;
        } else {
            baseAngle = random.nextDouble() * Math.PI * 2.0;
            spread = Math.PI * 2.0;
        }
        for (int i = 0; i < 20; i++) {
            double angle = baseAngle + (random.nextDouble() - 0.5) * spread;
            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
            BlockPos base = BlockPos.containing(
                    aim.x + Math.cos(angle) * radius,
                    aim.y + random.nextInt(3) - 1,
                    aim.z + Math.sin(angle) * radius);
            PerchSpot spot = perchAtGround(level, base, remnant.avoidsSky());
            if (spot != null) return spot;
        }
        return null;
    }

    @Nullable
    public static PerchSpot findRetreatPerch(BlockGetter level, Remnant remnant, Vec3 aim) {
        RandomSource random = remnant.getRandom();
        double awayAngle = Mth.atan2(remnant.getZ() - aim.z, remnant.getX() - aim.x);
        for (int i = 0; i < 16; i++) {
            double angle = awayAngle + (random.nextDouble() - 0.5) * 2.0;
            double radius = 2.5 + random.nextDouble() * 5.0;
            BlockPos base = BlockPos.containing(
                    remnant.getX() + Math.cos(angle) * radius,
                    remnant.getY() + random.nextInt(3) - 1,
                    remnant.getZ() + Math.sin(angle) * radius);
            PerchSpot spot = perchAtGround(level, base, remnant.avoidsSky());
            if (spot == null) continue;
            Vec3 facingVec = Vec3.atLowerCornerOf(spot.facing().getNormal());
            Vec3 toAim = aim.subtract(Vec3.atCenterOf(spot.feet())).normalize();
            if (facingVec.dot(toAim) > 0.1) return spot;
        }
        return null;
    }

    @Nullable
    public static PerchSpot perchAtGround(BlockGetter level, BlockPos base, boolean avoidSky) {
        BlockPos ground = findStandableNear(level, base, 4);
        if (ground == null) return null;
        if (avoidSky && level instanceof LevelReader reader && reader.canSeeSky(ground)) return null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos wall = ground.relative(direction);
            if (isWallColumn(level, wall) && isValidPerchFacing(level, wall, direction.getOpposite())) {
                return new PerchSpot(wall, direction.getOpposite());
            }
        }
        return null;
    }

    public static boolean isSkyExposed(LevelReader level, BlockPos pos) {
        return level.canSeeSky(pos);
    }

    public static boolean isNearOpenSky(LevelReader level, BlockPos pos) {
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
        return surface - pos.getY() < 5;
    }

    @Nullable
    public static BlockPos findCeilingSpot(LevelReader level, Vec3 feetPos, boolean allowHighDrop) {
        BlockPos head = BlockPos.containing(feetPos.x, feetPos.y + 1.6, feetPos.z);
        BlockPos.MutableBlockPos cursor = head.mutable();
        for (int i = 1; i <= 5; i++) {
            cursor.setY(head.getY() + i);
            BlockState state = level.getBlockState(cursor);
            if (state.getCollisionShape(level, cursor).isEmpty()) continue;
            if (!canPhaseThrough(level, cursor)) return null;
            if (!allowHighDrop && cursor.getY() - feetPos.y > 5.5) return null;
            return cursor.immutable();
        }
        return null;
    }

    @Nullable
    public static BlockPos findCaveSpot(LevelReader level, Remnant remnant, int tries) {
        RandomSource random = remnant.getRandom();
        BlockPos origin = remnant.blockPosition();
        for (int i = 0; i < tries; i++) {
            BlockPos probe = origin.offset(random.nextInt(33) - 16, -2 - random.nextInt(18), random.nextInt(33) - 16);
            BlockPos ground = findStandableNear(level, probe, 4);
            if (ground != null && !level.canSeeSky(ground)) return ground;
        }
        return null;
    }
}
