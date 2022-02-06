package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

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
    public AbstractBridgePiece createPiece() {
        for (int i = 0; i < deckLength; i++) {
            JigsawPiece deckPiece = getRandomDeckPiece();
            if (placedStructurePieces.isEmpty()) {
                placedStructurePieces.add(Jigsaws.createAbstractPiece(deckPiece, position, rotation, templateManager));
            }
            else {
                AbstractVillagePiece deckPiecePlaced = Jigsaws.createAbstractPiece(deckPiece, position, rotation, templateManager);
                joinJigsaws(placedStructurePieces.get(i-1), deckPiecePlaced);
                placedStructurePieces.add(deckPiecePlaced);
            }
        }
        return this;
    }

    @Override
    protected List<JigsawPiece> fetchPieces() {
        List<JigsawPiece> pieces = new ArrayList<>();
        for (int i = 0; i < deckLength; i++)
            pieces.add(getRandomDeckPiece());
        return pieces;
    }

    private JigsawPiece getRandomDeckPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedPieces.next();
        } while (!Jigsaws.getPieceName(deckPiece).contains("walk"));
        return deckPiece;
    }
}