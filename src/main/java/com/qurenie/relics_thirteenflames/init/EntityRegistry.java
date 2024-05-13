package com.qurenie.relics_thirteenflames.init;

import com.qurenie.relics_thirteenflames.content.entities.*;
import com.qurenie.relics_thirteenflames.content.entities.EntitySeliasetSun;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public class EntityRegistry {


    @RegistryName("knef_projectile")
    public static final EntityType<KnefProjectile> KNEF_PROJECTILE = Builder.of(KnefProjectile::new, MobCategory.MISC)
            .sized(0.1F, 0.1F)
            .setUpdateInterval(1)
            .updateInterval(1)
            .build("knef_projectile");

    @RegistryName("knef_projectile_special")
    public static final EntityType<KnefProjectileSpecial> KNEF_PROJECTILE_SPECIAL = Builder.of(KnefProjectileSpecial::new, MobCategory.MISC)
            .sized(0.1F, 0.1F)
            .build("knef_projectile_special");

    @RegistryName("knef_proj_carrier")
    public static final EntityType<KnefProjCarrier> KNEF_PROJECTILE_CARRIER = Builder.of(KnefProjCarrier::new, MobCategory.MISC)
            .sized(0.1F, 0.1F)
            .build("knef_proj_carrier");

    @RegistryName("knef_stormcaller")
    public static final EntityType<KnefStormcaller> KNEF_STORMCALLER = Builder.of(KnefStormcaller::new, MobCategory.MISC)
            .sized(0.1F, 0.1F)
            .build("knef_stormcaller");

    @RegistryName("knef_discharge")
    public static final EntityType<KnefDischarge> KNEF_DISCHARGE = Builder.of(KnefDischarge::new, MobCategory.MISC)
            .sized(0.1F, 0.1F)
            .build("knef_discharge");

    @RegistryName("knef_storm")
    public static final EntityType<KnefStormEntity> KNEF_STORM = Builder.of(KnefStormEntity::new, MobCategory.MISC)
            .sized(1F, 1F)
            .build("knef_storm");

    @RegistryName("knef_raindrop")
    public static final EntityType<KnefRaindrop> KNEF_RAINDROP = Builder.of(KnefRaindrop::new, MobCategory.MISC)
            .sized(0.6F, 0.5F)
            .build("knef_raindrop");

    @RegistryName("poisonwave")
    public static final EntityType<PoisonWaveProjectile> POISONWAVE = Builder.of(PoisonWaveProjectile::new, MobCategory.MISC)
            .sized(0.2F, 0.2F)
            .build("poisonwave");

    @RegistryName("fartcloud")
    public static final EntityType<FartCloudEntity> FARTCLOUD = Builder.of(FartCloudEntity::new, MobCategory.MISC)
            .sized(1F, 1F)
            .build("fartcloud");

    @RegistryName("deathcloud")
    public static final EntityType<DeathlyFartCloudEntity> DEATHCLOUD = Builder.of(DeathlyFartCloudEntity::new, MobCategory.MISC)
            .sized(1F, 1F)
            .build("fartcloud");

    @RegistryName("usablefallingblock")
    public static final EntityType<UsableFallingBlockEntity> USABLE_FALLING = Builder.of(UsableFallingBlockEntity::new, MobCategory.MISC)
            .sized(0.98F, 0.98F)
            .build("usablefallingblock");

    @RegistryName("seliaset_sun")
    public static final EntityType<EntitySeliasetSun> SELIASET_SUN = Builder.of(EntitySeliasetSun::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .build("seliaset_sun");

    @RegistryName("living_flesh")
    public static final EntityType<LivingFleshEntity> LIVING_FLESH = Builder.of(LivingFleshEntity::new, MobCategory.CREATURE)
            .sized(1F, 1F)
            .build("living_flesh");
}
