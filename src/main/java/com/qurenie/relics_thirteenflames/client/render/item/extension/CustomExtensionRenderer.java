package com.qurenie.relics_thirteenflames.client.render.item.extension;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class CustomExtensionRenderer implements IClientItemExtensions {
    
    Supplier<BlockEntityWithoutLevelRenderer> renderer;
    
    public CustomExtensionRenderer(Supplier<BlockEntityWithoutLevelRenderer> renderer) {
        this.renderer = Suppliers.memoize(renderer);
    }
    
    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer.get();
    }
    
}
