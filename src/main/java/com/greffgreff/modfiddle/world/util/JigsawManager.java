package com.greffgreff.modfiddle.world.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import java.util.*;

import com.greffgreff.modfiddle.ModFiddle;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.jigsaw.*;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableObject;

public class JigsawManager {
    public static void addPieces(DynamicRegistries dynamicRegistries, VillageConfig villageConfig, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos blockPos, List<? super AbstractVillagePiece> structurePieces, Random rand, boolean useHeightMap) {
        Rotation rotation = Rotation.randomRotation(rand);

        MutableRegistry<JigsawPattern> mutableregistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        JigsawPattern jigsawpattern = villageConfig.func_242810_c().get();

        JigsawPiece jigsawpiece = jigsawpattern.getRandomPiece(rand);
        AbstractVillagePiece piece =  new AbstractVillagePiece(templateManager, jigsawpiece, blockPos, jigsawpiece.getGroundLevelDelta(), rotation, jigsawpiece.getBoundingBox(templateManager, blockPos, rotation));

        // Get starting piece bounding box
        MutableBoundingBox pieceBoundingBox = piece.getBoundingBox();

        /// Unknown
        int i = (pieceBoundingBox.maxX + pieceBoundingBox.minX) / 2;
        int j = (pieceBoundingBox.maxZ + pieceBoundingBox.minZ) / 2;
        int k = useHeightMap ? blockPos.getY() + chunkGenerator.getNoiseHeight(i, j, Heightmap.Type.WORLD_SURFACE_WG) : blockPos.getY();

        int l = pieceBoundingBox.minY + piece.getGroundLevelDelta();
        piece.offset(0, k - l, 0);

        structurePieces.add(piece);

        if (villageConfig.func_236534_a_() > 0) {
            // Get non-expanded bounding box of starting piece
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(i - 80, k - 80, j - 80, i + 80 + 1, k + 80 + 1, j + 80 + 1);

            Assembler assembler = new Assembler(mutableregistry, villageConfig.func_236534_a_(), chunkGenerator, templateManager, structurePieces, rand);
            assembler.availablePieces.addLast(new Entry(piece, new MutableObject(VoxelShapes.combineAndSimplify(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)), IBooleanFunction.ONLY_FIRST)), k + 80, 0));

