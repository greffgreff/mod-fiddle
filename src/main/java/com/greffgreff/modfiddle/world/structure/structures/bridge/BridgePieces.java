package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.WeightedItems;
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
    }

    public void generateBridge() {
        DeckPiece deckPiece = new DeckPiece();
        deckPiece.createPiece();
    }

    private JigsawPattern getPool(ResourceLocation resourceLocation) {
        Supplier<JigsawPattern> piecesPool = () -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(resourceLocation);
        return piecesPool.get();
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
        public final int minDeckPieces = 3;
        public final JigsawPattern deckPool = getPool(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge"));
        public final WeightedItems<JigsawPiece> weightedDeckPieces = new WeightedItems<>(random);
        public final List<StructurePiece> deckPieces = new ArrayList<>();

        public DeckPiece() {
            deckPool.rawTemplates.forEach(p -> weightedDeckPieces.add(p.getSecond().doubleValue(), p.getFirst()));
        }

        public void createPiece() {
            for (int i = 0; i < minDeckPieces; i++) {
                JigsawPiece deckPiece = getRandomDeckPiece();
                ModFiddle.LOGGER.debug("Step: " + i);
                if (deckPieces.isEmpty()) {
                    deckPieces.add(createAbstractPiece(deckPiece, startingPosition, Rotation.NONE));
                }
                else {
                    MutableBoundingBox prevPieceBB = deckPieces.get(i-1).getBoundingBox();
                    ModFiddle.LOGGER.debug("Prev BB: " + deckPieces.get(i-1).getBoundingBox());
                    BlockPos deckPos = new BlockPos(startingPosition.getX(), startingPosition.getY(), prevPieceBB.minZ + prevPieceBB.getZSize());
                    deckPieces.add(createAbstractPiece(deckPiece, deckPos, Rotation.NONE));
                }
                ModFiddle.LOGGER.debug("BB: " + deckPieces.get(i).getBoundingBox());
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
}
