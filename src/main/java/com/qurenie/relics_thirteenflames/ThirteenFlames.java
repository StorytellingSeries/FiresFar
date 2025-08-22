package com.qurenie.relics_thirteenflames;

import com.qurenie.relics_thirteenflames.content.recipes.MontuRecipeProvider;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.util.Scheduler;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.hammerlib.api.items.CreativeTab;
import org.zeith.hammerlib.proxy.HLConstants;

import java.util.concurrent.CompletableFuture;

@Mod(ThirteenFlames.MODID)
@EventBusSubscriber(modid = ThirteenFlames.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ThirteenFlames {
    
    // Define mod id in a common place for everything to reference
    public static final String MODID = "relics_thirteenflames";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogManager.getLogger("ThirteenFlames");
    public static final Scheduler SCHEDULER = new Scheduler(ServerTickEvent.Post.class, ServerTickEvent::hasTime);
    @CreativeTab.RegisterTab
    public static final CreativeTab ITEM_TAB = new CreativeTab(ResourceLocation.fromNamespaceAndPath(ThirteenFlames.MODID, "thirteenflames"),
            b -> b.icon(ItemsRegistry.KNEF_BOW::getDefaultInstance)
                    .title(Component.translatable("itemGroup.relics_thirteenflames"))
    ).putAfter(HLConstants.HL_TAB);
    
    public ThirteenFlames(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        
        SoundsRegistry.SOUNDS.register(modEventBus);
        ParticlesRegistry.PARTICLES.register(modEventBus);
        MenuRegistry.MENU_TYPES.register(modEventBus);
        EffectsRegistry.EFFECTS.register(modEventBus);
        AttachmentsRegistry.ATTACHMENT_TYPES.register(modEventBus);
        EntityDataSerializers.DATA_SERIALIZERS.register(modEventBus);
        RecipeTypesRegistry.register(modEventBus);
        RecipeSerializersRegistry.register(modEventBus);
        ArmorMaterialRegistry.register(modEventBus);
    }
    
    public static ResourceLocation rl(String str) {
        return ResourceLocation.fromNamespaceAndPath(MODID, str);
    }
    
    @SubscribeEvent
    public static void dataGathering(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> lookupProvider = CompletableFuture
                .supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        
        event.getGenerator().addProvider(true, new MontuRecipeProvider(generator.getPackOutput(), lookupProvider));
//        event.getGenerator().addProvider(event.includeServer(), new LootTableProvider(generator.getPackOutput(), event.getLookupProvider()));
    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
    
    }
    
}
