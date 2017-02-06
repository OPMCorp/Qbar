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
import net.qbar.common.tile.TileSteamPipe;

public class BlockSteamPipe extends BlockMachineBase
{
    protected static final AxisAlignedBB AABB_NONE  = new AxisAlignedBB(0.34D, 0.24D, 0.34D, 0.66D, 0.56D, 0.66D);
    protected static final AxisAlignedBB AABB_EAST  = new AxisAlignedBB(0.66D, 0.24D, 0.34D, 1.00D, 0.56D, 0.66D);
    protected static final AxisAlignedBB AABB_WEST  = new AxisAlignedBB(0.00D, 0.24D, 0.34D, 0.34D, 0.56D, 0.34D);
    protected static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.66D, 0.24D, 0.34D, 0.66D, 0.56D, 1.00D);
    protected static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.34D, 0.24D, 0.00D, 0.34D, 0.56D, 0.34D);
    protected static final AxisAlignedBB AABB_UP    = new AxisAlignedBB(0.34D, 0.56D, 0.34D, 0.66D, 1.00D, 0.66D);
    protected static final AxisAlignedBB AABB_DOWN  = new AxisAlignedBB(0.34D, 0.00D, 0.34D, 0.66D, 0.24D, 0.66D);

    public BlockSteamPipe()
    {
        super("steampipe", Material.IRON);
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
        if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileSteamPipe)
        {
            final TileSteamPipe tile = (TileSteamPipe) world.getTileEntity(pos);
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
            ((TileSteamPipe) w.getTileEntity(pos)).scanSteamHandlers(posNeighbor);
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state)
    {
        GridManager.getInstance().disconnectCable((TileSteamPipe) w.getTileEntity(pos));

        super.breakBlock(w, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileSteamPipe(8);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        AxisAlignedBB res = AABB_NONE;
        if (source.getTileEntity(pos.east()) instanceof TileSteamPipe)
            res = res.union(AABB_EAST);
        if (source.getTileEntity(pos.west()) instanceof TileSteamPipe)
            res = res.union(AABB_WEST);
        if (source.getTileEntity(pos.north()) instanceof TileSteamPipe)
            res = res.union(AABB_NORTH);
        if (source.getTileEntity(pos.south()) instanceof TileSteamPipe)
            res = res.union(AABB_SOUTH);
        if (source.getTileEntity(pos.up()) instanceof TileSteamPipe)
            res = res.union(AABB_UP);
        if (source.getTileEntity(pos.down()) instanceof TileSteamPipe)
            res = res.union(AABB_DOWN);
        return res;
    }
}
