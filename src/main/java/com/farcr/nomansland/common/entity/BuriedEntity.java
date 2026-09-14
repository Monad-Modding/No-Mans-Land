package com.farcr.nomansland.common.entity;

//    TODO: BURIED
//public class BuriedEntity extends Monster implements RangedAttackMob {
//
//    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(BuriedEntity.class, EntityDataSerializers.INT);
//
//    public BuriedEntity(EntityType<? extends BuriedEntity> entityType, Level level) {
//        super(entityType, level);
//    }
//
//    private static boolean isHoldingBow(LivingEntity livingEntity) {
//        return livingEntity.isHolding(stack -> stack.getItem() instanceof BowItem);
//    }
//
//    public static AttributeSupplier.Builder createAttributes() {
//        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 1.0F).add(Attributes.KNOCKBACK_RESISTANCE, -0.1F)
//                .add(Attributes.MAX_HEALTH, 14.0F);
//    }
//
//    public BuriedVariant getVariant() {
//        return BuriedVariant.byId(this.getTypeVariant() & 255);
//    }
//
//    private void setVariant(BuriedVariant variant) {
//        this.entityData.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
//    }
//
//    private int getTypeVariant() {
//        return this.entityData.get(DATA_ID_TYPE_VARIANT);
//    }
//
//    @Override
//    public void readAdditionalSaveData(CompoundTag tag) {
//        super.readAdditionalSaveData(tag);
//        this.entityData.set(DATA_ID_TYPE_VARIANT, tag.getInt("Variant"));
//    }
//
//    @Override
//    public void addAdditionalSaveData(CompoundTag tag) {
//        super.addAdditionalSaveData(tag);
//        tag.putInt("Variant", this.getTypeVariant());
//    }
//
//    @Override
//    protected void defineSynchedData(SynchedEntityData.Builder builder) {
//        super.defineSynchedData(builder);
//        this.entityData.set(DATA_ID_TYPE_VARIANT, 0);
//    }
//
//    @Override
//    public void performRangedAttack(LivingEntity target, float velocity) {
//    }
//}
