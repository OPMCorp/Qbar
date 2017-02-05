package net.qbar.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qbar.common.tile.TileFluidPump;

public class BlockFluidPump extends BlockMachineBase
{
    public static PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockFluidPump()
    {
        super("fluidpump", Material.IRON);

        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockFluidPump.FACING, EnumFacing.UP));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BlockFluidPump.FACING);
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        final int facingInt = state.getValue(BlockFluidPump.FACING).ordinal();
        return facingInt;
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        int facingInt = meta;
        if (facingInt > 5)
            facingInt = facingInt - 5;
        final EnumFacing facing = EnumFacing.VALUES[facingInt];
        return this.getDefaultState().withProperty(BlockFluidPump.FACING, facing);
    }

    public EnumFacing getFacing(final IBlockState state)
    {
        return state.getValue(BlockFluidPump.FACING);
    }

    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing,
            final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta).withProperty(BlockFluidPump.FACING, facing);
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        if (facing == null)
            return false;
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockFluidPump.FACING, facing));
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
    public void onBlockPlacedBy(final World w, final BlockPos pos, final IBlockState state,
            final EntityLivingBase placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(w, pos, state, placer, stack);

        w.setBlockState(pos,
                state.withProperty(BlockFluidPump.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
        if (!w.isRemote)
        {
            ((TileFluidPump) w.getTileEntity(pos)).setFacing(state.getValue(BlockFluidPump.FACING));
            ((TileFluidPump) w.getTileEntity(pos)).scanFluidHandlers();
        }
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileFluidPump(64);
    }
}
