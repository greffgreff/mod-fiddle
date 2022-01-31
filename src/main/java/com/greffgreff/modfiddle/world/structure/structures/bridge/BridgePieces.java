package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.EmptyJigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class BridgePieces {
    public final DynamicRegistries dynamicRegistries;
    public final ChunkGenerator chunkGenerator;
    public final TemplateManager templateManager;
    public final BlockPos startingPosition;
    public final Random random;

    public BridgePieces(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, Random random) {
        this.dynamicRegistries = dynamicRegistries;
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

    private class DeckPiece {
        public static final int minBridgeDeckLength = 3;
        public static final int maxBridgeDeckLength = 5;
        public final Rotation rotation = Rotation.randomRotation(random);
        public final JigsawPattern bridgePool = getPool(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge"));
        public final List<? super AbstractVillagePiece> structurePieces = new ArrayList<>();

        public void createPiece() {
            // Adding random deck piece as deck origin since empty
            JigsawPiece initialDeckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));
            AbstractVillagePiece initialDeckPiecePlaced = createAbstractPiece(templateManager, initialDeckPiece, startingPosition, rotation);
            structurePieces.add(initialDeckPiecePlaced);

            for (int i = 0; i < minBridgeDeckLength - 1; i++) {
                JigsawPiece deckPiece = getRandomDeckPiece(bridgePool.getShuffledPieces(random));


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
