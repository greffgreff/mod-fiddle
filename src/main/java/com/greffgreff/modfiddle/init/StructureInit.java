package com.greffgreff.modfiddle.init;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.structure.structures.TotemStructure;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureInit {
    // Setup
    public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, ModFiddle.MOD_ID);

    // Structures
    public static final RegistryObject<Structure<NoFeatureConfig>> TOTEM_STRUCTURE = STRUCTURES.register("totem", TotemStructure::new);
}
