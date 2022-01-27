package com.greffgreff.modfiddle.world.structure.structures.bridge;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.*;

import java.util.Random;

public class BridgePieces {
    private static Piece findAndCreateBridgePieceFactory() {
        return null;
    }

    public static class DeckPiece extends Piece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public DeckPiece(ResourceLocation templateLocation, Rotation rotation) {
            this.templateLocation = templateLocation;
            this.rotation = rotation;
        }

        @Override
        protected void handleDataMarker(String s, BlockPos blockPos, IServerWorld iServerWorld, Random random, MutableBoundingBox mutableBoundingBox) {

        }
    }

    public static class TowerPiece extends Piece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public TowerPiece(ResourceLocation templateLocation, Rotation rotation) {
            this.templateLocation = templateLocation;
            this.rotation = rotation;
        }

        @Override
        protected void handleDataMarker(String s, BlockPos blockPos, IServerWorld iServerWorld, Random random, MutableBoundingBox mutableBoundingBox) {

        }
    }

    abstract static class Piece extends TemplateStructurePiece {
        public Piece() {
            super(IStructurePieceType.field_242786_ad, 0);
        }
    }
}
