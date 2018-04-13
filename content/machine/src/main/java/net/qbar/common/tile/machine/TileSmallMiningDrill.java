package net.qbar.common.tile.machine;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.qbar.common.QBarConstants;
import net.qbar.common.block.BlockVeinOre;
import net.qbar.common.container.BuiltContainer;
import net.qbar.common.container.ContainerBuilder;
import net.qbar.common.container.IContainerProvider;
import net.qbar.common.grid.node.IBelt;
import net.qbar.common.gui.MachineGui;
import net.qbar.common.init.QBarItems;
import net.qbar.common.machine.QBarMachines;
import net.qbar.common.machine.component.SteamComponent;
import net.qbar.common.machine.module.impl.BasicInventoryModule;
import net.qbar.common.machine.module.impl.FluidStorageModule;
import net.qbar.common.machine.module.impl.IOModule;
import net.qbar.common.machine.module.impl.SteamModule;
import net.qbar.common.ore.SludgeData;
import net.qbar.common.steam.ISteamTank;
import net.qbar.common.steam.SteamUtil;
import net.qbar.common.util.FluidUtils;

import java.util.Iterator;

public class TileSmallMiningDrill extends TileTickingModularMachine implements IContainerProvider
{
    @Getter
    @Setter
    private float heat, maxHeat;

    @Getter
    @Setter
    private boolean  completed;
    private BlockPos lastPos;

    private final float heatPerOperationTick = 30;
    private       int   tickBeforeHarvest;

    private final NonNullList<ItemStack> tempVarious;
    private       ItemStack              tempSludge;

    public TileSmallMiningDrill()
    {
        super(QBarMachines.SMALL_MINING_DRILL);

        this.heat = 0;
        this.maxHeat = 3000;
        this.lastPos = this.getPos();

        this.tempVarious = NonNullList.create();
        this.tempSludge = ItemStack.EMPTY;
    }

    @Override
    protected void reloadModules()
    {
        super.reloadModules();

        this.addModule(new BasicInventoryModule(this, 0));
        this.addModule(new SteamModule(this, SteamUtil::createTank));
        this.addModule(new FluidStorageModule(this)
                .addFilter("water", FluidUtils.WATER_FILTER));
        this.addModule(new IOModule(this));
    }

    @Override
    public void update()
    {
        super.update();

        if (this.isClient())
            return;

        boolean isDirty = false;

        ISteamTank steamTank = this.getModule(SteamModule.class).getInternalSteamHandler();
        SteamComponent steamComponent = this.getDescriptor().get(SteamComponent.class);

        if (!this.isCompleted() && this.tempVarious.isEmpty() && this.heat < this.maxHeat
                && steamTank.getSteam() >= steamComponent.getSteamConsumption())
        {
            BlockPos toCheck = this.lastPos;

            if (lastPos.equals(BlockPos.ORIGIN))
                toCheck = new BlockPos(this.getPos().getX() - 2, this.getPos().getY() - 1, this.getPos().getZ() - 2);
            else if (this.tickBeforeHarvest == 0)
            {
                if (toCheck.getX() == this.getPos().getX() + 2)
                {
                    if (toCheck.getZ() == this.getPos().getZ() + 2)
                    {
                        if (toCheck.getY() == 0)
                        {
                            this.setCompleted(true);
                        }
                        else
                            toCheck = new BlockPos(this.getPos().getX() - 2, toCheck.getY() - 1,
                                    this.getPos().getZ() - 2);
                    }
                    else
                        toCheck = new BlockPos(this.getPos().getX() - 2, toCheck.getY(), toCheck.getZ() + 1);
                }
                else
                    toCheck = new BlockPos(toCheck.getX() + 1, toCheck.getY(), toCheck.getZ());
                this.tickBeforeHarvest = (int) Math
                        .ceil(4 * (1 / (steamTank.getPressure() / steamComponent.getMaxPressureCapacity())));

                IBlockState state = this.world.getBlockState(toCheck);

                if (!this.world.isAirBlock(toCheck) && !(state.getBlock() instanceof IFluidBlock)
                        && state.getBlockHardness(world, toCheck) >= 0)
                {
                    if (state.getBlock() instanceof BlockVeinOre)
                    {
                        SludgeData sludge = ((BlockVeinOre) state.getBlock()).getOreFromState(state).toSludge();

                        ItemStack sludgeStack = new ItemStack(QBarItems.MINERAL_SLUDGE);
                        sludgeStack.setTagCompound(new NBTTagCompound());
                        sludgeStack.getTagCompound().setTag("sludgeData", sludge.writeToNBT(new NBTTagCompound()));
                        tempSludge = sludgeStack;
                        this.world.destroyBlock(toCheck, false);
                    }
                    else if (Math.abs(toCheck.getX() - this.getPos().getX()) < 2
                            && Math.abs(toCheck.getZ() - this.getPos().getZ()) < 2)
                    {
                        state.getBlock().getDrops(tempVarious, this.world, toCheck, state, 0);
                        this.world.destroyBlock(toCheck, false);
                    }
                    else
                        this.tickBeforeHarvest = 0;
                }
                else
                    this.tickBeforeHarvest = 0;
            }
            else
                this.tickBeforeHarvest--;

            lastPos = toCheck;

            this.heat += this.heatPerOperationTick * (steamTank.getPressure() / 2);

            steamTank.drainSteam((int) Math.max(steamComponent.getSteamConsumption() * steamTank.getPressure(),
                    steamComponent.getSteamConsumption()), true);
            isDirty = true;
        }
        if (!this.isCompleted())
        {
            IFluidTank fluidTank = (IFluidTank) this.getModule(FluidStorageModule.class).getFluidHandler("water");

            if (fluidTank.getFluidAmount() > 0)
            {
                int removable = Math.min(20, fluidTank.getFluidAmount());

                if (this.heat - removable <= this.getMinimumTemp())
                    removable = (int) (this.heat - this.getMinimumTemp());

                if (removable > 0)
                {
                    this.heat = this.heat - removable;
                    fluidTank.drain(removable, true);
                }
            }
        }
        if (!this.tempVarious.isEmpty())
        {
            if (this.tryInsertTrash(this.getFacing()))
                isDirty = true;
        }
        if (!this.tempSludge.isEmpty())
        {
            if (this.tryInsertSludge(this.getFacing()))
                isDirty = true;
        }

        if (this.world.getTotalWorldTime() % 5 == 0)
        {
            if (this.heat > this.getMinimumTemp())
            {
                this.heat--;
                isDirty = true;
            }
            else if (this.heat < this.getMinimumTemp())
            {
                this.heat = this.getMinimumTemp();
                isDirty = true;
            }
        }

        if (isDirty)
            this.sync();
    }

