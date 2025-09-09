package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import static com.qurenie.relics_thirteenflames.content.items.ItemJodahMask.GOLD_COLOR;
import static com.qurenie.relics_thirteenflames.content.items.ItemJodahMask.GRAY_COLOR;

@EventBusSubscriber
public class AntikintEffect extends MobEffect {
    
    public AntikintEffect() {
        super(MobEffectCategory.HARMFUL, GRAY_COLOR.getRGB());
    }
    
    @SubscribeEvent
    public static void remove(MobEffectEvent.Remove remove) {
        if (remove.getEffectInstance() == null || remove.getEffectInstance().getEffect() != EffectsRegistry.SKINTONIT_EFFECT)
            return;
        
        if (remove.getCure() != null)
            remove.setCanceled(true);
        else
            FlamesUtils.setSkint(remove.getEntity(), 0, false);
    }
    
    @SubscribeEvent
    public static void expired(MobEffectEvent.Expired expired) {
        if (expired.getEffectInstance() == null || expired.getEffectInstance().getEffect() != EffectsRegistry.SKINTONIT_EFFECT)
            return;
        
        int antiscint = expired.getEntity().getData(AttachmentsRegistry.ANTISKINT_DATA);
        FlamesUtils.addAntiskint(expired.getEntity(),  -1, antiscint);
    }
    
}
