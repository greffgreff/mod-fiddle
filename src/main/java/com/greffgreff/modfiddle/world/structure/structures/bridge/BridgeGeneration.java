package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.AbstractBridgePiece;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.DeckPiece;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.TowerPiece;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class BridgeGeneration {

    public static void generateBridge(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos centerPos, List<StructurePiece> structurePieces, Random random) {
        ResourceLocation loc = new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge");
        TowerPiece tower = new TowerPiece(dynamicRegistries, chunkGenerator, templateManager, centerPos, random, loc);
        DeckPiece deck = new DeckPiece(dynamicRegistries, chunkGenerator, templateManager, centerPos, random, loc);

        List<StructurePiece> towerPieces = tower.createPiece();
        List<StructurePiece> deckPieces = deck.createPiece();

        AbstractBridgePiece.joinJigsaws(tower, deck, random, templateManager);

        structurePieces.addAll(towerPieces);
        structurePieces.addAll(deckPieces);
    }
}
