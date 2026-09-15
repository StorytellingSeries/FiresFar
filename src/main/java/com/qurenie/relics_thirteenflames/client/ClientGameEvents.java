package com.qurenie.relics_thirteenflames.client;

import com.qurenie.relics_thirteenflames.init.AttachmentsRegistry;
import com.qurenie.relics_thirteenflames.util.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import static com.qurenie.relics_thirteenflames.style.ColorScheme.GOLD_COLOR;
import static com.qurenie.relics_thirteenflames.style.ColorScheme.GRAY_COLOR;

@EventBusSubscriber(Dist.CLIENT)
public class ClientGameEvents {

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
