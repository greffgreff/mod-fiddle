package com.greffgreff.modfiddle.world.structure.structures.bridge;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import com.mojang.bridge.Bridge;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TowerPiece extends BridgePiece {
    public final WeightedItems<JigsawPiece> weightedDeckPieces = new WeightedItems<>(random);
    public final JigsawPattern towerPool = getPool(new ResourceLocation(ModFiddle.MOD_ID, "bridge/bridge"));
    public final List<StructurePiece> towerPieces = new ArrayList<>();

    public TowerPiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, List<StructurePiece> structurePieces, Random random) {
        super(dynamicRegistries, chunkGenerator, templateManager, startingPosition, structurePieces, random);
        towerPool.rawTemplates.forEach(p -> weightedDeckPieces.add(p.getSecond().doubleValue(), p.getFirst()));
    }

    public void createPiece() {
        JigsawPiece towerSpine = getRandomPillarSpinePiece();
        JigsawPiece towerHead = getRandomPillarHeadPiece();
        AbstractVillagePiece towerSpinePlaced = createAbstractPiece(towerSpine, startingPosition, Rotation.NONE);
        AbstractVillagePiece towerHeadPlaced = createAbstractPiece(towerHead, startingPosition, Rotation.NONE);;

        for (Template.BlockInfo towerSpineJigsawBlock : towerSpine.getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.NONE, random)) {
            for (Template.BlockInfo towerHeadJigsawBlock: towerHead.getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.NONE, random)) {
                if (JigsawBlock.hasJigsawMatch(towerSpineJigsawBlock, towerHeadJigsawBlock)) {
                    int xDelta = towerSpineJigsawBlock.pos.getX() - towerHeadJigsawBlock.pos.getX();
                    int yDelta = towerSpineJigsawBlock.pos.getY() - towerHeadJigsawBlock.pos.getY();
                    int zDelta = towerSpineJigsawBlock.pos.getZ() - towerHeadJigsawBlock.pos.getZ();
                    towerHeadPlaced.offset(xDelta, yDelta, zDelta);
                }
            }
        }

        towerPieces.add(towerSpinePlaced);
        towerPieces.add(towerHeadPlaced);
        structurePieces.addAll(towerPieces);

        MutableBoundingBox towerBB = towerHeadPlaced.getBoundingBox();
        for (int x = 0; x < towerBB.getXSize(); x++) {
            for (int z = 0; z < towerBB.getZSize(); z++) {
                int terrainFloor = chunkGenerator.getNoiseHeight(x + towerSpinePlaced.getPos().getX(), z + towerSpinePlaced.getPos().getZ(), Heightmap.Type.OCEAN_FLOOR_WG);
                int groundDelta = towerBB.minY - terrainFloor;
            }
        }
    }

    private JigsawPiece getRandomPillarSpinePiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedDeckPieces.next();
        } while (!getPieceName(deckPiece).contains("pillarspine"));
        return deckPiece;
    }

    private JigsawPiece getRandomPillarHeadPiece() {
        JigsawPiece deckPiece;
        do {
            deckPiece = weightedDeckPieces.next();
        } while (!getPieceName(deckPiece).contains("pillarhead"));
        return deckPiece;
    }
}