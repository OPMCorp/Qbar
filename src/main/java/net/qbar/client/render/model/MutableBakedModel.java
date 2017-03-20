package net.qbar.client.render.model;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

public class MutableBakedModel implements IPerspectiveAwareModel
{
    private final List<List<BakedQuad>> quads;
    private final IBakedModel           parent;

    public MutableBakedModel(final IBakedModel parent)
    {
        this.parent = parent;
        this.quads = new ArrayList<>();

        for (final EnumFacing side : EnumFacing.VALUES)
            this.quads.add(new ArrayList<>());
        this.quads.add(new ArrayList<>());
    }

    public void addQuad(final EnumFacing side, final BakedQuad quad)
    {
        if (side != null)
            this.quads.get(side.ordinal()).add(quad);
        this.quads.get(6).add(quad);
    }

    @Override
    public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand)
    {
        if (side != null)
            return this.quads.get(side.ordinal());
        return this.quads.get(6);
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return this.parent != null ? this.parent.isAmbientOcclusion() : true;
    }

    @Override
    public boolean isGui3d()
    {
        return this.parent != null ? this.parent.isGui3d() : true;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return this.parent != null ? this.parent.getParticleTexture() : null;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides()
    {
        return ItemOverrideList.NONE;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
            final ItemCameraTransforms.TransformType cameraTransformType)
    {
        if (this.parent != null && this.parent instanceof IPerspectiveAwareModel)
        {
            final Pair<? extends IBakedModel, Matrix4f> pair = ((IPerspectiveAwareModel) this.parent)
                    .handlePerspective(cameraTransformType);
            if (pair.getLeft() != this.parent)
                return pair;
            return ImmutablePair.of(this, pair.getRight());
        }
        return ImmutablePair.of(this, null);
    }
}