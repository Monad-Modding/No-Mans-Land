package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.entity.bombs.AcridBomb;
import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AcridBombItem extends ThrowableBombItem {

    public AcridBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBombEntity asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new AcridBomb(level, position.x(), position.y(), position.z());
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        super.onUseTick(level, entity, stack, remainingTicks);
        int timeUsed = this.getUseDuration(stack, entity) - remainingTicks;
        if (timeUsed == DEFAULT_THROW_TIME && entity.isShiftKeyDown()) entity.playSound(NMLSounds.BOMB_PRIMED.get());
    }
}
