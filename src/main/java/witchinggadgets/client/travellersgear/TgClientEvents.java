package witchinggadgets.client.travellersgear;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import witchinggadgets.WitchingGadgets;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class TgClientEvents {
    private TgClientEvents() {
    }

    @SubscribeEvent
    static void registerCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("travellersgear");
        root.then(Commands.literal("gui").executes(context -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> minecraft.setScreen(new TgCustomizationScreen()));
            return 1;
        }));
        root.then(Commands.literal("toolDisplay").executes(context -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.execute(() -> minecraft.setScreen(new ToolDisplayScreen()));
            return 1;
        }));
        event.getDispatcher().register(root);
    }

    @SubscribeEvent
    static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && Minecraft.getInstance().screen instanceof TravellersInventoryScreen) {
            event.getToolTip().add(Component.translatable("TG.guitext.linkInChat").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
