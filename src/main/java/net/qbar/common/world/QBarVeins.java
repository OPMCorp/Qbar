package net.qbar.common.world;

import com.google.common.collect.Lists;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.qbar.common.init.QBarBlocks;

import java.util.List;

public class QBarVeins
{
    public static List<OreVeinDescriptor> VEINS;

    public static OreVeinDescriptor       IRON_NICKEL;
    public static OreVeinDescriptor       IRON_COPPER;
    public static OreVeinDescriptor       TIN;
    public static OreVeinDescriptor       IRON_ZINC;
    public static OreVeinDescriptor       GOLD;
    public static OreVeinDescriptor       REDSTONE;

    public static void initVeins()
    {
        IRON_NICKEL = new OreVeinDescriptor("iron-nickel").veinForm(EVeinForm.FLAT).heapForm(EVeinHeapForm.PLATES).heapQty(5)
                .heapDensity(0.8f).heapSize(10).rarity(0.002f).heightRange(4, 64)
                .content(QBarBlocks.IRON_NICKEL_ORE.getStateFromOre("pentlandite"), 0.3f)
                .content(QBarBlocks.IRON_NICKEL_ORE.getStateFromOre("garnierite"), 0.3f)
                .content(QBarBlocks.IRON_NICKEL_ORE.getStateFromOre("laterite"), 0.4f)
                .biomes(BiomeMatcher.fromBiomes(Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.SAVANNA,
                        Biomes.SAVANNA_PLATEAU));

        IRON_COPPER = new OreVeinDescriptor("iron-copper").veinForm(EVeinForm.FLAT).heapForm(EVeinHeapForm.PLATES).heapQty(5)
                .heapDensity(0.7f).heapSize(10).rarity(0.002f).heightRange(4, 64)
                .content(QBarBlocks.IRON_COPPER_ORE.getStateFromMeta(0), 0.3f)
                .content(QBarBlocks.IRON_COPPER_ORE.getStateFromOre("tetrahedrite"), 0.3f)
                .content(QBarBlocks.IRON_COPPER_ORE.getStateFromOre("malachite"), 0.4f)
                .biomes(BiomeMatcher.fromBiomes(Biomes.RIVER, Biomes.FROZEN_RIVER, Biomes.SWAMPLAND,
                        Biomes.MUTATED_SWAMPLAND, Biomes.FOREST_HILLS, Biomes.BIRCH_FOREST_HILLS, Biomes.JUNGLE_HILLS,
                        Biomes.EXTREME_HILLS, Biomes.TAIGA_HILLS));

        TIN = new OreVeinDescriptor("tin").veinForm(EVeinForm.UPWARD).heapForm(EVeinHeapForm.PLATES).heapQty(5)
                .heapDensity(0.5f).heapSize(15).rarity(0.001f).heightRange(4, 64)
                .content(QBarBlocks.TIN_ORE.getStateFromOre("cassiterite"), 1)
                .biomes(BiomeMatcher.fromBiomes(Biomes.FOREST, Biomes.FOREST_HILLS, Biomes.BIRCH_FOREST_HILLS,
                        Biomes.BIRCH_FOREST, Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_EDGE,
                        Biomes.EXTREME_HILLS_WITH_TREES));

        IRON_ZINC = new OreVeinDescriptor("iron-zinc").veinForm(EVeinForm.FLAT).heapForm(EVeinHeapForm.PLATES).heapQty(6)
                .heapDensity(0.75f).heapSize(13).rarity(0.0015f).heightRange(4, 64)
                .content(QBarBlocks.IRON_ZINC_ORE.getStateFromOre("sphalerite"), 1)
                .biomes(BiomeMatcher.fromBiomes(Biomes.FOREST, Biomes.FOREST_HILLS, Biomes.BIRCH_FOREST,
                        Biomes.BIRCH_FOREST_HILLS, Biomes.PLAINS, Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.FROZEN_OCEAN,
                        Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_WITH_TREES, Biomes.EXTREME_HILLS_EDGE));

        GOLD = new OreVeinDescriptor("gold").veinForm(EVeinForm.SCATTERED).heapForm(EVeinHeapForm.SHATTERED).heapQty(1)
                .heapDensity(0.1f).heapSize(20).rarity(0.0001f).heightRange(4, 64)
                .content(Blocks.GOLD_ORE.getDefaultState(), 1).biomes(BiomeMatcher.WILDCARD);

        REDSTONE = new OreVeinDescriptor("redstone").veinForm(EVeinForm.SCATTERED).heapForm(EVeinHeapForm.SPHERES).heapQty(5)
                .heapDensity(0.3f).heapSize(25).rarity(0.0001f).heightRange(4, 64)
                .content(Blocks.REDSTONE_ORE.getDefaultState(), 1)
                .biomes(BiomeMatcher.fromBiomes(Biomes.PLAINS, Biomes.SWAMPLAND, Biomes.FOREST, Biomes.FOREST_HILLS,
                        Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS));

        VEINS = Lists.newArrayList(IRON_NICKEL, IRON_COPPER, TIN, IRON_ZINC, GOLD, REDSTONE);
    }
}
