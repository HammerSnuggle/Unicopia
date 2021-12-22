package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.item.UItems;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class DiscoveryToast implements Toast {
    private static final long MAX_AGE = 5000L;
    private static final Text TITLE = new TranslatableText("unicopia.toast.discoveries.title");
    private static final Text DESCRIPTION = new TranslatableText("unicopia.toast.discoveries.description");

    private final List<Identifier> discoveries = new ArrayList<>();
    private long startTime;

    private boolean justUpdated;

    @Override
    public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        if (justUpdated) {
            this.startTime = startTime;
            justUpdated = false;
        }

        if (discoveries.isEmpty()) {
            return Toast.Visibility.HIDE;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1, 1, 1);
        manager.drawTexture(matrices, 0, 0, 0, 32, getWidth(), getHeight());
        manager.getClient().textRenderer.draw(matrices, TITLE, 30, 7, -11534256);
        manager.getClient().textRenderer.draw(matrices, DESCRIPTION, 30, 18, -16777216);

        Identifier icon = discoveries.get((int)(startTime / Math.max(1L, MAX_AGE / discoveries.size()) % discoveries.size()));

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.scale(0.6F, 0.6F, 1);
        RenderSystem.applyModelViewMatrix();
        manager.getClient().getItemRenderer().renderInGui(UItems.SPELLBOOK.getDefaultStack(), 3, 3);
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShaderTexture(0, icon);
        DrawableHelper.drawTexture(matrices, 8, 8, 1, 0, 0, 16, 16, 16, 16);

        // manager.getClient().getItemRenderer().renderInGui(recipe.getOutput(), 8, 8);

        return startTime - this.startTime >= MAX_AGE ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public DiscoveryToast addDiscoveries(Identifier icon) {
        if (!discoveries.contains(icon)) {
            discoveries.add(icon);
        }
        justUpdated = true;

        return this;
    }

    class Discovery {
        final Identifier icon;
        final Text description;

        Discovery(Identifier icon, Text description) {
            this.icon = icon;
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Discovery
                    && Objects.equal(icon, ((Discovery) o).icon)
                    && Objects.equal(description, ((Discovery) o).description);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(icon, description);
        }
    }

    public static void show(ToastManager manager, Identifier icon) {
        DiscoveryToast existing = manager.getToast(DiscoveryToast.class, TYPE);
        if (existing == null) {
            manager.add(new DiscoveryToast().addDiscoveries(icon));
        } else {
            existing.addDiscoveries(icon);
        }
    }
}
