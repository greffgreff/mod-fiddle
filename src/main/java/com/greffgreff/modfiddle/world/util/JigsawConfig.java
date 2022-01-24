package com.greffgreff.modfiddle.world.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;

import java.util.function.Supplier;

public class JigsawConfig implements IFeatureConfig {
    public static final Codec<JigsawConfig> CODEC = RecordCodecBuilder.create((codecBuilder) -> codecBuilder
        .group(
                JigsawPattern.field_244392_b_.fieldOf("start_pool").forGetter(JigsawConfig::getStartPoolSupplier),
                Codec.intRange(0, 10).fieldOf("size").forGetter(JigsawConfig::getMaxChainPieceLength))
        .apply(
                codecBuilder,
                JigsawConfig::new
        ));

    private ResourceLocation resourceLocation = null;
    private Supplier<JigsawPattern> startPoolSupplier = null;
    private int size = 0;

    public JigsawConfig(Supplier<JigsawPattern> startPoolSupplier, int size, ResourceLocation resourceLocation) {
        new JigsawConfig(startPoolSupplier, size);
        this.resourceLocation = resourceLocation;
    }

    public JigsawConfig(Supplier<JigsawPattern> startPoolSupplier, int size) {
        this.startPoolSupplier = startPoolSupplier;
        this.size = size;
    }

    public int getMaxChainPieceLength() {
        return this.size;
    }

    public Supplier<JigsawPattern> getStartPoolSupplier() {
        return this.startPoolSupplier;
    }

    public ResourceLocation getResourceLocation() { return this.resourceLocation; }
}