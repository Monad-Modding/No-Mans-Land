package com.farcr.nomansland.common.entity.remnant;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class RemnantNodeEvaluator extends WalkNodeEvaluator {

    private static final int[][] PHASE_OFFSETS = {
            {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0},
            {1, 0, 1}, {1, 0, -1}, {-1, 0, 1}, {-1, 0, -1}
    };

    private final Long2ByteOpenHashMap phaseCache = new Long2ByteOpenHashMap();

    @Override
    public void done() {
        this.phaseCache.clear();
        super.done();
    }

    private boolean canPhaseInto(int x, int y, int z) {
        long key = BlockPos.asLong(x, y, z);
        byte cached = this.phaseCache.get(key);
        if (cached != 0) return cached == 1;
        boolean result = RemnantPhasing.canPhaseThrough(this.currentContext.level(), new BlockPos(x, y, z));
        this.phaseCache.put(key, (byte) (result ? 1 : 2));
        return result;
    }

    private boolean isPhaseNodeValid(int x, int y, int z) {
        if (!canPhaseInto(x, y, z)) return false;
        if (canPhaseInto(x, y + 1, z)) return true;
        return RemnantPhasing.isPassable(this.currentContext.level(), new BlockPos(x, y + 1, z));
    }

    private Node phaseNode(int x, int y, int z) {
        Node node = this.getNode(x, y, z);
        node.type = PathType.OPEN;
        node.costMalus = this.isShallow(x, y, z) ? 2.0F : 0.0F;
        return node;
    }

    private boolean isShallow(int x, int y, int z) {
        return RemnantPhasing.isPassable(this.currentContext.level(), new BlockPos(x, y + 1, z))
                || RemnantPhasing.isPassable(this.currentContext.level(), new BlockPos(x, y + 2, z));
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        PathType type = super.getPathTypeOfMob(context, x, y, z, mob);
        if (type == PathType.WALKABLE && mob instanceof Remnant remnant && remnant.avoidsSky()
                && mob.level().canSeeSky(new BlockPos(x, y, z))) {
            return PathType.DANGER_OTHER;
        }
        return type;
    }

    @Override
    public Node getStart() {
        if (this.mob instanceof Remnant remnant && remnant.isPhased()) {
            BlockPos pos = this.mob.blockPosition();
            if (canPhaseInto(pos.getX(), pos.getY(), pos.getZ())) {
                return phaseNode(pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return super.getStart();
    }

    @Override
    public int getNeighbors(Node[] outputArray, Node node) {
        if (node.type == PathType.OPEN) {
            return getPhaseNeighbors(outputArray, node);
        }
        int count = super.getNeighbors(outputArray, node);
        return addPhaseEntries(outputArray, count, node);
    }

    private int getPhaseNeighbors(Node[] outputArray, Node node) {
        int count = 0;
        for (int[] offset : PHASE_OFFSETS) {
            if (count >= outputArray.length) break;
            int x = node.x + offset[0];
            int y = node.y + offset[1];
            int z = node.z + offset[2];
            if (isPhaseNodeValid(x, y, z)) {
                Node neighbor = phaseNode(x, y, z);
                if (!neighbor.closed) outputArray[count++] = neighbor;
            }
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            count = addExitNode(outputArray, count, node.x + direction.getStepX(), node.y, node.z + direction.getStepZ());
        }
        count = addExitNode(outputArray, count, node.x, node.y + 1, node.z);
        return addDropExit(outputArray, count, node);
    }

    private int addDropExit(Node[] outputArray, int count, Node node) {
        if (count >= outputArray.length) return count;
        if (!RemnantPhasing.isPassable(this.currentContext.level(), new BlockPos(node.x, node.y - 1, node.z))) return count;
        for (int drop = 1; drop <= 4; drop++) {
            int y = node.y - drop;
            PathType type = this.getCachedPathType(node.x, y, node.z);
            if (type == PathType.WALKABLE) {
                float malus = this.mob.getPathfindingMalus(type);
                if (malus < 0.0F) return count;
                Node landing = this.getNode(node.x, y, node.z);
                if (landing.closed) return count;
                landing.type = type;
                landing.costMalus = Math.max(landing.costMalus, malus + 1.0F);
                outputArray[count++] = landing;
                return count;
            }
            if (type != PathType.OPEN) return count;
        }
        return count;
    }

    private int addExitNode(Node[] outputArray, int count, int x, int y, int z) {
        if (count >= outputArray.length) return count;
        PathType pathType = this.getCachedPathType(x, y, z);
        if (pathType != PathType.WALKABLE) return count;
        float malus = this.mob.getPathfindingMalus(pathType);
        if (malus < 0.0F) return count;
        Node node = this.getNode(x, y, z);
        if (node.closed) return count;
        node.type = pathType;
        node.costMalus = Math.max(node.costMalus, malus);
        outputArray[count++] = node;
        return count;
    }

    private int addPhaseEntries(Node[] outputArray, int count, Node node) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            count = addEntryNode(outputArray, count, node.x + direction.getStepX(), node.y, node.z + direction.getStepZ());
        }
        return addEntryNode(outputArray, count, node.x, node.y - 1, node.z);
    }

    private int addEntryNode(Node[] outputArray, int count, int x, int y, int z) {
        if (count >= outputArray.length) return count;
        if (!isPhaseNodeValid(x, y, z)) return count;
        Node node = phaseNode(x, y, z);
        if (node.closed) return count;
        outputArray[count++] = node;
        return count;
    }
}
