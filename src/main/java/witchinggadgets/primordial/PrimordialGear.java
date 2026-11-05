package witchinggadgets.primordial;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;

public interface PrimordialGear {
    Aspect[] ABILITIES = {Aspect.AIR, Aspect.EARTH, Aspect.FIRE, Aspect.WATER, Aspect.ORDER, Aspect.ENTROPY};
    int AIR = 0;
    int EARTH = 1;
    int FIRE = 2;
    int WATER = 3;
    int ORDER = 4;
    int ENTROPY = 5;

    int returnedPearls();

    static int ability(ItemStack stack) {
        int ability = PrimordialFeature.ABILITY.getOrDefault(stack, 0);
        return ability >= 0 && ability < ABILITIES.length ? ability : 0;
    }

    static boolean has(ItemStack stack, int ability) {
        return stack.getItem() instanceof PrimordialGear && ability(stack) == ability;
    }

    static Aspect abilityAspect(ItemStack stack) {
        return ABILITIES[ability(stack)];
    }

    static void setAbility(ItemStack stack, int ability) {
        PrimordialFeature.ABILITY.set(stack, ability == 0 ? null : ability);
    }

    static void cycle(ItemStack stack) {
        setAbility(stack, (ability(stack) + 1) % ABILITIES.length);
    }

    static Component announcement(Aspect aspect) {
        return Component.translatable("wg.chat.primordialArmor.ability", aspectName(aspect));
    }

    static MutableComponent aspectName(Aspect aspect) {
        MutableComponent name = Component.literal(aspect.getName());
        String code = aspect.getChatcolor();
        ChatFormatting color = code == null || code.isEmpty() ? null : ChatFormatting.getByCode(code.charAt(0));
        return color == null ? name : name.withStyle(color);
    }

    static Component name(Component base, ItemStack stack) {
        return base.copy()
            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
            .append(aspectName(abilityAspect(stack)));
    }

    static void cycleAndAnnounce(Player player, ItemStack stack) {
        cycle(stack);
        player.displayClientMessage(announcement(abilityAspect(stack)), true);
    }
}
