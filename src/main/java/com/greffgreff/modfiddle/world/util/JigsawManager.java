package com.greffgreff.modfiddle.world.util;

import com.google.common.collect.Queues;
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
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.jigsaw.*;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class JigsawManager {
    public static void assembleJigsawStructure(
            DynamicRegistries dynamicRegistryManager,
            VillageConfig jigsawConfig,
            ChunkGenerator chunkGenerator,
            TemplateManager templateManager,
            BlockPos startPos,
            List<? super AbstractVillagePiece> structurePieces,
            Random random,
            boolean doBoundaryAdjustments,
            boolean useHeightmap
    ) {
        // Get jigsaw pool registry
        MutableRegistry<JigsawPattern> jigsawPoolRegistry = dynamicRegistryManager.getRegistry(Registry.JIGSAW_POOL_KEY);

        // Get a random orientation for the starting piece
        Rotation rotation = Rotation.randomRotation(random);

        // Get starting
        JigsawPattern startPool = jigsawConfig.func_242810_c().get();

        // Grab a random starting piece from the start pool without rotation or position
        JigsawPiece startPieceBlueprint = startPool.getRandomPiece(random);

        // Instantiate a piece using the "blueprint" we just got.
        AbstractVillagePiece startPiece = new AbstractVillagePiece(
                templateManager,
                startPieceBlueprint,
                startPos,
                startPieceBlueprint.getGroundLevelDelta(),
                rotation,
                startPieceBlueprint.getBoundingBox(templateManager, startPos, rotation)
        );

        // Store center position of starting piece's bounding box
        MutableBoundingBox pieceBoundingBox = startPiece.getBoundingBox();

        int pieceCenterX = (pieceBoundingBox.maxX + pieceBoundingBox.minX) / 2;
        int pieceCenterZ = (pieceBoundingBox.maxZ + pieceBoundingBox.minZ) / 2;
        int pieceCenterY = useHeightmap ? startPos.getY() + chunkGenerator.getNoiseHeight(pieceCenterX, pieceCenterZ, Heightmap.Type.WORLD_SURFACE_WG) : startPos.getY();

        int yAdjustment = pieceBoundingBox.minY + startPiece.getGroundLevelDelta(); // groundLevelDelta seems to always be 1. Not sure what the point of this is.
        startPiece.offset(0, pieceCenterY - yAdjustment, 0); // Ends up always offsetting the piece by y = -1?

        structurePieces.add(startPiece); // Add start piece to list of pieces

        ModFiddle.LOGGER.debug("////// Starting Generation //////");

        if (jigsawConfig.func_236534_a_() > 0) { // optional if
            // Get non-expanded bounding box of starting piece
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(pieceCenterX - 80, pieceCenterY - 80, pieceCenterZ - 80, pieceCenterX + 80 + 1, pieceCenterY + 80 + 1, pieceCenterZ + 80 + 1);

            Assembler assembler = new Assembler(jigsawPoolRegistry, jigsawConfig.func_236534_a_(), chunkGenerator, templateManager, structurePieces, random);
            assembler.availablePieces.addLast(new Entry(startPiece, new MutableObject<>(VoxelShapes.combineAndSimplify(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)), IBooleanFunction.ONLY_FIRST)), pieceCenterY + 80, 0));

            while(!assembler.availablePieces.isEmpty()) {
                Entry entry = assembler.availablePieces.removeFirst();
                assembler.tryPlacingChildren(entry.piece, entry.voxel, entry.boundsTop, entry.depth, doBoundaryAdjustments);
            }
        }

        ModFiddle.LOGGER.debug("////// Generation Complete //////");
    }

    public static final class Assembler {
        private final Registry<JigsawPattern> patternRegistry;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final TemplateManager templateManager;
        private final List<? super AbstractVillagePiece> structurePieces;
        private final Random random;
        public final Deque<Entry> availablePieces = Queues.newArrayDeque();

        public Assembler(Registry<JigsawPattern> patternRegistry, int maxDepth, ChunkGenerator chunkGenerator, TemplateManager templateManager, List<? super AbstractVillagePiece> structurePieces, Random random) {
            this.patternRegistry = patternRegistry;
            this.maxDepth = maxDepth;
            this.chunkGenerator = chunkGenerator;
            this.templateManager = templateManager;
            this.structurePieces = structurePieces; // final list of processed jigsaw pieces
            this.random = random;
        }

        public void tryPlacingChildren(AbstractVillagePiece piece, MutableObject<VoxelShape> voxelShape, int boundsTop, int depth, boolean doBoundaryAdjustments) {
            // Get jigsaw piece
            JigsawPiece pieceBlueprint = piece.getJigsawPiece();
            // Get piece position
            BlockPos piecePos = piece.getPos();
            // Get piece rotation
            Rotation pieceRotation = piece.getRotation();
            // Get piece bounding box
            MutableBoundingBox pieceBoundingBox = piece.getBoundingBox();
            int pieceMinY = pieceBoundingBox.minY;
            // Holder variable for reuse (?)
            MutableObject<VoxelShape> tempNewPieceVoxelShape = new MutableObject<>();
            // Get list of all jigsaw blocks in this piece
            List<Template.BlockInfo> pieceJigsawBlocks = pieceBlueprint.getJigsawBlocks(this.templateManager, piecePos, pieceRotation, this.random);

            for (Template.BlockInfo jigsawBlock : pieceJigsawBlocks) {
                // Gather jigsaw block information
                Direction direction = JigsawBlock.getConnectingDirection(jigsawBlock.state);
                BlockPos jigsawBlockPos = jigsawBlock.pos;
                BlockPos jigsawBlockTargetPos = jigsawBlockPos.offset(direction);

                // Get the jigsaw block's piece pool
                ResourceLocation jigsawBlockPool = new ResourceLocation(jigsawBlock.nbt.getString("pool"));
                Optional<JigsawPattern> jigsawBlockTargetPool = this.patternRegistry.getOptional(jigsawBlockPool);

                // Only continue if we are using the jigsaw pattern registry and if it is not empty
                if (!(jigsawBlockTargetPool.isPresent() && (jigsawBlockTargetPool.get().getNumberOfPieces() != 0 || Objects.equals(jigsawBlockPool, JigsawPatternRegistry.field_244091_a.getLocation())))) {
                    ModFiddle.LOGGER.warn("Empty or nonexistent pool: {}", jigsawBlockPool);
                    continue;
                }

                // Get the jigsaw block's fallback pool (which is a part of the pool's JSON)
                ResourceLocation jigsawBlockFallback = jigsawBlockTargetPool.get().getFallback();
                Optional<JigsawPattern> jigsawBlockFallbackPool = this.patternRegistry.getOptional(jigsawBlockFallback);

                // Only continue if the fallback pool is present and valid
                if (!(jigsawBlockFallbackPool.isPresent() && (jigsawBlockFallbackPool.get().getNumberOfPieces() != 0 || Objects.equals(jigsawBlockFallback, JigsawPatternRegistry.field_244091_a.getLocation())))) {
                    ModFiddle.LOGGER.warn("Empty or nonexistent fallback pool: {}", jigsawBlockFallback);
                    continue;
                }

                // Adjustments for if the target block position is inside the current piece
                boolean isTargetInsideCurrentPiece = pieceBoundingBox.isVecInside(jigsawBlockTargetPos);
                MutableObject<VoxelShape> pieceVoxelShape;
                int targetPieceBoundsTop;
                if (isTargetInsideCurrentPiece) {
                    pieceVoxelShape = tempNewPieceVoxelShape;
                    targetPieceBoundsTop = pieceMinY;
                    if (tempNewPieceVoxelShape.getValue() == null) {
                        tempNewPieceVoxelShape.setValue(VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)));
                    }
                } else {
                    pieceVoxelShape = voxelShape;
                    targetPieceBoundsTop = boundsTop;
                }

                // Process the pool pieces, randomly choosing different pieces from the pool to spawn
                if (depth != this.maxDepth) {
                    JigsawPiece generatedPiece = this.getPoolPiece(new ArrayList<>(jigsawBlockTargetPool.get().getShuffledPieces(random)), doBoundaryAdjustments, jigsawBlock, jigsawBlockTargetPos, pieceMinY, jigsawBlockPos, pieceVoxelShape, piece, depth, targetPieceBoundsTop);
                    if (generatedPiece != null) continue; // Stop here since we've already generated the piece
                }

                // Process the fallback pieces in the event none of the pool pieces work
                this.getPoolPiece(new ArrayList<>(jigsawBlockFallbackPool.get().getShuffledPieces(random)), doBoundaryAdjustments, jigsawBlock, jigsawBlockTargetPos, pieceMinY, jigsawBlockPos, pieceVoxelShape, piece, depth, targetPieceBoundsTop);
            }
        }

        private JigsawPiece getPoolPiece(
                List<JigsawPiece> candidatePieces,
                boolean doBoundaryAdjustments,
                Template.BlockInfo jigsawBlock,
                BlockPos jigsawBlockTargetPos,
                int pieceMinY,
                BlockPos jigsawBlockPos,
                MutableObject<VoxelShape> pieceVoxelShape,
                AbstractVillagePiece piece,
                int depth,
                int targetPieceBoundsTop
        ) {
            JigsawPattern.PlacementBehaviour piecePlacementBehavior = piece.getJigsawPiece().getPlacementBehaviour();
            boolean isPieceRigid = piecePlacementBehavior == JigsawPattern.PlacementBehaviour.RIGID;
            int jigsawBlockRelativeY = jigsawBlockPos.getY() - pieceMinY;
            int surfaceHeight = -1; // The y-coordinate of the surface. Only used if isPieceRigid is false

            while (candidatePieces.size() > 0) {
                // Prioritize portal room if the following conditions are met:
                // 1. It's a potential candidate for this pool
                // 2. It hasn't already been placed
                // 3. We are at least (maxDepth/2) pieces away from the starting room.
                // if (depth >= maxDepth / 2) { // Condition 3
                // if (this.pieceCounts.get(new ResourceLocation(BetterStrongholds.MOD_ID, "portal_rooms/portal_room")) > 0) { // Condition 2

                // Choose piece if portal room wasn't selected
                int randomInt = ThreadLocalRandom.current().nextInt(0, candidatePieces.size());
                JigsawPiece candidatePiece = candidatePieces.get(randomInt);

                SingleJigsawPiece singleJigsawPiece = (SingleJigsawPiece) candidatePiece;
                ModFiddle.LOGGER.debug(singleJigsawPiece.field_236839_c_.left().get().getPath());

                // Try different rotations to see which sides of the piece are fit to be the receiving end
                for (Rotation rotation : Rotation.shuffledRotations(this.random)) {
                    List<Template.BlockInfo> candidateJigsawBlocks = candidatePiece.getJigsawBlocks(this.templateManager, BlockPos.ZERO, rotation, this.random);
                    MutableBoundingBox tempCandidateBoundingBox = candidatePiece.getBoundingBox(this.templateManager, BlockPos.ZERO, rotation);

                    // Some sort of logic for setting the candidateHeightAdjustments var if doBoundaryAdjustments.
                    int candidateHeightAdjustments;
                    if (doBoundaryAdjustments && tempCandidateBoundingBox.getYSize() <= 16) { // optional
                        candidateHeightAdjustments = candidateJigsawBlocks.stream().mapToInt((pieceCandidateJigsawBlock) -> {
                            if (!tempCandidateBoundingBox.isVecInside(pieceCandidateJigsawBlock.pos.offset(JigsawBlock.getConnectingDirection(pieceCandidateJigsawBlock.state)))) {
                                return 0;
                            } else {
                                ResourceLocation candidateTargetPool = new ResourceLocation(pieceCandidateJigsawBlock.nbt.getString("pool"));
                                Optional<JigsawPattern> candidateTargetPoolOptional = this.patternRegistry.getOptional(candidateTargetPool);
                                Optional<JigsawPattern> candidateTargetFallbackOptional = candidateTargetPoolOptional.flatMap((p_242843_1_) -> this.patternRegistry.getOptional(p_242843_1_.getFallback()));
                                int tallestCandidateTargetPoolPieceHeight = candidateTargetPoolOptional.map((p_242842_1_) -> p_242842_1_.getMaxSize(this.templateManager)).orElse(0);
                                int tallestCandidateTargetFallbackPieceHeight = candidateTargetFallbackOptional.map((p_242840_1_) -> p_242840_1_.getMaxSize(this.templateManager)).orElse(0);
                                return Math.max(tallestCandidateTargetPoolPieceHeight, tallestCandidateTargetFallbackPieceHeight);
                            }
                        }).max().orElse(0);
                    } else {
                        candidateHeightAdjustments = 0;
                    }

                    // Check for each of the candidate's jigsaw blocks for a match
                    for (Template.BlockInfo candidateJigsawBlock : candidateJigsawBlocks) {
                        if (JigsawBlock.hasJigsawMatch(jigsawBlock, candidateJigsawBlock)) {
                            BlockPos candidateJigsawBlockPos = candidateJigsawBlock.pos;
                            BlockPos candidateJigsawBlockRelativePos = new BlockPos(jigsawBlockTargetPos.getX() - candidateJigsawBlockPos.getX(), jigsawBlockTargetPos.getY() - candidateJigsawBlockPos.getY(), jigsawBlockTargetPos.getZ() - candidateJigsawBlockPos.getZ());

                            // Get the bounding box for the piece, offset by the relative position difference
                            MutableBoundingBox candidateBoundingBox = candidatePiece.getBoundingBox(this.templateManager, candidateJigsawBlockRelativePos, rotation);

                            // Determine if candidate is rigid
                            JigsawPattern.PlacementBehaviour candidatePlacementBehavior = candidatePiece.getPlacementBehaviour();
                            boolean isCandidateRigid = candidatePlacementBehavior == JigsawPattern.PlacementBehaviour.RIGID;

                            // Determine how much the candidate jigsaw block is off in the y direction.
                            // This will be needed to offset the candidate piece so that the jigsaw blocks line up properly.
                            int candidateJigsawBlockRelativeY = candidateJigsawBlockPos.getY();
                            int candidateJigsawYOffsetNeeded = jigsawBlockRelativeY - candidateJigsawBlockRelativeY + JigsawBlock.getConnectingDirection(jigsawBlock.state).getYOffset();

                            // Determine how much we need to offset the candidate piece itself in order to have the jigsaw blocks aligned.
                            // Depends on if the placement of both pieces is rigid or not
                            int adjustedCandidatePieceMinY;
                            if (isPieceRigid && isCandidateRigid) {
                                adjustedCandidatePieceMinY = pieceMinY + candidateJigsawYOffsetNeeded;
                            } else {
                                if (surfaceHeight == -1) {
                                    surfaceHeight = this.chunkGenerator.getNoiseHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                }

                                adjustedCandidatePieceMinY = surfaceHeight - candidateJigsawBlockRelativeY;
                            }
                            int candidatePieceYOffsetNeeded = adjustedCandidatePieceMinY - candidateBoundingBox.minY;

                            // Offset the candidate's bounding box by the necessary amount
                            MutableBoundingBox adjustedCandidateBoundingBox = candidateBoundingBox.func_215127_b(0, candidatePieceYOffsetNeeded, 0);

                            // Add this offset to the relative jigsaw block position as well
                            BlockPos adjustedCandidateJigsawBlockRelativePos = candidateJigsawBlockRelativePos.add(0, candidatePieceYOffsetNeeded, 0);

                            // Final adjustments to the bounding box.
                            if (candidateHeightAdjustments > 0) {
                                int k2 = Math.max(candidateHeightAdjustments + 1, adjustedCandidateBoundingBox.maxY - adjustedCandidateBoundingBox.minY);
                                adjustedCandidateBoundingBox.maxY = adjustedCandidateBoundingBox.minY + k2;
                            }

                            // Comparing if adding the piece clips through already existing structure
                            if (!VoxelShapes.compare(pieceVoxelShape.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(adjustedCandidateBoundingBox).shrink(0.25D)), IBooleanFunction.ONLY_SECOND)) {
                                pieceVoxelShape.setValue(VoxelShapes.combine(pieceVoxelShape.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(adjustedCandidateBoundingBox)),  IBooleanFunction.ONLY_FIRST));

                                // Determine ground level delta for this new piece
                                int newPieceGroundLevelDelta = piece.getGroundLevelDelta();
                                int groundLevelDelta;
                                if (isCandidateRigid) {
                                    groundLevelDelta = newPieceGroundLevelDelta - candidateJigsawYOffsetNeeded;
                                } else {
                                    groundLevelDelta = candidatePiece.getGroundLevelDelta();
                                }

                                // Create new piece
                                AbstractVillagePiece newPiece = new AbstractVillagePiece(
                                        this.templateManager,
                                        candidatePiece,
                                        adjustedCandidateJigsawBlockRelativePos,
                                        groundLevelDelta,
                                        rotation,
                                        adjustedCandidateBoundingBox
                                );

                                // Determine actual y-value for the new jigsaw block
                                int candidateJigsawBlockY;
                                if (isPieceRigid) {
                                    candidateJigsawBlockY = pieceMinY + jigsawBlockRelativeY;
                                } else if (isCandidateRigid) {
                                    candidateJigsawBlockY = adjustedCandidatePieceMinY + candidateJigsawBlockRelativeY;
                                } else {
                                    if (surfaceHeight == -1) {
                                        surfaceHeight = this.chunkGenerator.getNoiseHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                    }

                                    candidateJigsawBlockY = surfaceHeight + candidateJigsawYOffsetNeeded / 2;
                                }

                                // Add the junction to the existing piece
                                piece.addJunction(new JigsawJunction(jigsawBlockTargetPos.getX(), candidateJigsawBlockY - jigsawBlockRelativeY + newPieceGroundLevelDelta, jigsawBlockTargetPos.getZ(), candidateJigsawYOffsetNeeded, candidatePlacementBehavior));

                                // Add the junction to the new piece
                                newPiece.addJunction(new JigsawJunction(jigsawBlockPos.getX(), candidateJigsawBlockY - candidateJigsawBlockRelativeY + groundLevelDelta, jigsawBlockPos.getZ(), -candidateJigsawYOffsetNeeded, piecePlacementBehavior));

                                // Add the piece
                                this.structurePieces.add(newPiece);

                                if (depth + 1 <= this.maxDepth)
                                    this.availablePieces.addLast(new Entry(newPiece, pieceVoxelShape, targetPieceBoundsTop, depth + 1));

//                                ModFiddle.LOGGER.debug("Piece name: ");
//                                ModFiddle.LOGGER.debug("Max depth: " + maxDepth + " Depth: " + depth);
//                                ModFiddle.LOGGER.debug("Total pieces: "+ availablePieces.size());

                                return candidatePiece;
                            }
                        }
                    }
                }

                // Remove piece to continue loop
                candidatePieces.remove(candidatePiece);
            }

            return null;
        }
    }

    static final class Entry {
        private final AbstractVillagePiece piece;
        private final MutableObject<VoxelShape> voxel;
        private final int boundsTop; // refers to the top y boundary
        private final int depth;

        private Entry(AbstractVillagePiece piece, MutableObject<VoxelShape> voxel, int boundsTop, int depth) {
            this.piece = piece;
            this.voxel = voxel;
            this.boundsTop = boundsTop;
            this.depth = depth;
        }
    }
}
