package net.qbar.common.container.slot;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.items.IItemHandler;

public class SlotFuel extends FilteredSlot
{
    public SlotFuel(final IItemHandler inventory, final int index, final int x, final int y)
    {
        super(inventory, index, x, y);
        this.setFilter(stack -> TileEntityFurnace.isItemFuel(stack) || SlotFuel.isBucket(stack));
    }

    @Override
    public int getItemStackLimit(final ItemStack stack)
    {
        return SlotFuel.isBucket(stack) ? 1 : super.getItemStackLimit(stack);
    }

    public static boolean isBucket(final ItemStack stack)
    {
        return stack.getItem() == Items.BUCKET;
    }
}
