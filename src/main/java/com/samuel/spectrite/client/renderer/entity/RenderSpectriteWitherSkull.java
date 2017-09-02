package com.samuel.spectrite.client.renderer.entity;

import com.google.common.collect.Maps;
import com.samuel.spectrite.Spectrite;
import com.samuel.spectrite.entities.EntitySpectriteWitherSkull;
import com.samuel.spectrite.etc.SpectriteHelper;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

@SideOnly(Side.CLIENT)
public class RenderSpectriteWitherSkull extends Render<EntitySpectriteWitherSkull>
{
    private static final Map<String, ResourceLocation> INVULNERABLE_SPECTRITE_WITHER_TEXTURE_RES_MAP = Maps.newHashMap();
    private static final Map<String, ResourceLocation> SPECTRITE_WITHER_TEXTURE_RES_MAP = Maps.newHashMap();
    /** The Skeleton's head model. */
    private final ModelSkeletonHead skeletonHeadModel = new ModelSkeletonHead();

    public RenderSpectriteWitherSkull(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    private float getRenderYaw(float p_82400_1_, float p_82400_2_, float p_82400_3_)
    {
        float f;

        for (f = p_82400_2_ - p_82400_1_; f < -180.0F; f += 360.0F)
        {
            ;
        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return p_82400_1_ + p_82400_3_ * f;
    }

    /**
     * Renders the desired {@code T} type Entity.
     */
    public void doRender(EntitySpectriteWitherSkull entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        float f = this.getRenderYaw(entity.prevRotationYaw, entity.rotationYaw, partialTicks);
        float f1 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.enableAlpha();
        this.bindEntityTexture(entity);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        this.skeletonHeadModel.render(entity, 0.0F, 0.0F, 0.0F, f, f1, 0.0625F);

        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntitySpectriteWitherSkull entity)
    {
        boolean invulnerable = entity.isInvulnerable();

        int curFrame = SpectriteHelper.getCurrentSpectriteFrame(entity.getEntityWorld());
        String textureLoc = "%s:textures/entities/spectrite_wither/%s/%d.png";
        ResourceLocation resourceLocation;

        if (invulnerable) {
            textureLoc = String.format(textureLoc, Spectrite.MOD_ID, "invulnerable", curFrame);
        } else {
            textureLoc = String.format(textureLoc, Spectrite.MOD_ID, "normal", curFrame);
        }

        resourceLocation = new ResourceLocation(textureLoc);

        if (resourceLocation == null) {
            if (invulnerable) {
                INVULNERABLE_SPECTRITE_WITHER_TEXTURE_RES_MAP.put(textureLoc, resourceLocation);
            } else {
                SPECTRITE_WITHER_TEXTURE_RES_MAP.put(textureLoc, resourceLocation);
            }
        }

        return resourceLocation;
    }
}