package com.qurenie.relics_thirteenflames.content.container;

import com.qurenie.api.event.PacketHandleEvent;
import com.qurenie.api.event.SmithingBlockCraftEvent;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.init.MenuRegistry;
import com.qurenie.relics_thirteenflames.mixins.AbstractContainerMenuAccessor;
import lombok.Getter;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.apache.logging.log4j.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Optional;
import java.util.function.BiFunction;

@EventBusSubscriber
public class MontuCompositeContainer extends AbstractContainerMenu {
    
    @Getter
    final int level;
    private final Inventory playerInv;
    private final ItemStack gloves;
    
    private final EnumMap<MontuMenuType, ItemStackHandler> containers = new EnumMap<>(MontuMenuType.class);
    
    @Getter
    @NotNull
    private AbstractContainerMenu activeMenu;
    
    @Getter
    @Nullable
    private AbstractContainerMenu lastActiveMenu;
    
    public MontuCompositeContainer(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readInt(), ItemStack.STREAM_CODEC.decode(buf));
    }
    
    public MontuCompositeContainer(int containerId, Inventory playerInv, int level, ItemStack gloves) {
        super(MenuRegistry.MONTU_COMPOSIT_MENU.get(), containerId);
        this.gloves = gloves;
        
        this.level = level;
        this.playerInv = playerInv;
        
        containers.put(MontuMenuType.ANVIl, new ItemStackHandler(3));
        containers.put(MontuMenuType.CRAFTTABLE, new ItemStackHandler(10));
        containers.put(MontuMenuType.SMITHTABLE, new ItemStackHandler(3));
        containers.put(MontuMenuType.STONECUTTER, new ItemStackHandler(2));
        
        activateType(MontuMenuType.ANVIl);
    }
    
    @SubscribeEvent
    public static void onPacket(PacketHandleEvent event) {
        if (event.getPacket() instanceof ServerboundRenameItemPacket packet
                && event.getProcessor() instanceof ServerPlayerConnection connection
                && connection.getPlayer().containerMenu instanceof MontuCompositeContainer container
                && container.activeMenu instanceof AnvilMenu anvilMenu) {
            anvilMenu.setItemName(packet.getName());
        }
    }
    
    @Override
    public void setCarried(@NotNull ItemStack stack) {
        super.setCarried(stack);
        activeMenu.setCarried(stack);
    }
    
    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        return activeMenu.clickMenuButton(player, id);
    }
    
    private void addPlayerInventorySlot(Inventory playerInv) {
        int y = 77;
        
        for (int l = 0; l < 3; ++l) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInv, k + l * 9 + 18, 8 + k * 18, l * 18 + 51 + y));
            }
        }
        
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInv, i1, 17 + i1 * 18, 109 + y));
        }
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return activeMenu.quickMoveStack(player, index);
    }
    
    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
    
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        
        for (ItemStackHandler handler : containers.values()) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty())
                    if (player.isAlive() && (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.hasDisconnected())) {
                        player.getInventory().placeItemBackInInventory(stack);
                    } else {
                        player.drop(stack, false);
                    }
            }
        }
    }
    
    public void activateType(MontuMenuType type) {// или другая очистка
        if (lastActiveMenu != null)
            lastActiveMenu.slots.clear();
        lastActiveMenu = activeMenu;
        
        AbstractContainerMenu menu = type.createMenu(containerId, playerInv);
        attachSlots(type, menu);
        this.activeMenu = menu;
    }
    
    private void attachSlots(MontuMenuType type, AbstractContainerMenu menu) {
        int size = (int) menu.slots.stream().filter(s -> s.container != playerInv).count();
        
        ItemStackHandler container = containers.get(type);
        if (container.getSlots() != size)
            container.setSize(size);
        
        this.slots.clear();
        ((AbstractContainerMenuAccessor) this).getLastSlots().clear();
        ((AbstractContainerMenuAccessor) this).getRemoteSlots().clear();
        
        int containerSlot = 0;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container != playerInv) {
                if (!playerInv.player.level().isClientSide)
                    slot.set(container.getStackInSlot(containerSlot));
                Slot newSlot = new ConductiveSlotIHandler(slot, containerSlot, container);
                newSlot.index = slot.index;
                menu.slots.set(i, newSlot);
                slot = newSlot;
                containerSlot++;
            }
            
            this.addSlot(slot);
        }

