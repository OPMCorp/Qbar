package net.qbar.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.qbar.common.IWrenchable;
import net.qbar.common.grid.GridManager;
import net.qbar.common.tile.TileBelt;

public class BlockBelt extends BlockMachineBase implements IWrenchable
{
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool      SLOP   = PropertyBool.create("slop");

    public BlockBelt()
    {
        super("belt", Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockBelt.FACING, EnumFacing.NORTH)
                .withProperty(BlockBelt.SLOP, false));
    }

    @Override
    public void neighborChanged(final IBlockState state, final World w, final BlockPos pos, final Block block,
            final BlockPos posNeighbor)
    {
        if (!w.isRemote && posNeighbor.equals(pos.offset(EnumFacing.UP)))
            ((TileBelt) w.getTileEntity(pos)).scanInput();
    }

    @Override
    public void breakBlock(final World w, final BlockPos pos, final IBlockState state)
    {
        GridManager.getInstance().disconnectCable((TileBelt) w.getTileEntity(pos));

        super.breakBlock(w, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockAdded(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        this.setDefaultFacing(worldIn, pos, state);
    }

    private void setDefaultFacing(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            final IBlockState iblockstate = worldIn.getBlockState(pos.north());
            final IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
            final IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
            final IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
            EnumFacing enumfacing = state.getValue(BlockBelt.FACING);

            if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock())
            {
                enumfacing = EnumFacing.SOUTH;
            }
            else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock())
            {
                enumfacing = EnumFacing.NORTH;
            }
            else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock())
            {
                enumfacing = EnumFacing.EAST;
            }
            else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock())
            {
                enumfacing = EnumFacing.WEST;
            }

            worldIn.setBlockState(pos, state.withProperty(BlockBelt.FACING, enumfacing), 2);
        }
    }

    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing,
            final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(BlockBelt.FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(final World w, final BlockPos pos, final IBlockState state,
            final EntityLivingBase placer, final ItemStack stack)
    {
        w.setBlockState(pos, state.withProperty(BlockBelt.FACING, placer.getHorizontalFacing().getOpposite()), 2);

        if (!w.isRemote)
            ((TileBelt) w.getTileEntity(pos)).setFacing(state.getValue(BlockBelt.FACING));
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        EnumFacing enumfacing = EnumFacing.getFront(meta >> 1);
        boolean slop = (meta & 1) == 1;

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
            enumfacing = EnumFacing.NORTH;

        return this.getDefaultState().withProperty(BlockBelt.FACING, enumfacing).withProperty(BlockBelt.SLOP, slop);
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        int meta = state.getValue(BlockBelt.FACING).getIndex();
        meta <<= 1;
        meta += state.getValue(BlockBelt.SLOP) ? 1 : 0;
        return meta;
    }

    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot)
    {
        return state.withProperty(BlockBelt.FACING, rot.rotate(state.getValue(BlockBelt.FACING)));
    }

    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(BlockBelt.FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, BlockBelt.FACING, BlockBelt.SLOP);
    }

    public boolean getSlopState(final IBlockState state)
    {
        return state.getValue(BlockBelt.SLOP).booleanValue();
    }

    public EnumFacing getFacing(final IBlockState state)
    {
        return state.getValue(BlockBelt.FACING);
    }

    public void setSlopState(final World world, final BlockPos pos, final boolean value)
    {
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockBelt.SLOP, value));
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        if (facing == null || !EnumFacing.Plane.HORIZONTAL.apply(facing))
            return false;
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockBelt.FACING, facing));
        if (!world.isRemote)
        {
            ((TileBelt) world.getTileEntity(pos)).setFacing(facing);
            ((TileBelt) world.getTileEntity(pos)).scanInput();
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileBelt(.05f);
    }

    @Override
    public boolean onWrench(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing)
    {
        IBlockState state = world.getBlockState(pos);
        if (player.isSneaking())
        {
            this.setSlopState(world, pos, !this.getSlopState(state));
        }
        else
            this.rotateBlock(world, pos, getFacing(state).rotateAround(Axis.Y));
        return true;
    }
}