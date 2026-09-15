package com.qurenie.relics_thirteenflames.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import it.hurts.octostudios.octolib.util.OctoColor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

@Getter
@Setter
public class CardGuiEntity {

    final String id;
    final InventoryType type;
    final ResourceLocation loc;
    CardPosition position;
    CardPosition prevPos;

    CardBehaviour behaviour = CardBehaviour.IDLE;

    private CardTarget target;
    private CardTarget prevTarget;

    private float size = 0f;
    private int shake = 0;

    private OctoColor tempColor = new OctoColor(0xFFFFFFFF);
    private OctoColor color = new OctoColor(0xFFFFFFFF);

    float cooldown = 0f;

    boolean isAlive() {
        return behaviour.isAlive(this);
    }

    boolean unconnected() {
        return behaviour.unconnected(this);
    }

    private static final float SPEED = 20f;
    private static final float COLOR_CHANGE = 0.1f;
    private static final float RESIZE_SPEED = 0.2f;

    public CardGuiEntity(String id, InventoryType type, ResourceLocation loc) {
        this.loc = loc;
        this.id = id;
        this.type = type;

        this.target = this.prevTarget = new CardTarget(new CardPosition(0, 0), 0.0f);
        this.position = this.prevPos = new CardPosition(0, 0);
    }

    private static float shakeX(float t) {
        return (float) (Math.sin(t * 17.0) * Math.log(1 + t));
    }

    private static float shakeY(float t) {
        return (float) (Math.cos(t * 23.0) * Math.log(1 + t));
    }

    public void setTarget(CardTarget target) {
        prevTarget = this.target;
        this.target = target;
    }

    public void tick() {
        move();

        float sizeScale = behaviour.getModifiers().stream()
                .<Float>reduce(1f, (f, mod) -> f * mod.getSizeModifier(this),
                        (f1, f2) -> f1 * f2);

        float delta = sizeScale - size;
        size += Mth.clamp(delta, -RESIZE_SPEED, RESIZE_SPEED);

        shake = Math.max(0, shake - 1);
        tempColor = approachWhite(tempColor, COLOR_CHANGE);

        if (cooldown > 0)
            color = new OctoColor(0.5f, 0.5f, 0.5f, 1.0f);
        else
            color = new OctoColor(1, 1, 1, 1);
    }

    public OctoColor approachWhite(OctoColor c, float intensity) {
        intensity = Math.max(0f, Math.min(1f, intensity));

        float r = c.r() + (color.r() - c.r()) * intensity;
        float g = c.g() + (color.g() - c.g()) * intensity;
        float b = c.b() + (color.b() - c.b()) * intensity;

        return new OctoColor(r, g, b, c.a());
    }

    public void tickBehaviour(List<CardGuiEntity> other, int index) {
        behaviour.tickTargets(this, other, index);
    }

    public void move() {
        var dvec = target.position().substruct(position);

        float length = dvec.length();
        var move = length < SPEED ? dvec : dvec.scale(SPEED / length);

        prevPos = position;
        position = position.add(move);
    }

    public boolean mouseSelectedAbsolute(double mouseX, double mouseY, boolean scaleMouse) {
        Minecraft mc = Minecraft.getInstance();
        var window = mc.getWindow();

        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        if (scaleMouse) {
            mouseX = mouseX * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth();
            mouseY = mouseY * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight();
        }

        double mx = mouseX - cx;
        double my = mouseY - cy;

        return position.x() - 10 < mx && mx < position.x() + 10 && position.y() - 15 < my && my < position.y() + 14;
    }

    public void preRender(GuiGraphics gui, int cx, int cy, float partialTicks) {
        float x = cx + prevPos.x() + (position.x() - prevPos.x()) * partialTicks;
        float y = cy + prevPos.y() + (position.y() - prevPos.y()) * partialTicks;

        Minecraft mc = Minecraft.getInstance();

        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();

        var pose = gui.pose();
        pose.pushPose();

        pose.translate(x, y, 0);
        pose.scale(size, size, 1);
        RenderSystem.enableBlend();
        boolean selected = mouseSelectedAbsolute(rawX, rawY, true) && cooldown <= 0;

        if (selected)
            pose.scale(1.2f, 1.2f, 1);
        //frame_ambient

        pose.pushPose();
        pose.scale(0.9f, 0.9f, 0.9f);
        gui.blit(ThirteenFlames.rl("textures/gui/ability/frame_ambient.png"), -16, -21, 32, 41, 32, 41, 32, 41);
        pose.popPose();

        gui.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        pose.popPose();
    }

    public void render(GuiGraphics gui, int cx, int cy, float partialTicks) {
        float x = cx + prevPos.x() + (position.x() - prevPos.x()) * partialTicks;
        float y = cy + prevPos.y() + (position.y() - prevPos.y()) * partialTicks;

        Minecraft mc = Minecraft.getInstance();

        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();

        var pose = gui.pose();
        pose.pushPose();

        pose.translate(x, y, 0);
        pose.translate(shakeX(Math.max(shake - partialTicks, 0)), shakeY(Math.max(shake - partialTicks, 0)) * 0.75f, 0);
        pose.scale(size, size, 1);
        boolean selected = mouseSelectedAbsolute(rawX, rawY, true) && cooldown <= 0;

        if (selected)
            pose.scale(1.2f, 1.2f, 1);

        gui.setColor(tempColor.r(), tempColor.g(), tempColor.b(), tempColor.a());
        gui.blit(loc, -10, -15, 20, 29, 20, 29, 20, 29);

        if (selected) {
            float time = (mc.player.tickCount) / 5f;
            pose.pushPose();

            double v = (1 / 1.1f) + (Mth.sin(time) + 1) / 2 * 0.1;
            pose.scale((float) v, (float) v, 1);
            gui.blit(ThirteenFlames.rl("textures/gui/ability/frame_selection.png"),
                    -12, -17, 24, 33, 24, 33, 24, 33);

            pose.popPose();
        }

        gui.setColor(1, 1, 1, 1);
        if (cooldown > 0) {
            pose.pushPose();
            pose.scale(0.5f, 0.5f, 0.5f);
            gui.drawCenteredString(Minecraft.getInstance().font, Component.literal(String.format("%.1f", cooldown / 20f)), 0, -1, 0xFFFFFFFF);
            pose.popPose();
        }

        behaviour.postRender(this, gui, partialTicks);

        pose.popPose();
    }
}
