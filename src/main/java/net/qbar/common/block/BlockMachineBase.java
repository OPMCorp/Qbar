package net.qbar.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.qbar.QBar;

public abstract class BlockMachineBase extends BlockContainer
{
    public String name;

    public BlockMachineBase(final String name, final Material material)
    {
        super(material);

        this.name = name;
        this.setUnlocalizedName(name);
        this.setCreativeTab(QBar.TAB_ALL);
    }

    @Override
    public EnumBlockRenderType getRenderType(final IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
}
