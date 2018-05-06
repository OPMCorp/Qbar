package net.ros.common.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.ros.common.ROSConstants;
import net.ros.common.init.ROSItems;
import net.ros.common.util.ItemUtils;

@ReceivedOn(Side.SERVER)
public class MultiblockBoxPacket extends Message
{
    @MarshalledAs("i32")
    private int slotID;

    public MultiblockBoxPacket(final NetworkContext ctx)
    {
        super(ctx);
    }

    public MultiblockBoxPacket(final int slotID)
    {
        this(ROSConstants.network);

        this.slotID = slotID;
    }

    @Override
    protected void handle(final EntityPlayer sender)
    {
        if (sender.inventoryContainer != null)
        {
            ItemStack stack = sender.openContainer.getSlot(slotID).getStack();

            if (stack.getItem() == ROSItems.MULTIBLOCK_BOX && stack.hasTagCompound())
            {
                NonNullList<ItemStack> items = NonNullList
                        .withSize(stack.getTagCompound().getTagList("Items", 10).tagCount(), ItemStack.EMPTY);
                ItemUtils.loadAllItems(stack.getTagCompound(), items);
                for (ItemStack item : items)
                {
                    if (!sender.addItemStackToInventory(item))
                        InventoryHelper.spawnItemStack(sender.world, sender.posX, sender.posY, sender.posZ, item);
                }
                stack.setCount(0);
            }
        }
    }
}
