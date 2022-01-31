package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.JigsawManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class BridgeStructure extends Structure<NoFeatureConfig> {
    public BridgeStructure() {
        super(NoFeatureConfig.field_236558_a_);
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return Start::new;
    }

    @Override
    public GenerationStage.Decoration getDecorationStage() {
        return GenerationStage.Decoration.SURFACE_STRUCTURES;
    }

    @Override
    // isFeatureChunk
    protected boolean func_230363_a_(ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, long seed, SharedSeedRandom sharedSeedRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos, NoFeatureConfig noFeatureConfig) {
        return true;
    }

    public static class Start extends StructureStart<NoFeatureConfig> {
        public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override
        // generatePieces
        public void func_230364_a_(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, int chunkX, int chunkZ, Biome biome, NoFeatureConfig noFeatureConfig) {
            int x = chunkX * 16;
            int z = chunkZ * 16;
            int submergedBaseOffset = -6;
            BlockPos centerPos = new BlockPos(x, submergedBaseOffset, z);
            Rotation rotation = Rotation.randomRotation(rand);

            JigsawManager.assembleJigsawStructure(
                dynamicRegistries,
                new VillageConfig(() -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge_start")), 5),
                chunkGenerator,
                templateManager,
                centerPos,
                this.components,
                this.rand,
                true
            );

            this.recalculateStructureSize();
        }
    }
}
