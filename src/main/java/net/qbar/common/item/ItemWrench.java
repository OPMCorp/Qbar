package net.qbar.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qbar.common.IWrenchable;

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
        Block block = world.getBlockState(pos).getBlock();
        if (!world.isRemote)
        {
            if (block instanceof IWrenchable)
            {
                ((IWrenchable) block).onWrench(player, world, pos, hand, facing);
            }
            else
                block.rotateBlock(world, pos, facing.rotateAround(facing.getAxis()));

        }
        return EnumActionResult.PASS;
    }
}
