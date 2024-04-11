package com.qurenie.relics_thirteenflames;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.ParticlesRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.hammerlib.api.items.CreativeTab;
import org.zeith.hammerlib.proxy.HLConstants;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ThirteenFlames.MODID)
public class ThirteenFlames
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "relics_thirteenflames";

    public static ResourceLocation rl(String str){
        return new ResourceLocation(MODID, str);
    }
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogManager.getLogger("ThirteenFlames");

    @CreativeTab.RegisterTab
    public static final CreativeTab ITEM_TAB = new CreativeTab(new ResourceLocation(ThirteenFlames.MODID,"thirteenflames"),
            b -> b.icon(ItemsRegistry.KNEF_BOW::getDefaultInstance)
                    .title(Component.translatable("itemGroup.relics_thirteenflames"))
    ).putAfter(HLConstants.HL_TAB);

    public ThirteenFlames()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        SoundsRegistry.registerSounds();
        ParticlesRegistry.PARTICLES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }



}
