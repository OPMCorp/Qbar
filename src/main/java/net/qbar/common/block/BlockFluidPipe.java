package net.qbar.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.qbar.common.grid.GridManager;
import net.qbar.common.tile.TileFluidPipe;

public class BlockFluidPipe extends BlockMachineBase
{
    protected static final AxisAlignedBB AABB_NONE  = new AxisAlignedBB(0.31D, 0.31D, 0.31D, 0.69D, 0.69D, 0.69D);
    protected static final AxisAlignedBB AABB_EAST  = new AxisAlignedBB(0.69D, 0.31D, 0.31D, 1.00D, 0.56D, 0.69D);
    protected static final AxisAlignedBB AABB_WEST  = new AxisAlignedBB(0.00D, 0.31D, 0.31D, 0.31D, 0.56D, 0.31D);
    protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.69D, 0.31D, 0.31D, 0.69D, 0.56D, 1.00D);
    protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.31D, 0.31D, 0.00D, 0.31D, 0.56D, 0.31D);
    protected static final AxisAlignedBB AABB_UP    = new AxisAlignedBB(0.31D, 0.56D, 0.31D, 0.69D, 1.00D, 0.69D);
    protected static final AxisAlignedBB AABB_DOWN  = new AxisAlignedBB(0.31D, 0.00D, 0.31D, 0.69D, 0.31D, 0.69D);

    public BlockFluidPipe()
    {
        super("fluidpipe", Material.IRON);
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
    public boolean causesSuffocation(final IBlockState state)
    {
        return false;
    }

    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos)
    {
        if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileFluidPipe)
        {
            final TileFluidPipe tile = (TileFluidPipe) world.getTileEntity(pos);
            return ((IExtendedBlockState) state).withProperty(Properties.AnimationProperty, tile.state);
        }
        return state;
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[] { Properties.AnimationProperty });
    }

    @Override
    public void neighborChanged(final IBlockState state, final World w, final BlockPos pos, final Block block,
            final BlockPos posNeighbor)
    {
        if (!w.isRemote)
            ((TileFluidPipe) w.getTileEntity(pos)).scanFluidHandlers(posNeighbor);
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state)
    {
        GridManager.getInstance().disconnectCable((TileFluidPipe) w.getTileEntity(pos));

        super.breakBlock(w, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileFluidPipe(64);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        AxisAlignedBB res = AABB_NONE;
        if (source.getTileEntity(pos.east()) instanceof TileFluidPipe)
            res = res.union(AABB_EAST);
        if (source.getTileEntity(pos.west()) instanceof TileFluidPipe)
            res = res.union(AABB_WEST);
        if (source.getTileEntity(pos.north()) instanceof TileFluidPipe)
            res = res.union(AABB_NORTH);
        if (source.getTileEntity(pos.south()) instanceof TileFluidPipe)
            res = res.union(AABB_SOUTH);
        if (source.getTileEntity(pos.up()) instanceof TileFluidPipe)
            res = res.union(AABB_UP);
        if (source.getTileEntity(pos.down()) instanceof TileFluidPipe)
            res = res.union(AABB_DOWN);
        return res;
    }
}
