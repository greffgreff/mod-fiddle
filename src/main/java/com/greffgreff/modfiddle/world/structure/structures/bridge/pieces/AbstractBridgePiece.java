package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.util.WeightedItems;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
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
    public final Random random;
    protected final WeightedItems<JigsawPiece> weightedPieces = new WeightedItems<>(); // should pass random
    protected final JigsawPattern piecePool;
    protected static final JigsawPattern.PlacementBehaviour PLACEMENT_BEHAVIOUR = JigsawPattern.PlacementBehaviour.RIGID;

    public AbstractBridgePiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos position, Random random, ResourceLocation poolLocation) {
        super(PLACEMENT_BEHAVIOUR);
        this.dynamicRegistries = dynamicRegistries;
        this.jigsawPoolRegistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManager;
        this.position = position;
        this.random = random;
        piecePool = getPool(poolLocation);
        piecePool.rawTemplates.forEach(p -> weightedPieces.add(p.getSecond().doubleValue(), p.getFirst()));
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

    protected AbstractVillagePiece createAbstractPiece(JigsawPiece piece, BlockPos position, Rotation rotation) {
        return new AbstractVillagePiece(templateManager, piece, position, piece.getGroundLevelDelta(), rotation, piece.getBoundingBox(templateManager, position, rotation));
    }

    protected abstract List<StructurePiece> createPiece();

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
        return getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.NONE, random);
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManager, BlockPos blockPos, Rotation rotation) {
        MutableBoundingBox mutableboundingbox = MutableBoundingBox.getNewBoundingBox();
        for(StructurePiece structurePiece : this.structurePieces)
            mutableboundingbox.expandTo(structurePiece.getBoundingBox());
        return mutableboundingbox;
    }

    public MutableBoundingBox getBoundingBox() {
        return getBoundingBox(templateManager, BlockPos.ZERO, Rotation.NONE);
    }

    @Override // ??
    public boolean func_230378_a_(TemplateManager templateManager, ISeedReader iSeedReader, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, MutableBoundingBox mutableBoundingBox, Random random, boolean b) {
        return true;
    }

    @Override
    public IJigsawDeserializer<?> getType() {
        return IJigsawDeserializer.LIST_POOL_ELEMENT;
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

    public static void joinJigsaws(AbstractBridgePiece piece1, AbstractBridgePiece piece2, Random random) {
        // for (Rotation rotation : Rotation.shuffledRotations(random)) { }

        for (Template.BlockInfo jigsawPiece1 : piece1.getJigsawBlocks()) {
            for (Template.BlockInfo jigsawPiece2: piece2.getJigsawBlocks()) {
                if (JigsawBlock.hasJigsawMatch(jigsawPiece1, jigsawPiece2)) {
                    Direction directionPiece1 = JigsawBlock.getConnectingDirection(jigsawPiece1.state);
                    Direction directionPiece2 = JigsawBlock.getConnectingDirection(jigsawPiece2.state);

                    int xDelta = jigsawPiece1.pos.getX() - jigsawPiece2.pos.getX();
                    int yDelta = jigsawPiece1.pos.getY() - jigsawPiece2.pos.getY();
                    int zDelta = jigsawPiece1.pos.getZ() - jigsawPiece2.pos.getZ();

                    if (directionPiece1 == Direction.SOUTH && directionPiece2 == Direction.NORTH)
                        zDelta +=1;
                    if (directionPiece1 == Direction.NORTH && directionPiece2 == Direction.SOUTH)
                        zDelta -=1;
                    if (directionPiece1 == Direction.EAST && directionPiece2 == Direction.WEST)
                        xDelta +=1;
                    if (directionPiece1 == Direction.WEST && directionPiece2 == Direction.EAST)
                        xDelta -=1;
                    if (directionPiece1 == Direction.DOWN && directionPiece2 == Direction.UP)
                        yDelta +=1;
                    if (directionPiece1 == Direction.UP && directionPiece2 == Direction.DOWN)
                        yDelta -=1;

                    piece2.offset(xDelta, yDelta, zDelta);
                }
            }
        }
    }
}