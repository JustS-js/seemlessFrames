package net.just_s.sframes.mixin;

import net.just_s.sframes.SFramesMod;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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
    private void injectAsItem(CallbackInfoReturnable<ItemStack> cir) {
        try {
            if (!SFramesMod.CONFIG.fixWithLeather) return;
            ItemFrameEntity frame = ((ItemFrameEntity)(Object)this);
            if (frame.getCommandTags().contains("invisibleframe")) {
                ItemStack item = cir.getReturnValue();
                item.set(DataComponentTypes.ITEM_NAME, Text.of("Невидимая светящаяся рамка"));
                item.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

                NbtComponent nbtCompound = item.get(DataComponentTypes.CUSTOM_DATA);
                NbtCompound nbt = (nbtCompound == null) ? NbtComponent.DEFAULT.copyNbt() : nbtCompound.copyNbt();
                nbt.putBoolean("invisibleframe", true);
                item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                cir.setReturnValue(item);
            }
        } catch (Exception e) {
            SFramesMod.LOGGER.error("SFrames error on GlowingItemFrameMixin: " + e);
        }
    }
}
