package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import com.sun.org.apache.xpath.internal.operations.Mod;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.SplitVoxelShape;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.jigsaw.*;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
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
    public final List<StructurePiece> structurePieces = new ArrayList<>();
    public BlockPos position;
    public Rotation rotation;
    public final Random random;
    protected final WeightedItems<JigsawPiece> weightedPieces = new WeightedItems<>(); // should pass random
    protected final JigsawPattern piecePool;
    protected static final JigsawPattern.PlacementBehaviour PLACEMENT_BEHAVIOUR = JigsawPattern.PlacementBehaviour.RIGID;

    public AbstractBridgePiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Rotation rotation, Random random, ResourceLocation poolLocation) {
        super(PLACEMENT_BEHAVIOUR);
        this.dynamicRegistries = dynamicRegistries;
        this.jigsawPoolRegistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManager;
        this.position = position;
        this.rotation = rotation;
        this.random = random;
        piecePool = getPool(poolLocation);
        piecePool.rawTemplates.forEach(p -> weightedPieces.add(p.getSecond().doubleValue(), p.getFirst()));
    }

    @Override
    public List<Template.BlockInfo> getJigsawBlocks(TemplateManager templateManager, BlockPos blockPos, Rotation rotation, Random random) {
        List<Template.BlockInfo> jigsawBlocks = new ArrayList<>();
        MutableBoundingBox boundingBox = getBoundingBox();
        List<Integer> edges = Arrays.asList(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);

        for (StructurePiece structurePiece: structurePieces) {
            JigsawPiece jigsawPiece = ((AbstractVillagePiece) structurePiece).getJigsawPiece();

            for (Template.BlockInfo jigsawBlock: jigsawPiece.getJigsawBlocks(templateManager, ((AbstractVillagePiece) structurePiece).getPos(), rotation, random)) {
                BlockPos jigsawPos = jigsawBlock.pos;

                if (edges.contains(jigsawPos.getX()) || edges.contains(jigsawPos.getY()) || edges.contains(jigsawPos.getZ())) {
                    jigsawBlocks.add(jigsawBlock);
                }
            }
        }

        return jigsawBlocks;
    }

    public List<Template.BlockInfo> getJigsawBlocks() {
        return getJigsawBlocks(templateManager, BlockPos.ZERO, rotation, random);
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManager, BlockPos blockPos, Rotation rotation) {
        MutableBoundingBox mutableboundingbox = MutableBoundingBox.getNewBoundingBox();
        for(StructurePiece structurePiece : this.structurePieces)
            mutableboundingbox.expandTo(structurePiece.getBoundingBox());
        return mutableboundingbox;
    }

    public MutableBoundingBox getBoundingBox() {
        return getBoundingBox(templateManager, BlockPos.ZERO, rotation);
    }

    @Override // ??
    public boolean func_230378_a_(TemplateManager templateManager, ISeedReader iSeedReader, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, MutableBoundingBox mutableBoundingBox, Random random, boolean b) {
        return true;
    }

    @Override
    public IJigsawDeserializer<?> getType() {
        return IJigsawDeserializer.LIST_POOL_ELEMENT;
    }

    public void offset(int x, int y, int z) {
        if (structurePieces.isEmpty())  {
            position.add(x, z, y);
        }
        else {
            for (StructurePiece structurePiece: structurePieces) {
                structurePiece.offset(x, y, z);
            }
        }
    }

    public void setRotation(Rotation rotation1) { }

    protected AbstractVillagePiece createAbstractPiece(JigsawPiece piece, BlockPos position, Rotation rotation) {
        return new AbstractVillagePiece(templateManager, piece, position, piece.getGroundLevelDelta(), rotation, piece.getBoundingBox(templateManager, position, rotation));
    }

    protected abstract List<StructurePiece> createPiece();

    public List<StructurePiece> getPiece() {
        return this.structurePieces;
    }

    private JigsawPattern getPool(ResourceLocation resourceLocation) {
        Supplier<JigsawPattern> piecesPool = () -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(resourceLocation);
        return piecesPool.get();
    }

    protected static String getPieceName(JigsawPiece jigsawPiece) {
        if (jigsawPiece == null) return "";
        SingleJigsawPiece singleJigsawPiece = (SingleJigsawPiece) jigsawPiece;
        return singleJigsawPiece.field_236839_c_.left().isPresent() ? singleJigsawPiece.field_236839_c_.left().get().getPath() : "";
    }

    public void joinJigsaws(AbstractVillagePiece piece1, AbstractVillagePiece piece2) {
        matching:
        for (Template.BlockInfo jigsawPiece1 : piece1.getJigsawPiece().getJigsawBlocks(templateManager, position, rotation, random)) {
            for (Template.BlockInfo jigsawPiece2 : piece2.getJigsawPiece().getJigsawBlocks(templateManager, position, rotation, random)) {
                if (JigsawBlock.hasJigsawMatch(jigsawPiece1, jigsawPiece2)) {
                    int xDelta = jigsawPiece1.pos.getX() - jigsawPiece2.pos.getX();
                    int yDelta = jigsawPiece1.pos.getY() - jigsawPiece2.pos.getY();
                    int zDelta = jigsawPiece1.pos.getZ() - jigsawPiece2.pos.getZ();
                    Direction directionPiece1 = JigsawBlock.getConnectingDirection(jigsawPiece1.state);
                    Direction directionPiece2 = JigsawBlock.getConnectingDirection(jigsawPiece2.state);

                    ModFiddle.LOGGER.debug("X offset: " + (xDelta + directionPiece1.getDirectionVec().getX()));
                    ModFiddle.LOGGER.debug("Y offset: " + (yDelta + directionPiece1.getDirectionVec().getY()));
                    ModFiddle.LOGGER.debug("Z offset: " + (zDelta + directionPiece1.getDirectionVec().getZ()));

                    if (directionPiece1 == Direction.SOUTH && directionPiece2 == Direction.NORTH) zDelta += 1;
                    if (directionPiece1 == Direction.NORTH && directionPiece2 == Direction.SOUTH) zDelta -= 1;
                    if (directionPiece1 == Direction.EAST && directionPiece2 == Direction.WEST) xDelta += 1;
                    if (directionPiece1 == Direction.WEST && directionPiece2 == Direction.EAST) xDelta -= 1;
                    if (directionPiece1 == Direction.DOWN && directionPiece2 == Direction.UP) yDelta += 1;
                    if (directionPiece1 == Direction.UP && directionPiece2 == Direction.DOWN) yDelta -= 1;

                    MutableBoundingBox BB = piece2.getBoundingBox();
                    MutableBoundingBox theoreticalBB = MutableBoundingBox.createProper(BB.minX+xDelta, BB.minY+yDelta, BB.minZ+zDelta, BB.maxX+xDelta, BB.maxY+yDelta, BB.maxZ+zDelta);

                    ModFiddle.LOGGER.debug("Direction 1: " + directionPiece1);
                    ModFiddle.LOGGER.debug("Direction 2: " + directionPiece2);

                    ModFiddle.LOGGER.debug("Piece 1: " + piece1.getPos());
                    ModFiddle.LOGGER.debug("Piece 2: " + piece2.getPos());

                    ModFiddle.LOGGER.debug("Piece 1 BB: " + piece1.getBoundingBox());
                    ModFiddle.LOGGER.debug("Piece 2 BB: " + piece2.getBoundingBox());

                    if (!getBoundingBox().intersectsWith(theoreticalBB)) {
                        piece2.offset(xDelta, yDelta, zDelta);
                        break matching;
                    }
                }
            }
        }
    }

    public static void joinJigsaws(AbstractBridgePiece piece1, AbstractBridgePiece piece2, TemplateManager templateManager, Rotation rotation, Random random) {
        matching:
        for (Template.BlockInfo jigsawPiece1 : piece1.getJigsawBlocks(templateManager, BlockPos.ZERO, rotation, random)) {
            for (Template.BlockInfo jigsawPiece2: piece2.getJigsawBlocks(templateManager, BlockPos.ZERO, rotation, random)) {
                if (JigsawBlock.hasJigsawMatch(jigsawPiece1, jigsawPiece2)) {
                    int xDelta = jigsawPiece1.pos.getX() - jigsawPiece2.pos.getX();
                    int yDelta = jigsawPiece1.pos.getY() - jigsawPiece2.pos.getY();
                    int zDelta = jigsawPiece1.pos.getZ() - jigsawPiece2.pos.getZ();

                    Direction directionPiece1 = JigsawBlock.getConnectingDirection(jigsawPiece1.state);
                    Direction directionPiece2 = JigsawBlock.getConnectingDirection(jigsawPiece2.state);
                    if (directionPiece1 == Direction.NORTH && directionPiece2 == Direction.SOUTH) zDelta -=1;
                    if (directionPiece1 == Direction.EAST && directionPiece2 == Direction.WEST) xDelta +=1;
                    if (directionPiece1 == Direction.WEST && directionPiece2 == Direction.EAST) xDelta -=1;
                    if (directionPiece1 == Direction.DOWN && directionPiece2 == Direction.UP) yDelta +=1;
                    if (directionPiece1 == Direction.UP && directionPiece2 == Direction.DOWN) yDelta -=1;

                    piece2.offset(xDelta, yDelta, zDelta);
                    break matching;
                }
            }
        }
    }
}