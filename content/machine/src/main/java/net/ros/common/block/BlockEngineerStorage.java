package net.ros.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ros.common.ROSConstants;
import net.ros.common.gui.MachineGui;
import net.ros.common.tile.machine.TileEngineerStorage;

import javax.annotation.Nullable;

public class BlockEngineerStorage extends BlockOrientableMachine<TileEngineerStorage>
{
    public BlockEngineerStorage()
    {
        super("engineerstorage", Material.WOOD, true, false, TileEngineerStorage.class);
    }

    @Override
    public boolean onBlockActivated(final World w, final BlockPos pos, final IBlockState state, final EntityPlayer
            player, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ)
    {
        if (player.isSneaking())
            return false;
        player.openGui(ROSConstants.MODINSTANCE, MachineGui.ENGINEERSTORAGE.getUniqueID(), w, pos.getX(), pos.getY(),
                pos.getZ());
        return true;
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state)
    {
        this.getWorldTile(w, pos).disconnectItself();
        super.breakBlock(w, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEngineerStorage();
    }
}
