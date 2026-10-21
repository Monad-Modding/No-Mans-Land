package com.farcr.nomansland.client.renderer.context;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataAttachments;
import net.minecraft.client.model.EntityModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class StasisEntityContext {
    private static Entity ENTITY_CONTEXT;
    public static Entity getEntityContext() { return ENTITY_CONTEXT; }
    public static <T extends Entity> void setEntityContext(T entity) { ENTITY_CONTEXT = entity; }
    public static void clearEntityContext() { ENTITY_CONTEXT = null; }

    public static <T> void copyFields(T source, T target) {
        Class<?> clazz = source.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                boolean accessible = field.canAccess(source);
                try {
                    field.setAccessible(true);
                    Object value = field.get(source);
                    field.set(target, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to copy field: " + field.getName(), e);
                } finally {
                    field.setAccessible(accessible);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static final Map<LivingEntity, LivingEntity> entityCopyMap = new HashMap<>();
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T getEntitySnapshot(T entity) {
        // check this first because it (in theory) avoids a cast which should be more performant????
        if (entity.hasData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER)) {
            // if the entity has that field then it should always be a living entity?
            LivingEntity livingEntity = (LivingEntity) entity;
            if (entity.getData(NMLEntityDataAttachments.STASIS_TICK_MULTIPLIER) <= 0f) {
                if (!entityCopyMap.containsKey(livingEntity)) {
                    // originally I was going to use reflection but this might just work
                    LivingEntity clone = (LivingEntity) livingEntity.getType()
                        .create(livingEntity.level());
                    NoMansLand.LOGGER.info("new cloned entity: " + livingEntity);
                    if (clone != null) {
                        CompoundTag cloneTag = new CompoundTag();
                        livingEntity.saveWithoutId(cloneTag);
                        clone.load(cloneTag);
                        copyFields(livingEntity, clone);
//                        copyFields(livingEntity.getEntityData(), clone.getEntityData());
                        entityCopyMap.put(livingEntity, clone);
                    } else entityCopyMap.put(livingEntity, livingEntity);
                }
                return (T) entityCopyMap.get(livingEntity);
            // and since the value isnt removed just nullified when the entity is out of stasis
            // then I guess we already know its a living entity to remove it from the list
            } else entityCopyMap.remove(livingEntity);
        }
        return entity;
    }
}
