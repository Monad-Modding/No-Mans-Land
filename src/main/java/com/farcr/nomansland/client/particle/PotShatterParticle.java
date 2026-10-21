package com.farcr.nomansland.client.particle;

import com.farcr.nomansland.common.block.pots.PotShatterParticleOption;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PotShatterParticle extends TextureSheetParticle {

    private static final int FADE_GRACE = 200;
    private static final int GATHER_SPREAD = 6;
    private static final int GATHER_MINIMUM = 24;
    private static final int GATHER_MAXIMUM = 160;
    private static final double GATHER_DENSITY = 40.0;
    private static final double GATHER_ARC = 0.35;
    private static final double SURFACE_EPSILON = 1.0E-3;
    private static final int SURFACE_ATTEMPTS = 16;
    private static final Long2ObjectOpenHashMap<List<PotShatterParticle>> WAITING = new Long2ObjectOpenHashMap<>();

    private final int persistTicks;
    private int gatherDelay = -1;
    private int gatherAge;
    private int gatherTicks;
    @Nullable
    private Vec3 gatherFrom;
    @Nullable
    private Vec3 gatherTo;
    private final float uo;
    private final float vo;
    private final boolean persistent;
    private final boolean jumpy;
    private final BlockPos origin;

    PotShatterParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite, int persistTicks, BlockPos origin) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        setSprite(sprite);
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        this.quadSize /= 2.0F;
        this.lifetime = persistTicks > 0 ? persistTicks + FADE_GRACE : (int) (4.0 / (Math.random() * 0.9 + 0.1)) + 2;
        this.persistTicks = persistTicks;
        this.hasPhysics = true;
        this.friction = 0.98F;
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
        this.persistent = persistTicks > 0;
        this.jumpy = this.persistent && random.nextFloat() < 0.1F;
        this.origin = origin;
        if (this.persistent) {
            WAITING.computeIfAbsent(origin.asLong(), key -> new ArrayList<>()).add(this);
        }
    }

    public static void assemble(BlockPos origin, ResourceLocation model, int ticks, List<AABB> boxes) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || boxes.isEmpty()) return;
        List<PotShatterParticle> waiting = WAITING.remove(origin.asLong());
        List<PotShatterParticle> gathering = waiting == null ? new ArrayList<>() : waiting;
        RandomSource random = level.getRandom();
        double area = 0.0;
        for (AABB box : boxes) {
            area += surfaceArea(box);
        }
        int wanted = Mth.clamp((int) Math.ceil(area * GATHER_DENSITY), GATHER_MINIMUM, GATHER_MAXIMUM);
        int missing = wanted - gathering.size();
        for (int index = 0; index < missing; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = 0.6 + random.nextDouble() * 0.9;
            double x = origin.getX() + 0.5 + Math.cos(angle) * distance;
            double z = origin.getZ() + 0.5 + Math.sin(angle) * distance;
            Particle spawned = Minecraft.getInstance().particleEngine.createParticle(new PotShatterParticleOption(model), x, origin.getY() + 0.05, z, 0.0, 0.0, 0.0);
            if (spawned instanceof PotShatterParticle shard) {
                gathering.add(shard);
            }
        }
        for (PotShatterParticle shard : gathering) {
            if (!shard.isAlive()) continue;
            Vec3 point = surfacePoint(boxes, area, random);
            shard.gather(point.add(origin.getX(), origin.getY(), origin.getZ()), Math.max(1, ticks - GATHER_SPREAD), random.nextInt(GATHER_SPREAD + 1));
        }
    }

    private static double surfaceArea(AABB box) {
        double x = box.getXsize();
        double y = box.getYsize();
        double z = box.getZsize();
        return 2.0 * (x * y + y * z + x * z);
    }

    private static Vec3 surfacePoint(List<AABB> boxes, double area, RandomSource random) {
        Vec3 fallback = boxes.get(0).getCenter();
        for (int attempt = 0; attempt < SURFACE_ATTEMPTS; attempt++) {
            double pick = random.nextDouble() * area;
            for (AABB box : boxes) {
                double boxArea = surfaceArea(box);
                if (pick > boxArea) {
                    pick -= boxArea;
                    continue;
                }
                Direction face = faceAt(box, pick);
                Vec3 point = pointOn(box, face, random);
                fallback = point;
                if (!covered(boxes, box, point.add(face.getStepX() * SURFACE_EPSILON, face.getStepY() * SURFACE_EPSILON, face.getStepZ() * SURFACE_EPSILON))) return point;
                break;
            }
        }
        return fallback;
    }

    private static Direction faceAt(AABB box, double pick) {
        double x = box.getXsize();
        double y = box.getYsize();
        double z = box.getZsize();
        double[] areas = {x * z, x * z, x * y, x * y, y * z, y * z};
        Direction[] faces = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (int index = 0; index < areas.length; index++) {
            if (pick <= areas[index]) return faces[index];
            pick -= areas[index];
        }
        return Direction.UP;
    }

    private static Vec3 pointOn(AABB box, Direction face, RandomSource random) {
        double x = Mth.lerp(random.nextDouble(), box.minX, box.maxX);
        double y = Mth.lerp(random.nextDouble(), box.minY, box.maxY);
        double z = Mth.lerp(random.nextDouble(), box.minZ, box.maxZ);
        return switch (face) {
            case DOWN -> new Vec3(x, box.minY, z);
            case UP -> new Vec3(x, box.maxY, z);
            case NORTH -> new Vec3(x, y, box.minZ);
            case SOUTH -> new Vec3(x, y, box.maxZ);
            case WEST -> new Vec3(box.minX, y, z);
            case EAST -> new Vec3(box.maxX, y, z);
        };
    }

    private static boolean covered(List<AABB> boxes, AABB own, Vec3 point) {
        for (AABB box : boxes) {
            if (box != own && box.contains(point)) return true;
        }
        return false;
    }

    private void gather(Vec3 target, int ticks, int delay) {
        this.gatherFrom = new Vec3(this.x, this.y, this.z);
        this.gatherTo = target;
        this.gatherTicks = ticks;
        this.gatherDelay = delay;
        this.gatherAge = 0;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.lifetime = Integer.MAX_VALUE;
    }

    @Override
    public void remove() {
        super.remove();
        if (this.persistent) {
            List<PotShatterParticle> waiting = WAITING.get(this.origin.asLong());
            if (waiting != null && waiting.remove(this) && waiting.isEmpty()) {
                WAITING.remove(this.origin.asLong());
            }
        }
    }

    @Override
    public void move(double x, double y, double z) {
        if (jumpy) {
            double d1 = y;
            if (this.hasPhysics && (x != 0 || y != 0 || z != 0)) {
                Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());
                x = vec3.x;
                y = vec3.y;
                z = vec3.z;
            }
            if (x != 0 || y != 0 || z != 0) {
                this.setBoundingBox(this.getBoundingBox().move(x, y, z));
                this.setLocationFromBoundingbox();
            }
            this.onGround = d1 != y && d1 < 0;
        } else {
            super.move(x, y, z);
        }
    }

    @Override
    public void tick() {
        if (this.gatherTo != null && this.gatherFrom != null) {
            this.tickGather();
            return;
        }
        super.tick();
        if (jumpy && onGround && random.nextFloat() < 0.01F) {
            this.yd = 0.12 + random.nextDouble() * 0.08;
            this.xd += (random.nextDouble() - 0.5) * 0.03;
            this.zd += (random.nextDouble() - 0.5) * 0.03;
        }
        if (this.persistent && !this.level.getBlockState(this.origin).isAir() && this.age < this.persistTicks) {
            this.age = this.persistTicks;
        }
        if (this.persistent && this.age > this.persistTicks) {
            float progress = (float) (this.age - this.persistTicks) / FADE_GRACE;
            if (this.random.nextFloat() < progress * 0.1F) {
                this.remove();
            }
        }
    }

    private void tickGather() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.gatherDelay > 0) {
            this.gatherDelay--;
            return;
        }
        this.gatherAge++;
        float progress = Mth.clamp((float) this.gatherAge / this.gatherTicks, 0.0F, 1.0F);
        float eased = progress * progress * (3.0F - 2.0F * progress);
        Vec3 from = this.gatherFrom;
        Vec3 to = this.gatherTo;
        double arc = Math.sin(progress * Math.PI) * GATHER_ARC;
        this.setPos(Mth.lerp(eased, from.x, to.x), Mth.lerp(eased, from.y, to.y) + arc, Mth.lerp(eased, from.z, to.z));
        if (progress >= 1.0F) {
            this.remove();
        }
    }

    @Override
    protected float getU0() {
        return sprite.getU((uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return sprite.getU(uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return sprite.getV(vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return sprite.getV((vo + 1.0F) / 4.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<PotShatterParticleOption> {
        @Override
        public Particle createParticle(PotShatterParticleOption option, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ModelManager manager = Minecraft.getInstance().getModelManager();
            ResourceLocation loc = option.model();
            ResourceLocation resolved = loc.getPath().startsWith("entity/") ? loc : loc.withPrefix("block/");
            ModelResourceLocation mrl = ModelResourceLocation.standalone(resolved);
            BakedModel model = manager.getModel(mrl);
            TextureAtlasSprite sprite = model.getParticleIcon(ModelData.EMPTY);
            return new PotShatterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite, option.persistTicks(), option.origin());
        }
    }
}
