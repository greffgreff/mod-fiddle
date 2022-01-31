package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.*;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.lwjgl.system.CallbackI;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class BridgePieces {
    public final DynamicRegistries dynamicRegistries;
    public final MutableRegistry<JigsawPattern> jigsawPoolRegistry;
    public final ChunkGenerator chunkGenerator;
    public final TemplateManager templateManager;
    public final BlockPos startingPosition;
    public final Random random;

    public BridgePieces(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, Random random) {
        this.dynamicRegistries = dynamicRegistries;
        this.jigsawPoolRegistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManager;
        this.startingPosition = startingPosition;
        this.random = random;
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

    private static AbstractVillagePiece createAbstractPiece(TemplateManager templateManager, JigsawPiece piece, BlockPos position, Rotation rotation) {
        return new AbstractVillagePiece(templateManager, piece, position, piece.getGroundLevelDelta(), rotation, piece.getBoundingBox(templateManager, position, rotation));
    }

    private boolean validateJigsawBlockInfo(Template.BlockInfo jigsawBlock) {
        ResourceLocation jigsawBlockPool = new ResourceLocation(jigsawBlock.nbt.getString("pool"));
        Optional<JigsawPattern> jigsawBlockTargetPool = jigsawPoolRegistry.getOptional(jigsawBlockPool);

        if (!(jigsawBlockTargetPool.isPresent() && (jigsawBlockTargetPool.get().getNumberOfPieces() != 0 || Objects.equals(jigsawBlockPool, JigsawPatternRegistry.field_244091_a.getLocation())))) {
            ModFiddle.LOGGER.warn("Empty or nonexistent pool: {}", jigsawBlockPool);
            return false;
        }

        ResourceLocation jigsawBlockFallback = jigsawBlockTargetPool.get().getFallback();
        Optional<JigsawPattern> jigsawBlockFallbackPool = jigsawPoolRegistry.getOptional(jigsawBlockFallback);

        if (!(jigsawBlockFallbackPool.isPresent() && (jigsawBlockFallbackPool.get().getNumberOfPieces() != 0 || Objects.equals(jigsawBlockFallback, JigsawPatternRegistry.field_244091_a.getLocation())))) {
            ModFiddle.LOGGER.warn("Empty or nonexistent fallback pool: {}", jigsawBlockFallback);
            return false;
        }

        return true;
    }

    private class DeckPiece {
        public static final int minBridgeDeckLength = 3;
        public static final int maxBridgeDeckLength = 5;
        public final Rotation rotation = Rotation.randomRotation(random);
        public final JigsawPattern bridgePool = getPool(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge"));
        public final List<? super AbstractVillagePiece> structurePieces = new ArrayList<>();

        public void createPiece() {
            // Adding random deck piece as deck origin
            JigsawPiece initialDeckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));
            AbstractVillagePiece initialDeckPiecePlaced = createAbstractPiece(templateManager, initialDeckPiece, startingPosition, rotation);
            structurePieces.add(initialDeckPiecePlaced);

            for (int i = 0; i < minBridgeDeckLength - 1; i++) {
                for (Template.BlockInfo jigsawBlock: initialDeckPiece.getJigsawBlocks(templateManager, startingPosition, rotation, random)) {
                    if (validateJigsawBlockInfo(jigsawBlock)) continue;

                    JigsawPiece deckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));
                }
            }
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
