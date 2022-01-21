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
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AltJigsawManager {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void func_242837_a(DynamicRegistries p_242837_0_, VillageConfig p_242837_1_, AltJigsawManager.IPieceFactory p_242837_2_, ChunkGenerator p_242837_3_, TemplateManager p_242837_4_, BlockPos p_242837_5_, List<? super AbstractVillagePiece> p_242837_6_, Random p_242837_7_, boolean p_242837_8_, boolean p_242837_9_) {
        Structure.init();
        MutableRegistry<JigsawPattern> lvt_10_1_ = p_242837_0_.getRegistry(Registry.JIGSAW_POOL_KEY);
        Rotation lvt_11_1_ = Rotation.randomRotation(p_242837_7_);
        JigsawPattern lvt_12_1_ = (JigsawPattern)p_242837_1_.func_242810_c().get();
        JigsawPiece lvt_13_1_ = lvt_12_1_.getRandomPiece(p_242837_7_);
        AbstractVillagePiece lvt_14_1_ = p_242837_2_.create(p_242837_4_, lvt_13_1_, p_242837_5_, lvt_13_1_.getGroundLevelDelta(), lvt_11_1_, lvt_13_1_.getBoundingBox(p_242837_4_, p_242837_5_, lvt_11_1_));
        MutableBoundingBox lvt_15_1_ = lvt_14_1_.getBoundingBox();
        int lvt_16_1_ = (lvt_15_1_.maxX + lvt_15_1_.minX) / 2;
        int lvt_17_1_ = (lvt_15_1_.maxZ + lvt_15_1_.minZ) / 2;
        int lvt_18_2_;
        if (p_242837_9_) {
            lvt_18_2_ = p_242837_5_.getY() + p_242837_3_.getNoiseHeight(lvt_16_1_, lvt_17_1_, Heightmap.Type.WORLD_SURFACE_WG);
        } else {
            lvt_18_2_ = p_242837_5_.getY();
        }

        int lvt_19_1_ = lvt_15_1_.minY + lvt_14_1_.getGroundLevelDelta();
        lvt_14_1_.offset(0, lvt_18_2_ - lvt_19_1_, 0);
        p_242837_6_.add(lvt_14_1_);
        if (p_242837_1_.func_236534_a_() > 0) {
            AxisAlignedBB lvt_21_1_ = new AxisAlignedBB((double)(lvt_16_1_ - 80), (double)(lvt_18_2_ - 80), (double)(lvt_17_1_ - 80), (double)(lvt_16_1_ + 80 + 1), (double)(lvt_18_2_ + 80 + 1), (double)(lvt_17_1_ + 80 + 1));
            AltJigsawManager.Assembler lvt_22_1_ = new AltJigsawManager.Assembler(lvt_10_1_, p_242837_1_.func_236534_a_(), p_242837_2_, p_242837_3_, p_242837_4_, p_242837_6_, p_242837_7_);
            lvt_22_1_.availablePieces.addLast(new AltJigsawManager.Entry(lvt_14_1_, new MutableObject(VoxelShapes.combineAndSimplify(VoxelShapes.create(lvt_21_1_), VoxelShapes.create(AxisAlignedBB.toImmutable(lvt_15_1_)), IBooleanFunction.ONLY_FIRST)), lvt_18_2_ + 80, 0));

            while(!lvt_22_1_.availablePieces.isEmpty()) {
                AltJigsawManager.Entry lvt_23_1_ = (AltJigsawManager.Entry)lvt_22_1_.availablePieces.removeFirst();
                lvt_22_1_.func_236831_a_(lvt_23_1_.villagePiece, lvt_23_1_.free, lvt_23_1_.boundsTop, lvt_23_1_.depth, p_242837_8_);
            }

        }
    }

    public static void func_242838_a(DynamicRegistries p_242838_0_, AbstractVillagePiece p_242838_1_, int p_242838_2_, AltJigsawManager.IPieceFactory p_242838_3_, ChunkGenerator p_242838_4_, TemplateManager p_242838_5_, List<? super AbstractVillagePiece> p_242838_6_, Random p_242838_7_) {
        MutableRegistry<JigsawPattern> lvt_8_1_ = p_242838_0_.getRegistry(Registry.JIGSAW_POOL_KEY);
        AltJigsawManager.Assembler lvt_9_1_ = new AltJigsawManager.Assembler(lvt_8_1_, p_242838_2_, p_242838_3_, p_242838_4_, p_242838_5_, p_242838_6_, p_242838_7_);
        lvt_9_1_.availablePieces.addLast(new AltJigsawManager.Entry(p_242838_1_, new MutableObject(VoxelShapes.INFINITY), 0, 0));

        while(!lvt_9_1_.availablePieces.isEmpty()) {
            AltJigsawManager.Entry lvt_10_1_ = (AltJigsawManager.Entry)lvt_9_1_.availablePieces.removeFirst();
            lvt_9_1_.func_236831_a_(lvt_10_1_.villagePiece, lvt_10_1_.free, lvt_10_1_.boundsTop, lvt_10_1_.depth, false);
        }

    }

    public interface IPieceFactory {
        AbstractVillagePiece create(TemplateManager var1, JigsawPiece var2, BlockPos var3, int var4, Rotation var5, MutableBoundingBox var6);
    }

    static final class Assembler {
        private final Registry<JigsawPattern> field_242839_a;
        private final int maxDepth;
        private final AltJigsawManager.IPieceFactory pieceFactory;
        private final ChunkGenerator chunkGenerator;
        private final TemplateManager templateManager;
        private final List<? super AbstractVillagePiece> structurePieces;
        private final Random rand;
        private final Deque<AltJigsawManager.Entry> availablePieces;

        private Assembler(Registry<JigsawPattern> p_i242005_1_, int p_i242005_2_, AltJigsawManager.IPieceFactory p_i242005_3_, ChunkGenerator p_i242005_4_, TemplateManager p_i242005_5_, List<? super AbstractVillagePiece> p_i242005_6_, Random p_i242005_7_) {
            this.availablePieces = Queues.newArrayDeque();
            this.field_242839_a = p_i242005_1_;
            this.maxDepth = p_i242005_2_;
            this.pieceFactory = p_i242005_3_;
            this.chunkGenerator = p_i242005_4_;
            this.templateManager = p_i242005_5_;
            this.structurePieces = p_i242005_6_;
            this.rand = p_i242005_7_;
        }

        private void func_236831_a_(AbstractVillagePiece p_236831_1_, MutableObject<VoxelShape> p_236831_2_, int p_236831_3_, int p_236831_4_, boolean p_236831_5_) {
            JigsawPiece lvt_6_1_ = p_236831_1_.getJigsawPiece();
            BlockPos lvt_7_1_ = p_236831_1_.getPos();
            Rotation lvt_8_1_ = p_236831_1_.getRotation();
            JigsawPattern.PlacementBehaviour lvt_9_1_ = lvt_6_1_.getPlacementBehaviour();
            boolean lvt_10_1_ = lvt_9_1_ == JigsawPattern.PlacementBehaviour.RIGID;
            MutableObject<VoxelShape> lvt_11_1_ = new MutableObject();
            MutableBoundingBox lvt_12_1_ = p_236831_1_.getBoundingBox();
            int lvt_13_1_ = lvt_12_1_.minY;
            Iterator var14 = lvt_6_1_.getJigsawBlocks(this.templateManager, lvt_7_1_, lvt_8_1_, this.rand).iterator();

            while(true) {
                while(true) {
                    while(true) {
                        label93:
                        while(var14.hasNext()) {
                            Template.BlockInfo lvt_15_1_ = (Template.BlockInfo)var14.next();
                            Direction lvt_16_1_ = JigsawBlock.getConnectingDirection(lvt_15_1_.state);
                            BlockPos lvt_17_1_ = lvt_15_1_.pos;
                            BlockPos lvt_18_1_ = lvt_17_1_.offset(lvt_16_1_);
                            int lvt_19_1_ = lvt_17_1_.getY() - lvt_13_1_;
                            int lvt_20_1_ = -1;
                            ResourceLocation lvt_21_1_ = new ResourceLocation(lvt_15_1_.nbt.getString("pool"));
                            Optional<JigsawPattern> lvt_22_1_ = this.field_242839_a.getOptional(lvt_21_1_);
                            if (lvt_22_1_.isPresent() && (((JigsawPattern)lvt_22_1_.get()).getNumberOfPieces() != 0 || Objects.equals(lvt_21_1_, JigsawPatternRegistry.field_244091_a.getLocation()))) {
                                ResourceLocation lvt_23_1_ = ((JigsawPattern)lvt_22_1_.get()).getFallback();
                                Optional<JigsawPattern> lvt_24_1_ = this.field_242839_a.getOptional(lvt_23_1_);
                                if (lvt_24_1_.isPresent() && (((JigsawPattern)lvt_24_1_.get()).getNumberOfPieces() != 0 || Objects.equals(lvt_23_1_, JigsawPatternRegistry.field_244091_a.getLocation()))) {
                                    boolean lvt_27_1_ = lvt_12_1_.isVecInside(lvt_18_1_);
                                    MutableObject lvt_25_2_;
                                    int lvt_26_2_;
                                    if (lvt_27_1_) {
                                        lvt_25_2_ = lvt_11_1_;
                                        lvt_26_2_ = lvt_13_1_;
                                        if (lvt_11_1_.getValue() == null) {
                                            lvt_11_1_.setValue(VoxelShapes.create(AxisAlignedBB.toImmutable(lvt_12_1_)));
                                        }
                                    } else {
                                        lvt_25_2_ = p_236831_2_;
                                        lvt_26_2_ = p_236831_3_;
                                    }

                                    List<JigsawPiece> lvt_28_1_ = Lists.newArrayList();
                                    if (p_236831_4_ != this.maxDepth) {
                                        lvt_28_1_.addAll(((JigsawPattern)lvt_22_1_.get()).getShuffledPieces(this.rand));
                                    }

                                    lvt_28_1_.addAll(((JigsawPattern)lvt_24_1_.get()).getShuffledPieces(this.rand));
                                    Iterator var29 = lvt_28_1_.iterator();

                                    while(var29.hasNext()) {
                                        JigsawPiece lvt_30_1_ = (JigsawPiece)var29.next();
                                        if (lvt_30_1_ == EmptyJigsawPiece.INSTANCE) {
                                            break;
                                        }

                                        Iterator var31 = Rotation.shuffledRotations(this.rand).iterator();

                                        label133:
                                        while(var31.hasNext()) {
                                            Rotation lvt_32_1_ = (Rotation)var31.next();
                                            List<Template.BlockInfo> lvt_33_1_ = lvt_30_1_.getJigsawBlocks(this.templateManager, BlockPos.ZERO, lvt_32_1_, this.rand);
                                            MutableBoundingBox lvt_34_1_ = lvt_30_1_.getBoundingBox(this.templateManager, BlockPos.ZERO, lvt_32_1_);
                                            int lvt_35_2_;
                                            if (p_236831_5_ && lvt_34_1_.getYSize() <= 16) {
                                                lvt_35_2_ = lvt_33_1_.stream().mapToInt((p_242841_2_) -> {
                                                    if (!lvt_34_1_.isVecInside(p_242841_2_.pos.offset(JigsawBlock.getConnectingDirection(p_242841_2_.state)))) {
                                                        return 0;
                                                    } else {
                                                        ResourceLocation lvt_3_1_ = new ResourceLocation(p_242841_2_.nbt.getString("pool"));
                                                        Optional<JigsawPattern> lvt_4_1_ = this.field_242839_a.getOptional(lvt_3_1_);
                                                        Optional<JigsawPattern> lvt_5_1_ = lvt_4_1_.flatMap((p_242843_1_) -> {
                                                            return this.field_242839_a.getOptional(p_242843_1_.getFallback());
                                                        });
                                                        int lvt_6_1_a = (Integer)lvt_4_1_.map((p_242842_1_) -> {
                                                            return p_242842_1_.getMaxSize(this.templateManager);
                                                        }).orElse(0);
                                                        int lvt_7_1_a = (Integer)lvt_5_1_.map((p_242840_1_) -> {
                                                            return p_242840_1_.getMaxSize(this.templateManager);
                                                        }).orElse(0);
                                                        return Math.max(lvt_6_1_a, lvt_7_1_a);
                                                    }
                                                }).max().orElse(0);
                                            } else {
                                                lvt_35_2_ = 0;
                                            }

                                            Iterator var36 = lvt_33_1_.iterator();

                                            JigsawPattern.PlacementBehaviour lvt_42_1_;
                                            boolean lvt_43_1_;
                                            int lvt_44_1_;
                                            int lvt_45_1_;
                                            int lvt_46_2_;
                                            MutableBoundingBox lvt_48_1_;
                                            BlockPos lvt_49_1_;
                                            int lvt_50_2_;
                                            do {
                                                Template.BlockInfo lvt_37_1_;
                                                do {
                                                    if (!var36.hasNext()) {
                                                        continue label133;
                                                    }

                                                    lvt_37_1_ = (Template.BlockInfo)var36.next();
                                                } while(!JigsawBlock.hasJigsawMatch(lvt_15_1_, lvt_37_1_));

                                                BlockPos lvt_38_1_ = lvt_37_1_.pos;
                                                BlockPos lvt_39_1_ = new BlockPos(lvt_18_1_.getX() - lvt_38_1_.getX(), lvt_18_1_.getY() - lvt_38_1_.getY(), lvt_18_1_.getZ() - lvt_38_1_.getZ());
                                                MutableBoundingBox lvt_40_1_ = lvt_30_1_.getBoundingBox(this.templateManager, lvt_39_1_, lvt_32_1_);
                                                int lvt_41_1_ = lvt_40_1_.minY;
                                                lvt_42_1_ = lvt_30_1_.getPlacementBehaviour();
                                                lvt_43_1_ = lvt_42_1_ == JigsawPattern.PlacementBehaviour.RIGID;
                                                lvt_44_1_ = lvt_38_1_.getY();
                                                lvt_45_1_ = lvt_19_1_ - lvt_44_1_ + JigsawBlock.getConnectingDirection(lvt_15_1_.state).getYOffset();
                                                if (lvt_10_1_ && lvt_43_1_) {
                                                    lvt_46_2_ = lvt_13_1_ + lvt_45_1_;
                                                } else {
                                                    if (lvt_20_1_ == -1) {
                                                        lvt_20_1_ = this.chunkGenerator.getNoiseHeight(lvt_17_1_.getX(), lvt_17_1_.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                                    }

                                                    lvt_46_2_ = lvt_20_1_ - lvt_44_1_;
                                                }

                                                int lvt_47_1_ = lvt_46_2_ - lvt_41_1_;
                                                lvt_48_1_ = lvt_40_1_.func_215127_b(0, lvt_47_1_, 0);
                                                lvt_49_1_ = lvt_39_1_.add(0, lvt_47_1_, 0);
                                                if (lvt_35_2_ > 0) {
                                                    lvt_50_2_ = Math.max(lvt_35_2_ + 1, lvt_48_1_.maxY - lvt_48_1_.minY);
                                                    lvt_48_1_.maxY = lvt_48_1_.minY + lvt_50_2_;
                                                }
                                            } while(VoxelShapes.compare((VoxelShape)lvt_25_2_.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(lvt_48_1_).shrink(0.25D)), IBooleanFunction.ONLY_SECOND));

                                            lvt_25_2_.setValue(VoxelShapes.combine((VoxelShape)lvt_25_2_.getValue(), VoxelShapes.create(AxisAlignedBB.toImmutable(lvt_48_1_)), IBooleanFunction.ONLY_FIRST));
                                            lvt_50_2_ = p_236831_1_.getGroundLevelDelta();
                                            int lvt_51_2_;
                                            if (lvt_43_1_) {
                                                lvt_51_2_ = lvt_50_2_ - lvt_45_1_;
                                            } else {
                                                lvt_51_2_ = lvt_30_1_.getGroundLevelDelta();
                                            }

                                            AbstractVillagePiece lvt_52_1_ = this.pieceFactory.create(this.templateManager, lvt_30_1_, lvt_49_1_, lvt_51_2_, lvt_32_1_, lvt_48_1_);
                                            int lvt_53_3_;
                                            if (lvt_10_1_) {
                                                lvt_53_3_ = lvt_13_1_ + lvt_19_1_;
                                            } else if (lvt_43_1_) {
                                                lvt_53_3_ = lvt_46_2_ + lvt_44_1_;
                                            } else {
                                                if (lvt_20_1_ == -1) {
                                                    lvt_20_1_ = this.chunkGenerator.getNoiseHeight(lvt_17_1_.getX(), lvt_17_1_.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
                                                }

                                                lvt_53_3_ = lvt_20_1_ + lvt_45_1_ / 2;
                                            }

                                            p_236831_1_.addJunction(new JigsawJunction(lvt_18_1_.getX(), lvt_53_3_ - lvt_19_1_ + lvt_50_2_, lvt_18_1_.getZ(), lvt_45_1_, lvt_42_1_));
                                            lvt_52_1_.addJunction(new JigsawJunction(lvt_17_1_.getX(), lvt_53_3_ - lvt_44_1_ + lvt_51_2_, lvt_17_1_.getZ(), -lvt_45_1_, lvt_9_1_));
                                            this.structurePieces.add(lvt_52_1_);
                                            if (p_236831_4_ + 1 <= this.maxDepth) {
                                                this.availablePieces.addLast(new AltJigsawManager.Entry(lvt_52_1_, lvt_25_2_, lvt_26_2_, p_236831_4_ + 1));
                                            }
                                            continue label93;
                                        }
                                    }
                                } else {
                                    ModFiddle.LOGGER.warn("Empty or none existent fallback pool: {}", lvt_23_1_);
                                }
                            } else {
                                ModFiddle.LOGGER.warn("Empty or none existent pool: {}", lvt_21_1_);
                            }
                        }

                        return;
                    }
                }
            }
        }
    }

    static final class Entry {
        private final AbstractVillagePiece villagePiece;
        private final MutableObject<VoxelShape> free;
        private final int boundsTop;
        private final int depth;

        private Entry(AbstractVillagePiece p_i232042_1_, MutableObject<VoxelShape> p_i232042_2_, int p_i232042_3_, int p_i232042_4_) {
            this.villagePiece = p_i232042_1_;
            this.free = p_i232042_2_;
            this.boundsTop = p_i232042_3_;
            this.depth = p_i232042_4_;
        }
    }
}
