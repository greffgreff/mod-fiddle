package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.world.util.Jigsaws;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public abstract class AbstractBridgePiece extends JigsawPiece {
    public final DynamicRegistries dynamicRegistries;
    public final MutableRegistry<JigsawPattern> jigsawPoolRegistry;
    public final ChunkGenerator chunkGenerator;
    public final TemplateManager templateManager;
    protected final List<AbstractVillagePiece> placedStructurePieces = new ArrayList<>();
    protected final List<JigsawPiece> structurePieces;
    public BlockPos position;
    public Rotation rotation;
    public final Random random;
    protected final WeightedItems<JigsawPiece> weightedPieces = new WeightedItems<>();
    protected final JigsawPattern piecePool;

    public AbstractBridgePiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation) {
        super(JigsawPattern.PlacementBehaviour.RIGID);
        this.dynamicRegistries = dynamicRegistries;
        this.jigsawPoolRegistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManager;
        this.position = position;
        this.rotation = rotation;
        this.random = random;
        piecePool = Jigsaws.getPool(poolLocation, dynamicRegistries);
        piecePool.rawTemplates.forEach(p -> weightedPieces.add(p.getSecond().doubleValue(), p.getFirst()));
        structurePieces = fetchPieces();
    }

    protected abstract AbstractBridgePiece createPiece();

    protected abstract List<JigsawPiece> fetchPieces();

    @Override
    public List<Template.BlockInfo> getJigsawBlocks(TemplateManager templateManager, BlockPos blockPos, Rotation rotation, Random random) {
        List<Template.BlockInfo> jigsawBlocks = new ArrayList<>();
        MutableBoundingBox boundingBox = getBoundingBox();
        List<Integer> edges = Arrays.asList(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);

        for (AbstractVillagePiece structurePiece: placedStructurePieces) {
            JigsawPiece jigsawPiece = structurePiece.getJigsawPiece();

            for (Template.BlockInfo jigsawBlock: jigsawPiece.getJigsawBlocks(templateManager, structurePiece.getPos(), rotation, random)) {
                BlockPos jigsawPos = jigsawBlock.pos;

                if (edges.contains(jigsawPos.getX()) || edges.contains(jigsawPos.getY()) || edges.contains(jigsawPos.getZ())) {
                    jigsawBlocks.add(jigsawBlock);
                }
            }
        }

        return jigsawBlocks;
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManager, BlockPos blockPos, Rotation rotation) {
        MutableBoundingBox mutableboundingbox = MutableBoundingBox.getNewBoundingBox();
        for(AbstractVillagePiece structurePiece : placedStructurePieces)
            mutableboundingbox.expandTo(structurePiece.getBoundingBox());
        return mutableboundingbox;
    }

    public MutableBoundingBox getBoundingBox() {
        return getBoundingBox(templateManager, BlockPos.ZERO, rotation);
    }

    @Override
    public boolean func_230378_a_(TemplateManager templateManager, ISeedReader iSeedReader, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, MutableBoundingBox mutableBoundingBox, Random random, boolean b) {
        return true;
    }

    @Override
    public IJigsawDeserializer<?> getType() {
        return IJigsawDeserializer.LIST_POOL_ELEMENT;
    }

    public AbstractBridgePiece offset(int x, int y, int z) {
        if (placedStructurePieces.isEmpty())  {
            position.add(x, z, y);
        }
        else {
            for (AbstractVillagePiece structurePiece: placedStructurePieces) {
                structurePiece.offset(x, y, z);
            }
        }
        return this;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public void joinJigsaws(AbstractVillagePiece parentPiece, AbstractVillagePiece childPiece) {
        matching:
        for (Template.BlockInfo parentJigsaw : parentPiece.getJigsawPiece().getJigsawBlocks(templateManager, position, rotation, random)) {
            for (Template.BlockInfo childJigsaw : childPiece.getJigsawPiece().getJigsawBlocks(templateManager, position, rotation, random)) {
                if (JigsawBlock.hasJigsawMatch(parentJigsaw, childJigsaw)) {
                    Vector3i parentDirection = JigsawBlock.getConnectingDirection(parentJigsaw.state).getDirectionVec();
                    int xDelta = parentJigsaw.pos.getX() - childJigsaw.pos.getX() + parentDirection.getX();
                    int yDelta = parentJigsaw.pos.getY() - childJigsaw.pos.getY() + parentDirection.getY();
                    int zDelta = parentJigsaw.pos.getZ() - childJigsaw.pos.getZ() + parentDirection.getZ();

                    MutableBoundingBox BB = childPiece.getBoundingBox();
                    MutableBoundingBox theoreticalBB = MutableBoundingBox.createProper(BB.minX+xDelta, BB.minY+yDelta, BB.minZ+zDelta, BB.maxX+xDelta, BB.maxY+yDelta, BB.maxZ+zDelta);

                    if (!getBoundingBox().intersectsWith(theoreticalBB)) {
                        childPiece.offset(xDelta, yDelta, zDelta);
                        break matching;
                    }
                }
            }
        }
    }

    public static void joinJigsaws(AbstractBridgePiece parentPiece, AbstractBridgePiece childPiece, TemplateManager templateManager, Random random) {
        matching:
        for (Rotation rotation: Rotation.shuffledRotations(random)) {
            for (Template.BlockInfo parentJigsaw : parentPiece.getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.NONE, random)) {
                for (Template.BlockInfo childJigsaw : childPiece.getJigsawBlocks(templateManager, BlockPos.ZERO, rotation, random)) {
                    if (JigsawBlock.hasJigsawMatch(parentJigsaw, childJigsaw)) {
                        Vector3i parentDirection = JigsawBlock.getConnectingDirection(parentJigsaw.state).getDirectionVec();
                        int xDelta = parentJigsaw.pos.getX() - childJigsaw.pos.getX() + parentDirection.getX();
                        int yDelta = parentJigsaw.pos.getY() - childJigsaw.pos.getY() + parentDirection.getY();
                        int zDelta = parentJigsaw.pos.getZ() - childJigsaw.pos.getZ() + parentDirection.getZ();

                        MutableBoundingBox BB = childPiece.getBoundingBox();
                        MutableBoundingBox theoreticalBB = MutableBoundingBox.createProper(BB.minX + xDelta, BB.minY + yDelta, BB.minZ + zDelta, BB.maxX + xDelta, BB.maxY + yDelta, BB.maxZ + zDelta);

                        if (!parentPiece.getBoundingBox().intersectsWith(theoreticalBB)) {
                            childPiece.offset(xDelta, yDelta, zDelta);
                            break matching;
                        }
                    }
                }
            }
        }
    }
}