//        menu.slots.clear();
//        addPlayerInventorySlot(playerInv);
    }
    
    @SubscribeEvent
    public static void onCraft(AnvilRepairEvent event) {
        if (event.getEntity().containerMenu instanceof MontuCompositeContainer montuCompositeContainer)
            ItemsRegistry.MONTU_GLOVES.addExperience(event.getEntity(), montuCompositeContainer.gloves, 4);
    }
    
    @SubscribeEvent
    public static void onCraft(SmithingBlockCraftEvent event) {
        if (event.getEntity().containerMenu instanceof MontuCompositeContainer montuCompositeContainer)
            ItemsRegistry.MONTU_GLOVES.addExperience(event.getEntity(), montuCompositeContainer.gloves, 4);
    }
    
    public enum MontuMenuType {
        ANVIl(AnvilMenu::new, MontuMenuType::createAccess, AnvilScreen::new, Component.translatable("container.repair")),
        SMITHTABLE(SmithingMenu::new, MontuMenuType::createAccess, SmithingScreen::new, Component.translatable("container.upgrade")),
        STONECUTTER(StonecutterMenu::new, MontuMenuType::createAccess, StonecutterScreen::new, Component.translatable("container.stonecutter")),
        CRAFTTABLE(CraftingMenu::new, MontuMenuType::createAccess, CraftingScreen::new, Component.translatable("container.crafting"));
        
        final MenuType.MenuSupplier<?> menuConstructor;
        final MenuScreens.ScreenConstructor<?, ?> screenConstructor;
        @Getter
        final Component containerName;
        
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> MontuMenuType(MenuType.MenuSupplier<M> menuConstructor,
                                                                                          MenuScreens.ScreenConstructor<M, U> screenConstructor,
                                                                                          Component name) {
            this.menuConstructor = menuConstructor;
            this.screenConstructor = screenConstructor;
            this.containerName = name;
        }
        
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> MontuMenuType(MenuSupplierWithAccess<M> menuConstructor,
                                                                                          AccessSupplier accessSuppler,
                                                                                          MenuScreens.ScreenConstructor<M, U> screenConstructor,
                                                                                          Component name) {
            this.menuConstructor = (container, inv) -> menuConstructor.create(container, inv, accessSuppler.create(inv.player));
            this.screenConstructor = screenConstructor;
            this.containerName = name;
        }
        
        private static ContainerLevelAccess createAccess(Player player) {
            return new ContainerLevelAccess() {
                @Override
                public <T> @NotNull Optional<T> evaluate(@NotNull BiFunction<Level, BlockPos, T> p_39311_) {
                    return Optional.of(p_39311_.apply(player.level(), player.blockPosition()));
                }
            };
        }
        
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
            return menuConstructor.create(containerId, playerInventory);
        }
        
        public <T extends AbstractContainerMenu> AbstractContainerScreen<T> createScreen(T menu, Inventory playerInventory, Component title) {
            return (AbstractContainerScreen<T>) screenConstructor.create(Cast.cast(menu), playerInventory, title);
        }
        
        public MontuMenuType next(int level) {
            return MontuMenuType.values()[(this.ordinal() + 1) % level];
        }
        
        public MontuMenuType previous(int level) {
            return MontuMenuType.values()[(this.ordinal() + level - 1) % level];
        }
        
        private interface MenuSupplierWithAccess<T extends AbstractContainerMenu> {
            
            T create(int containerId, Inventory playerInventory, ContainerLevelAccess access);
            
        }
        
        private interface AccessSupplier {
            
            ContainerLevelAccess create(Player player);
            
        }
    }
    
    protected static class ConductiveSlotIHandler extends SlotItemHandler {
        
        Slot delegate;
        
        public ConductiveSlotIHandler(@NotNull Slot delegate, int index, @NotNull ItemStackHandler container) {
            super(container, index, delegate.x, delegate.y);
            this.delegate = delegate;
        }
        
        @Override
        public void initialize(@NotNull ItemStack stack) {
            super.initialize(stack);
            this.setChanged();
        }
        
        @Override
        public boolean mayPickup(@NotNull Player playerIn) {
            return delegate.mayPickup(playerIn);
        }
        
        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return this.delegate.mayPlace(stack);
        }
        
        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            this.delegate.onTake(player, stack);
        }
        
        @Override
        public void set(@NotNull ItemStack stack) {
            this.delegate.set(stack);
            this.delegate.setChanged();
            sync();
        }
        
        @Override
        public @NotNull ItemStack getItem() {
            return this.delegate.getItem();
        }
        
        @Override
        public int getMaxStackSize() {
            return delegate.getMaxStackSize();
        }
        
        @Override
        public int getMaxStackSize(@NotNull ItemStack stack) {
            return delegate.getMaxStackSize();
        }
        
        @Override
        public @NotNull ItemStack remove(int amount) {
            return this.delegate.remove(amount);
        }
        
        public void sync() {
            ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(index, getItem());
        }
        
    }
    
    public record Provider(int level, ItemStack gloves) implements MenuProvider {
        
        @Override
            public @NotNull Component getDisplayName() {
                return Component.empty();
            }
            
            @Override
            public @NotNull AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
                return new MontuCompositeContainer(pContainerId, pPlayerInventory, level, gloves);
            }
            
        }

//    protected static class PacketProcessor {
//
//        private final HashMap<Class<? extends AbstractContainerMenu>, Multimap<PacketType<? extends Packet<?>>,
//                        BiConsumer<? extends AbstractContainerMenu, ? extends Packet<?>>>> actions = new HashMap<>();
//
//        public <T extends AbstractContainerMenu, P extends Packet<?>> void addProcessor(Class<T> menuType, PacketType<P> type, BiConsumer<T, P> onHandle) {
//            actions.putIfAbsent(menuType, ArrayListMultimap.create());
//            actions.get(menuType).put(type, onHandle);
//        }
//
//        public <P extends Packet<?>> void process(@NotNull AbstractContainerMenu menu, @NotNull P packet) {
//            actions.get(menu.getClass()).get(packet.type()).forEach(actions -> actions.accept(Cast.cast(menu), Cast.cast(packet)));
//        }
//    }

    
}
