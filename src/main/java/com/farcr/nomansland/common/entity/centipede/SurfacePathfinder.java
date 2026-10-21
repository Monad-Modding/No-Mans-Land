package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

public final class SurfacePathfinder {
    public record Cell(BlockPos block, Direction face) {
    }

    private record Node(Cell cell, int f) {
    }

    private final Level level;

    public SurfacePathfinder(Level level) {
        this.level = level;
    }

    private boolean solid(BlockPos p) {
        return !this.level.getBlockState(p).getCollisionShape(this.level, p).isEmpty();
    }

    public boolean isCell(Cell cell) {
        return solid(cell.block()) && !solid(cell.block().relative(cell.face()));
    }

    public Cell step(Cell cell, Direction t) {
        if (t.getAxis() == cell.face().getAxis()) {
            return null;
        }
        BlockPos air = cell.block().relative(cell.face());
        BlockPos ahead = air.relative(t);
        if (solid(ahead)) {
            return new Cell(ahead, t.getOpposite());
        }
        BlockPos floorAhead = cell.block().relative(t);
        if (solid(floorAhead)) {
            return new Cell(floorAhead, cell.face());
        }
        return new Cell(cell.block(), t);
    }

    private List<Direction> tangents(Direction face) {
        List<Direction> out = new ArrayList<>(4);
        for (Direction d : Direction.values()) {
            if (d.getAxis() != face.getAxis()) {
                out.add(d);
            }
        }
        return out;
    }

    public List<Cell> find(Cell start, Cell goal, int maxNodes) {
        if (!isCell(start) || !isCell(goal)) {
            return null;
        }
        if (start.equals(goal)) {
            return List.of(start);
        }
        Map<Cell, Cell> cameFrom = new HashMap<>();
        Map<Cell, Integer> gScore = new HashMap<>();
        Set<Cell> closed = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(Node::f));
        gScore.put(start, 0);
        open.add(new Node(start, heuristic(start, goal)));
        int expanded = 0;
        while (!open.isEmpty() && expanded < maxNodes) {
            Cell cur = open.poll().cell();
            if (cur.equals(goal)) {
                return reconstruct(cameFrom, cur);
            }
            if (!closed.add(cur)) {
                continue;
            }
            expanded++;
            int cg = gScore.get(cur);
            for (Direction t : tangents(cur.face())) {
                Cell nb = step(cur, t);
                if (nb == null || closed.contains(nb)) {
                    continue;
                }
                int ng = cg + cellCost(nb);
                if (ng < gScore.getOrDefault(nb, Integer.MAX_VALUE)) {
                    cameFrom.put(nb, cur);
                    gScore.put(nb, ng);
                    open.add(new Node(nb, ng + heuristic(nb, goal)));
                }
            }
        }
        return null;
    }

    public Cell pickReachable(Cell start, int minDist, int maxDist, int maxNodes, RandomSource random, Predicate<Cell> valid) {
        if (!isCell(start)) {
            return null;
        }
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        Map<Cell, Integer> dist = new HashMap<>();
        List<Cell> candidates = new ArrayList<>();
        queue.add(start);
        dist.put(start, 0);
        int expanded = 0;
        while (!queue.isEmpty() && expanded < maxNodes) {
            Cell cur = queue.poll();
            int d = dist.get(cur);
            expanded++;
            if (d >= minDist && d <= maxDist) {
                candidates.add(cur);
            }
            if (d >= maxDist) {
                continue;
            }
            for (Direction t : tangents(cur.face())) {
                Cell nb = step(cur, t);
                if (nb != null && !dist.containsKey(nb)) {
                    dist.put(nb, d + 1);
                    queue.add(nb);
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        int checks = 0;
        for (int pri = 2; pri >= 0; pri--) {
            List<Cell> tier = new ArrayList<>();
            for (Cell c : candidates) {
                if (facePriority(c.face()) == pri) {
                    tier.add(c);
                }
            }
            while (!tier.isEmpty() && checks < 40) {
                Cell c = tier.remove(random.nextInt(tier.size()));
                checks++;
                if (valid.test(c)) {
                    return c;
                }
            }
        }
        return null;
    }

    public boolean fullyBuried(Cell cell) {
        return buriedAlong(cell.block(), cell, cell);
    }

    public boolean tunnelBuried(Cell start, Cell end) {
        Vec3 from = Vec3.atCenterOf(start.block());
        Vec3 to = Vec3.atCenterOf(end.block());
        int steps = Math.max(1, (int) Math.ceil(from.distanceTo(to) / 0.1));
        BlockPos last = null;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            BlockPos p = BlockPos.containing(Mth.lerp(t, from.x, to.x), Mth.lerp(t, from.y, to.y), Mth.lerp(t, from.z, to.z));
            if (p.equals(last)) {
                continue;
            }
            last = p;
            if (!buriedAlong(p, start, end)) {
                return false;
            }
        }
        return true;
    }

    private boolean buriedAlong(BlockPos p, Cell start, Cell end) {
        if (!solid(p)) {
            return false;
        }
        for (Direction d : Direction.values()) {
            if (p.equals(start.block()) && d == start.face()) {
                continue;
            }
            if (p.equals(end.block()) && d == end.face()) {
                continue;
            }
            if (!solid(p.relative(d))) {
                return false;
            }
        }
        return true;
    }

    private static int facePriority(Direction face) {
        if (face == Direction.DOWN) {
            return 2;
        }
        return face.getAxis().isHorizontal() ? 1 : 0;
    }

    public Cell cellNear(BlockPos origin) {
        for (int r = 0; r <= 4; r++) {
            Cell best = null;
            double bestD = Double.MAX_VALUE;
            for (BlockPos p : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
                for (Direction f : Direction.values()) {
                    BlockPos b = p.relative(f.getOpposite());
                    Cell cell = new Cell(b.immutable(), f);
                    if (isCell(cell)) {
                        double d = p.distSqr(origin);
                        if (d < bestD) {
                            bestD = d;
                            best = cell;
                        }
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    private int cellCost(Cell cell) {
        BlockPos air = cell.block().relative(cell.face());
        int cost = 1;
        cost += this.level.getMaxLocalRawBrightness(air);
        if (this.level.canSeeSky(air)) {
            cost += 24;
        }
        int solids = 0;
        for (Direction d : Direction.values()) {
            if (solid(air.relative(d))) {
                solids++;
            }
        }
        cost += (6 - solids) * 3;
        return cost;
    }

    private int heuristic(Cell a, Cell b) {
        return a.block().distManhattan(b.block());
    }

    private List<Cell> reconstruct(Map<Cell, Cell> cameFrom, Cell cur) {
        List<Cell> path = new ArrayList<>();
        path.add(cur);
        while (cameFrom.containsKey(cur)) {
            cur = cameFrom.get(cur);
            path.add(cur);
        }
        Collections.reverse(path);
        return path;
    }
}
