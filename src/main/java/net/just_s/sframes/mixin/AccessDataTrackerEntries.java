package net.just_s.sframes.mixin;

import net.minecraft.entity.data.DataTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataTracker.class)
public interface AccessDataTrackerEntries {
    @Accessor
    public DataTracker.Entry<?>[] getEntries();
}
