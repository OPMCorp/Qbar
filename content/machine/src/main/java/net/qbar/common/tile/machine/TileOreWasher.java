package net.qbar.common.tile.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.qbar.common.QBarConstants;
import net.qbar.common.container.BuiltContainer;
import net.qbar.common.container.ContainerBuilder;
import net.qbar.common.gui.MachineGui;
import net.qbar.common.init.QBarItems;
import net.qbar.common.machine.QBarMachines;
import net.qbar.common.multiblock.BlockMultiblockBase;
import net.qbar.common.multiblock.MultiblockComponent;
import net.qbar.common.multiblock.MultiblockSide;
import net.qbar.common.recipe.QBarRecipeHandler;
import net.qbar.common.steam.CapabilitySteamHandler;
import net.qbar.common.tile.TileCraftingMachineBase;
import net.qbar.common.util.FluidUtils;

public class TileOreWasher extends TileCraftingMachineBase
{
    public TileOreWasher()
    {
        super(QBarMachines.ORE_WASHER);
    }

    @Override
    public Object[] getCustomData()
    {
        return new Object[]{0};
    }

    @Override
    public BuiltContainer createContainer(EntityPlayer player)
    {
        return new ContainerBuilder("orewasher", player).player(player.inventory).inventory(8, 84).hotbar(8, 142)
                .addInventory().tile(this)
                .recipeSlot(0, QBarRecipeHandler.ORE_WASHER_UID, 0, 47, 36,
                        slot -> this.isBufferEmpty() && this.isOutputEmpty())
                .outputSlot(1, 107, 35).outputSlot(2, 125, 35).displaySlot(3, -1000, 0)
                .syncFloatValue(this::getCurrentProgress, this::setCurrentProgress)
                .syncFloatValue(this::getMaxProgress, this::setMaxProgress)
                .syncIntegerValue(this.getSteamTank()::getSteam, this.getSteamTank()::setSteam)
                .syncFluidValue(() -> this.getInputFluidStack(0), stack -> this.setInputFluidStack(0, stack))
                .addInventory().create();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    public EnumFacing getFacing()
    {
        return this.world.getBlockState(this.pos).getValue(BlockMultiblockBase.FACING);
    }

    @Override
    public boolean onRightClick(final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY,
                                final float hitZ, BlockPos from)
    {
        if (player.isSneaking())
            return false;
        if (player.getHeldItemMainhand().getItem() == QBarItems.WRENCH)
            return false;

        if (FluidUtils.drainPlayerHand(this.getInputTanks()[0], player)
                || FluidUtils.fillPlayerHand(this.getInputTanks()[0], player))
        {
            this.markDirty();
            return true;
        }
        player.openGui(QBarConstants.MODINSTANCE, MachineGui.OREWASHER.getUniqueID(), this.world, this.pos.getX(),
                this.pos.getY(),
                this.pos.getZ());
        return true;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final BlockPos from, final EnumFacing facing)
    {
        MultiblockSide side = QBarMachines.ORE_WASHER.get(MultiblockComponent.class)
                .worldSideToMultiblockSide(new MultiblockSide(from, facing), this.getFacing());

        if (capability == CapabilitySteamHandler.STEAM_HANDLER_CAPABILITY && side.getPos().getX() == -1
                && side.getPos().getY() == 0 && side.getPos().getZ() == 0 && side.getFacing() == EnumFacing.WEST)
        {
            return true;
        }
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side.getPos().getX() == 1
                && side.getPos().getY() == 0 && side.getPos().getZ() == -1 && side.getFacing() == EnumFacing.EAST)
        {
            return true;
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side.getPos().getX() == 0
                && side.getPos().getY() == 1 && side.getPos().getZ() == -1 && side.getFacing() == EnumFacing.NORTH)
        {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final BlockPos from, final EnumFacing facing)
    {
        MultiblockSide side = QBarMachines.ORE_WASHER.get(MultiblockComponent.class)
                .worldSideToMultiblockSide(new MultiblockSide(from, facing), this.getFacing());

        if (capability == CapabilitySteamHandler.STEAM_HANDLER_CAPABILITY && side.getPos().getX() == -1
                && side.getPos().getY() == 0 && side.getPos().getZ() == 0 && side.getFacing() == EnumFacing.WEST)
        {
            return (T) this.getSteamTank();
        }
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side.getPos().getX() == 1
                && side.getPos().getY() == 0 && side.getPos().getZ() == -1 && side.getFacing() == EnumFacing.EAST)
        {
            return (T) this.getInputTanks()[0];
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side.getPos().getX() == 0
                && side.getPos().getY() == 1 && side.getPos().getZ() == -1 && side.getFacing() == EnumFacing.NORTH)
        {
            return (T) this.getInventoryWrapper(facing);
        }
        return null;
    }
}
