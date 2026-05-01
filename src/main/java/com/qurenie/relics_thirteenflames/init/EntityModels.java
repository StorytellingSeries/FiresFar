package com.qurenie.relics_thirteenflames.init;

import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

import static org.zeith.hammeranims.api.geometry.IGeometryContainer.create;

@SimplyRegister(prefix = "entity/")
public interface EntityModels {

    @RegistryName("seliaset_sun")
    IGeometryContainer SELIASET_SUN = create();

    @RegistryName("seliaset_sun_flawless")
    IGeometryContainer SELIASET_SUN_FLAWLESS = create();
    
    @RegistryName("jodah_staff_model")
    IGeometryContainer JODAH_STAFF_MODEL = create();

    @RegistryName("living_flesh")
    IGeometryContainer LIVING_FLESH = create();

    @RegistryName("ghost_big")
    IGeometryContainer GHOST_BIG = create();

    @RegistryName("ghost_small")
    IGeometryContainer GHOST_SMALL = create();
    
    @RegistryName("jodah_wings")
    IGeometryContainer JODAH_WINGS = create();
    
    @RegistryName("skint_cluster")
    IGeometryContainer SKINT_CLUSTER = create();
    
    @RegistryName("attack_book")
    IGeometryContainer ATTACK_BOOK = create();
    
    @RegistryName("respawn_book")
    IGeometryContainer RESPAWN_BOOK = create();
    
    @RegistryName("adventurer_sword_big")
    IGeometryContainer ADVENTURER_SWORD = create();
    
    @RegistryName("adventurer_sword_big_flipped")
    IGeometryContainer ADVENTURER_SWORD_FLIPPED = create();
    
}
