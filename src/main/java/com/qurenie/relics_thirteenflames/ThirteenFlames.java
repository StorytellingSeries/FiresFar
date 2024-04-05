package com.qurenie.relics_thirteenflames;

import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.SoundsRegistry;
import net.minecraft.network.chat.Component;
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

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ThirteenFlames.MODID)
public class ThirteenFlames
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "relics_thirteenflames";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogManager.getLogger("ThirteenFlames");

    public static final CreativeModeTab ITEM_TAB = CreativeModeTab.builder()
            .icon(ItemsRegistry.KNEF_BOW::getDefaultInstance)
            .title(Component.translatable("itemGroup.relics_thirteenflames"))
            .build()
    ;
    public ThirteenFlames()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        SoundsRegistry.registerSounds();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }


    @SubscribeEvent
    public void onCreativeTabBuild(BuildCreativeModeTabContentsEvent event)
    {
        event.accept(ItemsRegistry.KNEF_BOW);
        event.accept(ItemsRegistry.RONAS_SWORD);
        event.accept(ItemsRegistry.MONTU_HAMMER);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
    }
}
