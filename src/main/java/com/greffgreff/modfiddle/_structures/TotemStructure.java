package com.greffgreff.modfiddle._structures;

import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;

public class TotemStructure extends Structure<NoFeatureConfig> {

    public TotemStructure() {
        super(NoFeatureConfig.field_236558_a_);
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return null;
    }
}
