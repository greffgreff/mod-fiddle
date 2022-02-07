package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.Jigsaws;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.Random;

public class DeckPiece extends AbstractBridgePiece {
    public int deckLength;

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation, int deckLength) {
        super(dynamicRegistries, chunkGenerator, templateManager, position, rotation, random, poolLocation);
        this.deckLength = deckLength;
    }

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Random random, ResourceLocation poolLocation) {
        this(dynamicRegistries, chunkGenerator, templateManager, position, Rotation.NONE, random, poolLocation, 3);
    }

    @Override
    public AbstractBridgePiece buildPiece() {
        deckLength=3; // fix this

        structurePieces = new ArrayList<>();
        for (int i = 0; i < deckLength; i++) {
            JigsawPiece deckPiece = getRandomDeckPiece();
            if (structurePieces.isEmpty()) {
                structurePieces.add(Jigsaws.createAbstractPiece(deckPiece, position, rotation, templateManager));
            }
            else {
                AbstractVillagePiece deckPiecePlaced = Jigsaws.createAbstractPiece(deckPiece, position, rotation, templateManager);
                joinJigsaws(structurePieces.get(i-1), deckPiecePlaced);
                structurePieces.add(deckPiecePlaced);
            }
        }
        ModFiddle.LOGGER.debug("Actual BB:       " + this.getBoundingBox().minX + " " + this.getBoundingBox().minY + " " + this.getBoundingBox().minZ + "    " + this.getBoundingBox().maxX + " " + this.getBoundingBox().maxY + " " + this.getBoundingBox().maxZ);
        return this;
    }

    private JigsawPiece getRandomDeckPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedPieces.next();
        } while (!Jigsaws.getPieceName(deckPiece).contains("walk"));
        return deckPiece;
    }
}