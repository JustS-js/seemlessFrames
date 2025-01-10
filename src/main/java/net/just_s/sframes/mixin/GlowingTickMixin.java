package net.just_s.sframes.mixin;

import net.just_s.sframes.SFramesMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static net.just_s.sframes.SFramesMod.generateGlowPacket;

@Mixin(BlockAttachedEntity.class)
public abstract class GlowingTickMixin {
    @Unique
    private int tick = 0;

    @Inject(at = @At("HEAD"), method = "tick")
    private void inject(CallbackInfo ci) {
        try {
            //if (SFramesMod.CONFIG.clientSideGlowing) return;
            BlockAttachedEntity frame = (BlockAttachedEntity) (Object) this;
            if (!(frame instanceof ItemFrameEntity && SFramesMod.shouldGlow((ItemFrameEntity) frame))) return;

            List<Entity> entities = frame.getEntityWorld().getOtherEntities(null, new Box(
                    frame.getPos().add(SFramesMod.CONFIG.radiusOfGlowing + 1, SFramesMod.CONFIG.radiusOfGlowing + 1, SFramesMod.CONFIG.radiusOfGlowing + 1),
                    frame.getPos().add(-1 * SFramesMod.CONFIG.radiusOfGlowing, -1 * SFramesMod.CONFIG.radiusOfGlowing, -1 * SFramesMod.CONFIG.radiusOfGlowing)
            ));

            List<ServerPlayerEntity> players = (List<ServerPlayerEntity>) frame.getWorld().getPlayers();
            List<ServerPlayerEntity> playerOutOfBound = new ArrayList<>(players.size());
            playerOutOfBound.addAll(players);

            boolean noPlayerNearby = frame.getEntityWorld().getClosestPlayer(frame, SFramesMod.CONFIG.radiusOfGlowing + 1) == null;
            tick++;
            if (!noPlayerNearby) {
                Team team = frame.getServer().getScoreboard().getTeam("SeamlessFrames");
                for (Entity entity : entities) {
                    if (entity instanceof ServerPlayerEntity) {
                        if (SFramesMod.CONFIG.clientSideGlowing) {
                            if (tick % 10 == 0) {
                                clientSideTickGlow((ServerPlayerEntity) entity, (ItemFrameEntity) frame);
                                playerOutOfBound.remove(entity);
                            }
                        } else
                            serverSideTickGlow(frame);
                        // Send Custom Color of frame
                        if (SFramesMod.CONFIG.playerColor.containsKey(entity.getNameForScoreboard())) {
                            Formatting baseColor = team.getColor();
                            team.setColor(Formatting.byName(SFramesMod.CONFIG.playerColor.get(entity.getNameForScoreboard())));
                            TeamS2CPacket packet = TeamS2CPacket.updateTeam(team, false);
                            team.setColor(baseColor);
                            SFramesMod.sendPackets((ServerPlayerEntity)entity, packet);
                        }
                    }
                }
            }

            if (frame.isGlowing() && noPlayerNearby && !SFramesMod.CONFIG.clientSideGlowing) frame.setGlowing(false);
            if (SFramesMod.CONFIG.clientSideGlowing && tick % 10 == 0)
                SFramesMod.sendPackets(playerOutOfBound, generateGlowPacket((ItemFrameEntity) frame, false));
//                SFramesMod.sendPackets(playerOutOfBound, new RemoveEntityStatusEffectS2CPacket(
//                        frame.getId(), StatusEffects.GLOWING
//                ));
            tick = tick % 10;

        } catch (Exception e) {
            SFramesMod.LOGGER.error("SFrames error on GlowingTickMixin.tick(): " + e);
        }
    }

    private void serverSideTickGlow(Entity frame) {
        if (!frame.isGlowing()) frame.setGlowing(true);
    }

    private void clientSideTickGlow(ServerPlayerEntity player, ItemFrameEntity frame) {
        SFramesMod.sendPackets(player, generateGlowPacket(frame, true));
//        SFramesMod.sendPackets(player, new EntityStatusEffectS2CPacket(
//                frame.getId(), new StatusEffectInstance(StatusEffects.GLOWING)
//        ));
    }
}
