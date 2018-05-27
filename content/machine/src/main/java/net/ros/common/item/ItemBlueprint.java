package net.ros.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.ros.common.block.BlockStructure;
import net.ros.common.init.ROSBlocks;
import net.ros.common.machine.Machines;
import net.ros.common.multiblock.BlockMultiblockBase;
import net.ros.common.multiblock.MultiblockComponent;
import net.ros.common.multiblock.TileMultiblockGag;
import net.ros.common.multiblock.blueprint.Blueprint;
import net.ros.common.tile.TileStructure;
import net.ros.common.util.ItemUtils;

public class ItemBlueprint extends ItemBase
{
    public ItemBlueprint()
    {
        super("blueprint");
        this.setHasSubtypes(true);
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand,
                                      final EnumFacing facing, final float hitX, final float hitY, final float hitZ)
    {
        final IBlockState iblockstate = world.getBlockState(pos);
        final Block block = iblockstate.getBlock();

        if (!block.isReplaceable(world, pos))
            pos = pos.offset(facing);

        final ItemStack stack = player.getHeldItem(hand);

        if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey("blueprint"))
        {
            final String name = stack.getTagCompound().getString("blueprint");
            final Blueprint blueprint = Machines.getComponent(Blueprint.class,
                    stack.getTagCompound().getString("blueprint"));
            final BlockMultiblockBase base = (BlockMultiblockBase) Block.getBlockFromName("ros:" + name);

            if ((player.capabilities.isCreativeMode
                    || ItemUtils.hasPlayerEnough(player.inventory, blueprint.getRodStack(), false))
                    && player.canPlayerEdit(pos, facing, stack)
                    && world.mayPlace(base, pos, false, facing, null)
                    && base.canPlaceBlockAt(world, pos, player.getHorizontalFacing().getOpposite()))
            {
                final int i = this.getMetadata(stack.getMetadata());
                final IBlockState state = base.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, i, player,
                        hand);

                if (this.placeBlockAt(stack, player, world, pos, state, base.getMultiblock()))
                {
                    final SoundType soundtype = world.getBlockState(pos).getBlock()
                            .getSoundType(world.getBlockState(pos), world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                            (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    stack.shrink(1);
                    if (!player.capabilities.isCreativeMode)
                        ItemUtils.drainPlayer(player.inventory, blueprint.getRodStack());
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos,
                                final IBlockState newState, final MultiblockComponent descriptor)
    {
        if (!world.setBlockState(pos, ROSBlocks.STRUCTURE.getDefaultState(), 11))
            return false;

        final IBlockState state = world.getBlockState(pos);
        ItemBlock.setTileEntityNBT(world, player, pos, stack);
        state.getBlock().onBlockPlacedBy(world, pos, state, player, stack);
        final TileStructure structure = (TileStructure) world.getTileEntity(pos);
        if (structure != null)
        {
            structure.setBlueprint(
                    Machines.getComponent(Blueprint.class, stack.getTagCompound().getString("blueprint")));
            structure.setMeta(newState.getBlock().getMetaFromState(newState));
        }

        final Iterable<BlockPos> searchables = descriptor.getAllInBox(pos, BlockMultiblockBase.getFacing(newState));

        for (final BlockPos current : searchables)
        {
            if (!current.equals(pos))
            {
                if (!world.setBlockState(current,
                        ROSBlocks.STRUCTURE.getDefaultState().withProperty(BlockStructure.MULTIBLOCK_GAG, true)))
                    return false;
                final TileMultiblockGag gag = (TileMultiblockGag) world.getTileEntity(current);
                if (gag != null)
                    gag.setCorePos(pos);
            }
        }
        return true;
    }

    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> list)
    {
        if (this.isInCreativeTab(tab))
        {
            Machines.getAllByComponent(Blueprint.class).forEach(descriptor ->
            {
                ItemStack stack = new ItemStack(this);
                NBTTagCompound tag = new NBTTagCompound();
                stack.setTagCompound(tag);

                tag.setString("blueprint", descriptor.getName());
                list.add(stack);
            });
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(final ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("blueprint"))
            return I18n.format("item.blueprint.name",
                    I18n.format("tile." + stack.getTagCompound().getString("blueprint") + ".name")).trim();
        return this.name;
    }
}
