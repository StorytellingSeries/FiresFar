package com.qurenie.relics_thirteenflames.client.render.item.extension;

import net.minecraft.client.model.HumanoidModel;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.fml.common.asm.enumextension.RuntimeEnumExtender;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class RonasEnumExtension {
    
    public static final EnumProxy<HumanoidModel.ArmPose> FLAMES_ARM_POSE_LEFT = new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) (model, entity, arm) -> {
        var lArm = model.leftArm;
        
        lArm.xRot = lArm.xRot * 0.1F - 0.9424779F;
        lArm.yRot = ((float) Math.PI / 6F);
    });
    
    public static final EnumProxy<HumanoidModel.ArmPose> FLAMES_ARM_POSE_RIGHT = new EnumProxy<>(HumanoidModel.ArmPose.class, true, (IArmPoseTransformer) (model, entity, arm) ->
    {
        
        var rArm = model.rightArm;
        
        rArm.xRot = rArm.xRot * 0.1F - 0.9424779F;
        rArm.yRot = (-(float) Math.PI / 6F);
        
    });
    
}
