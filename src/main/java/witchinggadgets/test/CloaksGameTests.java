package witchinggadgets.test;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Baubles;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import witchinggadgets.cloaks.CloakEvents;
import witchinggadgets.cloaks.CloakStorageMenu;
import witchinggadgets.cloaks.CloaksFeature;
import witchinggadgets.cloaks.MagicCloakItem;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

public final class CloaksGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("cloaks")
        .add("cloak_recipes_registered", CloaksGameTests::recipesRegistered)
        .add("spectral_cloak_toggles_with_aer", CloaksGameTests::spectralToggles)
        .add("spectral_cloak_needs_vis", CloaksGameTests::spectralNeedsVis)
        .add("spectral_cloak_hides_from_mobs", CloaksGameTests::spectralHidesFromMobs)
        .add("wolf_cloak_buffs_after_damage", CloaksGameTests::wolfBuffs)
        .add("storage_cloak_keeps_items", CloaksGameTests::storageKeepsItems)
        .add("kama_is_worn_on_the_belt", CloaksGameTests::kamaOnBelt);

    private CloaksGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(player.getGameProfile(), false);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override
            public void send(Packet<?> packet) {
            }
        }.markClientLoaded();
        return player;
    }

    private static ItemStack wear(ServerPlayer player, ItemStack cloak) {
        Gear.set(player, GearSlot.CLOAK, cloak);
        return Gear.get(player, GearSlot.CLOAK);
    }

    private static void activate(ServerPlayer player) {
        ItemStack cloak = CloakEvents.activeCloak(player);
        MagicCloakItem item = (MagicCloakItem) cloak.getItem();
        if (item.canActivate(player, cloak)) {
            item.activate(player, cloak);
        }
    }

    private static void recipesRegistered(GameTestHelper helper) {
        for (String key : new String[] {"CLOAK", "CLOAK_STORAGE", "CLOAK_WOLF", "CLOAK_SPECTRAL", "CLOAKKAMA_0", "CLOAKKAMA_3"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void spectralToggles(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack wand = Wands.create(WandRegistry.CAP_GOLD, WandRegistry.ROD_GREATWOOD, false);
        Wands.fill(wand);
        int airBefore = Wands.getVis(wand, Aspect.AIR);
        player.getInventory().setItem(0, wand);
        ItemStack cloak = wear(player, new ItemStack(CloaksFeature.SPECTRAL_CLOAK.get()));
        activate(player);
        helper.assertTrue(MagicCloakItem.isSpectral(cloak), "cloak did not turn spectral");
        helper.assertTrue(CloakEvents.isSpectral(player), "player is not spectral");
        helper.assertTrue(Wands.getVis(player.getInventory().getItem(0), Aspect.AIR) < airBefore, "aer vis not consumed");
        activate(player);
        helper.assertFalse(MagicCloakItem.isSpectral(cloak), "cloak did not turn back");
        helper.succeed();
    }

    private static void spectralNeedsVis(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack cloak = wear(player, new ItemStack(CloaksFeature.SPECTRAL_CLOAK.get()));
        activate(player);
        helper.assertFalse(MagicCloakItem.isSpectral(cloak), "cloak turned spectral without vis");
        helper.succeed();
    }

    private static void spectralHidesFromMobs(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack cloak = wear(player, new ItemStack(CloaksFeature.SPECTRAL_CLOAK.get()));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(1, 1, 1));
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == player, "zombie ignores a visible player");
        zombie.setTarget(null);
        CloaksFeature.SPECTRAL.set(cloak, true);
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == null, "zombie targets a spectral player");
        zombie.discard();
        helper.succeed();
    }

    private static void wolfBuffs(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack cloak = wear(player, new ItemStack(CloaksFeature.WOLF_CLOAK.get()));
        player.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 8.0F);
        helper.assertValueEqual(CloaksFeature.WOLF_POTION.getOrDefault(cloak, 0), 2, "wolf amplifier");
        ((MagicCloakItem) cloak.getItem()).cloakTick(player, cloak);
        helper.assertTrue(player.hasEffect(MobEffects.STRENGTH), "no strength");
        helper.assertValueEqual(player.getEffect(MobEffects.SPEED).getAmplifier(), 2, "speed amplifier");
        helper.assertFalse(CloaksFeature.WOLF_POTION.has(cloak), "pending buff not cleared");
        helper.succeed();
    }

    private static void storageKeepsItems(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack cloak = wear(player, new ItemStack(CloaksFeature.STORAGE_CLOAK.get()));
        activate(player);
        helper.assertTrue(player.containerMenu instanceof CloakStorageMenu, "storage menu not opened");
        player.containerMenu.getSlot(4).set(new ItemStack(Items.DIAMOND, 5));
        ItemContainerContents contents = CloaksFeature.STORAGE.getOrDefault(cloak, ItemContainerContents.EMPTY);
        helper.assertTrue(contents.nonEmptyItemCopyStream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 5), "diamonds not stored");
        player.closeContainer();
        helper.succeed();
    }

    private static void kamaOnBelt(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        int belt = -1;
        for (int slot = 0; slot < Baubles.slotCount(); slot++) {
            if (Baubles.slotType(slot) == BaubleType.BELT) {
                belt = slot;
            }
        }
        helper.assertTrue(belt >= 0, "no belt slot");
        Baubles.set(player, belt, new ItemStack(CloaksFeature.STORAGE_KAMA.get()));
        helper.assertTrue(CloakEvents.activeCloak(player).is(CloaksFeature.STORAGE_KAMA.get()), "kama not active");
        Gear.set(player, GearSlot.CLOAK, new ItemStack(CloaksFeature.CLOAK.get()));
        helper.assertTrue(CloakEvents.activeCloak(player).is(CloaksFeature.CLOAK.get()), "cloak slot does not take priority");
        helper.succeed();
    }
}
