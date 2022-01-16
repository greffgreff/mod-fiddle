package com.greffgreff.modfiddle.init;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle._structures.TotemStructure;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureInit {
    // Setup
    public static final DeferredRegister<Structure<?>> STRUCTURE_FEATURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, ModFiddle.MOD_ID);

    public static RegistryObject<Structure<NoFeatureConfig>> registerStructure(String structureName, Structure<NoFeatureConfig> structure) {
        return STRUCTURE_FEATURES.register(structureName, () -> structure);
    }

    // Structures
    public static final RegistryObject<Structure<NoFeatureConfig>> TOTEM_STRUCTURE = STRUCTURE_FEATURES.register("totem", () -> new TotemStructure());
}
