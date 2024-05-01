package com.qurenie.relics_thirteenflames.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RenderTools {
    public static int DEFAULT_TEXT_COLOR = 0x5e3e0c;
    public static int DEFAULT_SHADOW_COLOR = 0xedc17e;

    public static void blitWithBlend(PoseStack matrices, float x, float y, float texPosX, float texPosY, float width, float height, float texWidth, float texHeight, float zOffset, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder vertex = Tesselator.getInstance().getBuilder();
        float u1 = texPosX / (float) texWidth;
        float u2 = (texPosX + width) / (float) texWidth;
        float v1 = texPosY / (float) texHeight;
        float v2 = (texPosY + height) / (float) texHeight;
        Matrix4f m = matrices.last().pose();
        vertex.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        vertex.vertex(m, x, y, zOffset).color(1, 1, 1, alpha).uv(u1, v1).endVertex();
        vertex.vertex(m, x, y + height, zOffset).color(1, 1, 1, alpha).uv(u1, v2).endVertex();
        vertex.vertex(m, x + width, y + height, zOffset).color(1, 1, 1, alpha).uv(u2, v2).endVertex();
        vertex.vertex(m, x + width, y, zOffset).color(1, 1, 1, alpha).uv(u2, v1).endVertex();


        BufferUploader.drawWithShader(vertex.end());
        RenderSystem.disableBlend();
    }

    public static void blitWithBlend(PoseStack matrices, float x, float y, float texPosX, float texPosY, float width, float height, float texWidth, float texHeight, float zOffset, float r, float g, float b, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder vertex = Tesselator.getInstance().getBuilder();
        float u1 = texPosX / (float) texWidth;
        float u2 = (texPosX + width) / (float) texWidth;
        float v1 = texPosY / (float) texHeight;
        float v2 = (texPosY + height) / (float) texHeight;
        Matrix4f m = matrices.last().pose();
        vertex.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        vertex.vertex(m, x, y, zOffset).color(r, g, b, alpha).uv(u1, v1).endVertex();
        vertex.vertex(m, x, y + height, zOffset).color(r, g, b, alpha).uv(u1, v2).endVertex();
        vertex.vertex(m, x + width, y + height, zOffset).color(r, g, b, alpha).uv(u2, v2).endVertex();
        vertex.vertex(m, x + width, y, zOffset).color(r, g, b, alpha).uv(u2, v1).endVertex();


        BufferUploader.drawWithShader(vertex.end());
        RenderSystem.disableBlend();
    }

    public static void bindText(ResourceLocation loc) {
        RenderSystem.setShaderTexture(0, loc);
    }

    public static boolean isMouseInBorders(int mx, int my, int x1, int y1, int x2, int y2) {
        return (mx >= x1 && mx <= x2) && (my >= y1 && my <= y2);
    }

    public static void renderScaledText(GuiGraphics guiGraphics, Component text, int x, int y, float scale){
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale,scale,0);
        int rx = (int) (x / scale);
        int ry = (int) (y / scale);
        guiGraphics.drawString(Minecraft.getInstance().font,text,rx,ry + 1, DEFAULT_SHADOW_COLOR);
        guiGraphics.drawString(Minecraft.getInstance().font,text,rx,ry, DEFAULT_TEXT_COLOR);

        guiGraphics.pose().popPose();
    }



    public static void renderCenteredScaledText(GuiGraphics guiGraphics,String text,int x,int y,float scale){
        renderCenteredScaledText(guiGraphics, text, x, y, scale, DEFAULT_SHADOW_COLOR, DEFAULT_TEXT_COLOR);
    }

    public static void renderCenteredScaledText(GuiGraphics guiGraphics,String text,int x,int y,float scale,int shadowColor,int textColor){
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale,scale,0);
        int width = (int)(Minecraft.getInstance().font.width(text) * scale);
        int rx = (int) ((x - width/2) / scale);
        int ry = (int) (y / scale);

        guiGraphics.drawString(Minecraft.getInstance().font,text,rx,ry + 1, shadowColor, false);
        guiGraphics.drawString(Minecraft.getInstance().font,text,rx,ry, textColor, false);
        guiGraphics.pose().popPose();
    }


}
