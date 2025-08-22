package com.qurenie.relics_thirteenflames.client.particles;

import it.hurts.sskirillss.relics.client.particles.BasicColoredParticle;
import net.minecraft.client.multiplayer.ClientLevel;

public class PotionTintParticle extends BasicColoredParticle {
    
    public PotionTintParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Constructor constructor) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, constructor);
    }
    
}
