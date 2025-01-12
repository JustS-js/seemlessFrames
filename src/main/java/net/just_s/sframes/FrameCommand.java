package net.just_s.sframes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.netty.buffer.Unpooled;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class FrameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
                CommandManager.literal("sframes").
                        then(
                                CommandManager.literal("color").
                                        executes(
                                                (context) -> executeShow(context.getSource(), "color", null)
                                        ).
                                        then(
                                        CommandManager.argument("value", ColorArgumentType.color()).executes(
                                                (context) -> executeColor(context.getSource(), ColorArgumentType.getColor(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("baseColor").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "baseColor", SFramesMod.CONFIG.outlineColor)
                                        ).
                                        then(
                                        CommandManager.argument("value", ColorArgumentType.color()).executes(
                                                (context) -> executeBaseColor(context.getSource(), ColorArgumentType.getColor(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("radius").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "radius", SFramesMod.CONFIG.radiusOfGlowing)
                                        ).
                                        then(
                                        CommandManager.argument("value", IntegerArgumentType.integer(-1, 128)).executes(
                                                (context) -> executeRadius(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("doShearsBreak").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "doShearsBreak", SFramesMod.CONFIG.doShearsBreak)
                                        ).
                                        then(
                                        CommandManager.argument("value", BoolArgumentType.bool()).executes(
                                                (context) -> executeDoShearsBreak(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("doLeatherFix").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "doLeatherFix", SFramesMod.CONFIG.fixWithLeather)
                                        ).
                                        then(
                                        CommandManager.argument("value", BoolArgumentType.bool()).executes(
                                                (context) -> executeDoLeatherFix(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("clientSideGlowing").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "clientSideGlowing", SFramesMod.CONFIG.clientSideGlowing)
                                        ).
                                        then(
                                        CommandManager.argument("value", BoolArgumentType.bool()).executes(
                                                (context) -> executeClientSideGlowing(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                )
                        )
        );

    }

    private static int executeShow(ServerCommandSource commandSource, String name, Object value) {
        String strValue = null;
        if (value instanceof Boolean) {
            strValue = (boolean) value ? "§6" + value : "§c" + value;
        }
        if (value instanceof Integer) {
            strValue = "§6" + value;
        }
        if (Objects.equals(name, "color")) {
            String colorName = SFramesMod.CONFIG.playerColor.get(commandSource.getPlayer().getNameForScoreboard());
            strValue = colorName != null ? Formatting.byName(colorName).toString() + colorName : "§fno custom color";
        }
        if (Objects.equals(name, "baseColor")) {
            strValue = Formatting.byName((String) value).toString() + value;
        }
        commandSource.getPlayer().sendMessage(Text.of("§6[§aSFrames§6] §2value of §b" + name + "§2 > " + strValue));
        return 1;
    }

    private static int executeColor(ServerCommandSource commandSource, Formatting color) {
        ServerPlayerEntity player = commandSource.getPlayer();
        SFramesMod.CONFIG.playerColor.put(player.getNameForScoreboard(), color.asString());

        Team team = player.getServer().getScoreboard().getTeam("SeamlessFrames");
        Formatting baseColor = team.getColor();
        team.setColor(Formatting.byName(SFramesMod.CONFIG.playerColor.get(player.getNameForScoreboard())));
        TeamS2CPacket packet = TeamS2CPacket.updateTeam(team, false);
        team.setColor(baseColor);
        SFramesMod.sendPackets(player, packet);

        SFramesMod.CONFIG.dumpJson();
        return 1;
    }

    private static int executeBaseColor(ServerCommandSource commandSource, Formatting color) {
        SFramesMod.CONFIG.outlineColor = color.asString();
        commandSource.getServer().getScoreboard().getTeam("SeamlessFrames").setColor(Formatting.byName(SFramesMod.CONFIG.outlineColor));
        executeShow(commandSource, "baseColor", SFramesMod.CONFIG.outlineColor);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeRadius(ServerCommandSource commandSource, int value) {
        SFramesMod.CONFIG.radiusOfGlowing = value;
        executeShow(commandSource, "radius", SFramesMod.CONFIG.radiusOfGlowing);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeDoShearsBreak(ServerCommandSource commandSource, boolean value) {
        SFramesMod.CONFIG.doShearsBreak = value;
        executeShow(commandSource, "doShearsBreak", SFramesMod.CONFIG.doShearsBreak);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeDoLeatherFix(ServerCommandSource commandSource, boolean value) {
        SFramesMod.CONFIG.fixWithLeather = value;
        executeShow(commandSource, "doLeatherFix", SFramesMod.CONFIG.fixWithLeather);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeClientSideGlowing(ServerCommandSource commandSource, boolean value) {
        SFramesMod.CONFIG.clientSideGlowing = value;
        executeShow(commandSource, "clientSideGlowing", SFramesMod.CONFIG.clientSideGlowing);
        SFramesMod.CONFIG.dump();
        return 1;
    }
}
