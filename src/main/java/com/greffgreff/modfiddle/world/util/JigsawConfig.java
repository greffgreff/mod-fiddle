package com.greffgreff.modfiddle.world.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;

import java.util.function.Supplier;

public class JigsawConfig  {
    private final JigsawConfigCodec Config;
    private final ResourceLocation resourceLocation;

    public JigsawConfig(ResourceLocation resourceLocation, Supplier<JigsawPattern> startPool, int size) {
        this.resourceLocation = resourceLocation;
        this.Config = new JigsawConfigCodec(startPool, size);
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public static class JigsawConfigCodec implements  IFeatureConfig{
        public static final Codec<JigsawConfigCodec> JIGSAW_CONFIG_CODEC = RecordCodecBuilder.create(instance ->  {
            return instance.group(
                    JigsawPattern.field_244392_b_.fieldOf("start_pool").forGetter(JigsawConfigCodec::getStartPool),
                    Codec.intRange(1,10).fieldOf("size").forGetter(JigsawConfigCodec::getMaxBoundSize)
            ).apply(instance, JigsawConfigCodec::new);
        });

        private final Supplier<JigsawPattern> startPool;
        private final int size;

        public JigsawConfigCodec(Supplier<JigsawPattern> startPool, int size) {
            this.startPool = startPool;
            this.size = size;
        }

        public Supplier<JigsawPattern> getStartPool() {
            return this.startPool;
        }

        public int getMaxBoundSize() {
            return this.size;
        }
    }
}