package net.just_s.sframes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Formatting;

public record ConfigRecord(
        Formatting baseColor,
        int radiusOfGlowing,
        boolean clientSideGlowing,
        boolean doShearsBreak,
        boolean fixWithLeather,
        String invisibleItemFrameName,
        String invisibleGlowItemFrameName
) {
    public static final Codec<ConfigRecord> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Formatting.CODEC.fieldOf("baseColor").forGetter(ConfigRecord::baseColor),
                    Codec.INT.fieldOf("radiusOfGlowing").forGetter(ConfigRecord::radiusOfGlowing),
                    Codec.BOOL.fieldOf("clientSideGlowing").forGetter(ConfigRecord::clientSideGlowing),
                    Codec.BOOL.fieldOf("doShearsBreak").forGetter(ConfigRecord::doShearsBreak),
                    Codec.BOOL.fieldOf("fixWithLeather").forGetter(ConfigRecord::fixWithLeather),
                    Codec.STRING.fieldOf("invisibleItemFrameName").forGetter(ConfigRecord::invisibleItemFrameName),
                    Codec.STRING.fieldOf("invisibleGlowItemFrameName").forGetter(ConfigRecord::invisibleGlowItemFrameName)
            ).apply(instance, ConfigRecord::new)
    );
    public static final ConfigRecord DEFAULT = new ConfigRecord(
            Formatting.WHITE,
            -1,
            true,
            true,
            true,
            "Invisible Item Frame",
            "Invisible Glow Item Frame"
    );

}