    private int getMinimumTemp()
    {
        return (int) (this.world.getBiome(this.getPos()).getTemperature(this.pos) * 200);
    }

    private boolean tryInsertSludge(final EnumFacing facing)
    {
        TileEntity slugdeTile = this.world.getTileEntity(this.pos.offset(facing, 2));
        if (slugdeTile instanceof IBelt)
        {
            final IBelt sludgeBelt = (IBelt) slugdeTile;

            ItemStack next = this.tempSludge;
            if (sludgeBelt.insert(next, false))
            {
                sludgeBelt.insert(next, true);
                this.tempSludge = ItemStack.EMPTY;
                return true;
            }
        }
        return false;
    }

    private boolean tryInsertTrash(final EnumFacing facing)
    {
        TileEntity trashTile = this.world.getTileEntity(this.pos.offset(facing.getOpposite(), 2));
        if (trashTile instanceof IBelt)
        {
            final IBelt trashBelt = (IBelt) trashTile;

            Iterator<ItemStack> variousIterator = this.tempVarious.iterator();

            while (variousIterator.hasNext())
            {
                ItemStack next = variousIterator.next();
                if (trashBelt.insert(next, false))
                {
                    trashBelt.insert(next, true);
                    variousIterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tag)
    {
        super.writeToNBT(tag);

        tag.setFloat("heat", this.heat);
        tag.setFloat("maxHeat", this.maxHeat);

        tag.setBoolean("completed", this.isCompleted());
        tag.setLong("lastPos", this.lastPos.toLong());
        tag.setInteger("tickBeforeHarvest", this.tickBeforeHarvest);

        tag.setTag("tempVarious", ItemStackHelper.saveAllItems(new NBTTagCompound(), this.tempVarious));
        tag.setTag("tempSludge", this.tempSludge.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(final NBTTagCompound tag)
    {
        super.readFromNBT(tag);

        this.heat = tag.getFloat("heat");
        this.maxHeat = tag.getFloat("maxHeat");

        this.completed = tag.getBoolean("completed");
        this.lastPos = BlockPos.fromLong(tag.getLong("lastPos"));
        this.tickBeforeHarvest = tag.getInteger("tickBeforeHarvest");

        ItemStackHelper.loadAllItems(tag.getCompoundTag("tempVarious"), this.tempVarious);
        this.tempSludge = new ItemStack(tag.getCompoundTag("tempSludge"));
    }

    @Override
    public BuiltContainer createContainer(EntityPlayer player)
    {
        SteamModule steamEngine = this.getModule(SteamModule.class);
        FluidStorageModule fluidStorage = this.getModule(FluidStorageModule.class);

        return new ContainerBuilder("smallminingdrill", player).player(player.inventory).inventory(8, 84).hotbar(8, 142)
                .addInventory().tile(this.getModule(BasicInventoryModule.class))
                .syncIntegerValue(steamEngine.getInternalSteamHandler()::getSteam,
                        steamEngine.getInternalSteamHandler()::setSteam)
                .syncFluidValue(((FluidTank) fluidStorage.getFluidHandler("water"))::getFluid,
                        ((FluidTank) fluidStorage.getFluidHandler("water"))::setFluid)
                .addInventory().create();
    }

    public boolean onRightClick(final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY,
                                final float hitZ, BlockPos from)
    {
        if (player.isSneaking())
            return false;
        if (player.getHeldItemMainhand().getItem() == QBarItems.WRENCH)
            return false;

        IFluidHandler water = this.getModule(FluidStorageModule.class).getFluidHandler("water");
        if (FluidUtils.drainPlayerHand(water, player)
                || FluidUtils.fillPlayerHand(water, player))
        {
            this.markDirty();
            return true;
        }

        player.openGui(QBarConstants.MODINSTANCE, MachineGui.SMALLMININGDRILL.getUniqueID(), this.world, this.pos
                        .getX(), this.pos.getY(),
                this.pos.getZ());
        return true;
    }
}
