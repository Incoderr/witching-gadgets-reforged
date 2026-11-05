package witchinggadgets.client.gems;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchGate;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.gems.GemAffinities;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class GemsClient {
    private GemsClient() {
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (player == null || !GemAffinities.isGem(stack) || !Screen.hasShiftDown() || !ResearchGate.isComplete(player, "GEMCUTTING")) {
            return;
        }
        List<Component> lines = event.getToolTip();
        List<Aspect> affinities = GemAffinities.affinities(stack);
        if (!affinities.isEmpty()) {
            lines.add(Component.translatable("wg.desc.gemaffinity").withStyle(ChatFormatting.DARK_GREEN));
            for (Aspect aspect : affinities) {
                lines.add(Component.literal(" " + aspect.getName()).withStyle(ChatFormatting.DARK_GREEN));
            }
        }
        List<Aspect> aversions = GemAffinities.aversions(stack);
        if (!aversions.isEmpty()) {
            lines.add(Component.translatable("wg.desc.gemaversion").withStyle(ChatFormatting.RED));
            for (Aspect aspect : aversions) {
                lines.add(Component.literal(" " + aspect.getName()).withStyle(ChatFormatting.RED));
            }
        }
    }
}
