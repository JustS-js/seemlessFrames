package net.just_s.sframes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.argument.ColorArgumentType;
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
                                                (context) -> executeShow(context.getSource(), "baseColor", SFramesMod.CONFIG.getData().baseColor())
                                        ).
                                        then(
                                        CommandManager.argument("value", ColorArgumentType.color()).executes(
                                                (context) -> executeBaseColor(context.getSource(), ColorArgumentType.getColor(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("radius").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "radius", SFramesMod.CONFIG.getData().radiusOfGlowing())
                                        ).
                                        then(
                                        CommandManager.argument("value", IntegerArgumentType.integer(-1, 128)).executes(
                                                (context) -> executeRadius(context.getSource(), IntegerArgumentType.getInteger(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("doShearsBreak").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "doShearsBreak", SFramesMod.CONFIG.getData().doShearsBreak())
                                        ).
                                        then(
                                        CommandManager.argument("value", BoolArgumentType.bool()).executes(
                                                (context) -> executeDoShearsBreak(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("doLeatherFix").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "doLeatherFix", SFramesMod.CONFIG.getData().fixWithLeather())
                                        ).
                                        then(
                                        CommandManager.argument("value", BoolArgumentType.bool()).executes(
                                                (context) -> executeDoLeatherFix(context.getSource(), BoolArgumentType.getBool(context, "value")))
                                )
                        ).
                        then(
                                CommandManager.literal("clientSideGlowing").requires((source) -> source.hasPermissionLevel(3)).
                                        executes(
                                                (context) -> executeShow(context.getSource(), "clientSideGlowing", SFramesMod.CONFIG.getData().clientSideGlowing())
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
            if (commandSource.getPlayer() == null) return 0;
            ServerPlayerEntity player = commandSource.getPlayer();
            if (player.getAttachedOrElse(SFramesMod.HAS_COLOR_ATTACHMENT, false)) {
                Formatting color = player.getAttachedOrElse(SFramesMod.COLOR_ATTACHMENT, SFramesMod.CONFIG.getData().baseColor());
                strValue = color.toString() + color.getName();
            } else {
                strValue = Formatting.WHITE + "not specified";
            }
        }
        if (Objects.equals(name, "baseColor")) {
            strValue = value.toString() + ((Formatting)value).getName();
        }
        commandSource.getPlayer().sendMessage(Text.of("§6[§aSFrames§6] §2value of §b" + name + "§2 > " + strValue));
        return 1;
    }

    private static int executeColor(ServerCommandSource commandSource, Formatting color) {
        ServerPlayerEntity player = commandSource.getPlayer();
        if (player == null) return 0;
        player.setAttached(SFramesMod.COLOR_ATTACHMENT, color);
        player.setAttached(SFramesMod.HAS_COLOR_ATTACHMENT, true);

        SFramesMod.sendPacket(player, SFramesMod.generateColorPacket(player));
        executeShow(commandSource, "color", color);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeBaseColor(ServerCommandSource commandSource, Formatting color) {
        SFramesMod.CONFIG.setData(
                color,
                SFramesMod.CONFIG.getData().radiusOfGlowing(),
                SFramesMod.CONFIG.getData().clientSideGlowing(),
                SFramesMod.CONFIG.getData().doShearsBreak(),
                SFramesMod.CONFIG.getData().fixWithLeather()
        );
        SFramesMod.getTeam().setColor(color);
        executeShow(commandSource, "baseColor", color);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeRadius(ServerCommandSource commandSource, int value) {
        SFramesMod.CONFIG.setData(
                SFramesMod.CONFIG.getData().baseColor(),
                value,
                SFramesMod.CONFIG.getData().clientSideGlowing(),
                SFramesMod.CONFIG.getData().doShearsBreak(),
                SFramesMod.CONFIG.getData().fixWithLeather()
        );
        executeShow(commandSource, "radius", value);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeClientSideGlowing(ServerCommandSource commandSource, boolean value) {
        SFramesMod.CONFIG.setData(
                SFramesMod.CONFIG.getData().baseColor(),
                SFramesMod.CONFIG.getData().radiusOfGlowing(),
                value,
                SFramesMod.CONFIG.getData().doShearsBreak(),
                SFramesMod.CONFIG.getData().fixWithLeather()
        );
        executeShow(commandSource, "clientSideGlowing", value);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeDoShearsBreak(ServerCommandSource commandSource, boolean value) {
        SFramesMod.CONFIG.setData(
                SFramesMod.CONFIG.getData().baseColor(),
                SFramesMod.CONFIG.getData().radiusOfGlowing(),
                SFramesMod.CONFIG.getData().clientSideGlowing(),
                value,
                SFramesMod.CONFIG.getData().fixWithLeather()
        );
        executeShow(commandSource, "doShearsBreak", value);
        SFramesMod.CONFIG.dump();
        return 1;
    }

    private static int executeDoLeatherFix(ServerCommandSource commandSource, boolean value) {
        SFramesMod.CONFIG.setData(
                SFramesMod.CONFIG.getData().baseColor(),
                SFramesMod.CONFIG.getData().radiusOfGlowing(),
                SFramesMod.CONFIG.getData().clientSideGlowing(),
                SFramesMod.CONFIG.getData().doShearsBreak(),
                value
        );
        executeShow(commandSource, "doLeatherFix", value);
        SFramesMod.CONFIG.dump();
        return 1;
    }
}
