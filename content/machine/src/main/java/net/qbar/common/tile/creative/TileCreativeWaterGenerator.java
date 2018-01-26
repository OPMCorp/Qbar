package net.qbar.common.tile.creative;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.qbar.common.fluid.CreativeFluidTank;
import net.qbar.common.tile.QBarTileBase;

public class TileCreativeWaterGenerator extends QBarTileBase implements ITickable
{
    private final FluidTank fluidTank;
    private final int transferCapacity;
    private EnumMap<EnumFacing, IFluidHandler> handler;
    
    public TileCreativeWaterGenerator()
    {
        this.fluidTank = new CreativeFluidTank(FluidRegistry.WATER);
        this.transferCapacity = Integer.MAX_VALUE;
        this.handler = new EnumMap<>(EnumFacing.class);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tag)
    {
        super.writeToNBT(tag);

        final NBTTagCompound subTag = new NBTTagCompound();
        this.fluidTank.writeToNBT(subTag);
        tag.setTag("fluidTank", subTag);

        return tag;
    }

    @Override
    public void readFromNBT(final NBTTagCompound tag)
    {
        super.readFromNBT(tag);

        if (tag.hasKey("fluidTank"))
            this.fluidTank.readFromNBT(tag.getCompoundTag("fluidTank"));
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return (T) this.fluidTank;
        return super.getCapability(capability, facing);
    }

    @Override
    public void addInfo(final List<String> lines)
    {
        lines.add("Water " + Double.POSITIVE_INFINITY);
    }

    public FluidTank getWaterTank()
    {
        return this.fluidTank;
    }
    
    @Override
    public void update()
    {
        if (this.isServer())
        {
            if (this.world.getTotalWorldTime() % 40 == 0)
                this.scanFluidHandlers();
                this.transferFluids();
        }
    }
    
    public void scanFluidHandlers()
    {
        for (final EnumFacing facing : EnumFacing.VALUES)
            this.scanFluidHandler(this.getPos().offset(facing), facing);
    }

    public void scanFluidHandler(final BlockPos posNeighbor, final EnumFacing facing)
    {
        final TileEntity tile = this.world.getTileEntity(posNeighbor);

        if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing))
        {
            this.handler.put(facing, tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing));
        }
        else if(this.handler.containsKey(facing))
            this.handler.remove(facing);
    }
    
    public void transferFluids()
    {
        this.handler.forEach((face, handler) -> handler.fill(new FluidStack(FluidRegistry.WATER, this.transferCapacity), true));
    }
}
