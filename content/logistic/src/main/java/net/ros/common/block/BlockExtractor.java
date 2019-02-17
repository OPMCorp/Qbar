package net.ros.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.ros.common.ROSConstants;
import net.ros.common.gui.LogisticGui;
import net.ros.common.tile.machine.TileExtractor;

import javax.annotation.Nullable;

public class BlockExtractor extends BlockMachineBase<TileExtractor>
{
    public static PropertyBool      FILTER = PropertyBool.create("filter");
    public static PropertyDirection FACING = PropertyDirection.create("facing", facing -> true);

    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    protected static final AxisAlignedBB AABB_TOP_HALF    = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB AABB_NORTH_HALF  = new AxisAlignedBB(0D, 0D, 0.5D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB AABB_SOUTH_HALF  = new AxisAlignedBB(0D, 0D, 0D, 1.0D, 1.0D, 0.5D);
    protected static final AxisAlignedBB AABB_EAST_HALF   = new AxisAlignedBB(0D, 0D, 0D, 0.5D, 1.0D, 1.0D);
    protected static final AxisAlignedBB AABB_WEST_HALF   = new AxisAlignedBB(0.5D, 0D, 0D, 1.0D, 1.0D, 1.0D);

    public BlockExtractor()
    {
        super("itemextractor", Material.IRON, TileExtractor.class);
        this.setDefaultState(this.blockState.getBaseState().withProperty(BlockExtractor.FACING, EnumFacing.UP)
                .withProperty(BlockExtractor.FILTER, false));
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos)
    {
        if (this.checkWorldTile(world, pos))
            return ((IExtendedBlockState) state).withProperty(Properties.AnimationProperty,
                    this.getWorldTile(world, pos).state);
        return state;
    }

    @Override
    public boolean onBlockActivated(final World w, final BlockPos pos, final IBlockState state, final EntityPlayer player,
                                    final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY,
                                    final float hitZ)
    {
        if (player.isSneaking())
            return false;
        final TileExtractor extractor = (TileExtractor) w.getTileEntity(pos);
        if (extractor != null && extractor.hasFilter())
        {
            player.openGui(ROSConstants.MODINSTANCE, LogisticGui.EXTRACTOR.getUniqueID(), w, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        return false;
    }

    @Override
    public void getSubBlocks(final CreativeTabs tab, final NonNullList<ItemStack> stacks)
    {
        stacks.add(new ItemStack(this, 1, 0));
        stacks.add(new ItemStack(this, 1, 1));
    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockState state, final IBlockAccess source, final BlockPos pos)
    {
        switch (state.getValue(BlockExtractor.FACING))
        {
            case DOWN:
                return BlockExtractor.AABB_TOP_HALF;
            case EAST:
                return BlockExtractor.AABB_EAST_HALF;
            case NORTH:
                return BlockExtractor.AABB_NORTH_HALF;
            case SOUTH:
                return BlockExtractor.AABB_SOUTH_HALF;
            case WEST:
                return BlockExtractor.AABB_WEST_HALF;
            default:
                return BlockExtractor.AABB_BOTTOM_HALF;
        }
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
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[]{BlockExtractor.FACING, BlockExtractor.FILTER},
                new IUnlistedProperty[]{Properties.AnimationProperty});
    }

    @Override
    public int damageDropped(final IBlockState state)
    {
        return state.getValue(BlockExtractor.FILTER) ? 1 : 0;
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(BlockExtractor.FACING).getIndex();

        if (state.getValue(BlockExtractor.FILTER).booleanValue())
            i |= 8;
        return i;
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(BlockExtractor.FACING, BlockExtractor.getFacing(meta))
                .withProperty(BlockExtractor.FILTER, Boolean.valueOf((meta & 8) > 0));
    }

    @Nullable
    public static EnumFacing getFacing(final int meta)
    {
        final int i = meta & 7;
        return i > 5 ? null : EnumFacing.byIndex(i);
    }

    public EnumFacing getFacing(final IBlockState state)
    {
        return state.getValue(BlockExtractor.FACING);
    }

    @Override
    public IBlockState getStateForPlacement(final World worldIn, final BlockPos pos, final EnumFacing facing,
                                            final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta).withProperty(BlockExtractor.FACING, facing)
                .withProperty(BlockExtractor.FILTER, meta == 1);
    }

    @Override
    public boolean rotateBlock(final World world, final BlockPos pos, final EnumFacing facing)
    {
        if (facing == null)
            return false;
        final NBTTagCompound previous = world.getTileEntity(pos).writeToNBT(new NBTTagCompound());
        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockExtractor.FACING, facing));
        world.getTileEntity(pos).readFromNBT(previous);
        if (!world.isRemote)
            ((TileExtractor) world.getTileEntity(pos)).setFacing(facing);
        return true;
    }

    @Override
    public void onBlockPlacedBy(final World w, final BlockPos pos, final IBlockState state,
                                final EntityLivingBase placer, final ItemStack stack)
    {
        super.onBlockPlacedBy(w, pos, state, placer, stack);

        if (!w.isRemote)
            ((TileExtractor) w.getTileEntity(pos)).setFacing(state.getValue(BlockExtractor.FACING));
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        final TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory)
        {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileExtractor(this.getStateFromMeta(meta).getValue(BlockExtractor.FILTER));
    }
}
