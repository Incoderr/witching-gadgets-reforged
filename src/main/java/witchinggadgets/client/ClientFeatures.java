package witchinggadgets.client;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import witchinggadgets.bags.BagsFeature;
import witchinggadgets.client.bags.BagScreen;
import witchinggadgets.client.bags.VoidBagScreen;
import witchinggadgets.client.cloaks.CloakStorageScreen;
import witchinggadgets.client.devices.EssentiaPumpRenderer;
import witchinggadgets.client.devices.LabelLibraryRenderer;
import witchinggadgets.client.devices.LabelLibraryScreen;
import witchinggadgets.client.gems.CuttingTableRenderer;
import witchinggadgets.client.gems.CuttingTableScreen;
import witchinggadgets.client.primordial.GauntletScreen;
import witchinggadgets.client.spinning.SpinningWheelScreen;
import witchinggadgets.client.terraform.TerraformFocusRenderer;
import witchinggadgets.client.terraform.TerraformerRenderer;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.devices.DevicesFeature;
import witchinggadgets.gems.GemsFeature;
import witchinggadgets.primordial.PrimordialFeature;
import witchinggadgets.spinning.SpinningFeature;
import witchinggadgets.terraform.TerraformFeature;
import witchinggadgets.client.travellersgear.ArmorStandRenderer;
import witchinggadgets.client.travellersgear.ArmorStandScreen;
import witchinggadgets.travellersgear.TravellersGearFeature;

public final class ClientFeatures {
    private ClientFeatures() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SpinningFeature.SPINNING_WHEEL_MENU.get(), SpinningWheelScreen::new);
        event.register(CloaksFeature.STORAGE_MENU.get(), CloakStorageScreen::new);
        event.register(BagsFeature.BAG_MENU.get(), BagScreen::new);
        event.register(BagsFeature.VOID_BAG_MENU.get(), VoidBagScreen::new);
        event.register(DevicesFeature.LABEL_LIBRARY_MENU.get(), LabelLibraryScreen::new);
        event.register(GemsFeature.CUTTING_TABLE_MENU.get(), CuttingTableScreen::new);
        event.register(PrimordialFeature.GAUNTLET_MENU.get(), GauntletScreen::new);
        event.register(TravellersGearFeature.ARMOR_STAND_MENU.get(), ArmorStandScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DevicesFeature.LABEL_LIBRARY_ENTITY.get(), LabelLibraryRenderer::new);
        event.registerBlockEntityRenderer(GemsFeature.CUTTING_TABLE_ENTITY.get(), CuttingTableRenderer::new);
        event.registerBlockEntityRenderer(DevicesFeature.ESSENTIA_PUMP_ENTITY.get(), EssentiaPumpRenderer::new);
        event.registerBlockEntityRenderer(TerraformFeature.TERRAFORMER_ENTITY.get(), TerraformerRenderer::new);
        event.registerBlockEntityRenderer(TerraformFeature.FOCUS_ENTITY.get(), TerraformFocusRenderer::new);
        event.registerBlockEntityRenderer(TravellersGearFeature.ARMOR_STAND_ENTITY.get(), ArmorStandRenderer::new);
    }
}
