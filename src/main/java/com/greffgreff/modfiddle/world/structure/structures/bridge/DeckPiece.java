package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeckPiece extends BridgePiece {
    public final WeightedItems<JigsawPiece> weightedDeckPieces = new WeightedItems<>(random);
    public final JigsawPattern deckPool = getPool(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge"));
    public final List<StructurePiece> deckPieces = new ArrayList<>();
    public final int deckLength;

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, List<StructurePiece> structurePieces, Random random, int deckLength) {
        super(dynamicRegistries, chunkGenerator, templateManager, startingPosition, structurePieces, random);
        deckPool.rawTemplates.forEach(p -> weightedDeckPieces.add(p.getSecond().doubleValue(), p.getFirst()));
        this.deckLength = deckLength;
    }

    public DeckPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, List<StructurePiece> structurePieces, Random random) {
        this(dynamicRegistries, chunkGenerator, templateManager, startingPosition, structurePieces, random, 3);
    }

    public void createPiece() {
        for (int i = 0; i < deckLength; i++) {
            JigsawPiece deckPiece = getRandomDeckPiece();
            if (deckPieces.isEmpty()) {
                deckPieces.add(createAbstractPiece(deckPiece, startingPosition, Rotation.NONE));
            }
            else {
                MutableBoundingBox prevPieceBB = deckPieces.get(i-1).getBoundingBox();
                BlockPos deckPos = new BlockPos(startingPosition.getX(), startingPosition.getY(), prevPieceBB.minZ + prevPieceBB.getZSize());
                deckPieces.add(createAbstractPiece(deckPiece, deckPos, Rotation.NONE));
            }
        }
        structurePieces.addAll(deckPieces);
    }

    private JigsawPiece getRandomDeckPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedDeckPieces.next();
        } while (!getPieceName(deckPiece).contains("walk"));
        return deckPiece;
    }
}