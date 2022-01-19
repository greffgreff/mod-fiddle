package com.greffgreff.modfiddle.world.gen;

import com.greffgreff.modfiddle.init.StructureInit;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ModStructureGeneration {
    public static void generateStructures(final BiomeLoadingEvent event) {
        // getting biomes registry key from event
        RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName());
        // getting biome types as set from event
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);

        if (types.contains(BiomeDictionary.Type.PLAINS)) {
            // add structure if PLAINS
            List<Supplier<StructureFeature<?, ?>>> structures = event.getGeneration().getStructures();
            structures.add(() -> StructureInit.TOTEM_STRUCTURE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        }
    }
}
