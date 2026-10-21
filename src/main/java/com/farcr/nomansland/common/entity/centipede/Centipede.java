package com.farcr.nomansland.common.entity.centipede;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Centipede extends Monster {
    public static final int MAX_SEGMENTS = 25;
    public static final int MAX_PIECES = MAX_SEGMENTS + 1;
    public static final int HEALTH_PER_SEGMENT = 2;
    public static final int SHRIVEL_DURATION = 40;
    public static final int SHRIVEL_COOLDOWN = 200;
    public static final double SEGMENT_SPACING = 0.375;

    private static final double CRUMB_MIN = 0.12;
    private static final double CRUMB_MAX = 0.5;
    private static final int TRACK_CAP = 160;
    private static final float NORMAL_EMA = 0.25F;
    private static final double LUNGE_SPEED = 0.55;
    private static final double LUNGE_MIN = 4.0;
    private static final double LUNGE_MAX = 6.0;
    public static final float GAIT_FREQ = 3.0F;
    public static final float WAVE_LAMBDA = 0.6F;

    private static final EntityDataAccessor<Integer> DATA_SEGMENTS = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_VENOMOUS = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STUNNING = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BURROWED = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHRIVELING = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_SURFACE = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BYTE);

    private final double[] pieceX = new double[MAX_PIECES];
    private final double[] pieceY = new double[MAX_PIECES];
    private final double[] pieceZ = new double[MAX_PIECES];
    private final double[] pieceXO = new double[MAX_PIECES];
    private final double[] pieceYO = new double[MAX_PIECES];
    private final double[] pieceZO = new double[MAX_PIECES];
    private final float[] pieceNX = new float[MAX_PIECES];
    private final float[] pieceNY = new float[MAX_PIECES];
    private final float[] pieceNZ = new float[MAX_PIECES];
    private final float[] pieceNXO = new float[MAX_PIECES];
    private final float[] pieceNYO = new float[MAX_PIECES];
    private final float[] pieceNZO = new float[MAX_PIECES];
    private final boolean[] pieceAir = new boolean[MAX_PIECES];

    private final double[] trackX = new double[TRACK_CAP];
    private final double[] trackY = new double[TRACK_CAP];
    private final double[] trackZ = new double[TRACK_CAP];
    private final double[] trackS = new double[TRACK_CAP];
    private final float[] trackNX = new float[TRACK_CAP];
    private final float[] trackNY = new float[TRACK_CAP];
    private final float[] trackNZ = new float[TRACK_CAP];
    private final boolean[] trackAir = new boolean[TRACK_CAP];
    private int trackHead = -1;
    private int trackCount = 0;
    private double headS;

    private float headNX = 0.0F;
    private float headNY = 1.0F;
    private float headNZ = 0.0F;
    private float headNXO = 0.0F;
    private float headNYO = 1.0F;
    private float headNZO = 0.0F;

    private double lastHeadX;
    private double lastHeadY;
    private double lastHeadZ;
    private double headGaitPhase;
    private double headGaitPhaseO;
    private float headSpeed;
    private boolean segmentsInitialized;

    private float shrivelAmount;

    private int lungeTicks;
    private int lungeDuration;
    private double lungeDirX;
    private double lungeDirZ;
    private double lungeVelY;
    private Vec3 surfaceNormal = new Vec3(0.0, 1.0, 0.0);
    private boolean attached;
    private BlockPos cellB;
    private Direction cellF;
    private Direction tangentT;
    private Vec3 headDir = new Vec3(0.0, 0.0, 1.0);
    private Vec3 moveTarget;
    private int moveTargetAge;
    private int floatTicks;
    private int stuckTicks;
    private int wanderCooldown;
    private float awareness;
    private float nerve;
    private float prevHealthSense;
    private Vec3 lastKnownPos;
    private int freezeTicks;
    private boolean paused;
    private int rhythmTicks;
    private Mood mood = Mood.CALM;
    private Mood prevMood = Mood.CALM;
    private List<SurfacePathfinder.Cell> surfacePath;
    private int surfacePathIndex;
    private int repathCooldown;
    private BlockPos pathGoal;

    private int shrivelTicks;
    private int shrivelCooldown;
    private int burrowCooldown;

    private final CentipedePart[] parts;

    private enum Mood { CALM, STALK, SEARCH, FLEE }

    public Centipede(EntityType<? extends Centipede> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.moveControl = new CentipedeMoveControl(this);
        CentipedePart[] built = new CentipedePart[MAX_PIECES];
        for (int i = 0; i < built.length; i++) {
            built[i] = new CentipedePart(this, 0.7F, 0.55F);
        }
        this.parts = built;
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.parts != null) {
            for (int i = 0; i < this.parts.length; i++) {
                this.parts[i].setId(id + i + 1);
            }
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    public boolean hurtPart(CentipedePart part, DamageSource source, float amount) {
        return this.hurt(source, amount);
    }

    @Override
    protected void doPush(Entity entity) {
        if (entity instanceof CentipedePart part && part.parent == this) {
            return;
        }
        super.doPush(entity);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return (int) (super.calculateFallDamage(fallDistance, damageMultiplier) * 0.5F);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.CRAMMING) || super.isInvulnerableTo(source);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(SEGMENT_SPACING * MAX_PIECES + 2.0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEGMENTS, 5);
        builder.define(DATA_VENOMOUS, false);
        builder.define(DATA_STUNNING, false);
        builder.define(DATA_BURROWED, false);
        builder.define(DATA_SHRIVELING, false);
        builder.define(DATA_SURFACE, (byte) 6);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new CentipedeShrivelGoal(this));
        this.goalSelector.addGoal(1, new CentipedeBurrowGoal(this));
        this.goalSelector.addGoal(3, new CentipedeAttackGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, false, Centipede::isPrey).setUnseenMemoryTicks(200));
    }

    public static boolean isPrey(LivingEntity entity) {
        return entity instanceof Animal && !(entity instanceof Centipede);
    }

    public static boolean canTargetPlayer(LivingEntity entity) {
        return entity instanceof Player player && !player.isCreative() && !player.isSpectator();
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    public int getSegments() {
        return this.entityData.get(DATA_SEGMENTS);
    }

    public int getPieceCount() {
        return getSegments() + 1;
    }

    public void setSegments(int segments) {
        int clamped = Mth.clamp(segments, 3, MAX_SEGMENTS);
        this.entityData.set(DATA_SEGMENTS, clamped);
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(clamped * HEALTH_PER_SEGMENT);
        }
    }

    public boolean isVenomous() {
        return this.entityData.get(DATA_VENOMOUS);
    }

    public void setVenomous(boolean venomous) {
        this.entityData.set(DATA_VENOMOUS, venomous);
    }

    public boolean isStunning() {
        return this.entityData.get(DATA_STUNNING);
    }

    public void setStunning(boolean stunning) {
        this.entityData.set(DATA_STUNNING, stunning);
    }

    public boolean isBurrowed() {
        return this.entityData.get(DATA_BURROWED);
    }

    public void setBurrowed(boolean burrowed) {
        this.entityData.set(DATA_BURROWED, burrowed);
    }

    public boolean isShriveling() {
        return this.entityData.get(DATA_SHRIVELING);
    }

    public void setShriveling(boolean shriveling) {
        this.entityData.set(DATA_SHRIVELING, shriveling);
    }

    public boolean canShrivel() {
        return this.shrivelCooldown <= 0 && !this.isShriveling();
    }

    public double getPieceRenderX(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceXO[i], this.pieceX[i]);
    }

    public double getPieceRenderY(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceYO[i], this.pieceY[i]);
    }

    public double getPieceRenderZ(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceZO[i], this.pieceZ[i]);
    }

    public float getPieceRenderNX(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceNXO[i], this.pieceNX[i]);
    }

    public float getPieceRenderNY(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceNYO[i], this.pieceNY[i]);
    }

    public float getPieceRenderNZ(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceNZO[i], this.pieceNZ[i]);
    }

    public float getHeadRenderNX(float partialTick) {
        return Mth.lerp(partialTick, this.headNXO, this.headNX);
    }

    public float getHeadRenderNY(float partialTick) {
        return Mth.lerp(partialTick, this.headNYO, this.headNY);
    }

    public float getHeadRenderNZ(float partialTick) {
        return Mth.lerp(partialTick, this.headNZO, this.headNZ);
    }

    public float getHeadGaitPhase(float partialTick) {
        return (float) Mth.lerp(partialTick, this.headGaitPhaseO, this.headGaitPhase);
    }

    public float getHeadSpeed() {
        return this.headSpeed;
    }

    public boolean requestLunge(double dx, double dy, double dz) {
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d < LUNGE_MIN || d > LUNGE_MAX) return false;
        int n = Mth.clamp((int) Math.ceil(d / LUNGE_SPEED), 6, 14);
        double speed = d / n;
        this.lungeDirX = dx / d * speed;
        this.lungeDirZ = dz / d * speed;
        this.lungeVelY = Mth.clamp(dy / n + 0.08 * (n - 1) * 0.5, 0.05, 0.8);
        this.lungeDuration = n;
        this.lungeTicks = n;
        this.attached = false;
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();
        float r = random.nextFloat();
        boolean venomous = r < 0.15F;
        boolean stunning = !venomous && r < 0.30F;
        setVenomous(venomous);
        setStunning(stunning);
        int segments;
        if (venomous || stunning) segments = 3 + random.nextInt(5);
        else segments = 6 + random.nextInt(8);
        setSegments(segments);
        setHealth(getMaxHealth());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Segments", getSegments());
        compound.putBoolean("Venomous", isVenomous());
        compound.putBoolean("Stunning", isStunning());
        compound.putInt("ShrivelCooldown", this.shrivelCooldown);
        compound.putInt("BurrowCooldown", this.burrowCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Segments")) {
            setSegments(compound.getInt("Segments"));
        }
        setVenomous(compound.getBoolean("Venomous"));
        setStunning(compound.getBoolean("Stunning"));
        this.shrivelCooldown = compound.getInt("ShrivelCooldown");
        this.burrowCooldown = compound.getInt("BurrowCooldown");
    }

    @Override
    public void travel(Vec3 input) {
        if (this.level().isClientSide) {
            return;
        }
        surfaceTravel();
    }

    private void surfaceTravel() {
        if (isBurrowed()) {
            return;
        }
        if (isShriveling()) {
            shrivelFall();
        } else if (this.lungeTicks > 0) {
            lungeStep();
        } else {
            crawl();
        }
    }

    private void shrivelFall() {
        this.attached = false;
        setSurfaceData();
        this.setOnGround(false);
        Vec3 dm = getDeltaMovement();
        Vec3 fall = new Vec3(dm.x * 0.9, dm.y - 0.08, dm.z * 0.9);
        this.move(MoverType.SELF, fall);
        setDeltaMovement(this.horizontalCollision ? 0.0 : fall.x, this.verticalCollision ? 0.0 : fall.y, this.horizontalCollision ? 0.0 : fall.z);
        this.resetFallDistance();
    }

    private void lungeStep() {
        this.lungeTicks--;
        Vec3 motion = new Vec3(this.lungeDirX, this.lungeVelY, this.lungeDirZ);
        this.lungeVelY -= 0.08;
        setSurfaceData();
        Vec3 before = position();
        this.move(MoverType.SELF, motion);
        setDeltaMovement(Vec3.ZERO);
        this.resetFallDistance();
        if (this.lungeTicks < this.lungeDuration - 1 && position().distanceToSqr(before) < 0.0025) {
            this.lungeTicks = 0;
        }
    }

    private void crawl() {
        Vec3 pos = position();
        double hh = getBbHeight() * 0.5;
        Vec3 center = pos.add(0.0, hh, 0.0);
        double step = currentSpeed();

        if (!this.attached || this.cellF == null) {
            fallStep();
            return;
        }
        if (!touching(center, this.cellF)) {
            boolean cellValid = solidBlock(this.cellB) && !solidBlock(this.cellB.relative(this.cellF))
                    && center.distanceToSqr(anchorCenter(this.cellB, this.cellF)) < 1.0;
            if (cellValid && this.floatTicks < 12) {
                this.floatTicks++;
            } else {
                Direction f2 = bestTouchingCell(center);
                if (f2 == null) {
                    this.attached = false;
                    this.floatTicks = 0;
                    setSurfaceData();
                    fallStep();
                    return;
                }
                this.cellF = f2;
                this.cellB = BlockPos.containing(center.subtract(dvec(f2).scale(faceExtent(f2) + 0.3)));
                this.tangentT = projectIntentAxis(this.headDir, this.cellF, this.tangentT);
                this.floatTicks = 0;
            }
        } else {
            this.floatTicks = 0;
        }

        this.setOnGround(true);
        this.resetFallDistance();
        this.setSpeed((float) step);

        BlockPos support = BlockPos.containing(center.subtract(dvec(this.cellF).scale(faceExtent(this.cellF) + 0.3)));
        if (solidBlock(support)) {
            this.cellB = support;
        }

        Vec3 want = computeMoveDir(pos);
        boolean moving = want.lengthSqr() > 1.0E-8;

        Vec3 n0 = dvec(this.cellF);
        Vec3 desired = want.subtract(n0.scale(want.dot(n0)));
        if (moving) {
            Vec3 hd = this.headDir.subtract(n0.scale(this.headDir.dot(n0)));
            hd = hd.lengthSqr() > 1.0E-8 ? hd.normalize()
                    : (desired.lengthSqr() > 1.0E-8 ? desired.normalize() : this.headDir);
            if (desired.lengthSqr() > 1.0E-8) {
                this.headDir = turnToward(hd, desired.normalize(), n0, step / (1.0 + 0.11 * getSegments()));
            } else {
                this.headDir = hd;
            }
        }
        this.tangentT = projectIntentAxis(this.headDir, this.cellF, this.tangentT);

        Direction t = this.tangentT;
        BlockPos b = this.cellB;
        BlockPos air = b.relative(this.cellF);
        double along = center.subtract(Vec3.atCenterOf(b)).dot(dvec(t));
        boolean wallAhead = solidBlock(air.relative(t));
        Vec3 oldN = dvec(this.cellF);
        double ext = faceExtent(this.cellF);
        Vec3 dest = center.add(this.headDir.scale(step));
        boolean destSupported = solidBlock(BlockPos.containing(dest.subtract(oldN.scale(ext + 0.3))));
        if (moving && wallAhead && along > 0.2) {
            this.cellB = air.relative(t);
            this.cellF = t.getOpposite();
            this.headDir = oldN;
        } else if (moving && !destSupported) {
            this.cellF = t;
            this.headDir = oldN.scale(-1.0);
        }

        Vec3 n = dvec(this.cellF);
        Vec3 hp = this.headDir.subtract(n.scale(this.headDir.dot(n)));
        this.headDir = hp.lengthSqr() > 1.0E-8 ? hp.normalize() : this.headDir;
        Vec3 tang = moving ? this.headDir.scale(step) : Vec3.ZERO;
        if (tang.lengthSqr() > 1.0E-8) {
            faceSurface(tang);
        }
        double normOff = center.subtract(anchorCenter(this.cellB, this.cellF)).dot(n);
        Vec3 seat = n.scale(-normOff);
        Vec3 c = center.subtract(Vec3.atCenterOf(this.cellB));
        double px = this.cellF.getAxis() != Direction.Axis.X && Math.abs(c.x) > 0.5 ? -(c.x - Math.signum(c.x) * 0.45) : 0.0;
        double py = this.cellF.getAxis() != Direction.Axis.Y && Math.abs(c.y) > 0.5 ? -(c.y - Math.signum(c.y) * 0.45) : 0.0;
        double pz = this.cellF.getAxis() != Direction.Axis.Z && Math.abs(c.z) > 0.5 ? -(c.z - Math.signum(c.z) * 0.45) : 0.0;
        Vec3 tanPull = new Vec3(px, py, pz);
        this.surfaceNormal = lerpNormal(this.surfaceNormal, n);
        setSurfaceData();
        this.move(MoverType.SELF, tang.add(seat).add(tanPull));
        setDeltaMovement(Vec3.ZERO);
        this.setOnGround(true);
        this.resetFallDistance();

        if (moving && position().distanceToSqr(pos) < (0.06 * step) * (0.06 * step)) {
            if (++this.stuckTicks > 8) {
                this.stuckTicks = 0;
                this.attached = false;
                this.surfacePath = null;
                setSurfaceData();
                setDeltaMovement(this.headDir.x * 0.22 + (this.random.nextDouble() - 0.5) * 0.12, 0.32,
                        this.headDir.z * 0.22 + (this.random.nextDouble() - 0.5) * 0.12);
            }
        } else {
            this.stuckTicks = 0;
        }
    }

    private double faceExtent(Direction f) {
        return f.getAxis().isVertical() ? getBbHeight() * 0.5 : getBbWidth() * 0.5;
    }

    private boolean touching(Vec3 center, Direction f) {
        Vec3 probe = center.subtract(dvec(f).scale(faceExtent(f) + 0.3));
        return solidBlock(BlockPos.containing(probe));
    }

    private Direction bestTouchingCell(Vec3 center) {
        Direction best = null;
        double bs = -1.0E9;
        for (Direction f : Direction.values()) {
            if (!touching(center, f)) {
                continue;
            }
            BlockPos b = BlockPos.containing(center.subtract(dvec(f).scale(faceExtent(f) + 0.3)));
            if (!solidBlock(b) || solidBlock(b.relative(f))) {
                continue;
            }
            double score = dvec(f).dot(this.surfaceNormal);
            if (score > bs) {
                bs = score;
                best = f;
            }
        }
        return best;
    }

    private void fallStep() {
        setSurfaceData();
        this.setOnGround(false);
        Vec3 dm = getDeltaMovement();
        Vec3 motion = new Vec3(dm.x * 0.92, dm.y - 0.08, dm.z * 0.92);
        this.move(MoverType.SELF, motion);
        Vec3 center = position().add(0.0, getBbHeight() * 0.5, 0.0);
        Direction f = this.verticalCollision || this.horizontalCollision ? bestTouchingCell(center) : null;
        if (f != null) {
            this.cellF = f;
            this.cellB = BlockPos.containing(center.subtract(dvec(f).scale(faceExtent(f) + 0.3)));
            this.tangentT = projectIntentAxis(this.headDir, this.cellF, this.tangentT);
            this.attached = true;
            setSurfaceData();
            setDeltaMovement(Vec3.ZERO);
            this.setOnGround(true);
            this.resetFallDistance();
        } else {
            double vy = this.verticalCollision ? 0.0 : motion.y;
            double vx = this.horizontalCollision ? 0.0 : motion.x;
            double vz = this.horizontalCollision ? 0.0 : motion.z;
            setDeltaMovement(vx, vy, vz);
        }
    }

    private void setSurfaceData() {
        byte v = this.attached && this.cellF != null ? (byte) this.cellF.get3DDataValue() : (byte) 6;
        if (this.entityData.get(DATA_SURFACE) != v) {
            this.entityData.set(DATA_SURFACE, v);
        }
    }

    private static Vec3 dvec(Direction d) {
        return new Vec3(d.getStepX(), d.getStepY(), d.getStepZ());
    }

    private boolean solidBlock(BlockPos b) {
        return !this.level().getBlockState(b).getCollisionShape(this.level(), b).isEmpty();
    }

    private Vec3 anchorCenter(BlockPos b, Direction f) {
        double clr = (f.getAxis().isVertical() ? getBbHeight() * 0.5 : getBbWidth() * 0.5) + 0.02;
        return Vec3.atCenterOf(b).add(dvec(f).scale(0.5 + clr));
    }

    private Direction firstPerp(Direction f) {
        for (Direction d : Direction.values()) {
            if (d.getAxis() != f.getAxis()) {
                return d;
            }
        }
        return Direction.NORTH;
    }

    private Direction projectIntentAxis(Vec3 want, Direction f, Direction prevT) {
        Direction fallback = prevT != null && prevT.getAxis() != f.getAxis() ? prevT : firstPerp(f);
        if (want.lengthSqr() < 1.0E-8) {
            return fallback;
        }
        Vec3 p = want.subtract(dvec(f).scale(want.dot(dvec(f))));
        if (p.lengthSqr() < 1.0E-8) {
            return fallback;
        }
        Direction best = fallback;
        double bs = -1.0E9;
        for (Direction d : Direction.values()) {
            if (d.getAxis() == f.getAxis()) {
                continue;
            }
            double sc = p.dot(dvec(d));
            if (sc > bs) {
                bs = sc;
                best = d;
            }
        }
        return best;
    }

    private Vec3 turnToward(Vec3 cur, Vec3 des, Vec3 axis, double maxTurn) {
        double dot = Mth.clamp(cur.dot(des), -1.0, 1.0);
        double angle = Math.acos(dot);
        if (angle <= maxTurn || angle < 1.0E-4) {
            return des;
        }
        double sign = Math.signum(cur.cross(des).dot(axis));
        if (sign == 0.0) {
            sign = 1.0;
        }
        return rotateAround(cur, axis, sign * maxTurn).normalize();
    }

    private Vec3 rotateAround(Vec3 v, Vec3 axis, double angle) {
        Vec3 a = axis.normalize();
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return v.scale(c).add(a.cross(v).scale(s)).add(a.scale(a.dot(v) * (1.0 - c)));
    }

    private Vec3 computeMoveDir(Vec3 pos) {
        if (needsSettling()) {
            return settleMove(pos);
        }
        if (this.freezeTicks > 0) {
            return Vec3.ZERO;
        }
        if (this.mood == Mood.FLEE) {
            return fleeDir(pos);
        }
        if (this.paused) {
            return Vec3.ZERO;
        }
        return switch (this.mood) {
            case STALK -> stalkDir(pos);
            case SEARCH -> searchDir(pos);
            default -> wanderDir(pos);
        };
    }

    private Vec3 stalkDir(Vec3 pos) {
        LivingEntity t = getTarget();
        if (t == null) {
            return Vec3.ZERO;
        }
        double reach = getBbWidth() * 2.0 * getBbWidth() * 2.0 + t.getBbWidth();
        if (distanceToSqr(t) <= reach) {
            return Vec3.ZERO;
        }
        return steer(pos, t.blockPosition(), new Vec3(t.getX(), t.getY() + t.getBbHeight() * 0.5, t.getZ()));
    }

    private Vec3 searchDir(Vec3 pos) {
        if (this.lastKnownPos == null) {
            return Vec3.ZERO;
        }
        if (pos.distanceToSqr(this.lastKnownPos) < 2.25) {
            this.lastKnownPos = null;
            this.awareness = 0.0F;
            return Vec3.ZERO;
        }
        return steer(pos, BlockPos.containing(this.lastKnownPos), this.lastKnownPos);
    }

    private Vec3 fleeDir(Vec3 pos) {
        LivingEntity t = getTarget();
        Vec3 away = t != null ? pos.subtract(t.position()) : this.headDir;
        if (away.lengthSqr() < 1.0E-6) {
            away = this.headDir;
        }
        Vec3 goal = pos.add(away.normalize().scale(10.0));
        return steer(pos, BlockPos.containing(goal), goal);
    }

    private Vec3 wanderDir(Vec3 pos) {
        if (this.moveTarget == null) {
            return Vec3.ZERO;
        }
        double dx = this.moveTarget.x - pos.x;
        double dz = this.moveTarget.z - pos.z;
        if (dx * dx + dz * dz < 1.5 * 1.5) {
            this.moveTarget = null;
            this.wanderCooldown = 40 + this.random.nextInt(80);
            return Vec3.ZERO;
        }
        return steer(pos, BlockPos.containing(this.moveTarget), this.moveTarget);
    }

    private Vec3 settleMove(Vec3 pos) {
        Vec3 centroid = problemCentroid();
        if (centroid == null) {
            return this.headDir;
        }
        Vec3 dir = pos.subtract(centroid);
        return dir.lengthSqr() > 1.0E-6 ? dir : this.headDir;
    }

    public boolean needsSettling() {
        return problemCentroid() != null;
    }

    @Nullable
    private Vec3 problemCentroid() {
        int count = getPieceCount();
        boolean[] problem = new boolean[count];
        for (int i = 1; i < count; i++) {
            if (pieceUnsettled(i)) {
                problem[i] = true;
            }
        }
        boolean[] folded = new boolean[count];
        double minSq = sq(SEGMENT_SPACING * 0.55);
        for (int i = 0; i < count; i++) {
            for (int j = i + 2; j < count; j++) {
                if (sq(this.pieceX[i] - this.pieceX[j]) + sq(this.pieceY[i] - this.pieceY[j]) + sq(this.pieceZ[i] - this.pieceZ[j]) < minSq) {
                    folded[i] = true;
                    folded[j] = true;
                }
            }
        }
        int colliding = 0;
        for (int i = 0; i < count; i++) {
            if (folded[i]) {
                colliding++;
            }
        }
        double ax = 0.0;
        double ay = 0.0;
        double az = 0.0;
        int n = 0;
        for (int i = 0; i < count; i++) {
            if (problem[i] || (colliding >= 3 && folded[i])) {
                ax += this.pieceX[i];
                ay += this.pieceY[i];
                az += this.pieceZ[i];
                n++;
            }
        }
        return n == 0 ? null : new Vec3(ax / n, ay / n, az / n);
    }

    private boolean pieceUnsettled(int i) {
        if (this.pieceAir[i]) {
            return true;
        }
        double px = this.pieceX[i];
        double py = this.pieceY[i];
        double pz = this.pieceZ[i];
        if (isSolidAt(px, py, pz)) {
            return true;
        }
        return !isSolidAt(px - this.pieceNX[i] * 0.5, py - this.pieceNY[i] * 0.5, pz - this.pieceNZ[i] * 0.5);
    }

    private Vec3 steer(Vec3 pos, BlockPos goalBlock, Vec3 goalPos) {
        if (this.cellF == null) {
            return goalPos.subtract(pos);
        }
        boolean goalMoved = this.pathGoal == null || goalBlock.distManhattan(this.pathGoal) > 3;
        if (this.surfacePath == null || this.repathCooldown <= 0 || goalMoved) {
            SurfacePathfinder pf = new SurfacePathfinder(this.level());
            SurfacePathfinder.Cell goalCell = pf.cellNear(goalBlock);
            this.surfacePath = goalCell == null ? null : pf.find(new SurfacePathfinder.Cell(this.cellB, this.cellF), goalCell, 700);
            this.surfacePathIndex = 0;
            this.pathGoal = goalBlock;
            this.repathCooldown = 20;
        } else {
            this.repathCooldown--;
        }
        if (this.surfacePath == null) {
            return goalPos.subtract(pos);
        }
        while (this.surfacePathIndex < this.surfacePath.size()) {
            SurfacePathfinder.Cell c = this.surfacePath.get(this.surfacePathIndex);
            Vec3 wp = anchorCenter(c.block(), c.face());
            double dw = pos.distanceToSqr(wp);
            if (dw < 0.55) {
                this.surfacePathIndex++;
                continue;
            }
            if (dw < 4.0 && this.surfacePathIndex + 1 < this.surfacePath.size()) {
                SurfacePathfinder.Cell next = this.surfacePath.get(this.surfacePathIndex + 1);
                if (pos.distanceToSqr(anchorCenter(next.block(), next.face())) < dw) {
                    this.surfacePathIndex++;
                    continue;
                }
            }
            return wp.subtract(pos);
        }
        return goalPos.subtract(pos);
    }

    public SurfacePathfinder.Cell currentCell() {
        return this.cellF == null ? null : new SurfacePathfinder.Cell(this.cellB, this.cellF);
    }

    public Vec3 surfaceAnchor(SurfacePathfinder.Cell cell) {
        return anchorCenter(cell.block(), cell.face());
    }

    public void attachToCell(SurfacePathfinder.Cell cell) {
        this.cellB = cell.block();
        this.cellF = cell.face();
        this.tangentT = projectIntentAxis(this.headDir, this.cellF, this.tangentT);
        this.attached = true;
        setSurfaceData();
    }

    private double currentSpeed() {
        return this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.85;
    }

    private Vec3 lerpNormal(Vec3 a, Vec3 b) {
        Vec3 r = new Vec3(Mth.lerp(0.35, a.x, b.x), Mth.lerp(0.35, a.y, b.y), Mth.lerp(0.35, a.z, b.z));
        double len = r.length();
        return len < 1.0E-6 ? b : r.scale(1.0 / len);
    }

    private void faceSurface(Vec3 tangent) {
        if (tangent.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 f = tangent.normalize();
        float yaw = (float) (Mth.atan2(-f.x, f.z) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isBurrowed() || isShriveling()) {
            return;
        }
        updateSenses();

        if (this.mood == Mood.CALM) {
            if (this.moveTarget == null) {
                this.moveTargetAge = 0;
                if (this.wanderCooldown > 0) {
                    this.wanderCooldown--;
                } else if (!this.paused && this.random.nextInt(50) == 0) {
                    double ang = this.random.nextDouble() * Math.PI * 2.0;
                    double dist = 4.0 + this.random.nextDouble() * 5.0;
                    this.moveTarget = position().add(Math.cos(ang) * dist, 0.0, Math.sin(ang) * dist);
                }
            } else if (++this.moveTargetAge > 120) {
                this.moveTarget = null;
                this.wanderCooldown = 30 + this.random.nextInt(40);
            }
        } else {
            this.moveTarget = null;
        }
    }

    private void updateSenses() {
        boolean hurt = getHealth() < this.prevHealthSense - 0.001F;
        this.prevHealthSense = getHealth();
        if (this.freezeTicks > 0) {
            this.freezeTicks--;
        }

        float sizeBrave = Mth.clamp((getSegments() - 3) / 24.0F, 0.0F, 0.75F);
        if (hurt) {
            this.nerve = Math.min(1.0F, this.nerve + 0.35F);
            this.awareness = Math.max(this.awareness, 0.85F);
            this.freezeTicks = Math.max(this.freezeTicks, 5);
        }
        LivingEntity target = getTarget();
        if (target instanceof Player p) {
            if (holdsFrightening(p)) {
                this.nerve = Math.min(1.0F, this.nerve + 0.06F);
            }
            Vec3 fromPlayer = position().subtract(p.position());
            Vec3 known = p.getKnownMovement();
            if (fromPlayer.horizontalDistanceSqr() > 1.0E-4 && fromPlayer.horizontalDistanceSqr() < 49.0
                    && known.horizontalDistanceSqr() > 0.01) {
                double toward = new Vec3(known.x, 0.0, known.z).normalize()
                        .dot(new Vec3(fromPlayer.x, 0.0, fromPlayer.z).normalize());
                if (toward > 0.5) {
                    this.nerve = Math.min(1.0F, this.nerve + 0.03F);
                }
            }
        }
        this.nerve = Math.max(0.0F, this.nerve - 0.008F - sizeBrave * 0.01F);

        double range = getAttributeValue(Attributes.FOLLOW_RANGE);
        Player nearest = level().getNearestPlayer(this, range);
        if (nearest != null && canTargetPlayer(nearest)) {
            double prox = 1.0 - Math.min(1.0, distanceTo(nearest) / range);
            double loud = loudness(nearest);
            boolean contact = distanceToSqr(nearest) < 9.0;
            float gain = (float) (loud * prox * 0.7 + prox * 0.02) + (contact ? 0.5F : 0.0F);
            this.awareness = Mth.clamp(this.awareness + gain - 0.015F, 0.0F, 1.0F);
            this.lastKnownPos = nearest.position();
            if (this.awareness > 0.4F && getTarget() == null) {
                setTarget(nearest);
            }
            if (getTarget() == nearest) {
                this.awareness = Math.max(this.awareness, 0.55F);
            }
        } else {
            this.awareness = Math.max(0.0F, this.awareness - 0.008F);
            if (getTarget() instanceof Player && this.awareness < 0.05F) {
                setTarget(null);
                this.lastKnownPos = null;
            }
        }

        LivingEntity t = getTarget();
        if (t instanceof Player) {
            if (this.nerve > 0.55F && distanceToSqr(t) < 144.0) {
                this.mood = Mood.FLEE;
            } else if (this.awareness > 0.4F) {
                this.mood = Mood.STALK;
            } else if (this.lastKnownPos != null) {
                this.mood = Mood.SEARCH;
            } else {
                this.mood = Mood.CALM;
            }
        } else if (t != null) {
            this.mood = Mood.STALK;
        } else {
            this.mood = Mood.CALM;
        }

        if ((this.mood == Mood.STALK || this.mood == Mood.FLEE) && this.prevMood != this.mood) {
            this.freezeTicks = 0;
            this.paused = false;
            this.rhythmTicks = 6 + this.random.nextInt(10);
        }
        this.prevMood = this.mood;

        tickRhythm();
    }

    private double loudness(Player p) {
        double moved = p.getKnownMovement().horizontalDistance();
        if (p.isCrouching()) {
            return Math.min(0.1, moved * 3.0);
        }
        double base = Math.min(1.0, moved * 6.0);
        if (p.isSprinting()) {
            base = Math.min(1.0, base + 0.35);
        }
        return base;
    }

    private boolean holdsFrightening(Player p) {
        return p.getMainHandItem().is(NMLTags.FRIGHTENS_BUGS) || p.getOffhandItem().is(NMLTags.FRIGHTENS_BUGS);
    }

    public boolean frightenedByHeld(LivingEntity e) {
        return e instanceof Player p && holdsFrightening(p);
    }

    private boolean isSkittish() {
        return getSegments() < 8 || this.nerve > 0.5F;
    }

    public boolean isFleeing() {
        return this.mood == Mood.FLEE;
    }

    private void tickRhythm() {
        if (this.mood == Mood.FLEE) {
            this.paused = false;
            return;
        }
        if (this.rhythmTicks > 0) {
            this.rhythmTicks--;
            return;
        }
        this.paused = !this.paused;
        boolean aroused = this.mood == Mood.STALK || this.mood == Mood.SEARCH;
        boolean skittish = isSkittish();
        if (this.paused) {
            int base = aroused ? (skittish ? 3 : 7) : (skittish ? 15 : 45);
            int rand = aroused ? (skittish ? 8 : 14) : (skittish ? 55 : 220);
            this.rhythmTicks = base + this.random.nextInt(rand);
        } else {
            int base = skittish ? 8 : 18;
            int rand = skittish ? 16 : 46;
            this.rhythmTicks = base + this.random.nextInt(rand);
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateTrail();
        updateParts();

        if (this.shrivelCooldown > 0) {
            this.shrivelCooldown--;
        }
        if (this.burrowCooldown > 0) {
            this.burrowCooldown--;
        }
        if (this.shrivelTicks > 0) {
            this.shrivelTicks--;
            if (this.shrivelTicks == 0 && !this.level().isClientSide) {
                setShriveling(false);
            }
        }
    }

    private void appendCrumb(double x, double y, double z, float nx, float ny, float nz, boolean air, double s) {
        this.trackHead = (this.trackHead + 1) % TRACK_CAP;
        this.trackX[this.trackHead] = x;
        this.trackY[this.trackHead] = y;
        this.trackZ[this.trackHead] = z;
        this.trackS[this.trackHead] = s;
        this.trackNX[this.trackHead] = nx;
        this.trackNY[this.trackHead] = ny;
        this.trackNZ[this.trackHead] = nz;
        this.trackAir[this.trackHead] = air;
        if (this.trackCount < TRACK_CAP) {
            this.trackCount++;
        }
    }

    private void seedTrack() {
        double hx = getX();
        double hy = getY();
        double hz = getZ();
        Vec3 n = surfaceDataNormal();
        this.headNX = this.headNXO = (float) n.x;
        this.headNY = this.headNYO = (float) n.y;
        this.headNZ = this.headNZO = (float) n.z;
        float rad = this.yBodyRot * Mth.DEG_TO_RAD;
        double bx = Mth.sin(rad) * CRUMB_MIN;
        double bz = -Mth.cos(rad) * CRUMB_MIN;
        this.trackHead = -1;
        this.trackCount = 0;
        double px = hx - bx * (TRACK_CAP - 1);
        double pz = hz - bz * (TRACK_CAP - 1);
        double s = 0.0;
        for (int k = 0; k < TRACK_CAP; k++) {
            appendCrumb(px, hy, pz, this.headNX, this.headNY, this.headNZ, false, s);
            s += CRUMB_MIN;
            px += bx;
            pz += bz;
        }
        this.headS = this.trackS[this.trackHead];
        this.lastHeadX = hx;
        this.lastHeadY = hy;
        this.lastHeadZ = hz;
        this.headGaitPhase = 0.0;
        this.headGaitPhaseO = 0.0;
        this.headSpeed = 0.0F;

        double sbx = Mth.sin(rad) * SEGMENT_SPACING;
        double sbz = -Mth.cos(rad) * SEGMENT_SPACING;
        for (int i = 0; i < MAX_PIECES; i++) {
            this.pieceX[i] = this.pieceXO[i] = hx + sbx * (i + 1);
            this.pieceY[i] = this.pieceYO[i] = hy;
            this.pieceZ[i] = this.pieceZO[i] = hz + sbz * (i + 1);
            this.pieceNX[i] = this.pieceNXO[i] = this.headNX;
            this.pieceNY[i] = this.pieceNYO[i] = this.headNY;
            this.pieceNZ[i] = this.pieceNZO[i] = this.headNZ;
            this.pieceAir[i] = false;
        }
        this.segmentsInitialized = true;
    }

    private void updateTrail() {
        if (!this.segmentsInitialized) {
            seedTrack();
            return;
        }

        int count = getPieceCount();
        boolean burrowed = isBurrowed();
        double hx = getX();
        double hy = getY();
        double hz = getZ();

        for (int i = 0; i < MAX_PIECES; i++) {
            this.pieceXO[i] = this.pieceX[i];
            this.pieceYO[i] = this.pieceY[i];
            this.pieceZO[i] = this.pieceZ[i];
            this.pieceNXO[i] = this.pieceNX[i];
            this.pieceNYO[i] = this.pieceNY[i];
            this.pieceNZO[i] = this.pieceNZ[i];
        }
        this.headNXO = this.headNX;
        this.headNYO = this.headNY;
        this.headNZO = this.headNZ;
        this.headGaitPhaseO = this.headGaitPhase;

        double disp = Math.sqrt(sq(hx - this.lastHeadX) + sq(hy - this.lastHeadY) + sq(hz - this.lastHeadZ));
        this.headGaitPhase += disp;
        this.headSpeed = Mth.lerp(0.3F, this.headSpeed, (float) disp);
        this.lastHeadX = hx;
        this.lastHeadY = hy;
        this.lastHeadZ = hz;

        Vec3 raw = surfaceDataNormal();
        this.headNX = Mth.lerp(NORMAL_EMA, this.headNX, (float) raw.x);
        this.headNY = Mth.lerp(NORMAL_EMA, this.headNY, (float) raw.y);
        this.headNZ = Mth.lerp(NORMAL_EMA, this.headNZ, (float) raw.z);
        float hnl = (float) Math.sqrt(this.headNX * this.headNX + this.headNY * this.headNY + this.headNZ * this.headNZ);
        if (hnl > 1.0E-5F) {
            this.headNX /= hnl;
            this.headNY /= hnl;
            this.headNZ /= hnl;
        } else {
            this.headNX = 0.0F;
            this.headNY = 1.0F;
            this.headNZ = 0.0F;
        }

        double newX = this.trackX[this.trackHead];
        double newY = this.trackY[this.trackHead];
        double newZ = this.trackZ[this.trackHead];
        double nd = Math.sqrt(sq(hx - newX) + sq(hy - newY) + sq(hz - newZ));
        if (nd >= CRUMB_MIN) {
            boolean air = !hasNearbySolid(hx - this.headNX * 0.5, hy - this.headNY * 0.5, hz - this.headNZ * 0.5);
            if (nd > CRUMB_MAX) {
                int nsub = (int) Math.ceil(nd / CRUMB_MIN);
                double px = newX;
                double py = newY;
                double pz = newZ;
                for (int k = 1; k <= nsub; k++) {
                    double t = (double) k / nsub;
                    double cx = Mth.lerp(t, newX, hx);
                    double cy = Mth.lerp(t, newY, hy);
                    double cz = Mth.lerp(t, newZ, hz);
                    this.headS += Math.sqrt(sq(cx - px) + sq(cy - py) + sq(cz - pz));
                    appendCrumb(cx, cy, cz, this.headNX, this.headNY, this.headNZ, air, this.headS);
                    px = cx;
                    py = cy;
                    pz = cz;
                }
            } else {
                this.headS += nd;
                appendCrumb(hx, hy, hz, this.headNX, this.headNY, this.headNZ, air, this.headS);
            }
            newX = this.trackX[this.trackHead];
            newY = this.trackY[this.trackHead];
            newZ = this.trackZ[this.trackHead];
        }

        double dHead = Math.sqrt(sq(hx - newX) + sq(hy - newY) + sq(hz - newZ));
        double virtHeadS = this.headS + dHead;
        int tail = (this.trackHead - this.trackCount + 1 + TRACK_CAP) % TRACK_CAP;
        int cursor = this.trackHead;

        for (int i = 0; i < count; i++) {
            double pathS = virtHeadS - (i + 1) * SEGMENT_SPACING;
            double rx;
            double ry;
            double rz;
            float rnx;
            float rny;
            float rnz;
            boolean rair;
            if (pathS >= this.headS) {
                double t = dHead < 1.0E-6 ? 0.0 : (pathS - this.headS) / dHead;
                t = Mth.clamp(t, 0.0, 1.0);
                rx = Mth.lerp(t, newX, hx);
                ry = Mth.lerp(t, newY, hy);
                rz = Mth.lerp(t, newZ, hz);
                rnx = Mth.lerp((float) t, this.trackNX[this.trackHead], this.headNX);
                rny = Mth.lerp((float) t, this.trackNY[this.trackHead], this.headNY);
                rnz = Mth.lerp((float) t, this.trackNZ[this.trackHead], this.headNZ);
                rair = false;
            } else {
                while (cursor != tail && this.trackS[cursor] > pathS) {
                    cursor = (cursor - 1 + TRACK_CAP) % TRACK_CAP;
                }
                if (this.trackS[cursor] > pathS) {
                    rx = this.trackX[cursor];
                    ry = this.trackY[cursor];
                    rz = this.trackZ[cursor];
                    rnx = this.trackNX[cursor];
                    rny = this.trackNY[cursor];
                    rnz = this.trackNZ[cursor];
                    rair = this.trackAir[cursor];
                } else {
                    int hi = (cursor + 1) % TRACK_CAP;
                    double span = this.trackS[hi] - this.trackS[cursor];
                    double t = span < 1.0E-9 ? 0.0 : (pathS - this.trackS[cursor]) / span;
                    t = Mth.clamp(t, 0.0, 1.0);
                    rx = Mth.lerp(t, this.trackX[cursor], this.trackX[hi]);
                    ry = Mth.lerp(t, this.trackY[cursor], this.trackY[hi]);
                    rz = Mth.lerp(t, this.trackZ[cursor], this.trackZ[hi]);
                    rnx = Mth.lerp((float) t, this.trackNX[cursor], this.trackNX[hi]);
                    rny = Mth.lerp((float) t, this.trackNY[cursor], this.trackNY[hi]);
                    rnz = Mth.lerp((float) t, this.trackNZ[cursor], this.trackNZ[hi]);
                    rair = this.trackAir[cursor] && this.trackAir[hi];
                }
            }
            this.pieceX[i] = rx;
            this.pieceY[i] = ry;
            this.pieceZ[i] = rz;
            float nl = (float) Math.sqrt(rnx * rnx + rny * rny + rnz * rnz);
            if (nl > 1.0E-5F) {
                this.pieceNX[i] = rnx / nl;
                this.pieceNY[i] = rny / nl;
                this.pieceNZ[i] = rnz / nl;
            } else {
                this.pieceNX[i] = 0.0F;
                this.pieceNY[i] = 1.0F;
                this.pieceNZ[i] = 0.0F;
            }
            this.pieceAir[i] = rair;
        }

        float shrivelTarget = isShriveling() ? 1.0F : 0.0F;
        this.shrivelAmount = Mth.lerp(0.15F, this.shrivelAmount, shrivelTarget);
        if (this.shrivelAmount > 0.01F) {
            double cx = getX();
            double cy = getY();
            double cz = getZ();
            float time = this.tickCount * 0.45F;
            float ang = this.yBodyRot * Mth.DEG_TO_RAD;
            double px = cx;
            double pz = cz;
            for (int i = 0; i < count; i++) {
                float writhe = Mth.sin(time + i * 0.7F) * 0.4F;
                ang += 0.55F + writhe;
                px += Mth.sin(ang) * SEGMENT_SPACING;
                pz += -Mth.cos(ang) * SEGMENT_SPACING;
                double vy = cy + Mth.sin(time * 1.4F + i * 0.5F) * 0.12F;
                float a = this.shrivelAmount;
                this.pieceX[i] = Mth.lerp(a, this.pieceX[i], px);
                this.pieceY[i] = Mth.lerp(a, this.pieceY[i], vy);
                this.pieceZ[i] = Mth.lerp(a, this.pieceZ[i], pz);
            }
        }

        if (!burrowed) for (int i = 0; i < count; i++) {
            if (isSolidAt(this.pieceX[i], this.pieceY[i], this.pieceZ[i])) {
                for (int s = 0; s < 6; s++) {
                    this.pieceX[i] += this.pieceNX[i] * 0.05;
                    this.pieceY[i] += this.pieceNY[i] * 0.05;
                    this.pieceZ[i] += this.pieceNZ[i] * 0.05;
                    if (!isSolidAt(this.pieceX[i], this.pieceY[i], this.pieceZ[i])) {
                        break;
                    }
                }
            }
        }

        double tx = count > 0 ? this.pieceX[count - 1] : hx;
        double ty = count > 0 ? this.pieceY[count - 1] : hy;
        double tz = count > 0 ? this.pieceZ[count - 1] : hz;
        for (int i = count; i < MAX_PIECES; i++) {
            this.pieceX[i] = this.pieceXO[i] = tx;
            this.pieceY[i] = this.pieceYO[i] = ty;
            this.pieceZ[i] = this.pieceZO[i] = tz;
            this.pieceNX[i] = 0.0F;
            this.pieceNY[i] = 1.0F;
            this.pieceNZ[i] = 0.0F;
            this.pieceAir[i] = false;
        }

        if (!Double.isFinite(this.pieceX[0]) || !Double.isFinite(this.pieceY[0]) || !Double.isFinite(this.pieceZ[0])) {
            seedTrack();
        }
    }

    private static double sq(double a) {
        return a * a;
    }

    private Vec3 surfaceDataNormal() {
        byte sb = this.entityData.get(DATA_SURFACE);
        return sb >= 0 && sb < 6 ? dvec(Direction.from3DDataValue(sb)) : new Vec3(0.0, 1.0, 0.0);
    }

    private boolean hasNearbySolid(double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y, z);
        if (!this.level().getBlockState(base).getCollisionShape(this.level(), base).isEmpty()) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            BlockPos b = base.relative(dir);
            if (!this.level().getBlockState(b).getCollisionShape(this.level(), b).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSolidAt(double x, double y, double z) {
        BlockPos bp = BlockPos.containing(x, y, z);
        return !this.level().getBlockState(bp).getCollisionShape(this.level(), bp).isEmpty();
    }

    private void updateParts() {
        int count = getPieceCount();
        for (int i = 0; i < this.parts.length; i++) {
            CentipedePart part = this.parts[i];
            part.xOld = part.getX();
            part.yOld = part.getY();
            part.zOld = part.getZ();
            part.xo = part.getX();
            part.yo = part.getY();
            part.zo = part.getZ();
            if (i >= count) {
                part.setPos(getX(), getY(), getZ());
            } else {
                double hh = getBbHeight() * 0.5;
                part.setPos(this.pieceX[i] - this.pieceNX[i] * hh, this.pieceY[i] + hh - this.pieceNY[i] * hh, this.pieceZ[i] - this.pieceNZ[i] * hh);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean success = super.doHurtTarget(target);
        if (success && target instanceof LivingEntity living) {
            if (isVenomous()) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0), this);
            }
            if (isStunning()) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), this);
            }
        }
        return success;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        if (isPrey(entity)) {
            entity.skipDropExperience();
            entity.nml$skipDroppingDeathLoot();
            if (!entity.isBaby()) eatAndGrow();
        }
        return super.killedEntity(level, entity);
    }

    private void eatAndGrow() {
        if (getSegments() < MAX_SEGMENTS) {
            setSegments(getSegments() + 1);
        }
        setHealth(getMaxHealth());
        this.playSound(NMLSounds.CENTIPEDE_HISS.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.is(DamageTypeTags.IS_FIRE) && canShrivel()) {
            startShrivel();
        }
        return hurt;
    }

    public void startShrivel() {
        this.shrivelTicks = SHRIVEL_DURATION;
        this.shrivelCooldown = SHRIVEL_COOLDOWN;
        setShriveling(true);
        setBurrowed(false);
        setBurrowPhysics(false);
        this.getNavigation().stop();
        this.setTarget(null);
    }

    public boolean isInShrivelState() {
        return this.shrivelTicks > 0;
    }

    public boolean canBurrowNow() {
        return this.burrowCooldown <= 0;
    }

    public void setBurrowPhysics(boolean active) {
        this.noPhysics = active;
        if (active) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    public boolean isBurrowBlock(BlockPos pos) {
        return this.level().getBlockState(pos).is(NMLTags.CENTIPEDE_BURROW);
    }

    public boolean isPassableAt(BlockPos pos) {
        return this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty();
    }

    public void resetBurrowCooldown() {
        this.burrowCooldown = 600;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.CENTIPEDE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.CENTIPEDE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.CENTIPEDE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(NMLSounds.CENTIPEDE_STEP.get(), 0.12F, 0.8F + this.random.nextFloat() * 0.3F);
    }

    public static boolean checkCentipedeSpawnRules(EntityType<Centipede> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }
}
