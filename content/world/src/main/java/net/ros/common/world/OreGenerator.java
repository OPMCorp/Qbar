package net.ros.common.world;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.ros.common.util.MathUtils;

import java.util.Random;
import java.util.function.Predicate;

public class OreGenerator implements IWorldGenerator
{
    public final Predicate<IBlockState> STONE_PREDICATE;
    public final Predicate<IBlockState> DECORATION_PREDICATE;

    private OreGenerator()
    {
        this.STONE_PREDICATE = state -> state != null && state.getBlock() == Blocks.STONE
                && state.getValue(BlockStone.VARIANT).isNatural();
        this.DECORATION_PREDICATE = this.STONE_PREDICATE
                .or(state -> state != null && (state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS
                        || state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.GRAVEL));
        OreVeins.initVeins();
    }

    private static OreGenerator INSTANCE;

    public static OreGenerator instance()
    {
        if (INSTANCE == null)
            INSTANCE = new OreGenerator();
        return INSTANCE;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (world.provider.getDimension() == 0)
            generateVeins(random, chunkX, chunkZ, world);
    }

    private void generateVeins(Random rand, int chunkX, int chunkZ, World world)
    {
        for (int x = 0; x < 16; x++)
        {
            for (int z = 0; z < 16; z++)
            {
                BlockPos center = new BlockPos(x + (chunkX * 16), 64, z + (chunkZ * 16));
                OreVeins.VEINS.forEach(vein -> generateVein(rand, center, world, vein));
            }
        }
    }

    private void generateVein(Random rand, BlockPos center, World world, OreVeinDescriptor vein)
    {
        int y;
        if (vein.getBiomeMatcher().test(world.getBiome(center)) && rand.nextFloat() < vein.getRarity())
        {
            y = rand.nextInt(vein.getHeightRange().getMaximum() - vein.getHeightRange().getMinimum())
                    + vein.getHeightRange().getMinimum();

            int quantity = vein.getHeapQty();

            if (quantity != 1)
                quantity = MathUtils.randBetweenGapRatio(rand, vein.getHeapQty(), .25f);
            for (int i = 0; i < quantity; i++)
            {
                int xShift = 0;
                int yShift = 0;
                int zShift = 0;

                switch (vein.getVeinForm())
                {
                    case FLAT:
                        xShift = rand.nextBoolean()
                                ? Math.max(rand.nextInt((int) (vein.getHeapSize() * 1.5f)), vein.getHeapSize())
                                : Math.max(-rand.nextInt((int) (vein.getHeapSize() * 1.5f)), -vein.getHeapSize());
                        yShift = rand.nextBoolean() ? Math.max(rand.nextInt(vein.getHeapSize()), vein.getHeapSize() / 4)
                                : Math.max(-rand.nextInt(vein.getHeapSize()), -vein.getHeapSize() / 4);
                        zShift = rand.nextBoolean()
                                ? Math.max(rand.nextInt((int) (vein.getHeapSize() * 1.5f)), vein.getHeapSize())
                                : Math.max(-rand.nextInt((int) (vein.getHeapSize() * 1.5f)), -vein.getHeapSize());
                        break;
                    case SCATTERED:
                        xShift = rand.nextBoolean()
                                ? Math.max(rand.nextInt(vein.getHeapSize() * 4), vein.getHeapSize() * 2)
                                : Math.max(-rand.nextInt(vein.getHeapSize() * 4), -vein.getHeapSize() * 2);
                        yShift = rand.nextBoolean()
                                ? Math.max(rand.nextInt(vein.getHeapSize() * 4), vein.getHeapSize() * 2)
                                : Math.max(-rand.nextInt(vein.getHeapSize() * 4), -vein.getHeapSize() * 2);
                        zShift = rand.nextBoolean()
                                ? Math.max(rand.nextInt(vein.getHeapSize() * 4), vein.getHeapSize() * 2)
                                : Math.max(-rand.nextInt(vein.getHeapSize() * 4), -vein.getHeapSize() * 2);
                        break;
                    case UPWARD:
                        xShift = rand.nextBoolean() ? Math.max(rand.nextInt(vein.getHeapSize()), vein.getHeapSize() / 4)
                                : Math.max(-rand.nextInt(vein.getHeapSize()), -vein.getHeapSize() / 4);
                        yShift = rand.nextBoolean()
                                ? Math.max(rand.nextInt((int) (vein.getHeapSize() * 1.5f)), vein.getHeapSize())
                                : Math.max(-rand.nextInt((int) (vein.getHeapSize() * 1.5f)), -vein.getHeapSize());
                        zShift = rand.nextBoolean() ? Math.max(rand.nextInt(vein.getHeapSize()), vein.getHeapSize() / 4)
                                : Math.max(-rand.nextInt(vein.getHeapSize()), -vein.getHeapSize() / 4);
                        break;
                    case MERGED:
                        xShift = rand.nextBoolean() ? Math.max(rand.nextInt(vein.getHeapSize()), vein.getHeapSize() / 3)
                                : Math.max(-rand.nextInt(vein.getHeapSize()), -vein.getHeapSize() / 3);
                        yShift = rand.nextBoolean() ? Math.max(rand.nextInt(vein.getHeapSize()), vein.getHeapSize() / 3)
                                : Math.max(-rand.nextInt(vein.getHeapSize()), -vein.getHeapSize() / 3);
                        zShift = rand.nextBoolean() ? Math.max(rand.nextInt(vein.getHeapSize()), vein.getHeapSize() / 3)
                                : Math.max(-rand.nextInt(vein.getHeapSize()), -vein.getHeapSize() / 3);
                        break;
                }

                BlockPos veinLocation = new BlockPos(center.getX() + xShift, y + yShift, center.getZ() + zShift);

                int heapSize = MathUtils.randBetweenGapRatio(rand, vein.getHeapSize(), .25f);

                if (!vein.getMarkers().isEmpty()
                        && (vein.getHeapQty() == 1 || rand.nextFloat() <= 1.0f / vein.getHeapQty()))
                {
                    if (world.isBlockLoaded(veinLocation))
                    {
                        if (vein.getMarkers().size() > 1)
                            vein.getMarkers().get(rand.nextInt(vein.getMarkers().size())).accept(world, veinLocation,
                                    heapSize, vein.getHeapSize());
                        else
                            vein.getMarkers().get(0).accept(world, veinLocation, heapSize, vein.getHeapSize());
                    }
                }
                switch (vein.getHeapForm())
                {
                    case SPHERES:
                        FeatureGenerator.generateSphere(world, veinLocation, vein.getVeinBlockSupplier(), heapSize,
                                vein.getHeapDensity(), STONE_PREDICATE);
                        break;
                    case PLATES:
                        FeatureGenerator.generatePlate(world, veinLocation, vein.getVeinBlockSupplier(), heapSize,
                                vein.getHeapSize() / 2, vein.getHeapDensity(), STONE_PREDICATE);
                        break;
                    case SCATTERED:
                        FeatureGenerator.generateSphere(world, veinLocation, vein.getVeinBlockSupplier(), heapSize,
                                vein.getHeapDensity(), STONE_PREDICATE);
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onOreGen(OreGenEvent.GenerateMinable e)
    {
        if (e.getType().equals(OreGenEvent.GenerateMinable.EventType.IRON)
                || e.getType().equals(OreGenEvent.GenerateMinable.EventType.GOLD)
                || e.getType().equals(OreGenEvent.GenerateMinable.EventType.REDSTONE))
            e.setResult(Event.Result.DENY);
    }
}
