package com.qurenie.relics_thirteenflames.client.render.item.extension;

import net.minecraft.client.model.HumanoidModel;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class TravellerEnumExtension {
    
    public static final EnumProxy<HumanoidModel.ArmPose> TRAVELLER_ARM_POSE_LEFT = new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) (model, entity, arm) -> {
        var lArm = model.leftArm;
        var rArm = model.rightArm;
        
        lArm.xRot = lArm.xRot * 0.02F - 0.9424779F;
        lArm.yRot = ((float) Math.PI / 22F);
        
        rArm.xRot = rArm.xRot * 0.02F - 0.9424779F;
        rArm.yRot = (-(float) Math.PI / 3F);
    });
    
    public static final EnumProxy<HumanoidModel.ArmPose> TRAVELLER_ARM_POSE_RIGHT = new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) (model, entity, arm) ->
    {
        
        var rArm = model.rightArm;
        var lArm = model.leftArm;
        
        rArm.xRot = rArm.xRot * 0.02F - 0.9424779F;
        rArm.yRot = (-(float) Math.PI / 22F);
        
        lArm.xRot = lArm.xRot * 0.02F - 0.9424779F;
        lArm.yRot = ((float) Math.PI / 3F);
    });
    
}
