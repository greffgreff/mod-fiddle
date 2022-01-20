package com.greffgreff.modfiddle.world.structure.structures;

import com.greffgreff.modfiddle.ModFiddle;
import net.minecraft.util.ResourceLocation;
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
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.Level;

public class TotemStructure extends Structure<NoFeatureConfig> {
    public TotemStructure() {
        super(NoFeatureConfig.field_236558_a_);
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return TotemStructure.Start::new;
    }

    @Override
    public GenerationStage.Decoration getDecorationStage() {
        return GenerationStage.Decoration.SURFACE_STRUCTURES;
    }

    @Override // can be generated?
    protected boolean func_230363_a_(ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, long seed, SharedSeedRandom sharedSeedRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos, NoFeatureConfig noFeatureConfig) {
        return true;
    }

    public static class Start extends StructureStart<NoFeatureConfig> {
        // structureIn     chunkX         chunkZ         mutableBoundingBox     referenceIn    seedIn
        // p_i225876_1_    p_i225876_2_   p_i225876_3_   p_i225876_4_           p_i225876_5_   p_i225876_6_

        public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
            super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
        }

        @Override // generatePieces
        public void func_230364_a_(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, int chunkX, int chunkZ, Biome biome, NoFeatureConfig noFeatureConfig) {
            int x = chunkX * 16;
            int z = chunkZ * 16;
            BlockPos centerPos = new BlockPos(x, 0, z);

            // addPieces
            JigsawManager.func_242837_a(
                    dynamicRegistries,
                    new VillageConfig(() -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(new ResourceLocation(ModFiddle.MOD_ID, "totem/start_pool")), 10),
                    AbstractVillagePiece::new,
                    chunkGenerator,
                    templateManager,
                    centerPos,
                    this.components,
                    this.rand,
                    false,
                    true);

            int heightOffset = (int) (Math.random() * 5);
            ModFiddle.LOGGER.debug(heightOffset);
            this.components.forEach(piece -> piece.offset(0, heightOffset, 0));

            this.recalculateStructureSize();

//            ModFiddle.LOGGER.log(Level.DEBUG, "Generated one at " +
//                    this.components.get(0).getBoundingBox().minX + " " +
//                    this.components.get(0).getBoundingBox().minY + " " +
//                    this.components.get(0).getBoundingBox().minZ);
        }
    }
}
