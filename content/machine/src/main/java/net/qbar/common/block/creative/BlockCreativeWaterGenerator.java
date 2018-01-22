package net.qbar.common.block.creative;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.qbar.common.block.BlockMachineBase;
import net.qbar.common.tile.creative.TileCreativeWaterGenerator;

public class BlockCreativeWaterGenerator extends BlockMachineBase<TileCreativeWaterGenerator>
{
    public BlockCreativeWaterGenerator()
    {
        super("creative_water_generator", Material.IRON, TileCreativeWaterGenerator.class);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileCreativeWaterGenerator();
    }
}
