package com.qurenie.relics_thirteenflames.content.container;

import com.qurenie.relics_thirteenflames.init.BlocksRegistry;
import com.qurenie.relics_thirteenflames.init.MenuRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.NotNull;

public class AuritekhBeaconMenu extends BeaconMenu {
    
    private final ContainerLevelAccess access;
    
    public AuritekhBeaconMenu(int containerId, Container container, RegistryFriendlyByteBuf buf) {
        this(containerId, container, new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }
    
    public AuritekhBeaconMenu(int containerId, Container container, ContainerData beaconData, ContainerLevelAccess access) {
        super(containerId, container, beaconData, access);
        this.access = access;
    }
    
    @Override
    public @NotNull MenuType<?> getType() {
        return MenuRegistry.AURITEKH_BEACON_MENU.get();
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, BlocksRegistry.BEACON);
    }
    
}
