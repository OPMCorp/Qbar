package net.qbar.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidUtils
{
    public static final boolean drainPlayerHand(final IFluidHandler fluidHandler, final EntityPlayer player)
    {
        final ItemStack input = player.getHeldItemMainhand();
        ItemStack output = ItemStack.EMPTY;

        final IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

        if (fluidHandler != null && inputFluidHandler != null
                && inputFluidHandler.getTankProperties()[0].getContents() != null)
        {

            final int simulated = fluidHandler.fill(inputFluidHandler.drain(Integer.MAX_VALUE, false), false);
            if (simulated > 0)
            {
                fluidHandler.fill(inputFluidHandler.drain(simulated, true), true);
                if ((inputFluidHandler.getTankProperties()[0].getContents() == null
                        || inputFluidHandler.getTankProperties()[0].getContents().amount == 0) && !player.isCreative())
                {
                    output = inputFluidHandler.getContainer();
                    if (input.getCount() == 1)
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, output);
                    else
                    {
                        player.inventory.decrStackSize(player.inventory.currentItem, 1);
                        if (!player.inventory.addItemStackToInventory(output))
                            player.entityDropItem(output, 0);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static final boolean fillPlayerHand(final IFluidHandler fluidHandler, final EntityPlayer player)
    {
        final ItemStack input = player.getHeldItemMainhand();
        ItemStack output = ItemStack.EMPTY;

        final IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

        if (fluidHandler != null && inputFluidHandler != null
                && fluidHandler.getTankProperties()[0].getContents() != null)
        {

            final int simulated = inputFluidHandler.fill(fluidHandler.drain(Integer.MAX_VALUE, false), false);
            if (simulated > 0)
            {
                inputFluidHandler.fill(fluidHandler.drain(simulated, true), true);

                output = inputFluidHandler.getContainer();
                if (input.getCount() == 1)
                {
                    if (!player.isCreative())
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, output);
                    else
                        player.inventory.addItemStackToInventory(output);
                }
                else
                {
                    if (!player.isCreative())
                        player.inventory.decrStackSize(player.inventory.currentItem, 1);
                    if (!player.inventory.addItemStackToInventory(output))
                        player.entityDropItem(output, 0);
                }
                return true;
            }
        }
        return false;

    }

    public static final boolean drainContainers(final IFluidHandler fluidHandler, final IInventory inv,
            final int inputSlot, final int outputSlot)
    {
        final ItemStack input = inv.getStackInSlot(inputSlot);
        final ItemStack output = inv.getStackInSlot(outputSlot);

        final IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

        if (inputFluidHandler != null)
        {
            /*
             * Making a simulation to check if the fluid can be drained into the
             * fluidhandler.
             */
            if (FluidUtil.tryFluidTransfer(fluidHandler, inputFluidHandler,
                    inputFluidHandler.getTankProperties()[0].getCapacity(), false) != null)
            {
                // Changes are really applied and the fluid is drained.
                final FluidStack drained = FluidUtil.tryFluidTransfer(fluidHandler, inputFluidHandler,
                        inputFluidHandler.getTankProperties()[0].getCapacity(), true);

                /*
                 * If the drained container doesn't disappear we need to update
                 * the inventory accordingly.
                 */
                if (drained != null && inputFluidHandler.getContainer() != ItemStack.EMPTY)
                    if (output == ItemStack.EMPTY)
                    {
                        inv.setInventorySlotContents(outputSlot, inputFluidHandler.getContainer());
                        inv.decrStackSize(inputSlot, 1);
                    }
                    else
                    {
                        /*
                         * When output is not EMPTY, it is needed to check if
                         * the two stacks can be merged together, there was no
                         * simple way to make that check before.
                         */
                        if (ItemUtils.deepEquals(output, inputFluidHandler.getContainer()))
                        {
                            inv.getStackInSlot(outputSlot).setCount(inv.getStackInSlot(outputSlot).getCount() + 1);
                            inv.decrStackSize(inputSlot, 1);
                        }
                        else
                        {
                            /*
                             * Due to the late check of stacks merge we need to
                             * reverse any changes made to the FluidHandlers
                             * when the merge fail.
                             */
                            FluidUtil.tryFluidTransfer(inputFluidHandler, fluidHandler, drained.amount, true);
                            return false;
                        }
                    }
                return true;
            }
        }
        return false;
    }

    public static boolean fillContainers(final IFluidHandler fluidHandler, final IInventory inv, final int inputSlot,
            final int outputSlot, final Fluid fluidToFill)
    {
        final ItemStack input = inv.getStackInSlot(inputSlot);
        final ItemStack output = inv.getStackInSlot(outputSlot);

        if (input != ItemStack.EMPTY)
        {
            final IFluidHandlerItem inputFluidHandler = FluidUtils.getFluidHandler(input);

            /*
             * The copy is needed to get the filled container without altering
             * the original ItemStack.
             */
            final ItemStack containerCopy = input.copy();
            containerCopy.setCount(1);

            /*
             * It's necessary to check before any alterations that the resulting
             * ItemStack can be placed into the outputSlot.
             */
            if (inputFluidHandler != null && (output == ItemStack.EMPTY || output.getCount() < output.getMaxStackSize()
                    && ItemUtils.deepEquals(FluidUtils.getFilledContainer(fluidToFill, containerCopy), output)))
            {
                /*
                 * Making a simulation to check if the fluid can be transfered
                 * into the fluidhandler.
                 */
                if (FluidUtil.tryFluidTransfer(inputFluidHandler, fluidHandler,
                        inputFluidHandler.getTankProperties()[0].getCapacity(), false) != null)
                {
                    // Changes are really applied and the fluid is transfered.
                    FluidUtil.tryFluidTransfer(inputFluidHandler, fluidHandler,
                            inputFluidHandler.getTankProperties()[0].getCapacity(), true);

                    // The inventory is modified and stacks are merged.
                    if (output == ItemStack.EMPTY)
                        inv.setInventorySlotContents(outputSlot, inputFluidHandler.getContainer());
                    else
                        inv.getStackInSlot(outputSlot).setCount(inv.getStackInSlot(outputSlot).getCount() + 1);
                    inv.decrStackSize(inputSlot, 1);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public static final IFluidHandlerItem getFluidHandler(final ItemStack container)
    {
        final ItemStack copy = container.copy();
        copy.setCount(1);
        return FluidUtil.getFluidHandler(copy);
    }

    @Nonnull
    public static ItemStack getFilledContainer(final Fluid fluid, final ItemStack empty)
    {
        if (fluid == null || empty == ItemStack.EMPTY)
            return ItemStack.EMPTY;
        final IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(empty);
        fluidHandler.fill(new FluidStack(fluid, fluidHandler.getTankProperties()[0].getCapacity()), true);
        return empty;
    }
}
