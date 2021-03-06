package net.ros.common.grid.impl;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.ros.common.grid.node.IPipeValve;
import net.ros.common.grid.node.ISteamPipe;
import net.ros.common.grid.node.ITileCable;
import net.ros.common.grid.node.ITileNode;
import net.ros.common.network.SteamEffectPacket;
import net.ros.common.steam.ISteamHandler;
import net.ros.common.steam.SteamTank;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SteamGrid extends CableGrid
{
    private int   transferCapacity;
    private float maxPressure;

    private final ListMultimap<ISteamHandler, ISteamPipe> handlersConnections;

    public SteamGrid(final int identifier, final int transferCapacity, float maxPressure)
    {
        super(identifier);
        this.transferCapacity = transferCapacity;
        this.maxPressure = maxPressure;

        this.handlersConnections = MultimapBuilder.hashKeys().arrayListValues().build();
    }

    @Override
    public CableGrid copy(final int identifier)
    {
        return new SteamGrid(identifier, this.transferCapacity, this.maxPressure);
    }

    @Override
    public boolean canMerge(final CableGrid grid)
    {
        if (grid instanceof SteamGrid && ((SteamGrid) grid).getTransferCapacity() == this.transferCapacity)
            return super.canMerge(grid);
        return false;
    }

    @Override
    public void onMerge(final CableGrid grid)
    {
        this.handlersConnections.putAll(((SteamGrid) grid).handlersConnections);
    }

    @Override
    public void onSplit(final CableGrid grid)
    {
        ((SteamGrid) grid).handlersConnections.forEach((handler, pipe) ->
        {
            if (this.hasCable(pipe))
                this.handlersConnections.put(handler, pipe);
        });
    }

    @Override
    public void tick()
    {
        super.tick();

        for (ITileNode<?> node : this.getCables())
        {
            ISteamPipe pipe = (ISteamPipe) node;

            if ((pipe instanceof IPipeValve && !((IPipeValve) pipe).isOpen()) ||
                    (pipe.getBufferTank().getSteam() == 0 && pipe.getConnectedHandlers().isEmpty()))
                continue;

            SteamTank tank = pipe.getBufferTank();
            int transferred = 0;
            for (ISteamHandler handler : pipe.getConnectedHandlers())
            {
                transferred += balanceTanks(tank, handler, pipe.getTransferRate() - transferred, false);

                if (transferred >= pipe.getTransferRate())
                    break;
            }

            if (transferred >= pipe.getTransferRate())
                continue;

            for (Map.Entry<EnumFacing, ITileCable<SteamGrid>> entry : pipe.getConnectionsMap().entrySet())
            {
                transferred += balanceTanks(tank, ((ISteamPipe) entry.getValue()).getBufferTank(),
                        pipe.getTransferRate() - transferred, true);

                if (transferred >= pipe.getTransferRate())
                    break;
            }
        }

        if (this.handlersConnections.isEmpty())
            return;

        List<ISteamHandler> overPressure = this.handlersConnections.keySet().stream()
                .filter(handler -> handler.getPressure() > this.maxPressure * 0.9f).collect(Collectors.toList());

        if (overPressure.isEmpty())
            return;

        overPressure.forEach(handler ->
        {
            float pressureDiff = handler.getPressure() - this.maxPressure;
            steamJet(handler, (int) Math.ceil(pressureDiff / 0.1f),
                    (int) Math.ceil(pressureDiff / 0.1f), false);

            if (handler.getPressure() > this.maxPressure * 1.25f)
            {
                steamJet(handler, (int) Math.ceil(pressureDiff / 0.3f),
                        (int) Math.ceil(pressureDiff / 0.3f), true);

            }
            if (handler.getPressure() > this.maxPressure * 1.5f)
                blewPipes(handler, (int) Math.ceil((handler.getPressure() - this.maxPressure * 1.5f) / 0.05f));
        });
    }

    private int balanceTanks(ISteamHandler first, ISteamHandler second, int transferRate, boolean onlyFill)
    {
        float average = (second.getPressure() + first.getPressure()) / 2;

        if (second.getPressure() < average && first.getSteamDifference(average) > 1)
        {
            int drained = first.drainSteam(Math.min(transferRate, first.getSteamDifference(average)), false);
            int filled = second.fillSteam(drained, false);

            if (filled < 1)
                return 0;

            return second.fillSteam(first.drainSteam(filled, true), true);
        }
        else if (!onlyFill && second.getPressure() > average && second.getSteamDifference(average) > 1)
        {
            int drained = second.drainSteam(Math.min(transferRate, second.getSteamDifference(average)), false);
            int filled = first.fillSteam(drained, false);

            if (filled < 1)
                return 0;

            return first.fillSteam(second.drainSteam(filled, true), true);
        }
        return 0;
    }

    private void steamJet(ISteamHandler handler, int range, int weight, boolean damage)
    {
        this.getCablesConnected(handler, range).forEach(cable ->
        {
            Random rand = cable.getBlockWorld().rand;
            if (rand.nextInt(200) < weight)
            {
                handler.drainSteam(damage ? 60 : 20, true);
                BlockPos target = cable.getBlockPos()
                        .add(rand.nextInt(4) - 2, rand.nextInt(2) - 1, rand.nextInt(4) - 2);

                new SteamEffectPacket(cable.getBlockWorld(), cable.getBlockPos(), target, !damage)
                        .sendToAllAround(cable.getBlockWorld(), cable.getBlockPos(), 24);

                if (damage)
                {
                    cable.getBlockWorld().getEntitiesWithinAABB(EntityLivingBase.class,
                            new AxisAlignedBB(cable.getBlockPos(), target)).forEach(entity ->
                    {
                        entity.attackEntityFrom(DamageSource.IN_FIRE, 4);
                        entity.setFire(3);
                    });
                }
            }
            if (damage && rand.nextInt(300) < weight)
            {
                for (EnumFacing facing : EnumFacing.VALUES)
                {
                    BlockPos adjacent = cable.getBlockPos().offset(facing);

                    if (!cable.getBlockWorld().isAirBlock(adjacent))
                        continue;
                    cable.getBlockWorld().setBlockState(adjacent, Blocks.FIRE.getDefaultState(), 11);
                    break;
                }
            }
        });
    }

    private void blewPipes(ISteamHandler handler, int range)
    {
        this.getCablesConnected(handler, range).forEach(cable ->
                cable.getBlockWorld().createExplosion(null, cable.getBlockPos().getX(), cable.getBlockPos().getY(),
                        cable.getBlockPos().getZ(), 0.5f, true));
    }

    private Collection<ITileCable> getCablesConnected(ISteamHandler handler, int maxDepth)
    {
        final Set<ITileCable> openset = new HashSet<>();

        final Set<ITileCable> frontier = new HashSet<>(this.handlersConnections.get(handler));

        int currentDepth = 0;
        while (currentDepth < maxDepth && !frontier.isEmpty())
        {
            final Set<ITileCable> frontierCpy = new HashSet<>(frontier);
            for (ITileCable current : frontierCpy)
            {
                openset.add(current);
                for (int edge : current.getConnections())
                {
                    ITileCable facingCable = current.getConnected(edge);
                    if (!openset.contains(facingCable) && !frontier.contains(facingCable))
                        frontier.add(facingCable);
                }
                frontier.remove(current);
            }
            currentDepth++;
        }

        return openset;
    }

    public int getCapacity()
    {
        return this.getCables().size() * this.getTransferCapacity();
    }

    public void addConnectedPipe(final ISteamPipe pipe, final ISteamHandler handler)
    {
        if (!this.handlersConnections.containsEntry(handler, pipe))
            this.handlersConnections.put(handler, pipe);
    }

    private void clearConnectedPipe(final ISteamPipe pipe)
    {
        pipe.getConnectedHandlers().forEach(handler -> this.removeConnectedPipe(pipe, handler));
    }

    public void removeConnectedPipe(final ISteamPipe pipe, final ISteamHandler handler)
    {
        this.handlersConnections.remove(handler, pipe);
    }

    @Override
    public boolean removeCable(final ITileNode cable)
    {
        if (super.removeCable(cable))
        {
            this.clearConnectedPipe((ISteamPipe) cable);
            return true;
        }
        return false;
    }
}