            while(!assembler.availablePieces.isEmpty()) {
                Entry entry = assembler.availablePieces.removeFirst();
                assembler.tryPlacingChildren(entry.piece, entry.voxel, entry.boundsTop, entry.depth);
            }
        }
    }

    static final class Assembler {
        private final Registry<JigsawPattern> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final TemplateManager structureManager;
        private final List<? super AbstractVillagePiece> pieces;
        private final Random rand;
        private final Deque<Entry> availablePieces;

        private Assembler(Registry<JigsawPattern> pools, int maxDepth, ChunkGenerator chunkGenerator, TemplateManager templateManager, List<? super AbstractVillagePiece> pieces, Random rand) {
            this.availablePieces = Queues.newArrayDeque();
            this.pools = pools;
            this.maxDepth = maxDepth; // refers to structure generation tree boundsTop
            this.chunkGenerator = chunkGenerator;
            this.structureManager = templateManager;
            this.pieces = pieces;
            this.rand = rand;
        }

        private void tryPlacingChildren(AbstractVillagePiece piece, MutableObject<VoxelShape> voxel, int boundsTop, int depth) {
            // Get jigsaw piece
            JigsawPiece jigsawpiece = piece.getJigsawPiece();
            // Get piece position
            BlockPos blockPos = piece.getPos();
            // Get piece rotation
            Rotation rotation = piece.getRotation();
            // Get placmenet behavior of piece
            JigsawPattern.PlacementBehaviour piecePlacementBehavior = jigsawpiece.getPlacementBehaviour();
            // Terrain matching VS Rigid
            boolean isRigid = piecePlacementBehavior == JigsawPattern.PlacementBehaviour.RIGID;
            // Get piece Bounding box
            MutableBoundingBox pieceBoundingBox = piece.getBoundingBox();
            // Instanciate voxel shape for later use
            MutableObject<VoxelShape> pieceVoxel = new MutableObject<>();
            // Get all jigsaw blocks in piece
            List<Template.BlockInfo> jigsawBlocks = jigsawpiece.getJigsawBlocks(this.structureManager, blockPos, rotation, this.rand);

            findMatch:
            for (Template.BlockInfo jigsawBlock : jigsawBlocks) {
                // Get jigsaw block direction
                Direction direction = JigsawBlock.getConnectingDirection(jigsawBlock.state);
                // Get jigsaw block pos and set it into 3D space
                BlockPos jigsawBlockPos = jigsawBlock.pos;
                BlockPos adjustedJigsawBlockPos = jigsawBlockPos.offset(direction);
                // Get target pool from jigsaw block
                ResourceLocation resourcelocation = new ResourceLocation(jigsawBlock.nbt.getString("pool"));
                // Get target pool as usable JigsawPattern class
                Optional<JigsawPattern> jigsawPoolPattern = this.pools.getOptional(resourcelocation);

                if (jigsawPoolPattern.isPresent() && (jigsawPoolPattern.get().getNumberOfPieces() != 0 || Objects.equals(resourcelocation, JigsawPatternRegistry.field_244091_a.getLocation()))) {
                    // Get fallback pool
                    ResourceLocation fallBackPool = jigsawPoolPattern.get().getFallback();
                    // Get fallback pool as JigsawPattern
                    Optional<JigsawPattern> fallBackPoolPattern = this.pools.getOptional(fallBackPool);

                    if (fallBackPoolPattern.isPresent() && (fallBackPoolPattern.get().getNumberOfPieces() != 0 || Objects.equals(fallBackPool, JigsawPatternRegistry.field_244091_a.getLocation()))) {
                        // Get candidate position for child piece relative to parent
                        int i = pieceBoundingBox.minY;  // is K instead of J
                        int j = jigsawBlockPos.getY() - i;
                        int k = -1; // offset by 1 to account for parent jigsaw block
                        // Check if jigsaw block is within original jigsaw piece
                        boolean isVecInside = pieceBoundingBox.isVecInside(adjustedJigsawBlockPos);
                        // If inside then use parent piece bounding box, else use
                        MutableObject<VoxelShape> childPieceVoxel;
                        int childTopBounds;
                        if (isVecInside) {
                            childPieceVoxel = pieceVoxel;
                            childTopBounds = i;
                            if (pieceVoxel.getValue() == null)
                                pieceVoxel.setValue(VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)));
                        }
                        else {
                            childPieceVoxel = voxel;
                            childTopBounds = boundsTop;
                        }
                        // Check whether the max boundsTop of has been reached
                        List<JigsawPiece> list = Lists.newArrayList();
                        if (depth != this.maxDepth)
                            list.addAll(jigsawPoolPattern.get().getShuffledPieces(this.rand));
                        // Add fallback pieces regardless
                        list.addAll(fallBackPoolPattern.get().getShuffledPieces(this.rand));

                        for (JigsawPiece childJigsawPiece : list) {
                            if (childJigsawPiece == EmptyJigsawPiece.INSTANCE)
                                break;

                            // Maybe use first suitable rotation as candidate child jigsaw piece
                            for (Rotation childJigsawPieceRotation : Rotation.shuffledRotations(this.rand)) {
                                // Get all jigsaw blocks
                                List<Template.BlockInfo> childPieceJigsawBlocks = childJigsawPiece.getJigsawBlocks(this.structureManager, BlockPos.ZERO, childJigsawPieceRotation, this.rand);

                                for (Template.BlockInfo childJigsawBlock : childPieceJigsawBlocks) {
                                    if (JigsawBlock.hasJigsawMatch(jigsawBlock, childJigsawBlock)) {
                                        // Get child jigsaw block pos and adjust it to parent jigsaw
                                        BlockPos childJigsawBlockPos = childJigsawBlock.pos;
                                        BlockPos adjustedChildJigsawBlockPos = new BlockPos(adjustedJigsawBlockPos.getX() - childJigsawBlockPos.getX(), adjustedJigsawBlockPos.getY() - childJigsawBlockPos.getY(), adjustedJigsawBlockPos.getZ() - childJigsawBlockPos.getZ());
                                        // Get child piece bounding box
                                        MutableBoundingBox childPieceBoundingBox = childJigsawPiece.getBoundingBox(this.structureManager, adjustedChildJigsawBlockPos, childJigsawPieceRotation);
                                        // Get child piece placement behavior
                                        JigsawPattern.PlacementBehaviour childPiecePlacementBehavior = childJigsawPiece.getPlacementBehaviour();
                                        boolean isChildRigid = childPiecePlacementBehavior == JigsawPattern.PlacementBehaviour.RIGID;

                                        // Get starting Y position
                                        /// Unknown ///
                                        int j1 = childPieceBoundingBox.minY;
                                        int i1 = 0;
                                        int k1 = childJigsawBlockPos.getY();
                                        int l1 = j - k1 + JigsawBlock.getConnectingDirection(jigsawBlock.state).getYOffset();
                                        int i2;

                                        if (isRigid && isChildRigid) {
                                            i2 = i + l1;
                                        }
                                        else {
                                            if (k == -1)
                                                k = this.chunkGenerator.getNoiseHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);

                                            i2 = k - k1;
                                        }
                                        int j2 = i2 - j1;

                                        // .move() adjust piece bounding box relative to the parent
                                        MutableBoundingBox newChildPieceBoundingBox = childPieceBoundingBox.func_215127_b(0, j2, 0);
                                        BlockPos newAdjustedChildJigsawBlockPos = adjustedChildJigsawBlockPos.add(0, j2, 0);

                                        if (!VoxelShapes.compare(childPieceVoxel.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(newChildPieceBoundingBox).shrink(0.25D)), IBooleanFunction.ONLY_SECOND)) {
                                            // If [something] then set childpiece to [something]
                                            childPieceVoxel.setValue(VoxelShapes.combine(childPieceVoxel.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(newChildPieceBoundingBox)), IBooleanFunction.ONLY_FIRST));

                                            /// Unknown ///
                                            int j3 = piece.getGroundLevelDelta();
                                            int l2;
                                            if (isChildRigid)
                                                l2 = j3 - l1;
                                            else
                                                l2 = childJigsawPiece.getGroundLevelDelta();

                                            // If something valid, then create child piece abstract village from jigsaw piece
                                            AbstractVillagePiece pieceChild = new AbstractVillagePiece(this.structureManager, childJigsawPiece, newAdjustedChildJigsawBlockPos, l2, childJigsawPieceRotation, newChildPieceBoundingBox);

                                            /// Unknown ///
                                            int i3;
                                            if (isRigid)
                                                i3 = i + j;
                                            else if (isChildRigid)
                                                i3 = i2 + k1;
                                            else {
                                                if (k == -1)
                                                    k = this.chunkGenerator.getNoiseHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);

                                                i3 = k + l1 / 2;
                                            }

                                            // Occupy jigsaw block on child piece, connection to parent piece?
                                            /// Unknown
                                            pieceChild.addJunction(new JigsawJunction(adjustedJigsawBlockPos.getX(), i3 - j + j3, adjustedJigsawBlockPos.getZ(), l1, childPiecePlacementBehavior));
                                            pieceChild.addJunction(new JigsawJunction(jigsawBlockPos.getX(), i3 - k1 + l2, jigsawBlockPos.getZ(), -l1, piecePlacementBehavior));

                                            // Add piece to componenets
                                            this.pieces.add(pieceChild);

                                            ModFiddle.LOGGER.debug("Max depth: " + maxDepth + " Depth: " + depth + " Top bounds: " + boundsTop);

                                            // Check if max tree depth has been reached
                                            if (depth + 1 <= this.maxDepth)
                                                this.availablePieces.addLast(new Entry(pieceChild, childPieceVoxel, childTopBounds, depth + 1));

                                            continue findMatch;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else {
                        ModFiddle.LOGGER.warn("Empty or none existent fallback pool: {}", (Object)fallBackPool);
                    }
                }
                else {
                    ModFiddle.LOGGER.warn("Empty or none existent pool: {}", (Object)resourcelocation);
                }
            }
        }
    }

    static final class Entry {
        private final AbstractVillagePiece piece;
        private final MutableObject<VoxelShape> voxel;
        private final int boundsTop; // refers to the top y boundry
        private final int depth;

        private Entry(AbstractVillagePiece piece, MutableObject<VoxelShape> voxel, int boundsTop, int depth) {
            this.piece = piece;
            this.voxel = voxel;
            this.boundsTop = boundsTop;
            this.depth = depth;
        }
    }
}

