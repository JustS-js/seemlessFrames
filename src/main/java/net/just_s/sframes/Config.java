package net.just_s.sframes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;

import java.io.*;
import java.util.Map;
import java.util.UUID;

public class Config {
    private final File configFile = FabricLoader.getInstance().getConfigDir().resolve("SeamlessFrames.json").toFile();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ConfigRecord data = ConfigRecord.DEFAULT;

    public ConfigRecord getData() {
        return data;
    }

    public void setData( Formatting baseColor,
                         int radiusOfGlowing,
                         boolean clientSideGlowing,
                         boolean doShearsBreak,
                         boolean fixWithLeather) {
        data = new ConfigRecord(
                baseColor,
                radiusOfGlowing,
                clientSideGlowing,
                doShearsBreak,
                fixWithLeather,
                data.invisibleItemFrameName(),
                data.invisibleGlowItemFrameName()
        );
    }

    public void load() {
        try (FileReader f = new FileReader(configFile)) {
            data = ConfigRecord.CODEC
                    .decode(JsonOps.INSTANCE, JsonParser.parseReader(f))
                    .getOrThrow()
                    .getFirst();
        }
        catch (FileNotFoundException ignored) {
            dump();
        }
        catch (Exception e) {
            SFramesMod.LOGGER.warn("Could not load config. ", e);
        }
    }

    public void dump() {
        JsonElement json = ConfigRecord.CODEC
                .encode(getData(), JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
                .getOrThrow();
        try (FileWriter w = new FileWriter(configFile)) {
            gson.toJson(json, w);
        } catch (IOException e) {
            SFramesMod.LOGGER.warn("Could not dump config. ", e);
        }
    }
}
