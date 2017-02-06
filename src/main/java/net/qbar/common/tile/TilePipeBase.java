package net.qbar.common.tile;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.qbar.client.render.tile.VisibilityModelState;
import net.qbar.common.event.TickHandler;
import net.qbar.common.grid.CableGrid;
import net.qbar.common.grid.GridManager;
import net.qbar.common.grid.ITileCable;

public abstract class TilePipeBase<G extends CableGrid, H> extends QBarTileBase
        implements ITileInfoProvider, ILoadable, ITileCable<G>
{
    protected final EnumMap<EnumFacing, ITileCable<G>> connections;
    protected final EnumMap<EnumFacing, H>             adjacentHandler;
    protected final Capability<H>                      capability;
    protected int                                      grid;

    protected int                                      transferCapacity;

    public TilePipeBase(final int transferCapacity, final Capability<H> capability)
    {
        this.transferCapacity = transferCapacity;
        this.capability = capability;

        this.connections = new EnumMap<>(EnumFacing.class);
        this.adjacentHandler = new EnumMap<>(EnumFacing.class);
        this.grid = -1;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing)
    {
        if (capability == this.capability)
            return this.getGrid() != -1 && this.getGridObject() != null;
        return super.hasCapability(capability, facing);
    }

    @Override
    public void addInfo(final List<String> lines)
    {
        lines.add("Grid: " + this.grid);

        if (this.getGrid() != -1 && this.getGridObject() != null)
        {
            addSpecificInfo(lines);
        }
        else
            lines.add("Errored grid!");
        this.connections.forEach((facing, cable) -> lines.add("Pipe " + facing + ": " + (cable != null)));
        this.adjacentHandler.forEach((facing, handler) -> lines.add("Handler " + facing + ": " + (handler != null)));
    }

    public abstract void addSpecificInfo(final List<String> lines);

    @Override
    public EnumFacing[] getConnections()
    {
        return this.connections.keySet().toArray(new EnumFacing[0]);
    }

    public Collection<H> getConnectedHandlers()
    {
        return this.adjacentHandler.values();
    }

    @Override
    public ITileCable<G> getConnected(final EnumFacing facing)
    {
        return this.connections.get(facing);
    }

    @Override
    public int getGrid()
    {
        return this.grid;
    }

    @Override
    public void connect(final EnumFacing facing, final ITileCable<G> to)
    {
        this.connections.put(facing, to);
        this.updateState();
    }

    @Override
    public void disconnect(final EnumFacing facing)
    {
        this.connections.remove(facing);
        this.updateState();
    }

    public void connectHandler(final EnumFacing facing, final H to)
    {
        this.adjacentHandler.put(facing, to);
        this.updateState();
    }

    public void disconnectHandler(final EnumFacing facing)
    {
        this.adjacentHandler.remove(facing);
        this.updateState();
    }

    @Override
    public void onChunkUnload()
    {
        GridManager.getInstance().disconnectCable(this);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (!this.world.isRemote && this.getGrid() == -1)
            TickHandler.loadables.add(this);
        else if (this.isClient())
        {
            this.forceSync();
            this.updateState();
        }
    }

    @Override
    public void load()
    {
        GridManager.getInstance().connectCable(this);
        for (final EnumFacing facing : EnumFacing.VALUES)
            this.scanHandlers(this.pos.offset(facing));
    }

    @Override
    public void readFromNBT(final NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        this.transferCapacity = tagCompound.getInteger("transferCapacity");

        this.connections.clear();
        for (final EnumFacing facing : EnumFacing.VALUES)
        {
            if (tagCompound.hasKey("connected" + facing.ordinal()))
                this.connect(facing, null);
            if (tagCompound.hasKey("connectedHandler" + facing.ordinal()))
                this.connectHandler(facing, null);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        tagCompound.setInteger("transferCapacity", this.transferCapacity);

        for (final Entry<EnumFacing, ITileCable<G>> entry : this.connections.entrySet())
            tagCompound.setBoolean("connected" + entry.getKey().ordinal(), true);
        for (final Entry<EnumFacing, H> entry : this.adjacentHandler.entrySet())
            tagCompound.setBoolean("connectedHandler" + entry.getKey().ordinal(), true);
        return tagCompound;
    }

    public abstract void scanHandlers(final BlockPos posNeighbor);

    ////////////
    // RENDER //
    ////////////

    public final VisibilityModelState state = new VisibilityModelState();

    private void updateState()
    {
        if (this.isServer())
        {
            this.sync();
            return;
        }
        this.state.hidden.clear();

        this.state.hidden.add("valvex1");
        this.state.hidden.add("valvex2");
        this.state.hidden.add("valvey1");
        this.state.hidden.add("valvey2");
        this.state.hidden.add("valvez1");
        this.state.hidden.add("valvez2");

        if (this.connections.isEmpty() && this.adjacentHandler.isEmpty())
        {
            this.state.hidden.add("armx1");
            this.state.hidden.add("armx2");
            this.state.hidden.add("army1");
            this.state.hidden.add("army2");
            this.state.hidden.add("armz1");
            this.state.hidden.add("armz2");
            this.state.hidden.add("straightx");
            this.state.hidden.add("straighty");
            this.state.hidden.add("straightz");
        }
        else if (this.isStraight())
        {
            this.state.hidden.add("center");
            this.state.hidden.add("armx1");
            this.state.hidden.add("armx2");

            this.state.hidden.add("army1");
            this.state.hidden.add("army2");

            this.state.hidden.add("armz1");
            this.state.hidden.add("armz2");

            if (this.isConnected(EnumFacing.WEST))
            {
                this.state.hidden.add("straighty");
                this.state.hidden.add("straightz");
            }
            else if (this.isConnected(EnumFacing.NORTH))
            {
                this.state.hidden.add("straighty");
                this.state.hidden.add("straightx");
            }
            else if (this.isConnected(EnumFacing.UP))
            {
                this.state.hidden.add("straightx");
                this.state.hidden.add("straightz");
            }
        }
        else
        {
            this.state.hidden.add("straightx");
            this.state.hidden.add("straighty");
            this.state.hidden.add("straightz");

            if (!this.isConnected(EnumFacing.UP))
                this.state.hidden.add("army1");
            if (!this.isConnected(EnumFacing.DOWN))
                this.state.hidden.add("army2");
            if (!this.isConnected(EnumFacing.NORTH))
                this.state.hidden.add("armz1");
            if (!this.isConnected(EnumFacing.SOUTH))
                this.state.hidden.add("armz2");
            if (!this.isConnected(EnumFacing.EAST))
                this.state.hidden.add("armx1");
            if (!this.isConnected(EnumFacing.WEST))
                this.state.hidden.add("armx2");
        }

        this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }

    public boolean isStraight()
    {
        if (this.connections.size() + this.adjacentHandler.size() == 2)
            return this.isConnected(EnumFacing.NORTH) && this.isConnected(EnumFacing.SOUTH)
                    || this.isConnected(EnumFacing.WEST) && this.isConnected(EnumFacing.EAST)
                    || this.isConnected(EnumFacing.UP) && this.isConnected(EnumFacing.DOWN);
        return false;
    }

    public boolean isConnected(final EnumFacing facing)
    {
        return this.connections.containsKey(facing) || this.adjacentHandler.containsKey(facing);
    }

}
