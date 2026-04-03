package com.qurenie.relics_thirteenflames.event;

import com.qurenie.api.IActivityContainer;
import com.qurenie.relics_thirteenflames.activity.call.CallInput;
import com.qurenie.relics_thirteenflames.activity.call.settings.InventoryType;
import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import com.qurenie.relics_thirteenflames.init.ItemsRegistry;
import com.qurenie.relics_thirteenflames.net.EntityPacket;
import com.qurenie.relics_thirteenflames.net.PlaneshiftSyncPacket;
import com.qurenie.relics_thirteenflames.net.SkintDataAttachmentPacket;
import com.qurenie.relics_thirteenflames.util.FlamesUtils;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


@EventBusSubscriber
public class CommonGameEvents {
    
    private static final Color GOLD_COLOR = new Color(200, 150, 20);
    private static final Color PURPLE_COLOR = new Color(100, 20, 150);
    public static final Color GRAY_COLOR = new Color(50, 50, 50);
    
    @SubscribeEvent
    public static void joinEvent(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            Network.sendToServer(new EntityPacket(event.getEntity().getId()) {
                @Override
                public void serverExecute(PacketContext ctx) {
                    Entity e = getEntity(ctx.getLevel());
                    if (e == null)
                        return;
                    
                    if (e.hasData(AttachmentsRegistry.ANTISKINT_DATA) || e.hasData(AttachmentsRegistry.SKINT_DATA))
                        Network.sendTo(ctx.getSender(), new SkintDataAttachmentPacket(e));
                    
                    if (e.hasData(AttachmentsRegistry.PLANESHIFT_TICK))
                        Network.sendTo(ctx.getSender(), new PlaneshiftSyncPacket(e.getData(AttachmentsRegistry.PLANESHIFT_TICK), e.getId()));
                }
            });
        }
    }

    @SubscribeEvent
    public static void tickActivities(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide)
            return;

        var items = new ArrayList<ItemStack>();
        items.addAll(player.getInventory().items);
        items.addAll(player.getInventory().armor);
        items.addAll(player.getInventory().offhand);
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() instanceof IActivityContainer container)
                container.tick(stack, player);
        }

        CuriosApi.getCuriosInventory(player).ifPresent((handler) -> handler.getCurios().values().forEach(stacks -> {
            var dStacks = stacks.getStacks();
            for (int i = 0; i < dStacks.getSlots(); i++) {
                ItemStack stack = dStacks.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getItem() instanceof IActivityContainer container)
                    container.tick(stack, player);
            }
        }));
    }
    
    @SubscribeEvent
    public static void tickAttachments(EntityTickEvent.Post event) {
        FlamesUtils.tickAttachment(event.getEntity(), AttachmentsRegistry.WINGS_LAYER_DATA);
        FlamesUtils.tickAttachment(event.getEntity(), AttachmentsRegistry.PLANESHIFT_TICK, e -> {
            if (e instanceof LivingEntity p)
                ItemsRegistry.JODAH_MASK.onPlaneshiftEnd(p);
        });
    }
    
    @SubscribeEvent
    public static void skintDamageEvent(LivingDamageEvent.Pre event) {
        LivingEntity l = event.getEntity();
        int skintCount = l.hasData(AttachmentsRegistry.SKINT_DATA) ? l.getData(AttachmentsRegistry.SKINT_DATA) : 0;
        int antiskintCount = l.hasData(AttachmentsRegistry.ANTISKINT_DATA) ? l.getData(AttachmentsRegistry.ANTISKINT_DATA) : 0;
        
        event.setNewDamage(event.getNewDamage() + antiskintCount * 2);
        if (event.getSource().getEntity() instanceof LivingEntity living) {
            ItemStack stack = living.getItemBySlot(EquipmentSlot.HEAD);
            if (stack.is(ItemsRegistry.JODAH_MASK))
                ItemsRegistry.JODAH_MASK.addExperience(living, stack, 1 + (antiskintCount / 3));

            if (skintCount > 0) {
                event.setNewDamage(Math.max(event.getNewDamage() - 10, 0));
                FlamesUtils.addSkint(null, living, l, -1);
            }
        }
    }
    
    @SubscribeEvent
    public static void skintSparkParticlesEvent(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide)
            return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.isInvisible() || mc.options.getCameraType().isFirstPerson() && mc.player == entity)
                continue;
            
            if (entity instanceof LivingEntity living && living.tickCount % 2 == 0) {
                double rotate = mc.level.getGameTime() / 220f * Math.PI;
                int skintCharges = living.hasData(AttachmentsRegistry.SKINT_DATA) ? living.getData(AttachmentsRegistry.SKINT_DATA) : 0;
                int antiskintCharges = living.hasData(AttachmentsRegistry.ANTISKINT_DATA) ? living.getData(AttachmentsRegistry.ANTISKINT_DATA) : 0;
                
                if (skintCharges + antiskintCharges == 0)
                    continue;
                
                boolean nextSkint = skintCharges > 0;
                double delta = Math.PI * 2 / (antiskintCharges + skintCharges);
                AABB aabb = living.getBoundingBox();
                double radius = Math.sqrt(Math.pow(aabb.getXsize() / 2, 2) + Math.pow(aabb.getZsize() / 2, 2)) * 1.5 + 0.3;
                for (double angle = 0; angle < Math.PI * 2; angle += delta) {
                    Vec3 vec = new Vec3(1, 0, 0).yRot((float) (rotate + angle)).scale(radius).add(entity.getBoundingBox().getCenter());
                    
                    for (int i = 0; i < 3; i++)
                        ParticleHelper.spawnDirectedParticle(mc.level,
                                ParticleHelper.constructSimpleSpark(nextSkint ? GOLD_COLOR : GRAY_COLOR, 0.3f, 10, 0.89f).withLightning(nextSkint),
                                vec.x + mc.level.random.nextGaussian() * 0.04, vec.y + mc.level.random.nextGaussian() * 0.04, vec.z + mc.level.random.nextGaussian() * 0.04,
                                mc.level.random.nextGaussian() * 0.0024, mc.level.random.nextGaussian() * 0.0024, mc.level.random.nextGaussian() * 0.0024);
                    
                    for (int i = 0; i < 2; i++)
                        ParticleHelper.spawnDirectedParticle(mc.level,
                                ParticleHelper.constructSmoke(nextSkint ? GOLD_COLOR : GRAY_COLOR, 0.13f, 20, 0).withLightning(nextSkint),
                                vec.x + mc.level.random.nextGaussian() * 0.04, vec.y + mc.level.random.nextGaussian() * 0.04, vec.z + mc.level.random.nextGaussian() * 0.04,
                                mc.level.random.nextGaussian() * 0.0017, mc.level.random.nextGaussian() * 0.0017, mc.level.random.nextGaussian() * 0.0017);
                    
                    nextSkint = nextSkint ? antiskintCharges-- <= 0 : skintCharges-- > 0;
                }
            }
        }
    }
    
}