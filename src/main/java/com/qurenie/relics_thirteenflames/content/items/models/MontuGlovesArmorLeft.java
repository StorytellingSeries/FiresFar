package com.qurenie.relics_thirteenflames.content.items.models;// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.qurenie.relics_thirteenflames.ThirteenFlames;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class MontuGlovesArmorLeft<T extends LivingEntity> extends HumanoidModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ThirteenFlames.rl("montu_gloves_armor_left"), "main");
    private final ModelPart left_arm;
    
    public MontuGlovesArmorLeft(ModelPart root) {
        super(root);
        this.left_arm = root.getChild("left_arm");
    }
    
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.0F), 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        
        PartDefinition right_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition bipedRightArm = right_arm.addOrReplaceChild("bipedRightArm", CubeListBuilder.create(), PartPose.offset(0, 3, 0.0F));
        
        PartDefinition glove_right = bipedRightArm.addOrReplaceChild("glove_right", CubeListBuilder.create().texOffs(0, 0).addBox(-2.37F, -3.3956F, -2.3559F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-2.37F, -3.3956F, -2.3559F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.25F))
                .texOffs(24, 1).addBox(-3.37F, 3.6044F, -1.8559F, 6.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.37F, 6.3956F, -0.6441F));
        
        PartDefinition cube_r1 = glove_right.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(21, 12).addBox(-2.98F, 4.8598F, -1.2754F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.63F, -3.3956F, 0.6441F, -0.3927F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }
    
    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        left_arm.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}