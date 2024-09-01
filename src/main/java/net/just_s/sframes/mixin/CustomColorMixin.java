package net.just_s.sframes.mixin;

import net.just_s.sframes.SFramesMod;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class CustomColorMixin {
    @Inject(at=@At("TAIL"), method = "onPlayerConnected")
    private void sframes$sendInitialColorPacket(ServerPlayerEntity player, CallbackInfo ci) {
        if (!player.getAttachedOrElse(SFramesMod.HAS_COLOR_ATTACHMENT, false)) return;

        Team team = SFramesMod.getTeam();
        Formatting baseColor = team.getColor();

        team.setColor(player.getAttachedOrElse(SFramesMod.COLOR_ATTACHMENT, SFramesMod.CONFIG.getData().baseColor()));
        TeamS2CPacket packet = TeamS2CPacket.updateTeam(team, false);
        team.setColor(baseColor);

        SFramesMod.sendPacket(player, packet);
    }
}
