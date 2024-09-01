package net.just_s.sframes.mixin;

import net.just_s.sframes.SFramesMod;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlowItemFrameEntity.class)
public class GlowingItemFrameMixin {
    @Inject(at = @At("TAIL"), method = "getAsItemStack", cancellable = true)
    private void sframes$onDroppingAsItem(CallbackInfoReturnable<ItemStack> cir) {
        // If players can't fix item frames with leather, we should drop vanilla frames
        if (!SFramesMod.CONFIG.getData().fixWithLeather()) return;

        ItemFrameEntity itemFrame = ((ItemFrameEntity)(Object)this);
        if (!SFramesMod.isFrameInTeam(itemFrame)) return;

        ItemStack item = cir.getReturnValue();
        item.set(DataComponentTypes.ITEM_NAME, Text.of(SFramesMod.CONFIG.getData().invisibleGlowItemFrameName()));
        item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("invisibleframe", true);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        cir.setReturnValue(item);
    }
}
