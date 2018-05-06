package net.ros.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.ros.common.block.property.PropertyString;
import net.ros.common.recipe.Materials;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiConsumer;

public class BlockMetal extends BlockBase implements IModelProvider
{
    public static PropertyString VARIANTS = new PropertyString("variant",
            Materials.metals.stream().filter(metal ->
                    !OreDictionary.doesOreNameExist("block" + StringUtils.capitalize(metal))).toArray(String[]::new));

    public BlockMetal()
    {
        super("blockmetal", Material.IRON);

        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANTS, VARIANTS.getByIndex(0)));

    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getItemModelCount()
    {
        return VARIANTS.getAllowedValues().size();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemModelByIndex(int index)
    {
        return "variant=" + this.getStateFromMeta(index).getValue(VARIANTS);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiConsumer<Integer, Block> registerItemModels()
    {
        return (i, block) ->
        {
            int index = Materials.metals.indexOf(VARIANTS.getByIndex(i));

            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), index, new ModelResourceLocation(
                    block.getRegistryName(), this.getItemModelByIndex(index)));
        };
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        for (int i = 0; i < Materials.metals.size(); i++)
        {
            if (VARIANTS.getAllowedValues().contains(Materials.metals.get(i)))
                items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        return this.getDefaultState().withProperty(VARIANTS, Materials.metals.get(meta));
    }

    @Override
    public int getMetaFromState(final IBlockState state)
    {
        return Materials.metals.indexOf(state.getValue(VARIANTS));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, VARIANTS);
    }
}
