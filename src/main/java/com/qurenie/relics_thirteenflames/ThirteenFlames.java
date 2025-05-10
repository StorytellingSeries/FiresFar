package com.qurenie.relics_thirteenflames;

import com.qurenie.relics_thirteenflames.content.blocks.BlockShaking;
import com.qurenie.relics_thirteenflames.content.items.scroll_of_truth.ScrollOfTruthInit;
import com.qurenie.relics_thirteenflames.init.*;
import com.qurenie.relics_thirteenflames.util.Scheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.hammerlib.api.io.NBTSerializationHelper;
import org.zeith.hammerlib.api.items.CreativeTab;
import org.zeith.hammerlib.proxy.HLConstants;

@Mod(ThirteenFlames.MODID)
public class ThirteenFlames
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "relics_thirteenflames";

    public static ResourceLocation rl(String str){
        return ResourceLocation.fromNamespaceAndPath(MODID, str);
    }
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogManager.getLogger("ThirteenFlames");
    
    public static final Scheduler SCHEDULER = new Scheduler(ServerTickEvent.Post.class, ServerTickEvent::hasTime);

    @CreativeTab.RegisterTab
    public static final CreativeTab ITEM_TAB = new CreativeTab(ResourceLocation.fromNamespaceAndPath(ThirteenFlames.MODID,"thirteenflames"),
            b -> b.icon(ItemsRegistry.KNEF_BOW::getDefaultInstance)
                    .title(Component.translatable("itemGroup.relics_thirteenflames"))
    ).putAfter(HLConstants.HL_TAB);

    public ThirteenFlames(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        SoundsRegistry.SOUNDS.register(modEventBus);
        ParticlesRegistry.PARTICLES.register(modEventBus);
        ScrollOfTruthInit.MENU_TYPES.register(modEventBus);
        EffectsRegistry.EFFECTS.register(modEventBus);
        EntityDataSerializers.DATA_SERIALIZERS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }



}
