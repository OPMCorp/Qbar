package net.ros.common.tile.machine;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.ros.client.render.model.obj.ROSOBJState;
import net.ros.common.block.property.BeltSlope;
import net.ros.common.event.TickHandler;
import net.ros.common.grid.GridManager;
import net.ros.common.grid.IConnectionAware;
import net.ros.common.grid.ItemBelt;
import net.ros.common.grid.impl.BeltGrid;
import net.ros.common.grid.impl.CableGrid;
import net.ros.common.grid.node.IBelt;
import net.ros.common.grid.node.ITileCable;
import net.ros.common.grid.node.ITileNode;
import net.ros.common.steam.ISteamHandler;
import net.ros.common.steam.SteamCapabilities;
import net.ros.common.steam.SteamUtil;
import net.ros.common.tile.ILoadable;
import net.ros.common.tile.ITileInfoList;
import net.ros.common.tile.TileBase;
import net.ros.common.util.ItemUtils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TileBelt extends TileBase implements IBelt, ILoadable, IConnectionAware
{
    @Getter
    private       int                                       grid;
    @Getter
    private final EnumMap<EnumFacing, ITileCable<BeltGrid>> connectionsMap;
    private       float                                     beltSpeed;

    private EnumFacing facing;

    private final ItemBelt[] items;

    private boolean hasChanged = false;
    private boolean isWorking  = false;

    private final EnumMap<EnumFacing, ISteamHandler> steamConnections;

    private BeltSlope slopeState;

    private long lastWorkStateChange;

    private int storedSteam;

    public TileBelt(final float beltSpeed)
    {
        this.beltSpeed = beltSpeed;

        this.grid = -1;
        this.connectionsMap = new EnumMap<>(EnumFacing.class);
        this.steamConnections = new EnumMap<>(EnumFacing.class);
        this.facing = EnumFacing.UP;

        this.items = new ItemBelt[3];

        this.slopeState = BeltSlope.NORMAL;
    }

    public TileBelt()
    {
        this(0);
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing)
    {
        if (capability == SteamCapabilities.STEAM_HANDLER && facing != EnumFacing.UP)
            return this.getGrid() != -1 && this.getGridObject() != null;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing)
    {
        if (capability == SteamCapabilities.STEAM_HANDLER && facing != EnumFacing.UP)
            return (T) this.getGridObject().getTank();
        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tag)
    {
        super.writeToNBT(tag);

        tag.setInteger("facing", this.facing.ordinal());
        tag.setFloat("beltSpeed", this.beltSpeed);
        tag.setInteger("isSlope", this.slopeState.ordinal());

        for (int i = 0; i < 3; i++)
        {
            if (this.items[i] == null)
                continue;
            final NBTTagCompound subTag = new NBTTagCompound();

            subTag.setDouble("posX", this.items[i].getPosX());
            subTag.setDouble("posY", this.items[i].getPosY());

            this.items[i].getStack().writeToNBT(subTag);

            tag.setTag("item" + i, subTag);
        }
        tag.setInteger("itemCount", this.items.length);
        tag.setBoolean("isWorking", this.isWorking);

        if (this.grid != -1)
            tag.setInteger("storedSteam", this.getGridObject().getTank().drainSteam(
                    this.getGridObject().getTank().getSteam() / this.getGridObject().getCables().size(), false));

        for (final Entry<EnumFacing, ISteamHandler> entry: this.steamConnections.entrySet())
            tag.setBoolean("connectedSteam" + entry.getKey().ordinal(), true);
        for (final Entry<EnumFacing, ITileCable<BeltGrid>> entry: this.connectionsMap.entrySet())
            tag.setBoolean("connected" + entry.getKey().ordinal(), true);
        return tag;
    }

    private final EnumMap<EnumFacing, ITileCable<BeltGrid>> tmpConnections      = new EnumMap<>(EnumFacing.class);
    private final EnumMap<EnumFacing, ISteamHandler>        tmpSteamConnections = new EnumMap<>(EnumFacing.class);

    @Override
    public void readFromNBT(final NBTTagCompound tag)
    {
        super.readFromNBT(tag);

        this.facing = EnumFacing.VALUES[tag.getInteger("facing")];
        this.beltSpeed = tag.getFloat("beltSpeed");
        this.slopeState = BeltSlope.values()[tag.getInteger("isSlope")];

        for (int i = 0; i < 3; i++)
        {
            if (!tag.hasKey("item" + i))
            {
                if (this.items[i] != null)
                    this.items[i] = null;
                continue;
            }
            final NBTTagCompound subTag = tag.getCompoundTag("item" + i);

            ItemStack newItem = new ItemStack(subTag);
            if (this.items[i] == null || !ItemUtils.deepEquals(this.items[i].getStack(), newItem))
            {
                this.items[i] = new ItemBelt(new ItemStack(subTag),
                        subTag.getDouble("posX"), subTag.getDouble("posY"));
            }
            else
            {
                this.items[i].setPosX(subTag.getDouble("posX"));
                this.items[i].setPosY(subTag.getDouble("posY"));
            }
        }


        boolean needStateUpdate = false;
        if (this.isWorking != tag.getBoolean("isWorking"))
            needStateUpdate = true;

        this.isWorking = tag.getBoolean("isWorking");
        this.storedSteam = tag.getInteger("storedSteam");

        if (this.isClient())
        {
            this.tmpConnections.clear();
            this.tmpConnections.putAll(this.connectionsMap);
            this.connectionsMap.clear();

            this.tmpSteamConnections.clear();
            this.tmpSteamConnections.putAll(this.steamConnections);
            this.steamConnections.clear();

            for (final EnumFacing facing: EnumFacing.VALUES)
            {
                if (tag.hasKey("connectedSteam" + facing.ordinal()))
                    this.steamConnections.put(facing, null);
                if (tag.hasKey("connected" + facing.ordinal()))
                    this.connectionsMap.put(facing, null);
            }
            if (!this.tmpConnections.equals(this.connectionsMap)
                    || !this.tmpSteamConnections.equals(this.steamConnections))
                needStateUpdate = true;

            if (needStateUpdate)
                this.updateState();
        }
    }

    @Override
    public void addInfo(ITileInfoList list)
    {
        list.addText("Slope: " + this.slopeState);
        list.addText("Orientation: " + this.getFacing());
        list.addText("Grid: " + this.getGrid());

        if (this.getGrid() != -1 && this.getGridObject() != null)
        {
            list.addText("Contains: " + this.getGridObject().getTank().getSteam() + " / "
                    + this.getGridObject().getTank().getCapacity());
            list.addText("Pressure " + SteamUtil.pressureFormat.format(this.getGridObject().getTank().getPressure())
                    + " / " + SteamUtil.pressureFormat.format(this.getGridObject().getTank().getMaxPressure()));
        }
        else
            list.addText("Errored grid!");

        for (int i = 0; i < this.items.length; i++)
        {
            if (this.items[i] == null)
                continue;
            list.addText("Slot " + i + ": " + ItemUtils.getPrettyStackName(this.items[i].getStack()));
        }
    }

    @Override
    public void setGrid(final int gridIdentifier)
    {
        this.grid = gridIdentifier;

        if (this.getGridObject() != null)
        {
            this.getGridObject().getTank().fillSteam(this.storedSteam, true);
            this.storedSteam = 0;
            this.world.notifyNeighborsOfStateChange(this.getBlockPos(), this.getBlockType(), true);
        }
    }

    @Override
    public void onChunkUnload()
    {
        GridManager.getInstance().disconnectCable(this);
    }

    @Override
    public boolean canConnect(EnumFacing facing, ITileNode<?> to)
    {
        if (to instanceof TileBelt)
        {
            final IBelt adjacentBelt = (IBelt) to;
            return adjacentBelt.getFacing() != this.getFacing().getOpposite();
        }
        return false;
    }

    @Override
    public void connect(final EnumFacing facing, final ITileCable<BeltGrid> to)
    {
        this.connectionsMap.put(facing, to);
        this.updateState();
    }

    @Override
    public void disconnect(final EnumFacing facing)
    {
        this.connectionsMap.remove(facing);
        this.updateState();
    }

    @Override
    public BeltGrid createGrid(final int nextID)
    {
        return new BeltGrid(nextID, this.beltSpeed);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (this.isServer() && this.getGrid() == -1)
            TickHandler.loadables.add(this);
        if (this.isClient())
        {
            this.askServerSync();
            this.updateState();
        }
    }

    @Override
    public void load()
    {
        GridManager.getInstance().connectCable(this);
    }

    @Override
    public BlockPos getAdjacentPos(final EnumFacing facing)
    {
        if (this.slopeState.equals(BeltSlope.DOWN) && facing == this.getFacing())
            return this.getBlockPos().down().offset(facing);
        else if (this.slopeState.equals(BeltSlope.UP) && facing == this.getFacing().getOpposite())
            return this.getBlockPos().down().offset(facing);
        return this.getBlockPos().offset(facing);
    }

    @Override
    public void adjacentConnect()
    {
        for (final EnumFacing facing: EnumFacing.HORIZONTALS)
        {
            final TileEntity adjacent = this.getBlockWorld().getTileEntity(this.getAdjacentPos(facing).up());
            if (adjacent instanceof IBelt && ((IBelt) adjacent).isSlope() && this.canConnect(facing, (IBelt) adjacent)
                    && ((IBelt) adjacent).canConnect(facing.getOpposite(), this))
            {
                this.connect(facing, (IBelt) adjacent);
                ((IBelt) adjacent).connect(facing.getOpposite(), this);
            }
        }

        for (final EnumFacing facing: EnumFacing.HORIZONTALS)
        {
            if (this.isConnected(facing))
                continue;
            final TileEntity adjacent = this.getBlockWorld().getTileEntity(this.getAdjacentPos(facing));
            if (adjacent instanceof IBelt && this.canConnect(facing, (IBelt) adjacent)
                    && ((IBelt) adjacent).canConnect(facing.getOpposite(), this))
            {
                this.connect(facing, (IBelt) adjacent);
                ((IBelt) adjacent).connect(facing.getOpposite(), this);
            }
        }
    }

    public float getBeltSpeed()
    {
        return this.beltSpeed;
    }

    @Override
    public ItemBelt[] getItems()
    {
        return this.items;
    }

    @Override
    public EnumFacing getFacing()
    {
        return this.facing;
    }

    public void setFacing(final EnumFacing facing)
    {
        this.facing = facing;
    }

    @Override
    public boolean isSlope()
    {
        return this.slopeState.isSlope();
    }

    public BeltSlope getSlopeState()
    {
        return this.slopeState;
    }

    public void setSlope(final BeltSlope slope)
    {
        this.slopeState = slope;
    }

    @Override
    public boolean insert(ItemStack stack, float posX, float posY, boolean doInsert)
    {
        if (this.getGridObject() != null)
            return this.getGridObject().insert(this, stack, posX, posY, doInsert);
        return false;
    }

    @Override
    public void itemUpdate()
    {
        this.sync();
    }

    @Override
    public boolean hasChanged()
    {
        return this.hasChanged;
    }

    @Override
    public void setChanged(final boolean change)
    {
        this.hasChanged = change;
    }

    @Override
    public boolean isWorking()
    {
        return this.isWorking;
    }

    @Override
    public void setWorking(final boolean working)
    {
        this.isWorking = working;
        this.lastWorkStateChange = System.currentTimeMillis();
    }

    @Override
    public long getLastWorkStateChange()
    {
        return this.lastWorkStateChange;
    }

    ////////////
    // RENDER //
    ////////////

    private static final HashMap<String, ROSOBJState> variants = new HashMap<>();

    public ROSOBJState getVisibilityState()
    {
        String key = this.getVariantKey();

        if (!this.variants.containsKey(key))
            this.variants.put(key, buildVisibilityState());
        return this.variants.get(key);
    }

    public String getVariantKey()
    {
        if (this.getFacing().getAxis().isVertical())
            return "vertical";

        StringBuilder rtn = new StringBuilder(2);

        if (this.isConnected(this.getFacing().rotateY()))
            rtn.append("e");
        if (this.isConnected(this.getFacing().rotateY().getOpposite()))
            rtn.append("w");
        return rtn.toString();
    }

    private ROSOBJState buildVisibilityState()
    {
        List<String> parts = new ArrayList<>();

        if (this.getFacing().getAxis().isVertical())
        {
            parts.add("east");
            parts.add("west");
        }
        else
        {
            if (!this.isConnected(this.getFacing().rotateY()))
                parts.add("east");
            if (!this.isConnected(this.getFacing().rotateY().getOpposite()))
                parts.add("west");
        }
        return new ROSOBJState(parts, false);
    }

    @Override
    public void updateState()
    {
        if (this.isServer())
        {
            this.sync();
            return;
        }

        this.world.markBlockRangeForRenderUpdate(this.getPos(), this.getPos());
    }

    public boolean isConnected(final EnumFacing facing)
    {
        return this.connectionsMap.containsKey(facing) || this.steamConnections.containsKey(facing);
    }

    public void connectSteam(final EnumFacing facing, final ISteamHandler handler)
    {
        this.steamConnections.put(facing, handler);
        this.updateState();
    }

    public void disconnectSteam(final EnumFacing facing)
    {
        this.steamConnections.remove(facing);
        this.updateState();
    }

    @Override
    public void connectTrigger(final EnumFacing facing, CableGrid grid)
    {
        this.connectSteam(facing, null);
    }

    @Override
    public void disconnectTrigger(final EnumFacing facing, CableGrid grid)
    {
        this.disconnectSteam(facing);
    }

    @Override
    public BlockPos getBlockPos()
    {
        return this.getPos();
    }

    @Override
    public World getBlockWorld()
    {
        return this.getWorld();
    }
}
