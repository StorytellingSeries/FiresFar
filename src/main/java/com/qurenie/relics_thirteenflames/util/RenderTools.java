package com.qurenie.relics_thirteenflames.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderTools {
    public static int DEFAULT_TEXT_COLOR = 0x5e3e0c;
    public static int DEFAULT_SHADOW_COLOR = 0xedc17e;

    public static void blitWithBlend(PoseStack matrices, float x, float y, float texPosX, float texPosY, float width, float height, float texWidth, float texHeight, float zOffset, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        float u1 = texPosX / texWidth;
        float u2 = (texPosX + width) / texWidth;
        float v1 = texPosY / texHeight;
        float v2 = (texPosY + height) / texHeight;
        Matrix4f m = matrices.last().pose();
        var builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        builder.addVertex(m, x, y, zOffset).setColor(1, 1, 1, alpha).setUv(u1, v1);
        builder.addVertex(m, x, y + height, zOffset).setColor(1, 1, 1, alpha).setUv(u1, v2);
        builder.addVertex(m, x + width, y + height, zOffset).setColor(1, 1, 1, alpha).setUv(u2, v2);
        builder.addVertex(m, x + width, y, zOffset).setColor(1, 1, 1, alpha).setUv(u2, v1);


        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void blitWithBlend(PoseStack matrices, float x, float y, float texPosX, float texPosY, float width, float height, float texWidth, float texHeight, float zOffset, float r, float g, float b, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        float u1 = texPosX / (float) texWidth;
        float u2 = (texPosX + width) / (float) texWidth;
        float v1 = texPosY / (float) texHeight;
        float v2 = (texPosY + height) / (float) texHeight;
        Matrix4f m = matrices.last().pose();
        var buider = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buider.addVertex(m, x, y, zOffset).setColor(r, g, b, alpha).setUv(u1, v1);
        buider.addVertex(m, x, y + height, zOffset).setColor(r, g, b, alpha).setUv(u1, v2);
        buider.addVertex(m, x + width, y + height, zOffset).setColor(r, g, b, alpha).setUv(u2, v2);
        buider.addVertex(m, x + width, y, zOffset).setColor(r, g, b, alpha).setUv(u2, v1);


        BufferUploader.drawWithShader(buider.buildOrThrow());
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
