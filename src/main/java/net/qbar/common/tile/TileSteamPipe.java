package net.qbar.common.tile;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.qbar.common.grid.ISteamPipe;
import net.qbar.common.grid.ITileCable;
import net.qbar.common.grid.SteamGrid;
import net.qbar.common.steam.CapabilitySteamHandler;
import net.qbar.common.steam.ISteamHandler;
import net.qbar.common.steam.SteamUtil;

public class TileSteamPipe extends TilePipeBase<SteamGrid, ISteamHandler> implements ISteamPipe
{

    private int coldStorage;

    public TileSteamPipe(final int transferCapacity)
    {
        super(transferCapacity, CapabilitySteamHandler.STEAM_HANDLER_CAPABILITY);
    }

    public TileSteamPipe()
    {
        this(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == this.capability)
            return (T) this.getGridObject().getTank();
        return super.getCapability(capability, facing);
    }

    @Override
    public void addSpecificInfo(final List<String> lines)
    {
        lines.add("Contains: " + this.getGridObject().getTank().getSteam() + " / "
                + this.getGridObject().getTank().getCapacity());
        lines.add("Pressure " + SteamUtil.pressureFormat.format(this.getGridObject().getTank().getPressure()) + " / "
                + SteamUtil.pressureFormat.format(this.getGridObject().getTank().getMaxPressure()));
    }

    @Override
    public void setGrid(final int gridIdentifier)
    {
        final int previous = this.grid;
        this.grid = gridIdentifier;

        if (gridIdentifier == -1)
            this.coldStorage = 0;
        else if (this.coldStorage != 0 && previous == -1)
        {
            this.getGridObject().getTank().fillInternal(this.coldStorage, true);
            this.coldStorage = 0;
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        this.coldStorage = tagCompound.getInteger("coldStorage");
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        this.toColdStorage();
        if (this.coldStorage != 0)
            tagCompound.setInteger("coldStorage", this.coldStorage);

        return tagCompound;
    }

    @Override
    public boolean canConnect(final ITileCable<?> to)
    {
        if (to instanceof TileSteamPipe)
            return true;
        return false;
    }

    @Override
    public void scanHandlers(final BlockPos posNeighbor)
    {
        final TileEntity tile = this.world.getTileEntity(posNeighbor);

        final BlockPos substract = posNeighbor.subtract(this.pos);
        final EnumFacing facing = EnumFacing.getFacingFromVector(substract.getX(), substract.getY(), substract.getZ())
                .getOpposite();

        if (this.adjacentHandler.containsKey(facing.getOpposite()))
        {
            if (tile == null || !tile.hasCapability(this.capability, facing))
            {
                this.disconnectHandler(facing.getOpposite());
                if (this.adjacentHandler.isEmpty())
                    this.getGridObject().removeConnectedPipe(this);
            }
        }
        else
        {
            if (tile != null)
            {
                if (tile.hasCapability(this.capability, facing) && !(tile instanceof TileSteamPipe))
                {
                    this.connectHandler(facing.getOpposite(), tile.getCapability(this.capability, facing));
                    this.getGridObject().addConnectedPipe(this);
                }
            }
        }
    }

    public void toColdStorage()
    {
        if (this.getGridObject() != null && this.getGridObject().getTank().getSteam() != 0)
        {
            this.coldStorage = this.getGridObject().getTank().getSteam();
            this.coldStorage /= this.getGridObject().getCables().size();
        }
    }

    @Override
    public SteamGrid createGrid(final int id)
    {
        return new SteamGrid(id, this.transferCapacity);
    }

}
