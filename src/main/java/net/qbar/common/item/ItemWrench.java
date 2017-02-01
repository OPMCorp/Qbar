package net.qbar.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qbar.common.tile.TileBelt;

public class ItemWrench extends ItemBase
{

    public ItemWrench()
    {
        super("wrench");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if (!world.isRemote)
        {
            if (te instanceof TileBelt)
            {
                TileBelt belt = (TileBelt) te;
                EnumFacing face = belt.getFacing().rotateAround(Axis.Y);
                System.out.println(face);
                world.getBlockState(pos).getBlock().rotateBlock(world, pos, face);
            }
        }
        return EnumActionResult.PASS;
    }
}
