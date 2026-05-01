package com.qurenie.relics_thirteenflames.content.effects;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import com.qurenie.relics_thirteenflames.init.EffectsRegistry;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.GOLD_COLOR;

@EventBusSubscriber
public class SkintEffect extends MobEffect {
    
    public SkintEffect() {
        super(MobEffectCategory.BENEFICIAL, GOLD_COLOR.getARGB());
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        return super.applyEffectTick(livingEntity, amplifier);
    }
    
    @SubscribeEvent
    public static void remove(MobEffectEvent.Remove remove) {
        if (remove.getEffectInstance() == null || remove.getEffectInstance().getEffect() != EffectsRegistry.SKINT_EFFECT)
            return;
        
        if (remove.getCure() != null)
            remove.setCanceled(true);
        else
            FlamesUtils.setSkint(remove.getEntity(), 0, false);
    }
    
    @SubscribeEvent
    public static void expired(MobEffectEvent.Expired expired) {
        if (expired.getEffectInstance() == null || expired.getEffectInstance().getEffect() != EffectsRegistry.SKINT_EFFECT)
            return;
        
        int scints = expired.getEntity().getData(AttachmentsRegistry.SKINT_DATA);
        FlamesUtils.addSkint(expired.getEntity(), -1, scints);
        expired.setCanceled(true);
    }
    
}
