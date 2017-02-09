package net.qbar.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.qbar.common.tile.TileOffshorePump;

public class BlockOffshorePump extends BlockOrientableMachine
{
    protected static final AxisAlignedBB AABB_EAST  = new AxisAlignedBB(0.20D, 0.00D, 0.00D, 1.00D, 1.00D, 0.77D);
    protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.00D, 0.00D, 0.00D, 0.80D, 1.00D, 0.77D);
    protected static final AxisAlignedBB AABB_WEST  = new AxisAlignedBB(0.00D, 0.00D, 0.23D, 0.80D, 1.00D, 1.00D);
    protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.20D, 0.00D, 0.23D, 1.00D, 1.00D, 1.00D);

    public BlockOffshorePump()
    {
        super("offshore_pump", Material.IRON, true, false);
    }

    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state)
    {
        return false;
    }

    @Override
    public void onBlockPlacedBy(final World w, final BlockPos pos, final IBlockState state,
            final EntityLivingBase placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(w, pos, state, placer, stack);
        if (!w.isRemote)
            ((TileOffshorePump) w.getTileEntity(pos)).setFacing(this.getFacing(state));
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        super.rotateBlock(world, pos, facing);

        if (!world.isRemote)
            ((TileOffshorePump) world.getTileEntity(pos)).setFacing(facing);
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileOffshorePump(64);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        switch (getFacing(state))
        {
            case EAST:
                return AABB_EAST;
            case WEST:
                return AABB_WEST;
            case NORTH:
                return AABB_NORTH;
            case SOUTH:
                return AABB_SOUTH;
            default:
                return FULL_BLOCK_AABB;
        }
    }
}
