package com.greffgreff.modfiddle.world.structure;

import net.minecraftforge.common.ForgeConfigSpec;

public class StructureConfigs {
    public static ForgeConfigSpec.ConfigValue<Integer> maxBridgeSize = null;

    public StructureConfigs(final ForgeConfigSpec.Builder builder) {
        builder
            .comment(
                    "###############################\n" +
                    "## Bridge Structure Settings ##\n" +
                    "###############################\n"
            ).push("Bridge");

        maxBridgeSize = builder
            .comment("Maximum size of a bridge.")
            .worldRestart()
            .define("Max Bridge Size", 10);

        builder.pop();
    }
}