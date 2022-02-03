package com.greffgreff.modfiddle.world.structure.structures.bridge.pieces;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
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
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractBridgePiece extends JigsawPiece {
    public final DynamicRegistries dynamicRegistries;
    public final MutableRegistry<JigsawPattern> jigsawPoolRegistry;
    public final ChunkGenerator chunkGenerator;
    public final TemplateManager templateManager;
    public final BlockPos startingPosition;
    public final List<StructurePiece> structurePieces;
    public final Random random;

    public AbstractBridgePiece(DynamicRegistries dynamicRegistries, ChunkGenerator chunkGenerator, TemplateManager templateManager, BlockPos startingPosition, List<StructurePiece> structurePieces, Random random) {
        super(JigsawPattern.PlacementBehaviour.RIGID);
        this.dynamicRegistries = dynamicRegistries;
        this.jigsawPoolRegistry = dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY);
        this.chunkGenerator = chunkGenerator;
        this.templateManager = templateManager;
        this.startingPosition = startingPosition;
        this.structurePieces = structurePieces;
        this.random = random;
    }

    protected JigsawPattern getPool(ResourceLocation resourceLocation) {
        Supplier<JigsawPattern> piecesPool = () -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(resourceLocation);
        return piecesPool.get();
    }

    protected static String getPieceName(JigsawPiece jigsawPiece) {
        if (jigsawPiece == null) return "";
        SingleJigsawPiece singleJigsawPiece = (SingleJigsawPiece) jigsawPiece;
        return singleJigsawPiece.field_236839_c_.left().isPresent() ? singleJigsawPiece.field_236839_c_.left().get().getPath() : "";
    }

    protected AbstractVillagePiece createAbstractPiece(JigsawPiece piece, BlockPos position, Rotation rotation) {
        return new AbstractVillagePiece(templateManager, piece, position, piece.getGroundLevelDelta(), rotation, piece.getBoundingBox(templateManager, position, rotation));
    }

    protected abstract void createPiece();

    @Override
    public List<Template.BlockInfo> getJigsawBlocks(TemplateManager templateManager, BlockPos blockPos, Rotation rotation, Random random) {
        // could either return all jigsaw blocks and treat jigsaw junctions OR return any jigsaw blocks adjastent to the piece BB

        List<Template.BlockInfo> jigsawBlocks = new ArrayList<>();
        for (StructurePiece structurePiece: this.structurePieces) {
            JigsawPiece jigsawPiece = ((AbstractVillagePiece) structurePiece).getJigsawPiece();
            jigsawBlocks.addAll(jigsawPiece.getJigsawBlocks(templateManager, blockPos, rotation, random));
        }
        return jigsawBlocks;
    }

    @Override
    public MutableBoundingBox getBoundingBox(TemplateManager templateManager, BlockPos blockPos, Rotation rotation) {
        MutableBoundingBox mutableboundingbox = MutableBoundingBox.getNewBoundingBox();
        for(StructurePiece structurePiece : this.structurePieces)
            mutableboundingbox.expandTo(structurePiece.getBoundingBox());
        return mutableboundingbox;
    }

    @Override // ??
    public boolean func_230378_a_(TemplateManager templateManager, ISeedReader iSeedReader, StructureManager structureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos1, Rotation rotation, MutableBoundingBox mutableBoundingBox, Random random, boolean b) {
        return true;
    }

    @Override
    public IJigsawDeserializer<?> getType() {
        return IJigsawDeserializer.LIST_POOL_ELEMENT;
    }
}