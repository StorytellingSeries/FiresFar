package com.qurenie.relics_thirteenflames.event;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.PacketContext;

import java.awt.*;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.GOLD_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.GRAY_COLOR;


@EventBusSubscriber
public class CommonGameEvents {
    
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
    public static void tickAttachments(EntityTickEvent.Post event) {
        FlamesUtils.tickAttachment(event.getEntity(), AttachmentsRegistry.WINGS_LAYER_DATA);
        FlamesUtils.tickAttachment(event.getEntity(), AttachmentsRegistry.PLANESHIFT_TICK, e -> {
            if (e instanceof LivingEntity p)
                ItemsRegistry.JODAH_MASK.onPlaneshiftEnd(p);
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void jodahShield(LivingDamageEvent.Pre event) {
        LivingEntity l = event.getEntity();
        float jodahShield = l.getData(AttachmentsRegistry.JODAH_SHEILD);

        float shieldDamage = Math.min(event.getNewDamage(), jodahShield);
        event.setNewDamage(event.getNewDamage() - shieldDamage);
        if (event.getEntity() instanceof LivingEntity living) {
            FlamesUtils.gainJodahShield(living, -shieldDamage);
        }
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
}