/*
/////////// Boundry Adj? ////////////
// Get child piece bounding box
MutableBoundingBox childPieceBoundingBox = childJigsawPiece.getBoundingBox(this.structureManager, BlockPos.ZERO, childJigsawPieceRotation);

int i1;
if (doBoundryAdjustmentsMAYBE && childPieceBoundingBox.getYSize() <= 16) {
    i1 = childPieceJigsawBlocks.stream().mapToInt((p_242841_2_) -> {
        if (!childPieceBoundingBox.isVecInside(p_242841_2_.pos.offset(JigsawBlock.getConnectingDirection(p_242841_2_.state)))) {
            return 0;
        }
        else {
            ResourceLocation resourcelocation2 = new ResourceLocation(p_242841_2_.nbt.getString("pool"));
            Optional<JigsawPattern> jigsawPattern2 = this.pools.getOptional(resourcelocation2);
            Optional<JigsawPattern> jigsawPattern3 = jigsawPattern2.flatMap((p_242843_1_) -> {
                return this.pools.getOptional(p_242843_1_.getFallback());
            });
            int k3 = jigsawPattern2.map((p_242842_1_) -> {
                return p_242842_1_.getMaxSize(this.structureManager);
            }).orElse(0);
            int l3 = jigsawPattern3.map((p_242840_1_) -> {
                return p_242840_1_.getMaxSize(this.structureManager);
            }).orElse(0);
            return Math.max(k3, l3);
        }
    }).max().orElse(0);
}
else {
    i1 = 0;
}

if (i1 > 0) {
    int k2 = Math.max(i1 + 1, newChildPieceBoundingBox.maxY - newChildPieceBoundingBox.minY);
    newChildPieceBoundingBox.maxY = newChildPieceBoundingBox.minY + k2;
}
////////////////////////////////////////
 */