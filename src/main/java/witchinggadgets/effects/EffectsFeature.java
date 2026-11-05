package witchinggadgets.effects;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import thaumcraft.aspect.AspectList;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.registry.ModEffects;
import witchinggadgets.research.WgResearch;

@EventBusSubscriber(modid = WitchingGadgets.MODID)
public final class EffectsFeature {
    public static final Holder<MobEffect> DISSOLVE = ModEffects.EFFECTS.register("dissolve", DissolveEffect::new);
    public static final Holder<MobEffect> CINDER_COAT = ModEffects.EFFECTS.register("cinder_coat", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x8F3F1F) {
    });

    private EffectsFeature() {
    }

    public static void load() {
    }

    public static void research() {
        WgResearch.item("WGPOTIONS", new AspectList(), 8, 0, 0, WitchingGadgets.id("textures/gui/research/icon_potioneffects.png"))
            .setRound()
            .setAutoUnlock()
            .setPages(WgResearch.text("1"), WgResearch.text("2"))
            .registerResearchItem();
    }

    public static float cinderMultiplier(net.minecraft.world.entity.LivingEntity entity) {
        MobEffectInstance cinder = entity.getEffect(CINDER_COAT);
        return cinder == null ? 1.0F : 2.0F + cinder.getAmplifier();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            event.setAmount(event.getAmount() * cinderMultiplier(event.getEntity()));
        }
    }
}
