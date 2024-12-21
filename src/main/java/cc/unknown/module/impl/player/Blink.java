package cc.unknown.module.impl.player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.netty.PacketEvent;
import cc.unknown.event.impl.player.TickEvent;
import cc.unknown.module.impl.Module;
import cc.unknown.module.impl.api.Category;
import cc.unknown.module.impl.api.ModuleInfo;
import cc.unknown.utils.network.PacketUtil;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager.InboundHandlerTuplePacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

@SuppressWarnings("all")
@ModuleInfo(name = "Blink", category = Category.Player)
public class Blink extends Module {
	private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
	
	@Override
	public void onEnable() {
		packets.clear();
	}

	@Override
	public void onDisable() {
		for (Packet packet : packets) {
			mc.getNetHandler().getNetworkManager().outboundPacketsQueue.add(new InboundHandlerTuplePacketListener(packet, (GenericFutureListener) null));
		}
		packets.clear();
	}

	@EventLink
	public void onTickPost(TickEvent.Post event) {
		if (mc.thePlayer == null) return;
		while (!packets.isEmpty()) {
			Packet packet = packets.get(0);

			if (packet instanceof S32PacketConfirmTransaction) {
				S32PacketConfirmTransaction transaction = (S32PacketConfirmTransaction) packet;
				PacketUtil.sendPacketNoEvent(new C0FPacketConfirmTransaction(transaction.getWindowId(), transaction.getActionNumber(), false));
			} else if (packet instanceof S00PacketKeepAlive) {
				S00PacketKeepAlive keepAlive = (S00PacketKeepAlive) packet;
				PacketUtil.sendPacketNoEvent(new C00PacketKeepAlive(keepAlive.func_149134_c()));
			} else if (packet instanceof C03PacketPlayer) {
				break;
			}

			PacketUtil.sendPacketNoEvent(packets.get(0));
			packets.remove(packets.get(0));
		}
	}
	
	@EventLink
	public void onPacket(PacketEvent event) {
		if (event.isSend()) {
			packets.add(event.getPacket());
			event.setCancelled(true);
		} else if (event.isReceive()) {
			if (event.getPacket() instanceof S18PacketEntityTeleport || event.getPacket() instanceof S14PacketEntity
					|| event.getPacket() instanceof S14PacketEntity.S15PacketEntityRelMove
					|| event.getPacket() instanceof S14PacketEntity.S16PacketEntityLook
					|| event.getPacket() instanceof S14PacketEntity.S17PacketEntityLookMove) {
				return;
			}
		}
	}
}
