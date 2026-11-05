package witchinggadgets.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import thaumcraft.damage.ModDamageTypes;

public class DissolveEffect extends MobEffect {
    public static final int TICK_RATE = 40;

    public DissolveEffect() {
        super(MobEffectCategory.HARMFUL, 0x450B45);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int rate = TICK_RATE >> amplifier;
        return rate <= 0 || duration % rate == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        entity.hurtServer(level, level.damageSources().source(ModDamageTypes.DISSOLVE), 1.0F);
        return true;
    }
}
