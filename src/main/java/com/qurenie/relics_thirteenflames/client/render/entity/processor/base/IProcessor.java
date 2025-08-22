package com.qurenie.relics_thirteenflames.client.render.entity.processor.base;

import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import net.minecraft.world.entity.Entity;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

public interface IProcessor<T extends Entity & IAnimatedEntity> {
    
    void process(T t, SimpleBedrockModel<? extends T> model, float partialTick);
    
}