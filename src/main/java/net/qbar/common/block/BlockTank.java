package net.qbar.common.block;

import javax.annotation.Nullable;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qbar.common.IWrenchable;
import net.qbar.common.multiblock.BlockMultiblockBase;
import net.qbar.common.multiblock.Multiblocks;
import net.qbar.common.tile.TileTank;
import net.qbar.common.util.FluidUtils;

public class BlockTank extends BlockMultiblockBase implements IWrenchable
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockTank()
    {
        super("fluidtank", Material.IRON, Multiblocks.FLUID_TANK);

        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                .withProperty(BlockTank.FACING, EnumFacing.NORTH));
    }

    @Nullable
    public static EnumFacing getFacing(final int meta)
    {
        final int i = meta & 7;
        return i > 5 ? null : EnumFacing.getFront(i);
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(BlockTank.FACING, BlockTank.getFacing(meta))
                .withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, Boolean.valueOf((meta & 8) > 0));
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(BlockTank.FACING).getIndex();

        if (state.getValue(BlockMultiblockBase.MULTIBLOCK_GAG).booleanValue())
            i |= 8;
        return i;
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BlockMultiblockBase.MULTIBLOCK_GAG, BlockTank.FACING);
    }

    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot)
    {
        return state.withProperty(BlockTank.FACING, rot.rotate(state.getValue(BlockTank.FACING)));
    }

    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockTank.FACING)));
    }

    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing,
            final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(BlockMultiblockBase.MULTIBLOCK_GAG, false)
                .withProperty(BlockTank.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        if (facing == null || !EnumFacing.Plane.HORIZONTAL.apply(facing))
            return false;
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockTank.FACING, facing));
        return true;
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
    public boolean onBlockActivated(final World w, final BlockPos pos, final IBlockState state,
            final EntityPlayer player, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY,
            final float hitZ)
    {
        if (player.isSneaking())
            return false;

        final TileTank tank = (TileTank) w.getTileEntity(pos);

        if (tank != null)
        {
            if (FluidUtils.drainPlayerHand(tank.getTank(), player) || FluidUtils.fillPlayerHand(tank.getTank(), player))
                return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileTank();
    }

    @Override
    public boolean onWrench(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
            IBlockState state)
    {
        this.rotateBlock(world, pos, state.getValue(BlockTank.FACING).rotateAround(Axis.Y));
        return true;
    }
}
