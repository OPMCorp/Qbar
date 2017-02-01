package net.qbar.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWrenchable
{
    boolean onWrench(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing);
}
