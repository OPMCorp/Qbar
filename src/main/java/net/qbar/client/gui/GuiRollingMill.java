package net.qbar.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.qbar.QBar;
import net.qbar.client.gui.util.GuiMachineBase;
import net.qbar.common.tile.machine.TileRollingMill;

public class GuiRollingMill extends GuiMachineBase<TileRollingMill>
{
    public static final ResourceLocation BACKGROUND = new ResourceLocation(QBar.MODID, "textures/gui/rollingmill.png");

    public GuiRollingMill(final EntityPlayer player, final TileRollingMill rollingmill)
    {
        super(player, rollingmill, BACKGROUND);

        this.addSteamTank(rollingmill.getSteamTank(), 151, 7, 18, 73);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);

        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;

        final int j = this.getMachine().getProgressScaled(24);
        if (j > 0)
            this.drawTexturedModalRect(x + 79, y + 34, 176, 14, j + 1, 16);
    }
}
