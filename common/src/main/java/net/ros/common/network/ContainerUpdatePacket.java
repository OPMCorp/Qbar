package net.ros.common.network;

import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.ros.common.ROSConstants;
import net.ros.common.container.BuiltContainer;

@ReceivedOn(Side.CLIENT)
public class ContainerUpdatePacket extends Message
{
    @MarshalledAs("i32")
    private int            windowId;
    @MarshalledAs("i32")
    private int            property;
    @MarshalledAs("nbt")
    private NBTTagCompound value;

    public ContainerUpdatePacket(final NetworkContext ctx)
    {
        super(ctx);
    }

    public ContainerUpdatePacket(final int windowID, final int property, final NBTTagCompound value)
    {
        this(ROSConstants.network);

        this.windowId = windowID;
        this.property = property;
        this.value = value;
    }

    @Override
    protected void handle(final EntityPlayer sender)
    {
        if (sender.openContainer != null && sender.openContainer.windowId == this.windowId
                && sender.openContainer instanceof BuiltContainer)
            ((BuiltContainer) sender.openContainer).updateProperty(this.property, this.value);
    }
}
