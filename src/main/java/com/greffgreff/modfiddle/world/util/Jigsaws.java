package com.greffgreff.modfiddle.world.util;

import com.greffgreff.modfiddle.ModFiddle;
import com.greffgreff.modfiddle.world.structure.structures.bridge.pieces.AbstractBridgePiece;
import net.minecraft.block.Block;
import net.minecraft.block.JigsawBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

import java.util.Random;
import java.util.function.Supplier;

public class Jigsaws {

    public static AbstractVillagePiece createAbstractPiece(JigsawPiece piece, BlockPos position, Rotation rotation, TemplateManager templateManager) {
        return new AbstractVillagePiece(templateManager, piece, position, piece.getGroundLevelDelta(), rotation, piece.getBoundingBox(templateManager, position, rotation));
    }

    public static JigsawPattern getPool(ResourceLocation resourceLocation, DynamicRegistries dynamicRegistries) {
        Supplier<JigsawPattern> piecesPool = () -> dynamicRegistries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(resourceLocation);
        return piecesPool.get();
    }

    public static String getPieceName(JigsawPiece jigsawPiece) {
        if (jigsawPiece == null) return "";
        SingleJigsawPiece singleJigsawPiece = (SingleJigsawPiece) jigsawPiece;
        return singleJigsawPiece.field_236839_c_.left().isPresent() ? singleJigsawPiece.field_236839_c_.left().get().getPath() : "";
    }

    public static void joinJigsaws(AbstractBridgePiece parentPiece, AbstractBridgePiece childPiece, TemplateManager templateManager, Random random) {
        matching:
        for (Rotation rotation: Rotation.shuffledRotations(random)) {
            for (Template.BlockInfo parentJigsaw : parentPiece.getJigsawBlocks(templateManager, BlockPos.ZERO, Rotation.NONE, random)) {
                for (Template.BlockInfo childJigsaw : childPiece.getJigsawBlocks(templateManager, BlockPos.ZERO, rotation, random)) {
                    if (JigsawBlock.hasJigsawMatch(parentJigsaw, childJigsaw)) {
                        Vector3i parentDirection = JigsawBlock.getConnectingDirection(parentJigsaw.state).getDirectionVec();
                        int xDelta = parentJigsaw.pos.getX() - childJigsaw.pos.getX() + parentDirection.getX();
                        int yDelta = parentJigsaw.pos.getY() - childJigsaw.pos.getY() + parentDirection.getY();
                        int zDelta = parentJigsaw.pos.getZ() - childJigsaw.pos.getZ() + parentDirection.getZ();

                        // FIXME: 06/02/2022 rotate before offset
                        MutableBoundingBox BB = childPiece.getBoundingBox();
                        MutableBoundingBox offsetBB = MutableBoundingBox.createProper(BB.minX + xDelta, BB.minY + yDelta, BB.minZ + zDelta, BB.maxX + xDelta, BB.maxY + yDelta, BB.maxZ + zDelta);
                        MutableBoundingBox rotatedBB = Jigsaws.rotateBB(offsetBB, rotation);

//                        ModFiddle.LOGGER.debug("Rotation:        " + rotation);
//                        ModFiddle.LOGGER.debug("Child BB:        " + BB.minX + " " + BB.minY + " " + BB.minZ + "    " + BB.maxX + " " + BB.maxY + " " + BB.maxZ);
//                        ModFiddle.LOGGER.debug("Offset BB:       " + offsetBB.minX + " " + offsetBB.minY + " " + offsetBB.minZ + "    " + offsetBB.maxX + " " + offsetBB.maxY + " " + offsetBB.maxZ);
//                        ModFiddle.LOGGER.debug("Expected BB:     " + rotatedBB.minX + " " + rotatedBB.minY + " " + rotatedBB.minZ + "    " + rotatedBB.maxX + " " + rotatedBB.maxY + " " + rotatedBB.maxZ);
//                        ModFiddle.LOGGER.debug("Expected Origin: " + new BlockPos(childPiece.position.getX() + xDelta, childPiece.position.getY() + yDelta, childPiece.position.getZ() + zDelta));
//                        ModFiddle.LOGGER.debug("Parent origin:   " + parentPiece.position);
//                        ModFiddle.LOGGER.debug("Child origin:    " + childPiece.position);

                        if (!parentPiece.getBoundingBox().intersectsWith(rotatedBB)) {
                            ModFiddle.LOGGER.debug("Rotation:        " + rotation);
                            ModFiddle.LOGGER.debug("Offset:          " + childPiece.position.add(xDelta, yDelta, zDelta));

                            ModFiddle.LOGGER.debug("===SUCCESS===");
                            ModFiddle.LOGGER.debug("Rotation:        " + rotation);
                            ModFiddle.LOGGER.debug("Child BB:        " + BB.minX + " " + BB.minY + " " + BB.minZ + "    " + BB.maxX + " " + BB.maxY + " " + BB.maxZ);
                            ModFiddle.LOGGER.debug("Offset BB:       " + offsetBB.minX + " " + offsetBB.minY + " " + offsetBB.minZ + "    " + offsetBB.maxX + " " + offsetBB.maxY + " " + offsetBB.maxZ);
                            ModFiddle.LOGGER.debug("Expected BB:     " + rotatedBB.minX + " " + rotatedBB.minY + " " + rotatedBB.minZ + "    " + rotatedBB.maxX + " " + rotatedBB.maxY + " " + rotatedBB.maxZ);
                            ModFiddle.LOGGER.debug("Expected Origin: " + new BlockPos(childPiece.position.getX() + xDelta, childPiece.position.getY() + yDelta, childPiece.position.getZ() + zDelta));

                            childPiece.rotate(rotation).offset(xDelta, yDelta, zDelta).createPiece();
                            ModFiddle.LOGGER.debug("===END===");
                            break matching;
                        }
                    }
                }
            }
        }
    }

