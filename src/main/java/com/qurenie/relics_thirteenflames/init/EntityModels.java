package com.qurenie.relics_thirteenflames.init;

import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

import static org.zeith.hammeranims.api.geometry.IGeometryContainer.create;

@SimplyRegister(prefix = "entity/")
public interface EntityModels {

    @RegistryName("seliaset_sun")
    IGeometryContainer SELIASET_SUN = create();

    @RegistryName("living_flesh")
    IGeometryContainer LIVING_FLESH = create();
}
