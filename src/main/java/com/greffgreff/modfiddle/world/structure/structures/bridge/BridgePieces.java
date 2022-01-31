package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.jigsaw.*;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class BridgePieces {
    public final DynamicRegistries dynamicRegistries;
    public final MutableRegistry<JigsawPattern> jigsawPoolRegistry;
    public final ChunkGenerator chunkGenerator;
    public final TemplateManager templateManager;
    public final BlockPos startingPosition;
    public final List<StructurePiece> structurePieces;
    public final Random random;

    public BridgePieces(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, List<StructurePiece> structurePieces, Random random) {
        this.dynamicRegistries = dynamicRegistries;
        this.jigsawPoolRegistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManager;
        this.startingPosition = startingPosition;
        this.structurePieces = structurePieces;
        this.random = random;

        ModFiddle.LOGGER.debug("Components: " + structurePieces.size());
    }

    public void generateBridge() {
        DeckPiece deckPiece = new DeckPiece();
        deckPiece.createPiece();
    }

    private JigsawPattern getPool(ResourceLocation resourceLocation) {
        Supplier<JigsawPattern> piecesPool = () -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(resourceLocation);
        return piecesPool.get();
    }

    private JigsawPiece getRandomJigsawPiece(List<JigsawPiece> candidatePieces) {
        int randomInt = ThreadLocalRandom.current().nextInt(0, candidatePieces.size());
        return candidatePieces.get(randomInt);
    }

    private static String getPieceName(JigsawPiece jigsawPiece) {
        if (jigsawPiece == null) return "";
        SingleJigsawPiece singleJigsawPiece = (SingleJigsawPiece) jigsawPiece;
        return singleJigsawPiece.field_236839_c_.left().isPresent() ? singleJigsawPiece.field_236839_c_.left().get().getPath() : "";
    }

    private AbstractVillagePiece createAbstractPiece(JigsawPiece piece, BlockPos position, Rotation rotation) {
        return new AbstractVillagePiece(templateManager, piece, position, piece.getGroundLevelDelta(), rotation, piece.getBoundingBox(templateManager, position, rotation));
    }

    private boolean validateJigsawBlockInfo(Template.BlockInfo originJigsawBlock) {
        ResourceLocation originJigsawBlockPool = new ResourceLocation(originJigsawBlock.nbt.getString("pool"));
        Optional<JigsawPattern> originJigsawBlockTargetPool = jigsawPoolRegistry.getOptional(originJigsawBlockPool);

        if (!(originJigsawBlockTargetPool.isPresent() && (originJigsawBlockTargetPool.get().getNumberOfPieces() != 0 || Objects.equals(originJigsawBlockPool, JigsawPatternRegistry.field_244091_a.getLocation())))) {
            ModFiddle.LOGGER.warn("Empty or nonexistent pool: {}", originJigsawBlockPool);
            return false;
        }

        ResourceLocation originJigsawBlockFallback = originJigsawBlockTargetPool.get().getFallback();
        Optional<JigsawPattern> originJigsawBlockFallbackPool = jigsawPoolRegistry.getOptional(originJigsawBlockFallback);

        if (!(originJigsawBlockFallbackPool.isPresent() && (originJigsawBlockFallbackPool.get().getNumberOfPieces() != 0 || Objects.equals(originJigsawBlockFallback, JigsawPatternRegistry.field_244091_a.getLocation())))) {
            ModFiddle.LOGGER.warn("Empty or nonexistent fallback pool: {}", originJigsawBlockFallback);
            return false;
        }

        return true;
    }

    private class DeckPiece {
        public static final int minBridgeDeckLength = 3;
        public static final int maxBridgeDeckLength = 5;
        public final Rotation rotation = Rotation.randomRotation(random);
        public final JigsawPattern bridgePool = getPool(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge"));

        public void createPiece() {
            JigsawPiece initialDeckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));
            AbstractVillagePiece initialDeckPiecePlaced = createAbstractPiece(initialDeckPiece, startingPosition, Rotation.NONE);
            structurePieces.add(initialDeckPiecePlaced);

            List<Pair<JigsawPiece, Integer>> jigsawPieces = bridgePool.rawTemplates;
            jigsawPieces.forEach(x -> ModFiddle.LOGGER.debug("Piece: " + getPieceName(x.getFirst()) + " Integer: " + x.getSecond()));

            MutableBoundingBox initialDeckPieceBB = initialDeckPiecePlaced.getBoundingBox();

            BlockPos secondPiecePos = new BlockPos(startingPosition.getX(), startingPosition.getY(), startingPosition.getZ() + initialDeckPieceBB.getZSize());
            JigsawPiece secondDeckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));
            AbstractVillagePiece secondDeckPiecePlaced = createAbstractPiece(secondDeckPiece, secondPiecePos, Rotation.NONE);
            structurePieces.add(secondDeckPiecePlaced);

            BlockPos thirdPiecePos = new BlockPos(startingPosition.getX(), startingPosition.getY(), startingPosition.getZ() + initialDeckPieceBB.getZSize() * 2);
            JigsawPiece thirdDeckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));
            AbstractVillagePiece thirdDeckPiecePlaced = createAbstractPiece(thirdDeckPiece, thirdPiecePos, Rotation.NONE);
            structurePieces.add(thirdDeckPiecePlaced);
        }

        private JigsawPiece getRandomDeckPiece(List<JigsawPiece> jigsawPieces) {
            JigsawPiece deckPiece;
            do {
                deckPiece = getRandomJigsawPiece(jigsawPieces);
                ModFiddle.LOGGER.debug(getPieceName(deckPiece));
            } while (!getPieceName(deckPiece).contains("walk"));
            return deckPiece;
        }
    }
}