    public static MutableBoundingBox rotateBB(MutableBoundingBox boundingBox, Rotation rotation) {
        if (rotation == Rotation.COUNTERCLOCKWISE_90) {
//             boundingBox.func_215127_b(0, 0, 1);
            // just requires a shift
            int maxX = boundingBox.minX + boundingBox.getZSize() - 1;
            int maxZ = boundingBox.maxZ - boundingBox.getXSize() - 1;
            return MutableBoundingBox.createProper(boundingBox.minX, boundingBox.minY, boundingBox.minZ, maxX, boundingBox.maxY, maxZ);
        }
        else if (rotation == Rotation.CLOCKWISE_90) {
            // requires shift in x + z size
//            boundingBox.func_215127_b(1, 0, 0);
            int maxX = boundingBox.minX - boundingBox.getZSize() + 1;
            int maxZ = boundingBox.maxZ - boundingBox.getXSize() - 1;
            return MutableBoundingBox.createProper(boundingBox.minX, boundingBox.minY, boundingBox.minZ, maxX, boundingBox.maxY, maxZ);
        }
        return boundingBox;
    }

//    public static MutableBoundingBox rotateBB(MutableBoundingBox boundingBox, Rotation rotation) {
//        if (rotation == Rotation.COUNTERCLOCKWISE_90) {
//            int maxX = boundingBox.maxX + (boundingBox.getZSize() - boundingBox.getXSize());
//            int maxZ = boundingBox.maxZ + (boundingBox.getXSize()) - boundingBox.getZSize();
//            return MutableBoundingBox.createProper(boundingBox.minX, boundingBox.minY, boundingBox.minZ, maxX, boundingBox.maxY, maxZ);
//        }
//        else if (rotation == Rotation.CLOCKWISE_90) {
//            int maxX = boundingBox.maxX - (boundingBox.getZSize() - boundingBox.getXSize());
//            int maxZ = boundingBox.maxZ - (boundingBox.getXSize()) - boundingBox.getZSize();
//            return MutableBoundingBox.createProper(boundingBox.minX, boundingBox.minY, boundingBox.minZ, maxX, boundingBox.maxY, maxZ);
//        }
//        return boundingBox;
//    }
}
