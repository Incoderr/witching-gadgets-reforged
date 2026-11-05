package witchinggadgets.client.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.CrystalCapsuleItem;
import witchinggadgets.gems.InfusedGemItem;
import witchinggadgets.primordial.PrimordialArmorItem;
import witchinggadgets.primordial.PrimordialGear;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class WgItemTintSources {
    private WgItemTintSources() {
    }

    @SubscribeEvent
    static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(WitchingGadgets.id("gem_aspect"), GemAspect.MAP_CODEC);
        event.register(WitchingGadgets.id("capsule_fluid"), CapsuleFluid.MAP_CODEC);
        event.register(WitchingGadgets.id("primordial_ability"), PrimordialAbility.MAP_CODEC);
    }

    public static int fluidColor(Fluid fluid) {
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        int tint = model != null && model.fluidTintSource() != null ? model.fluidTintSource().colorAsStack(new FluidStack(fluid, 1000)) : -1;
        if ((tint & 0xFFFFFF) != 0xFFFFFF) {
            return ARGB.opaque(tint);
        }
        if (fluid.isSame(Fluids.LAVA)) {
            return 0xFFD96415;
        }
        return ARGB.opaque(fluid.defaultFluidState().createLegacyBlock().getBlock().defaultMapColor().col);
    }

    public record GemAspect() implements ItemTintSource {
        public static final MapCodec<GemAspect> MAP_CODEC = MapCodec.unit(new GemAspect());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            Aspect aspect = InfusedGemItem.aspect(stack);
            return aspect == null ? 0xFFFFFFFF : ARGB.opaque(aspect.getColor());
        }

        @Override
        public MapCodec<GemAspect> type() {
            return MAP_CODEC;
        }
    }

    public record CapsuleFluid() implements ItemTintSource {
        public static final MapCodec<CapsuleFluid> MAP_CODEC = MapCodec.unit(new CapsuleFluid());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            Fluid fluid = CrystalCapsuleItem.fluid(stack);
            return fluid == null ? 0x00FFFFFF : fluidColor(fluid);
        }

        @Override
        public MapCodec<CapsuleFluid> type() {
            return MAP_CODEC;
        }
    }

    public record PrimordialAbility() implements ItemTintSource {
        public static final MapCodec<PrimordialAbility> MAP_CODEC = MapCodec.unit(new PrimordialAbility());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return ARGB.opaque(PrimordialGear.abilityAspect(stack).getColor());
        }

        @Override
        public MapCodec<PrimordialAbility> type() {
            return MAP_CODEC;
        }
    }
}
