package com.qurenie.relics_thirteenflames.client.render.entity.processor.base;

import com.google.common.collect.Lists;
import com.qurenie.relics_thirteenflames.client.render.entity.SimpleBedrockModel;
import net.minecraft.world.entity.Entity;
import org.zeith.hammeranims.api.tile.IAnimatedEntity;

import java.util.List;

public class CentralModelProcessor<T extends Entity & IAnimatedEntity> {
    protected final List<IProcessor<? super T>> prc = Lists.newArrayList();

    public void add(IProcessor<? super T> processor) {
        prc.add(processor);
    }

    public void apply(T object, SimpleBedrockModel<? extends T> model, float partialTick) {
        for (var p : prc)
            p.process(object, model, partialTick);
    }
}