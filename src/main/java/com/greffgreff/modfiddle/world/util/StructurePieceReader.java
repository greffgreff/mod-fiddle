package com.greffgreff.modfiddle.world.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;

import java.util.List;

public class StructurePieceReader {
    Codec<StructurePieceReader> STRUCTURE_CODEC = RecordCodecBuilder.create(instance ->  {
        return instance.group(
                Codec.STRING.fieldOf("pool").forGetter(StructurePieceReader::getPool),
                Codec.STRING.fieldOf("fallback").forGetter(StructurePieceReader::getFallback),
                JigsawPiece.field_236847_e_.listOf().fieldOf("jigsawPieces").forGetter(StructurePieceReader::getJigsawPieces)
        ).apply(instance, StructurePieceReader::new);
    });

    private final String pool;
    private final String fallback;
    private final List<JigsawPiece> jigsawPieces;

    public StructurePieceReader(String pool, String fallback, List<JigsawPiece> jigsawPieces) {
        this.pool = pool;
        this.fallback = fallback;
        this.jigsawPieces = jigsawPieces;
    }

    public String getPool() {
        return this.pool;
    }

    public String getFallback() {
        return this.fallback;
    }

    public List<JigsawPiece> getJigsawPieces() {
        return this.jigsawPieces;
    }
}

//      instance.group(ResourceLocation.CODEC.fieldOf("name").forGetter(JigsawPattern::getName), ResourceLocation.CODEC.fieldOf("fallback").forGetter(JigsawPattern::getFallback), Codec.mapPair(JigsawPiece.CODEC.fieldOf("element"), Codec.INT.fieldOf("weight")).codec().listOf().promotePartial(Util.prefix("Pool element: ", LOGGER::error)).fieldOf("elements").forGetter((p_236857_0_) -> {

//    Codec<StructurePieceReader> CODEC = RecordCodecBuilder.create(
//            instance -> instance.group(
//                    Codec.BOOL.fieldOf("foo").forGetter((Foobar o) -> o.foo), // Basic boolean Codec
//                    // Codec for building a list
//                    Codec.INT.listOf().fieldOf("bar").forGetter((Foobar o) -> o.bar),
//                    // Example usage of using a different class's Codec
//                    BlockState.CODEC.fieldOf("blockstate_example").forGetter((Foobar o) -> o.blockState)
//            ).apply(instance, (fooC, barC, blockStateC) -> new Foobar(fooC, barC, blockStateC))
//    );