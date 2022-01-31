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
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class JigsawManager {
    public static void assembleJigsawStructure(DynamicRegistries dynamicRegistryManager, VillageConfig jigsawConfig, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startPos, List<? super AbstractVillagePiece> structurePieces, Random random, boolean useHeightmap) {
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

        // Get non-expanded bounding box of starting piece
        AxisAlignedBB axisalignedbb = new AxisAlignedBB(pieceCenterX - 80, pieceCenterY - 80, pieceCenterZ - 80, pieceCenterX + 80 + 1, pieceCenterY + 80 + 1, pieceCenterZ + 80 + 1);

        Assembler assembler = new Assembler(jigsawPoolRegistry, jigsawConfig.func_236534_a_(), chunkGenerator, templateManager, structurePieces, random);
        assembler.availablePieces.addLast(new Entry(startPiece, new MutableObject<>(VoxelShapes.combineAndSimplify(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)), IBooleanFunction.ONLY_FIRST)), pieceCenterY + 80, 0));

        ModFiddle.LOGGER.debug("////// Starting Generation //////");

        while(!assembler.availablePieces.isEmpty()) {
            Entry entry = assembler.availablePieces.removeFirst();
            String processedPiece = getPieceName(entry.piece.getJigsawPiece());
            assembler.tryPlacingChildren(entry.piece, entry.voxel, entry.boundsTop, entry.depth, processedPiece);
        }

        ModFiddle.LOGGER.debug("Build structure with pieces: " + assembler.structurePieces.size());
        ModFiddle.LOGGER.debug("////// Generation Complete //////");
    }

    private static String getPieceName(JigsawPiece jigsawPiece) {
        SingleJigsawPiece singleJigsawPiece = (SingleJigsawPiece) jigsawPiece;
        return singleJigsawPiece.field_236839_c_.left().isPresent() ? singleJigsawPiece.field_236839_c_.left().get().getPath() : "";
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
            this.structurePieces = structurePieces;
            this.random = random;
        }

        public void tryPlacingChildren(AbstractVillagePiece piece, MutableObject<VoxelShape> voxelShape, int boundsTop, int depth, String processedPiece) {
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
                    if (tempNewPieceVoxelShape.getValue() == null)
                        tempNewPieceVoxelShape.setValue(VoxelShapes.create(AxisAlignedBB.toImmutable(pieceBoundingBox)));
                } else {
                    pieceVoxelShape = voxelShape;
                    targetPieceBoundsTop = boundsTop;
                }

                if (depth != this.maxDepth) {
                    // Process the pool pieces, randomly choosing different pieces from the pool to spawn
                    JigsawPiece generatedPiece = getRandomMatchingPoolPiece(new ArrayList<>(jigsawBlockTargetPool.get().getShuffledPieces(random)), jigsawBlock, jigsawBlockTargetPos, pieceMinY, jigsawBlockPos, pieceVoxelShape, piece, depth, targetPieceBoundsTop, processedPiece);
                    if (generatedPiece != null) continue;
                }

                // Process the fallback pieces in the event none of the pool pieces work
                this.getRandomMatchingPoolPiece(new ArrayList<>(jigsawBlockFallbackPool.get().getShuffledPieces(random)), jigsawBlock, jigsawBlockTargetPos, pieceMinY, jigsawBlockPos, pieceVoxelShape, piece, depth, targetPieceBoundsTop, processedPiece);
            }
        }

        private JigsawPiece getRandomJigsawPiece(List<JigsawPiece> candidatePieces) {
            int randomInt = ThreadLocalRandom.current().nextInt(0, candidatePieces.size());
            return candidatePieces.get(randomInt);
        }

        private JigsawPiece getRandomMatchingPoolPiece(List<JigsawPiece> candidatePieces, Template.BlockInfo jigsawBlock, BlockPos jigsawBlockTargetPos, int pieceMinY, BlockPos jigsawBlockPos, MutableObject<VoxelShape> pieceVoxelShape, AbstractVillagePiece piece, int depth, int targetPieceBoundsTop, String processedPiece) {
            JigsawPattern.PlacementBehaviour piecePlacementBehavior = piece.getJigsawPiece().getPlacementBehaviour();
            boolean isPieceRigid = piecePlacementBehavior == JigsawPattern.PlacementBehaviour.RIGID;
            int jigsawBlockRelativeY = jigsawBlockPos.getY() - pieceMinY;

            // Exhaustive search of candidate child piece
            while (!candidatePieces.isEmpty()) {
                // Check whether head piece, if so use only bridge deck pieces
                JigsawPiece candidatePiece = null;
                String pieceName = "";
                if (processedPiece.contains("head")) {
                    while (!pieceName.contains("walk")) {
                        candidatePiece = getRandomJigsawPiece(candidatePieces);
                        pieceName = getPieceName(candidatePiece);
                        candidatePieces.remove(candidatePiece);
                    }
                }
                else if (processedPiece.contains("walk")) {
                    while (!pieceName.contains("walk")) {
                        candidatePiece = getRandomJigsawPiece(candidatePieces);
                        pieceName = getPieceName(candidatePiece);
                        candidatePieces.remove(candidatePiece);
                    }
                    candidatePiece = getRandomJigsawPiece(candidatePieces);
                }
                else {
                    candidatePiece = getRandomJigsawPiece(candidatePieces);
                }

                ModFiddle.LOGGER.debug(pieceName);

                for (Rotation rotation : Rotation.shuffledRotations(this.random)) {
                    List<Template.BlockInfo> candidateJigsawBlocks = candidatePiece.getJigsawBlocks(this.templateManager, BlockPos.ZERO, rotation, this.random);

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
                            int surfaceHeight = -1; // The y-coordinate of the surface. Only used if isPieceRigid is false
                            int adjustedCandidatePieceMinY;
                            if (isPieceRigid && isCandidateRigid)
                                adjustedCandidatePieceMinY = pieceMinY + candidateJigsawYOffsetNeeded;
                            else {
                                surfaceHeight = this.chunkGenerator.getNoiseHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                adjustedCandidatePieceMinY = surfaceHeight - candidateJigsawBlockRelativeY;
                            }
                            int candidatePieceYOffsetNeeded = adjustedCandidatePieceMinY - candidateBoundingBox.minY;

                            // Offset the candidate's bounding box by the necessary amount
                            MutableBoundingBox adjustedCandidateBoundingBox = candidateBoundingBox.func_215127_b(0, candidatePieceYOffsetNeeded, 0);
                            // Add this offset to the relative jigsaw block position as well
                            BlockPos adjustedCandidateJigsawBlockRelativePos = candidateJigsawBlockRelativePos.add(0, candidatePieceYOffsetNeeded, 0);

                            // Comparing if adding the piece clips through already existing structure
                            if (!VoxelShapes.compare(pieceVoxelShape.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(adjustedCandidateBoundingBox).shrink(0.25D)), IBooleanFunction.ONLY_SECOND)) {
                                pieceVoxelShape.setValue(VoxelShapes.combine(pieceVoxelShape.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(adjustedCandidateBoundingBox)),  IBooleanFunction.ONLY_FIRST));

                                // Determine ground level delta for this new piece
                                int newPieceGroundLevelDelta = piece.getGroundLevelDelta();
                                int groundLevelDelta = isCandidateRigid ? newPieceGroundLevelDelta - candidateJigsawYOffsetNeeded : candidatePiece.getGroundLevelDelta();

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
                                if (isPieceRigid)
                                    candidateJigsawBlockY = pieceMinY + jigsawBlockRelativeY;
                                else if (isCandidateRigid)
                                    candidateJigsawBlockY = adjustedCandidatePieceMinY + candidateJigsawBlockRelativeY;
                                else {
                                    if (surfaceHeight == -1)
                                        surfaceHeight = this.chunkGenerator.getNoiseHeight(jigsawBlockPos.getX(), jigsawBlockPos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
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

                                return candidatePiece;
                            }
                        }
                    }
                }

                candidatePieces.remove(candidatePiece);
            }
            return null;
        }
    }

    static final class Entry {
        private final AbstractVillagePiece piece;
        private final MutableObject<VoxelShape> voxel;
        private final int boundsTop;
        private final int depth;

        private Entry(AbstractVillagePiece piece, MutableObject<VoxelShape> voxel, int boundsTop, int depth) {
            this.piece = piece;
            this.voxel = voxel;
            this.boundsTop = boundsTop;
            this.depth = depth;
        }
    }
}
