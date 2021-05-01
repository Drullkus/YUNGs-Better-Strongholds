package com.yungnickyoung.minecraft.betterstrongholds.world;

import com.mojang.serialization.Codec;
import com.yungnickyoung.minecraft.betterstrongholds.BetterStrongholds;
import com.yungnickyoung.minecraft.betterstrongholds.world.jigsaw.JigsawConfig;
import com.yungnickyoung.minecraft.betterstrongholds.world.jigsaw.JigsawManager;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class BetterStrongholdStructure extends StructureFeature<DefaultFeatureConfig> {
    public BetterStrongholdStructure(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
        return BetterStrongholdStructure.Start::new;
    }

    /**
     * shouldStartAt
     *
     * A less constrained form of the ring-based placement of vanilla strongholds.
     *
     * Thickness of rings: 1,536  (96 chunks)
     * Distance between rings: 1,536 (96 chunks)
     * Distance to first ring: 1,280 (80 chunks)
     *
     * Vanilla has 8 rings.
     *
     * Credits to TelepathicGrunt for this.
     */
    @Override
    protected boolean shouldStartAt(ChunkGenerator chunkGenerator, BiomeSource biomeProvider, long seed, ChunkRandom chunkRandom, int xChunk, int zChunk, Biome biome, ChunkPos chunkPos, DefaultFeatureConfig featureConfig) {
        int ringThickness = 96;
        int distanceToFirstRing = 80;

        int chunkDistance = (int) Math.sqrt((xChunk * xChunk) + (zChunk * zChunk));

        // Offset the distance so that the first ring is closer to spawn
        int shiftedChunkDistance = chunkDistance + (ringThickness - distanceToFirstRing);

        // Determine which ring we are in.
        // non-stronghold rings are even number ringSection
        // stronghold rings are odd number ringSection.
        int ringSection = shiftedChunkDistance / ringThickness;

        // Would mimic vanilla's 8 ring result
        // if(ringSection > 16) return false;

        // Only spawn strongholds on odd number sections
        return ringSection % 2 == 1;
    }

    public static class Start extends StructureStart<DefaultFeatureConfig> {
        public Start(StructureFeature<DefaultFeatureConfig> structure, int chunkX, int chunkZ, BlockBox blockBox, int refences, long seedInseed) {
            super(structure, chunkX, chunkZ, blockBox, refences, seedInseed);
        }

        @Override
        public void init(DynamicRegistryManager dynamicRegistryManager, ChunkGenerator chunkGenerator, StructureManager structureManager, int chunkX, int chunkZ, Biome biomeIn, DefaultFeatureConfig config) {
            // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
            int x = (chunkX << 4) + 7;
            int z = (chunkZ << 4) + 7;

            int minY = BetterStrongholds.CONFIG.betterStrongholds.general.strongholdStartMinY;
            int maxY = BetterStrongholds.CONFIG.betterStrongholds.general.strongholdStartMaxY;
            int y = this.random.nextInt(maxY - minY) + minY;

            BlockPos blockpos = new BlockPos(x, y, z);
            JigsawConfig jigsawConfig = new JigsawConfig(
                () -> dynamicRegistryManager.get(Registry.TEMPLATE_POOL_WORLDGEN)
                    .get(new Identifier(BetterStrongholds.MOD_ID, "starts")),
                BetterStrongholds.CONFIG.betterStrongholds.general.strongholdSize
            );

            // Generate the structure
            JigsawManager.assembleJigsawStructure(
                dynamicRegistryManager,
                jigsawConfig,
                chunkGenerator,
                structureManager,
                blockpos,
                this.children,
                this.random,
                false,
                false
            );

            // Sets the bounds of the structure once you are finished.
            this.setBoundingBoxFromChildren();

            // Debug log the coordinates of the center starting piece.
            BetterStrongholds.LOGGER.debug("Better Stronghold at {} {} {}",
                this.children.get(0).getBoundingBox().minX,
                this.children.get(0).getBoundingBox().minY,
                this.children.get(0).getBoundingBox().minZ
            );
        }
    }
}
