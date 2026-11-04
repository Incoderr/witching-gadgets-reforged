package witchinggadgets.gadgets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.entity.CombatUtils;
import thaumcraft.fx.Fx;
import thaumcraft.registry.ModItems;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ScanManager;

public class ScanCameraItem extends Item {
    public record Shot(ScanManager.Target target, Component name, Vec3 runes, int runeTicks) {
    }

    public ScanCameraItem(Properties properties) {
        super(properties);
    }

    public static @Nullable Shot aim(Level level, Player player) {
        Entity pointed = CombatUtils.getPointedEntity(level, player, 0.5, 10.0, 0.0F, true, null);
        if (pointed != null) {
            ScanManager.Target target = ScanManager.resolveEntity(pointed);
            Vec3 runes = new Vec3(pointed.getX() - 0.5, pointed.getY() + pointed.getEyeHeight() / 2.0F, pointed.getZ() - 0.5);
            return target == null ? null : new Shot(target, ScanSnapshot.nameOf(pointed), runes, (int) (pointed.getBbHeight() * 15.0F));
        }
        BlockHitResult hit = ScanManager.pickBlock(level, player);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = hit.getBlockPos();
        ScanManager.Target target = ScanManager.resolveBlock(level, pos);
        if (target == null) {
            return null;
        }
        Component name = target.clueStack().isEmpty() ? level.getBlockState(pos).getBlock().getName() : ScanSnapshot.nameOf(target.clueStack());
        return new Shot(target, name, new Vec3(pos.getX(), pos.getY() + 0.25, pos.getZ()), 15);
    }

    public static int plateSlot(Inventory inventory) {
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).is(ModItems.MIRRORED_GLASS.get())) {
                return slot;
            }
        }
        return -1;
    }

    public static boolean takePhoto(Player player, Shot shot) {
        if (!ScanManager.validScan(shot.target().aspects(), player)) {
            return false;
        }
        int slot = plateSlot(player.getInventory());
        if (slot < 0) {
            return false;
        }
        player.getInventory().removeItem(slot, 1);
        ItemStack photo = new ItemStack(GadgetsFeature.photoPlate());
        GadgetsFeature.SCAN.set(photo, ScanSnapshot.of(shot.target(), shot.name()));
        if (!player.getInventory().add(photo)) {
            player.drop(photo, false, Prediction.SERVER_ONLY);
        }
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Shot shot = aim(level, player);
        if (shot == null) {
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) {
            float red = 0.3F + level.getRandom().nextFloat() * 0.7F;
            float blue = 0.3F + level.getRandom().nextFloat() * 0.7F;
            Fx.get().blockRunes(level, shot.runes().x, shot.runes().y, shot.runes().z, red, 0.0F, blue, shot.runeTicks(), 0.03F);
            if (ScanManager.validScan(shot.target().aspects(), player) && plateSlot(player.getInventory()) >= 0) {
                level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.CAMERATICKS.get(), SoundSource.PLAYERS, 0.3F, 1.9F + level.getRandom().nextFloat() * 0.2F, false);
            }
            return InteractionResult.SUCCESS;
        }
        takePhoto(player, shot);
        return InteractionResult.SUCCESS;
    }
}
