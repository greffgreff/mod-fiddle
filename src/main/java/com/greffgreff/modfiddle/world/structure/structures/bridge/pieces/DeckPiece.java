package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.ModFiddle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class DeckPiece extends AbstractBridgePiece {
    public final int deckLength;

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation, int deckLength) {
        super(dynamicRegistries, chunkGenerator, templateManager, position, rotation, random, poolLocation);
        this.deckLength = deckLength;
    }

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation) {
        this(dynamicRegistries, chunkGenerator, templateManager, position, rotation, random, poolLocation, 3);
    }

    @Override
    public List<StructurePiece> createPiece() {
//        for (int i = 0; i < deckLength; i++) {
//            JigsawPiece deckPiece = getRandomDeckPiece();
//            if (structurePieces.isEmpty()) {
//                structurePieces.add(createAbstractPiece(deckPiece, position, rotation));
//            }
//            else {
//                MutableBoundingBox prevPieceBB = structurePieces.get(i-1).getBoundingBox();
//                BlockPos deckPos = new BlockPos(position.getX(), position.getY(), prevPieceBB.minZ + prevPieceBB.getZSize());
//                structurePieces.add(createAbstractPiece(deckPiece, deckPos, rotation));
//            }
//        }

        for (int i = 0; i < deckLength; i++) {
            JigsawPiece deckPiece = getRandomDeckPiece();
            if (structurePieces.isEmpty()) {
                structurePieces.add(createAbstractPiece(deckPiece, position, rotation));
            }
            else {
                AbstractVillagePiece deckPiecePlaced = createAbstractPiece(deckPiece, position, rotation);
                joinJigsaws((AbstractVillagePiece) structurePieces.get(i-1), deckPiecePlaced);
                structurePieces.add(deckPiecePlaced);
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