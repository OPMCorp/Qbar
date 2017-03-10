package net.qbar.common.network;

import com.elytradev.concrete.Message;
import com.elytradev.concrete.NetworkContext;
import com.elytradev.concrete.annotation.field.MarshalledAs;
import com.elytradev.concrete.annotation.type.ReceivedOn;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.qbar.QBar;
import net.qbar.common.tile.QBarTileBase;

@ReceivedOn(Side.SERVER)
public class TileSyncRequestPacket extends Message
{
    @MarshalledAs("i32")
    private int dimensionId;
    @MarshalledAs("i32")
    private int x;
    @MarshalledAs("i32")
    private int y;
    @MarshalledAs("i32")
    private int z;

    public TileSyncRequestPacket(final NetworkContext ctx)
    {
        super(ctx);
    }

    public TileSyncRequestPacket(final int dimensionID, final int x, final int y, final int z)
    {
        this(QBar.network);

        this.dimensionId = dimensionID;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    protected void handle(final EntityPlayer sender)
    {
        final BlockPos pos = new BlockPos(this.x, this.y, this.z);
        if (sender.getEntityWorld().provider.getDimension() == this.dimensionId
                && sender.getEntityWorld().getTileEntity(pos) != null
                && sender.getEntityWorld().getTileEntity(pos) instanceof QBarTileBase)
            NetworkHandler.sendTileToPlayer((QBarTileBase) sender.getEntityWorld().getTileEntity(pos),
                    (EntityPlayerMP) sender);
    }
}
