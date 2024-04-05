//package com.qurenie.relics_thirteenflames.content.entities;
//
//import com.qurenie.relics_thirteenflames.util.ParticleHelper;
//import it.hurts.sskirillss.relics.client.particles.circle.CircleTintData;
//import it.hurts.sskirillss.relics.init.EffectRegistry;
//import it.hurts.sskirillss.relics.items.relics.base.utils.AbilityUtils;
//import it.hurts.sskirillss.relics.utils.Scheduler;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//import net.minecraft.core.particles.ParticleTypes;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.protocol.Packet;
//import net.minecraft.network.syncher.EntityDataAccessor;
//import net.minecraft.network.syncher.EntityDataSerializers;
//import net.minecraft.network.syncher.SynchedEntityData;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.sounds.SoundSource;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.entity.*;
//import net.minecraft.world.entity.item.ItemEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.Vec3;
//import net.minecraftforge.network.NetworkHooks;
//import net.minecraftforge.network.PacketDistributor;
//import org.zeith.hammerlib.net.Network;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SeliasetHornEntity extends LivingEntity {
//
//    public static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(SeliasetHornEntity.class, EntityDataSerializers.BOOLEAN);
//    public static final EntityDataAccessor<Boolean> IS_ON_COOLDOWN = SynchedEntityData.defineId(SeliasetHornEntity.class, EntityDataSerializers.BOOLEAN);
//
//    public CompoundTag serializedSeliasetHorn;
//
//    private int cooldown = 0;
//    private int ticker = 0;
//    private int remainingWaveCount = 0;
//    public SeliasetHornEntity(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
//        super(pEntityType, pLevel);
//        this.serializedSeliasetHorn = ItemsST.SELIASET_HORN.getDefaultInstance().serializeNBT();
//    }
//
//    public static SeliasetHornEntity createHorn(Level level, Vec3 pos,ItemStack itemStack){
//        SeliasetHornEntity horn = new SeliasetHornEntity(EntitiesST.SELIASET_HORN,level);
//        horn.setHornItem(itemStack);
//        horn.setPos(pos);
//        horn.loadHornData(itemStack.getOrCreateTag().getCompound("hornData"));
//        return horn;
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//        if (!level.isClientSide){
//            if (serializedSeliasetHorn == null) this.remove(RemovalReason.DISCARDED);
//            if (this.isActive()){
//                if (ticker++ % 60 == 0) {
//                    remainingWaveCount--;
//                    if (remainingWaveCount <= -1){
//                        this.setActive(false);
//                        this.setOnCooldown(true);
//                        remainingWaveCount = 0;
//
//                    }else {
//                        this.releaseWave();
//                    }
//                }else{
//                    int tick = ticker % 60;
//                    this.tickWave(tick);
//
//                }
//            }else if (this.isOnCooldown()){
//                if (this.cooldown++ >= this.getCooldownStat()){
//                    this.cooldown = 0;
//                    this.setOnCooldown(false);
//                }
//            }
//        }
//    }
//
//
//
//
//
//
//
//    private int getCooldownStat(){
//        ItemStack item = this.getHornItem();
//        int cooldown = (int)AbilityUtils.getAbilityValue(item,"block","cooldown") * 20;
//        return cooldown;
//    }
//
//    private int getWaveCountStat(){
//        ItemStack item = this.getHornItem();
//        int cooldown = (int)AbilityUtils.getAbilityValue(item,"block","wavesCount");
//        return cooldown;
//    }
//
//    private double getStunDurationStat(){
//        ItemStack item = this.getHornItem();
//        double stun = AbilityUtils.getAbilityValue(item,"block","stunDuration");
//        return stun;
//    }
//
//    public void tickWave(int tick){
//        if (tick <= 30) {
//            double radius = tick / 30f * 10;
//            List<LivingEntity> toThrowOut = level.getEntitiesOfClass(LivingEntity.class, new AABB(
//                    -radius, -radius, -radius, radius, radius, radius
//            ).move(this.position()), e -> !(e instanceof Player player) && e.distanceTo(this) <= radius);
//            for (LivingEntity entity : toThrowOut) {
//                if (entity == this) continue;
//                Vec3 b = entity.position().subtract(this.position());
//                Vec3 sp = b.normalize().multiply(2,2,2).add(0,0.5,0);
//                entity.setDeltaMovement(sp);
//                entity.addEffect(new MobEffectInstance(EffectRegistry.STUN.get(),(int)Math.round(this.getStunDurationStat()*20),0));
//            }
//        }
//    }
//    public void releaseWave(){
//        if(!this.level.isClientSide()) Minecraft.getInstance().getSoundManager().playDelayed(new SimpleSoundInstance(SoundsST.SELI_HORN_WAVE, SoundSource.MASTER, 1, 1, this.random, this.blockPosition()), 2);
//        //this.playSound(SoundsST.SELI_HORN_WAVE);
//        Network.send(PacketDistributor.TRACKING_ENTITY.with(()->this),new SeliasetHornWaveReleasePacket(this));
//        Vec3 ePos = this.position();
//        for(int i = 0; i < 14; i++){
//            double r = 0.512 * i;
//            int count = (int) Math.round(2 * Math.PI * r * 10);
//            double r2 = r + 0.256;
//            int count2 = (int) Math.round(2 * Math.PI * r2 * 10);
//            int finalI = i;
//            Scheduler.schedule(i , () -> {
//                for(int j = 0; j < count; j++){
////                    level.addParticle(new CircleTintWithTrailData(new Color(255, 255, 255), 0.1f,0.2f,40,-1,false,true),
////                            pos.x(), pos.y(), pos.z(), sped.x(), 0, sped.z());
//                    Vec3 pos = ePos.add(new Vec3(r, 0, 0).yRot((float) Math.toRadians(360.0 / count * j)));
//                    Vec3 sped = pos.subtract(ePos.add(new Vec3(0, 0.3, 0))).normalize().scale(0.11);
//                    ParticleHelper.spawnDirectedParticle(level, new CircleTintData(new Color(42, 41, 26), 0.2f, (int) Math.round(10 + r * 3), -1, false),
//                            pos.x, pos.y + 0.3, pos.z, sped.x, 0, sped.z);
//                    if(finalI == 13) ParticleHelper.spawnDirectedParticle(level, ParticleTypes.CLOUD,
//                            pos.x, pos.y, pos.z, sped.scale(2).x, 0, sped.scale(2).z);
//
//                }
//
//                for (int j = 0; j < count2; j++) {
//                    Vec3 pos = ePos.add(new Vec3(r2, 0, 0).yRot((float) Math.toRadians(360.0 / count2 * j)));
//                    Vec3 sped = pos.subtract(ePos.add(new Vec3(0, 0.3, 0))).normalize().scale(0.11);
//                    ParticleHelper.spawnDirectedParticle(level, new CircleTintData(new Color(42, 41, 26), 0.2f, (int) Math.round(10 + r2 * 3), -1, false),
//                            pos.x, pos.y + 0.3, pos.z, sped.x, 0, sped.z);
//
//                }
//            });
//
//        }
//    }
//
//    @Override
//    public InteractionResult interact(Player player, InteractionHand hand) {
//
//        if (!level.isClientSide){
//            if (!this.isActive() && !this.isOnCooldown()){
//                this.setActive(true);
//                this.cooldown = 0;
//                this.remainingWaveCount = this.getWaveCountStat();
//                this.ticker = 0;
//                player.swing(hand);
//            }
//        }
//        return InteractionResult.SUCCESS;
//    }
//
//    @Override
//    public HumanoidArm getMainArm() {
//        return HumanoidArm.RIGHT;
//    }
//
//    public void releaseWaveParticles(){
//        int c = 36;
//        double angle = Math.PI * 2 / 72;
//        for (float g = -0.5f; g <= 0.5;g += 0.25f){
//            for (int i = 0; i <= 72;i++){
//                double vangle = angle*i + (g * angle);
//                double x = Math.sin(vangle);
//                double y = Math.cos(vangle);
//
//
//                float md = (1 - 2.5f / Math.abs(g)) * 0.9f;
//                Vec3 dir = new Vec3(x*md,g,y*md);
//                Vec3 ppos = this.position().add(dir.multiply(0.1,0.1,0.1)).add(0,0.2,0);
//                Vec3 speed = dir.normalize().multiply(0.5,0.5,0.5);
//                for (int k = 0; k <= 3;k++){
//                    Vec3 ppos1 = ppos .add(
//                            level.random.nextFloat() * 0.5 - 0.25,
//                            level.random.nextFloat() * 0.5 - 0.25,
//                            level.random.nextFloat() * 0.5 - 0.25
//                    );
//                    level.addParticle(ParticleTypes.CLOUD,ppos1.x,ppos1.y,ppos1.z,speed.x,speed.y,speed.z);
//                }
////                level.addParticle(new CircleTintWithTrailData(
////                        Color.WHITE,0.1f,0.2f,40,-1,false,true
////                ),ppos.x,ppos.y,ppos.z,speed.x*0.5,speed.y*0.5,speed.z*0.5);
//            }
//        }
//
//    }
//
//    public void setHornItem(ItemStack item){
//        if (item.getItem() != ItemsST.SELIASET_HORN) throw new RuntimeException("The fuck?");
//        this.serializedSeliasetHorn = item.serializeNBT();
//    }
//
//    public ItemStack getHornItem(){
//        if (serializedSeliasetHorn == null) return null;
//        return ItemStack.of(serializedSeliasetHorn);
//    }
//
//    @Override
//    public boolean hurt(DamageSource pSource, float pAmount) {
//        if (pSource.getEntity() instanceof ServerPlayer player){
//            ItemStack horn = this.getHornItem();
//            CompoundTag hornData = new CompoundTag();
//            this.saveHornData(hornData);
//            horn.getOrCreateTag().put("hornData",hornData);
//            ItemEntity item = new ItemEntity(level,this.getX(),this.getY(),this.getZ(),horn);
//            level.addFreshEntity(item);
//            this.remove(RemovalReason.DISCARDED);
//        }
//        return false;
//    }
//
//
//    @Override
//    public boolean save(CompoundTag tag) {
//        tag.put("hornItem",this.serializedSeliasetHorn);
//        this.saveHornData(tag);
//        return super.save(tag);
//    }
//
//    public void saveHornData(CompoundTag tag){
//        tag.putInt("remainingWaves",this.remainingWaveCount);
//        tag.putInt("ticker",this.ticker);
//        tag.putInt("cooldown",this.cooldown);
//        tag.putBoolean("isOnCooldown",this.isOnCooldown());
//        tag.putBoolean("isActive",this.isActive());
//    }
//
//    @Override
//    public void load(CompoundTag tag) {
//        this.serializedSeliasetHorn = tag.getCompound("hornItem");
//        this.loadHornData(tag);
//        super.load(tag);
//    }
//
//    public void loadHornData(CompoundTag tag){
//        this.cooldown = tag.getInt("cooldown");
//        this.remainingWaveCount = tag.getInt("remainingWaves");
//        this.ticker = tag.getInt("ticker");
//        this.setActive(tag.getBoolean("isActive"));
//        this.setOnCooldown(tag.getBoolean("isOnCooldown"));
//    }
//
//
//    public boolean isActive(){
//        return this.entityData.get(ACTIVE);
//    }
//
//    public void setActive(boolean state){
//        this.entityData.set(ACTIVE,state);
//    }
//
//    public boolean isOnCooldown(){
//        return this.entityData.get(IS_ON_COOLDOWN);
//    }
//
//    public void setOnCooldown(boolean onCooldown){
//        this.entityData.set(IS_ON_COOLDOWN,onCooldown);
//    }
//
//    @Override
//    protected void defineSynchedData() {
//        super.defineSynchedData();
//        entityData.define(IS_ON_COOLDOWN,false);
//        entityData.define(ACTIVE,false);
//    }
//
//    @Override
//    public boolean shouldRender(double pX, double pY, double pZ) {
//        return true;
//    }
//
//
//    @Override
//    public boolean shouldRenderAtSqrDistance(double pDistance) {
//        return true;
//    }
//
//    private List<ItemStack> fake = new ArrayList<>();
//
//    @Override
//    public Iterable<ItemStack> getArmorSlots() {
//        return fake;
//    }
//
//    @Override
//    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
//        return ItemStack.EMPTY;
//    }
//
//
//    @Override
//    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
//
//    }
//
//    @Override
//    public Packet<?> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }
//
//
//    @Override
//    public boolean canCollideWith(Entity pEntity) {
//        return false;
//    }
//
//    @Override
//    public void push(double pX, double pY, double pZ) {
//
//    }
//
//    @Override
//    protected void pushEntities() {
//
//    }
//
//    @Override
//    public boolean canBeCollidedWith() {
//        return false;
//    }
//
//
//}
