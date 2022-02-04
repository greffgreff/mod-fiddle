package com.greffgreff.modfiddle.world.gen;

import com.greffgreff.modfiddle.world.structure.ModStructures;
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
        RegistryKey<Biome> key = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName());
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);

        List<Supplier<StructureFeature<?, ?>>> structures = event.getGeneration().getStructures();

        if (types.contains(BiomeDictionary.Type.PLAINS)) {
            structures.add(() -> ModStructures.TOTEM_STRUCTURE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        }

        if (types.contains(BiomeDictionary.Type.OCEAN)) {
            structures.add(() -> ModStructures.BRIDGE_STRUCTURE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        }

        event.setClimate(new Biome.Climate(Biome.RainType.SNOW, -9999F, Biome.TemperatureModifier.FROZEN, 9999F));
        event.setCategory(Biome.Category.ICY);
    }
}
