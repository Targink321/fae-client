package cc.unknown.utils.network.cpackets;

import lombok.Getter;
import net.minecraft.network.play.client.C00PacketKeepAlive;

@Getter
public class CPacketKeepAlive extends C00PacketKeepAlive {

	private final long time;
	
    public CPacketKeepAlive(final int key, final long time) {
        super(key);
        this.time = time;
    }
}
