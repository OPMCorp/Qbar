package net.qbar.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qbar.common.IWrenchable;

public abstract class BlockOrientableMachine extends BlockMachineBase implements IWrenchable
{
    public static final PropertyDirection FACING     = BlockHorizontal.FACING;
    public static final int               NEEDED_BIT = 3;

    public BlockOrientableMachine(String name, Material material)
    {
        super(name, material);
        this.setDefaultState(
                this.blockState.getBaseState().withProperty(BlockOrientableMachine.FACING, EnumFacing.NORTH));
    }

    @Nullable
    public static EnumFacing getFacing(final int meta)
    {
        final int i = meta & 7;
        if (i > 5)
            return null;
        else
        {
            EnumFacing result = EnumFacing.getFront(i);

            if (result.getAxis() == EnumFacing.Axis.Y)
                result = EnumFacing.NORTH;

            return result;
        }
    }

    public EnumFacing getFacing(final IBlockState state)
    {
        return state.getValue(BlockOrientableMachine.FACING);
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(BlockOrientableMachine.FACING,
                BlockOrientableMachine.getFacing(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return state.getValue(BlockOrientableMachine.FACING).getIndex();
    }

    @Override
    public boolean onWrench(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
            IBlockState state)
    {
        this.rotateBlock(world, pos, getFacing(state).rotateAround(Axis.Y));
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BlockOrientableMachine.FACING);
    }

    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot)
    {
        return state.withProperty(BlockOrientableMachine.FACING,
                rot.rotate(state.getValue(BlockOrientableMachine.FACING)));
    }

    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockOrientableMachine.FACING)));
    }

    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing,
            final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(BlockOrientableMachine.FACING,
                placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        if (facing == null || !EnumFacing.Plane.HORIZONTAL.apply(facing))
            return false;
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockOrientableMachine.FACING, facing));
        return true;
    }

    @Override
    public void onBlockPlacedBy(final World w, final BlockPos pos, final IBlockState state,
            final EntityLivingBase placer, final ItemStack stack)
    {
        w.setBlockState(pos,
                state.withProperty(BlockOrientableMachine.FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }
}
