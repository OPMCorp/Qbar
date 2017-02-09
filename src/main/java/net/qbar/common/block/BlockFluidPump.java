package net.qbar.common.block;

import net.minecraft.block.Block;
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
import net.qbar.common.tile.TileFluidPump;

public class BlockFluidPump extends BlockOrientableMachine
{
    protected static final AxisAlignedBB AABB_EAST  = new AxisAlignedBB(0.00D, 0.31D, 0.00D, 1.00D, 0.80D, 0.85D);
    protected static final AxisAlignedBB AABB_NOTH  = new AxisAlignedBB(0.00D, 0.31D, 0.00D, 0.85D, 0.80D, 1.00D);
    protected static final AxisAlignedBB AABB_WEST  = new AxisAlignedBB(0.00D, 0.31D, 0.15D, 1.00D, 0.80D, 1.00D);
    protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.15D, 0.31D, 0.00D, 1.00D, 0.80D, 1.00D);

    public BlockFluidPump()
    {
        super("fluidpump", Material.IRON, true, true);
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
        {
            ((TileFluidPump) w.getTileEntity(pos)).setFacing(this.getFacing(state));
            ((TileFluidPump) w.getTileEntity(pos)).scanFluidHandlers();
        }
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        super.rotateBlock(world, pos, facing);

        if (!world.isRemote)
        {
            ((TileFluidPump) world.getTileEntity(pos)).setFacing(facing);
            ((TileFluidPump) world.getTileEntity(pos)).scanFluidHandlers();
        }
        return true;
    }

    @Override
    public void neighborChanged(final IBlockState state, final World w, final BlockPos pos, final Block block,
            final BlockPos posNeighbor)
    {
        if (!w.isRemote)
        {
            final BlockPos substract = posNeighbor.subtract(pos);
            ((TileFluidPump) w.getTileEntity(pos)).scanFluidHandler(posNeighbor,
                    EnumFacing.getFacingFromVector(substract.getX(), substract.getY(), substract.getZ()));
        }
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileFluidPump(64);
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
                return AABB_NOTH;
            case SOUTH:
                return AABB_SOUTH;
            default:
                return FULL_BLOCK_AABB;
        }
    }
}
