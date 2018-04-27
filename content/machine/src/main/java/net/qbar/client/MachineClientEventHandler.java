package net.qbar.client;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.qbar.client.render.RenderIOOverlay;
import net.qbar.client.render.RenderStructureOverlay;
import net.qbar.client.render.RenderUtil;
import net.qbar.common.init.QBarBlocks;
import net.qbar.common.item.ItemBlueprint;
import net.qbar.common.machine.QBarMachines;
import net.qbar.common.multiblock.BlockMultiblockBase;
import net.qbar.common.multiblock.MultiblockComponent;
import net.qbar.common.multiblock.blueprint.Blueprint;
import net.qbar.common.util.ItemUtils;

import java.util.concurrent.ExecutionException;

public class MachineClientEventHandler
{
    private final BlockRendererDispatcher blockRender = Minecraft.getMinecraft().getBlockRendererDispatcher();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e)
    {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * e.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * e.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * e.getPartialTicks();

        try
        {
            RenderIOOverlay.renderIO(player, playerX, playerY, playerZ, e.getPartialTicks());
        } catch (ExecutionException e1)
        {
            e1.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onDrawblockHightlight(final DrawBlockHighlightEvent e)
    {
        if (e.getTarget().typeOfHit.equals(RayTraceResult.Type.BLOCK))
        {
            if (e.getPlayer().getHeldItemMainhand().getItem() instanceof ItemBlueprint)
            {
                if (e.getPlayer().getHeldItemMainhand().hasTagCompound()
                        && e.getPlayer().getHeldItemMainhand().getTagCompound().hasKey("blueprint"))
                {
                    final String name = e.getPlayer().getHeldItemMainhand().getTagCompound().getString("blueprint");
                    final Blueprint blueprint = QBarMachines.getComponent(Blueprint.class,
                            e.getPlayer().getHeldItemMainhand().getTagCompound().getString("blueprint"));
                    final BlockMultiblockBase base = (BlockMultiblockBase) Block.getBlockFromName("qbar:" + name);
                    final World w = Minecraft.getMinecraft().world;
                    if (base != null)
                    {
                        if (base.canPlaceBlockAt(w, e.getTarget().getBlockPos().offset(e.getTarget().sideHit),
                                e.getPlayer().getHorizontalFacing().getOpposite()))
                        {
                            GlStateManager.enableBlend();
                            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                                    GlStateManager.DestFactor.ZERO);
                            GlStateManager.glLineWidth(2.0F);
                            GlStateManager.disableTexture2D();
                            GlStateManager.depthMask(false);
                            final BlockPos pos = e.getTarget().getBlockPos().offset(e.getTarget().sideHit);

                            if (e.getPlayer().world.getWorldBorder().contains(pos))
                            {
                                final IBlockState placementState = base.getStateForPlacement(w, pos,
                                        e.getTarget().sideHit, 0, 0, 0, 0, e.getPlayer(),
                                        e.getPlayer().getActiveHand());

                                final double x = e.getPlayer().lastTickPosX
                                        + (e.getPlayer().posX - e.getPlayer().lastTickPosX) * e.getPartialTicks();
                                final double y = e.getPlayer().lastTickPosY
                                        + (e.getPlayer().posY - e.getPlayer().lastTickPosY) * e.getPartialTicks();
                                final double z = e.getPlayer().lastTickPosZ
                                        + (e.getPlayer().posZ - e.getPlayer().lastTickPosZ) * e.getPartialTicks();
                                RenderGlobal.drawSelectionBoundingBox(
                                        blueprint.getDescriptor().get(MultiblockComponent.class)
                                                .getBox(e.getPlayer().getHorizontalFacing().getOpposite()).offset(pos)
                                                .grow(0.0020000000949949026D).offset(-x, -y, -z),
                                        0.0F, 0.0F, 0.0F, 0.4F);

                                GlStateManager.depthMask(true);
                                GlStateManager.enableTexture2D();
                                GlStateManager.disableBlend();

                                RenderUtil.renderMultiblock(placementState, pos.getX() - x, pos.getY() - y,
                                        pos.getZ() - z, this.blockRender);

                                GlStateManager.pushMatrix();
                                GlStateManager.translate(pos.getX() - x + 0.5, pos.getY() - y, pos.getZ() - z + .5);
                                GlStateManager.rotate(180, 1, 0, 0);
                                GlStateManager.rotate(e.getPlayer().getHorizontalFacing().getHorizontalAngle() - 180, 0,
                                        1, 0);
                                GlStateManager.translate(0.15, -1.4, -.69);
                                GlStateManager.scale(0.625f / 32, 0.625f / 32, 0.625f / 32);
                                GlStateManager.disableLighting();

                                Minecraft.getMinecraft().fontRenderer
                                        .drawString(String.valueOf(blueprint.getRodAmount()),
                                                -Minecraft.getMinecraft().fontRenderer
                                                        .getStringWidth(String.valueOf(blueprint.getRodAmount())) / 2,
                                                0,
                                                e.getPlayer().capabilities.isCreativeMode || ItemUtils.hasPlayerEnough(
                                                        e.getPlayer().inventory, blueprint.getRodStack(), false) ? 38400
                                                        : 9830400);

                                GlStateManager.enableLighting();
                                GlStateManager.popMatrix();

                                GlStateManager.pushMatrix();
                                GlStateManager.translate(pos.getX() - x + 0.5, pos.getY() - y, pos.getZ() - z + .5);

                                GlStateManager.rotate(90, 1, 0, 0);
                                GlStateManager.rotate(-90, 0, 1, 0);
                                GlStateManager.rotate(e.getPlayer().getHorizontalFacing().getHorizontalAngle(), 1, 0,
                                        0);
                                GlStateManager.translate(-1.5, -0.5, 0);

                                RenderUtil.handleRenderItem(blueprint.getRodStack(), false);
                                GlStateManager.popMatrix();
                                e.setCanceled(true);
                            }
                        }
                    }
                }
            }
            else if (e.getPlayer().world.getBlockState(e.getTarget().getBlockPos()).getBlock() == QBarBlocks.STRUCTURE)
                RenderStructureOverlay.renderStructureOverlay(e.getPlayer(), e.getTarget().getBlockPos(),
                        e.getPartialTicks());
        }
    }
}
