package net.just_s.sframes.mixin;

import net.just_s.sframes.SFramesMod;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(MinecraftDedicatedServer.class)
public class ServerStartMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(at = @At("TAIL"), method = "setupServer")
    private void inject(CallbackInfoReturnable<Boolean> cir) {
        MinecraftDedicatedServer server = ((MinecraftDedicatedServer)(Object)this);
        World world = server.getWorlds().iterator().next();

        try {
            teamSettings(Objects.requireNonNull(world.getScoreboard().getTeam("SeamlessFrames")));
            LOGGER.info("SeamlessFrames scoreboard loaded successfully");
        } catch (NullPointerException e) {
            LOGGER.info("SeamlessFrames does not exist, setting up...");
            Team team = world.getScoreboard().addTeam("SeamlessFrames");
            teamSettings(team);
        }
    }

    @Unique
    private void teamSettings(Team team) {
        team.setColor(Formatting.byName(SFramesMod.CONFIG.outlineColor));
    }
}
