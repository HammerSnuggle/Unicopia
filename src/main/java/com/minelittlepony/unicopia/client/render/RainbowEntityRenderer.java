package com.minelittlepony.unicopia.client.render;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.entity.RainbowEntity;
import com.minelittlepony.unicopia.util.WorldHelper;
import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;

public class RainbowEntityRenderer extends EntityRenderer<RainbowEntity> {

    public RainbowEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager);
    }

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/environment/rainbow.png");

    public void doRender(RainbowEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float distance = MinecraftClient.getInstance().getCameraEntity().distanceTo(entity);
        float maxDistance = 16 * MinecraftClient.getInstance().options.viewDistance;
        double r = entity.getRadius();
        float light = WorldHelper.getDaylightBrightness(entity.getEntityWorld(), partialTicks);

        float opacity = ((maxDistance - distance) / maxDistance);

        opacity *= light;

        if (opacity <= 0) {
            return;
        }

        bindEntityTexture(entity);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.translated(x, y, z);
        GlStateManager.rotatef(entityYaw, 0, 1, 0);

        GlStateManager.color4f(1, 1, 1, opacity);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBufferBuilder();

        bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV);
        bufferbuilder.vertex(-r, r, 0).texture(1, 0).end();
        bufferbuilder.vertex( r, r, 0).texture(0, 0).end();
        bufferbuilder.vertex( r, 0, 0).texture(0, 1).end();
        bufferbuilder.vertex(-r, 0, 0).texture(1, 1).end();

        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected Identifier getTexture(RainbowEntity entity) {
        return TEXTURE;
    }

}