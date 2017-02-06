package net.qbar.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.qbar.common.tile.TilePipeBase;
import net.qbar.common.tile.TileSteamPipe;

public class BlockSteamPipe extends BlockPipeBase
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
        super("steampipe");
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileSteamPipe(8);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        TilePipeBase<?, ?> tile = (TilePipeBase<?, ?>) source.getTileEntity(pos);
        AxisAlignedBB res = AABB_NONE;
        if (tile != null)
        {
            if (tile.isConnected(EnumFacing.EAST))
                res = res.union(AABB_EAST);
            if (tile.isConnected(EnumFacing.WEST))
                res = res.union(AABB_WEST);
            if (tile.isConnected(EnumFacing.NORTH))
                res = res.union(AABB_NORTH);
            if (tile.isConnected(EnumFacing.SOUTH))
                res = res.union(AABB_SOUTH);
            if (tile.isConnected(EnumFacing.UP))
                res = res.union(AABB_UP);
            if (tile.isConnected(EnumFacing.DOWN))
                res = res.union(AABB_DOWN);
        }
        return res;
    }
}
