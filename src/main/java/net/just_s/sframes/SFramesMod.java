package net.just_s.sframes;

import com.google.common.collect.Lists;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.just_s.sframes.mixin.AccessDataTrackerEntries;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SFramesMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("sframes");
	public static final Config CONFIG = new Config();
	public static final String TEAM_NAME = "SeamlessFrames";

	@Override
	public void onInitialize() {
		LOGGER.info("Seamless Frames loaded successfully!");
		CONFIG.load();
		CommandRegistrationCallback.EVENT.register(FrameCommand::register);
		ServerLifecycleEvents.SERVER_STARTED.register(
				server -> {
					Team team = server.getScoreboard().getTeam(TEAM_NAME);
					if (team == null) {
						team = server.getScoreboard().addTeam(TEAM_NAME);
					}
					team.setColor(Formatting.byName(SFramesMod.CONFIG.outlineColor));
				}
		);
	}

	public static void sendPackets(List<ServerPlayerEntity> players, Packet<?> packet) {
		for (ServerPlayerEntity player : players) {
			sendPackets(player, packet);
		}
	}

	public static void sendPackets(ServerPlayerEntity player, Packet<?> packet) {
		player.networkHandler.sendPacket(packet);
	}

	public static boolean shouldGlow(ItemFrameEntity frame) {
		return frame.getCommandTags().contains("invisibleframe") && frame.getHeldItemStack().isEmpty() && CONFIG.radiusOfGlowing > -1;
	}

	public static EntityTrackerUpdateS2CPacket generateGlowPacket(ItemFrameEntity frame, boolean shouldGlow) {
		DataTracker tracker = frame.getDataTracker();
		List<DataTracker.Entry<?>> trackedValues = getAllEntries(tracker);
		List<DataTracker.SerializedEntry<?>> serializedEntryList = new ArrayList<>();
		boolean wasModified = false;
		for (DataTracker.Entry<?> entry : trackedValues) {
			if (entry.get().getClass() == Byte.class && !wasModified) {
				DataTracker.Entry<Byte> byteEntry = (DataTracker.Entry<Byte>) entry;
				if (shouldGlow)
					byteEntry.set((byte) ((byte)entry.get() | 1 << 6));
				else
					byteEntry.set((byte) ((byte)entry.get() & ~(1 << 6)));
				wasModified = true;
			}
			serializedEntryList.add(entry.toSerialized());
		}
		//LOGGER.info(serializedEntryList.toString());

		return new EntityTrackerUpdateS2CPacket(frame.getId(), serializedEntryList);
	}

	@Nullable
	private static List<DataTracker.Entry<?>> getAllEntries(DataTracker tracker) {
		List<DataTracker.Entry<?>> list = Lists.newArrayList();

		for (DataTracker.Entry entry : ((AccessDataTrackerEntries)tracker).getEntries()) {
			list.add(new DataTracker.Entry(entry.getData(), entry.getData().dataType().copy(entry.get())));
		}

		return list.isEmpty() ? null : list;
	}
}
