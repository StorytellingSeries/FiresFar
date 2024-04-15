package com.qurenie.relics_thirteenflames.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import com.qurenie.relics_thirteenflames.client.particles.CircleTintFactory;
import com.qurenie.relics_thirteenflames.client.render.entity.EntityRendererSeliasetSun;
import com.qurenie.relics_thirteenflames.client.render.entity.FallingRenderer;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import com.qurenie.relics_thirteenflames.content.entities.LivingFleshEntity;
import com.qurenie.relics_thirteenflames.content.items.ItemRonasSword;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.init.EntityRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import it.hurts.sskirillss.relics.client.renderer.entities.NullRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = ThirteenFlames.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {


    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SELIASET_SUN, EntitySeliasetSun.createLivingAttributes().build());
        event.put(EntityRegistry.LIVING_FLESH, LivingFleshEntity.createMobAttributes().build());
    }

}