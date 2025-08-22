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

@EventBusSubscriber
public class SkintEffect extends MobEffect {
    
    public SkintEffect() {
        super(MobEffectCategory.BENEFICIAL, GOLD_COLOR.getRGB());
    }
    
    @SubscribeEvent
    public static void expired(MobEffectEvent.Expired expired) {
        if (expired.getEffectInstance() == null || expired.getEffectInstance().getEffect() != EffectsRegistry.SKINT_EFFECT)
            return;
        
        int scints = expired.getEntity().getData(AttachmentsRegistry.SKINT_DATA);
        FlamesUtils.addSkint(expired.getEntity(), -1, scints);
    }
    
}
