//package com.qurenie.relics_thirteenflames.net;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.phys.Vec3;
//import org.zeith.hammerlib.net.IPacket;
//import org.zeith.hammerlib.net.MainThreaded;
//import org.zeith.hammerlib.net.PacketContext;
//
//@MainThreaded
//public class SeliasetHornWaveReleasePacket implements IPacket {
//
//    private int id;
//    public SeliasetHornWaveReleasePacket(SeliasetHornEntity entity){
//        this.id = entity.getId();
//    }
//
//
//    @Override
//    public void write(FriendlyByteBuf buf) {
//        IPacket.super.write(buf);
//        buf.writeInt(id);
//    }
//
//    @Override
//    public void read(FriendlyByteBuf buf) {
//        IPacket.super.read(buf);
//        this.id = buf.readInt();
//    }
//
//
//    @Override
//    public void clientExecute(PacketContext ctx) {
//        IPacket.super.clientExecute(ctx);
//        if (Minecraft.getInstance().level.getEntity(id) instanceof SeliasetHornEntity horn){
//            horn.releaseWaveParticles();
//        }
//    }
//}
