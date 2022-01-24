package com.greffgreff.modfiddle.world.util;

import com.greffgreff.modfiddle.ModFiddle;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.List;
import java.util.Random;

public class JigsawGeneration {
    public final ResourceLocation resourceLocation;
    public final DynamicRegistries dynamicRegistries;
    public final VillageConfig config;

    public JigsawGeneration(ResourceLocation resourceLocation, DynamicRegistries dynamicRegistries) {
        this.dynamicRegistries = dynamicRegistries;
        this.resourceLocation = resourceLocation;
        this.config =  new VillageConfig(() -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(resourceLocation), 10);
    }

    public void addPieces(ResourceLocation resourceLocation, TemplateManager templateManager, ChunkGenerator chunkGenerator, BlockPos startPos, Rotation rotation, List<StructurePiece> pieces, Random random) {
        ModFiddle.LOGGER.debug("addPiece");
        Structure.init();

        Piece startPiece = new Piece(templateManager, this.resourceLocation, rotation);

        MutableBoundingBox pieceBoundingBox = startPiece.getBoundingBox();
        int pieceCenterX = (pieceBoundingBox.maxX + pieceBoundingBox.minX) / 2;
        int pieceCenterZ = (pieceBoundingBox.maxZ + pieceBoundingBox.minZ) / 2;

        boolean useHeightmap = false;
        int pieceCenterY = useHeightmap
                ? startPos.getY() + chunkGenerator.getNoiseHeight(pieceCenterX, pieceCenterZ, Heightmap.Type.WORLD_SURFACE_WG)
                : startPos.getY();

        startPiece.offset(0, pieceCenterY, 0);

        pieces.add(startPiece);
    }

    public class Piece extends TemplateStructurePiece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public Piece(TemplateManager templateManager, ResourceLocation resourceLocation, Rotation rotation) {
            super(IStructurePieceType.field_242786_ad, 0);

            ModFiddle.LOGGER.debug("piece construct");

            this.templateLocation = resourceLocation;
            this.rotation = rotation;
            loadTemplate(templateManager);
        }

        private void loadTemplate(TemplateManager templateManager) {
            ModFiddle.LOGGER.debug("load template");
            Template template = templateManager.getTemplate(this.templateLocation);
            PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE);
//                    .setRotationPivot(IglooPieces.PIVOTS.get(this.templateLocation))
//                    .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
            this.setup(template, this.templatePosition, placementsettings);
        }

        @Override
        protected void handleDataMarker(String s, BlockPos blockPos, IServerWorld iServerWorld, Random random, MutableBoundingBox mutableBoundingBox) {
        }
    }
}