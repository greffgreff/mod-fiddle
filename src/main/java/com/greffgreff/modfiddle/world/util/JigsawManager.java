package com.greffgreff.modfiddle.world.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

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
    public static void addPieces(DynamicRegistries dynamicregistries, VillageConfig villageConfig, ChunkGenerator chunkgenerator, TemplateManager templatemanager, BlockPos blockpos, List<? super AbstractVillagePiece> structurepieces, Random rand, boolean villagepiece, boolean useHeightMap) {
        Rotation rotation = Rotation.randomRotation(rand);

        MutableRegistry<JigsawPattern> mutableregistry = dynamicregistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        JigsawPattern jigsawpattern = (JigsawPattern)villageConfig.func_242810_c().get();

        JigsawPiece jigsawpiece = jigsawpattern.getRandomPiece(rand);
        AbstractVillagePiece piece =  new AbstractVillagePiece(templatemanager, jigsawpiece, blockpos, jigsawpiece.getGroundLevelDelta(), rotation, jigsawpiece.getBoundingBox(templatemanager, blockpos, rotation));

        MutableBoundingBox mutableboundingbox = piece.getBoundingBox();
        int i = (mutableboundingbox.maxX + mutableboundingbox.minX) / 2;
        int j = (mutableboundingbox.maxZ + mutableboundingbox.minZ) / 2;
        int k = useHeightMap ? blockpos.getY() + chunkgenerator.getNoiseHeight(i, j, Heightmap.Type.WORLD_SURFACE_WG) : blockpos.getY();

        int l = mutableboundingbox.minY + piece.getGroundLevelDelta();
        piece.offset(0, k - l, 0);

        structurepieces.add(piece);

        if (villageConfig.func_236534_a_() > 0) {
            AxisAlignedBB axisalignedbb = new AxisAlignedBB((double)(i - 80), (double)(k - 80), (double)(j - 80), (double)(i + 80 + 1), (double)(k + 80 + 1), (double)(j + 80 + 1));

            Assembler assembler = new Assembler(mutableregistry, villageConfig.func_236534_a_(), chunkgenerator, templatemanager, structurepieces, rand);
            assembler.availablePieces.addLast(new Entry(piece, new MutableObject(VoxelShapes.combineAndSimplify(VoxelShapes.create(axisalignedbb), VoxelShapes.create(AxisAlignedBB.toImmutable(mutableboundingbox)), IBooleanFunction.ONLY_FIRST)), k + 80, 0));

            while(!assembler.availablePieces.isEmpty()) {
                Entry jigsawmanager$entry = (Entry)assembler.availablePieces.removeFirst();
                assembler.tryPlacingChildren(jigsawmanager$entry.piece, jigsawmanager$entry.free, jigsawmanager$entry.boundstop, jigsawmanager$entry.depth, villagepiece);
            }
        }
    }

    public static void addPieces(DynamicRegistries dynamicregistries, AbstractVillagePiece villagepiece, int maxdepth, ChunkGenerator chunkgenerator, TemplateManager templatemanager, List<? super AbstractVillagePiece> structurepieces, Random rand) {
        MutableRegistry<JigsawPattern> availablepieces = dynamicregistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        Assembler assembler = new Assembler(availablepieces, maxdepth, chunkgenerator, templatemanager, structurepieces, rand);
        assembler.availablePieces.addLast(new Entry(villagepiece, new MutableObject(VoxelShapes.INFINITY), 0, 0));

        while(!assembler.availablePieces.isEmpty()) {
            Entry mutableregistry = (Entry)assembler.availablePieces.removeFirst();
            assembler.tryPlacingChildren(mutableregistry.piece, mutableregistry.free, mutableregistry.boundstop, mutableregistry.depth, false);
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

        private Assembler(Registry<JigsawPattern> pools, int maxdepth, ChunkGenerator chunkgenerator, TemplateManager templatemanager, List<? super AbstractVillagePiece> pieces, Random rand) {
            this.availablePieces = Queues.newArrayDeque();
            this.pools = pools;
            this.maxDepth = maxdepth;
            this.chunkGenerator = chunkgenerator;
            this.structureManager = templatemanager;
            this.pieces = pieces;
            this.rand = rand;
        }

        private void tryPlacingChildren(AbstractVillagePiece p_236831_1_, MutableObject<VoxelShape> p_236831_2_, int p_236831_3_, int p_236831_4_, boolean p_236831_5_) {
            JigsawPiece jigsawpiece = p_236831_1_.getJigsawPiece();
            BlockPos blockpos = p_236831_1_.getPos();
            Rotation rotation = p_236831_1_.getRotation();
            JigsawPattern.PlacementBehaviour jigsawpattern$placementbehaviour = jigsawpiece.getPlacementBehaviour();
            boolean flag = jigsawpattern$placementbehaviour == JigsawPattern.PlacementBehaviour.RIGID;
            MutableObject<VoxelShape> mutableobject = new MutableObject<>();
            MutableBoundingBox mutableboundingbox = p_236831_1_.getBoundingBox();
            int i = mutableboundingbox.minY; // was .y0

            label139:
            for(Template.BlockInfo template$blockinfo : jigsawpiece.getJigsawBlocks(this.structureManager, blockpos, rotation, this.rand)) {
                Direction direction = JigsawBlock.getConnectingDirection(template$blockinfo.state);
                BlockPos blockpos1 = template$blockinfo.pos;
                BlockPos blockpos2 = blockpos1.offset(direction);
                int j = blockpos1.getY() - i;
                int k = -1;

                ResourceLocation resourcelocation = new ResourceLocation(template$blockinfo.nbt.getString("pool"));
                Optional<JigsawPattern> optional = this.pools.getOptional(resourcelocation);
                if (optional.isPresent() && (optional.get().getNumberOfPieces() != 0 || Objects.equals(resourcelocation, JigsawPatternRegistry.field_244091_a.getLocation()))) { // was EMPTY.location()
                    ResourceLocation resourcelocation1 = optional.get().getFallback();
                    Optional<JigsawPattern> optional1 = this.pools.getOptional(resourcelocation1);
                    if (optional1.isPresent() && (optional1.get().getNumberOfPieces() != 0 || Objects.equals(resourcelocation1, JigsawPatternRegistry.field_244091_a.getLocation()))) { // was EMPTY.location()
                        boolean flag1 = mutableboundingbox.isVecInside(blockpos2);
                        MutableObject<VoxelShape> mutableobject1;
                        int l;
                        if (flag1) {
                            mutableobject1 = mutableobject;
                            l = i;
                            if (mutableobject.getValue() == null) {
                                mutableobject.setValue(VoxelShapes.create(AxisAlignedBB.toImmutable(mutableboundingbox)));
                            }
                        } else {
                            mutableobject1 = p_236831_2_;
                            l = p_236831_3_;
                        }

                        List<JigsawPiece> list = Lists.newArrayList();
                        if (p_236831_4_ != this.maxDepth) {
                            list.addAll(optional.get().getShuffledPieces(this.rand));
                        }

                        list.addAll(optional1.get().getShuffledPieces(this.rand));

                        for(JigsawPiece jigsawpiece1 : list) {
                            if (jigsawpiece1 == EmptyJigsawPiece.INSTANCE) {
                                break;
                            }

                            for(Rotation rotation1 : Rotation.shuffledRotations(this.rand)) {
                                List<Template.BlockInfo> list1 = jigsawpiece1.getJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation1, this.rand);
                                MutableBoundingBox mutableboundingbox1 = jigsawpiece1.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation1);
                                int i1;
                                if (p_236831_5_ && mutableboundingbox1.getYSize() <= 16) { // was .getYSpan
                                    i1 = list1.stream().mapToInt((p_242841_2_) -> {
                                        if (!mutableboundingbox1.isVecInside(p_242841_2_.pos.offset(JigsawBlock.getConnectingDirection(p_242841_2_.state)))) { // was .relative()
                                            return 0;
                                        } else {
                                            ResourceLocation resourcelocation2 = new ResourceLocation(p_242841_2_.nbt.getString("pool"));
                                            Optional<JigsawPattern> optional2 = this.pools.getOptional(resourcelocation2);
                                            Optional<JigsawPattern> optional3 = optional2.flatMap((p_242843_1_) -> {
                                                return this.pools.getOptional(p_242843_1_.getFallback());
                                            });
                                            int k3 = optional2.map((p_242842_1_) -> {
                                                return p_242842_1_.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            int l3 = optional3.map((p_242840_1_) -> {
                                                return p_242840_1_.getMaxSize(this.structureManager);
                                            }).orElse(0);
                                            return Math.max(k3, l3);
                                        }
                                    }).max().orElse(0);
                                } else {
                                    i1 = 0;
                                }

                                for(Template.BlockInfo template$blockinfo1 : list1) {
                                    if (JigsawBlock.hasJigsawMatch(template$blockinfo, template$blockinfo1)) {
                                        BlockPos blockpos3 = template$blockinfo1.pos;
                                        BlockPos blockpos4 = new BlockPos(blockpos2.getX() - blockpos3.getX(), blockpos2.getY() - blockpos3.getY(), blockpos2.getZ() - blockpos3.getZ());
                                        MutableBoundingBox mutableboundingbox2 = jigsawpiece1.getBoundingBox(this.structureManager, blockpos4, rotation1);
                                        int j1 = mutableboundingbox2.minY;
                                        JigsawPattern.PlacementBehaviour jigsawpattern$placementbehaviour1 = jigsawpiece1.getPlacementBehaviour();
                                        boolean flag2 = jigsawpattern$placementbehaviour1 == JigsawPattern.PlacementBehaviour.RIGID;
                                        int k1 = blockpos3.getY();
                                        int l1 = j - k1 + JigsawBlock.getConnectingDirection(template$blockinfo.state).getYOffset();
                                        int i2;
                                        if (flag && flag2) {
                                            i2 = i + l1;
                                        } else {
                                            if (k == -1) {
                                                k = this.chunkGenerator.getNoiseHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                            }

                                            i2 = k - k1;
                                        }

                                        int j2 = i2 - j1;
                                        MutableBoundingBox mutableboundingbox3 = mutableboundingbox2.func_215127_b(0, j2, 0); // was .moved()
                                        BlockPos blockpos5 = blockpos4.add(0, j2, 0);
                                        if (i1 > 0) {
                                            int k2 = Math.max(i1 + 1, mutableboundingbox3.maxY - mutableboundingbox3.minY);
                                            mutableboundingbox3.maxY = mutableboundingbox3.minY + k2;
                                        }

                                        // joinIsNotEmpty = compare
                                        // joinUnoptimized = combine
                                        if (!VoxelShapes.compare(mutableobject1.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(mutableboundingbox3).shrink(0.25D)), IBooleanFunction.ONLY_SECOND)) {
                                            mutableobject1.setValue(VoxelShapes.combine(mutableobject1.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(mutableboundingbox3)), IBooleanFunction.ONLY_FIRST));
                                            int j3 = p_236831_1_.getGroundLevelDelta();
                                            int l2;
                                            if (flag2) {
                                                l2 = j3 - l1;
                                            } else {
                                                l2 = jigsawpiece1.getGroundLevelDelta();
                                            }

                                            AbstractVillagePiece abstractvillagepiece = new AbstractVillagePiece(this.structureManager, jigsawpiece1, blockpos5, l2, rotation1, mutableboundingbox3);
                                            int i3;
                                            if (flag) {
                                                i3 = i + j;
                                            } else if (flag2) {
                                                i3 = i2 + k1;
                                            } else {
                                                if (k == -1) {
                                                    k = this.chunkGenerator.getNoiseHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                                }

                                                i3 = k + l1 / 2;
                                            }

                                            p_236831_1_.addJunction(new JigsawJunction(blockpos2.getX(), i3 - j + j3, blockpos2.getZ(), l1, jigsawpattern$placementbehaviour1));
                                            abstractvillagepiece.addJunction(new JigsawJunction(blockpos1.getX(), i3 - k1 + l2, blockpos1.getZ(), -l1, jigsawpattern$placementbehaviour));
                                            this.pieces.add(abstractvillagepiece);
                                            if (p_236831_4_ + 1 <= this.maxDepth) {
                                                this.availablePieces.addLast(new Entry(abstractvillagepiece, mutableobject1, l, p_236831_4_ + 1)); // was pieces (from availablePieces)
                                            }
                                            continue label139;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        ModFiddle.LOGGER.warn("Empty or none existent fallback pool: {}", (Object)resourcelocation1);
                    }
                } else {
                    ModFiddle.LOGGER.warn("Empty or none existent pool: {}", (Object)resourcelocation);
                }
            }

        }
    }

    static final class Entry {
        private final AbstractVillagePiece piece;
        private final MutableObject<VoxelShape> free;
        private final int boundstop;
        private final int depth;

        private Entry(AbstractVillagePiece piece, MutableObject<VoxelShape> free, int topbounds, int depth) {
            this.piece = piece;
            this.free = free;
            this.boundstop = topbounds;
            this.depth = depth;
        }
    }
}