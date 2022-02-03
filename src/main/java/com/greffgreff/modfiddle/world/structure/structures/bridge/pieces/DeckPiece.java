package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.AbstractBridgePiece;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeckPiece extends AbstractBridgePiece {
    public final int deckLength;

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, Random random, ResourceLocation poolLocation, int deckLength) {
        super(dynamicRegistries, chunkGenerator, templateManager, startingPosition, random, poolLocation);
        this.deckLength = deckLength;
    }

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, Random random, ResourceLocation poolLocation) {
        this(dynamicRegistries, chunkGenerator, templateManager, startingPosition, random, poolLocation, 3);
    }

    @Override
    public List<StructurePiece> createPiece() {
        for (int i = 0; i < deckLength; i++) {
            JigsawPiece deckPiece = getRandomDeckPiece();
            if (structurePieces.isEmpty()) {
                structurePieces.add(createAbstractPiece(deckPiece, startingPosition, Rotation.NONE));
            }
            else {
                MutableBoundingBox prevPieceBB = structurePieces.get(i-1).getBoundingBox();
                BlockPos deckPos = new BlockPos(startingPosition.getX(), startingPosition.getY(), prevPieceBB.minZ + prevPieceBB.getZSize());
                structurePieces.add(createAbstractPiece(deckPiece, deckPos, Rotation.NONE));
            }
        }

        return structurePieces;
    }

    private JigsawPiece getRandomDeckPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedPieces.next();
        } while (!getPieceName(deckPiece).contains("walk"));
        return deckPiece;
    }
}