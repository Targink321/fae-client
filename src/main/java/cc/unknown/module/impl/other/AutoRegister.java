package cc.unknown.module.impl.other;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.network.PacketEvent;
import cc.unknown.event.impl.player.TickEvent;
import cc.unknown.module.impl.Module;
import cc.unknown.module.impl.api.Category;
import cc.unknown.module.impl.api.Register;
import cc.unknown.utils.client.Cold;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import org.apache.commons.lang3.StringUtils;

@Register(name = "AutoRegister", category = Category.Other)
public class AutoRegister extends Module {

    private String text;
    private final Cold cold;

    public AutoRegister() {
        cold = new Cold();
    }

    @EventLink
    public void onPacket (PacketEvent event) {
        final Packet packet = event.getPacket();
        if (packet instanceof S02PacketChat) {
            final S02PacketChat s02PacketChat = (S02PacketChat)packet;
            String text = s02PacketChat.getChatComponent().getUnformattedText();
            if (StringUtils.containsIgnoreCase(text, "/register") || StringUtils.containsIgnoreCase(text, "/register password password") || text.equalsIgnoreCase("/register <password> <password>")) {
                this.text = "/register DglaMaska13 DglaMaska13";
                cold.reset();
            }
            else if (StringUtils.containsIgnoreCase(text, "/login password") || StringUtils.containsIgnoreCase(text, "/login") || text.equalsIgnoreCase("/login <password>")) {
                this.text = "/login DglaMaska13";
                cold.reset();
            }
        }
    }

    @EventLink
    public void onTick (TickEvent event) {
        if (cold.reached(1500L) && text != null && !text.equals("")) {
            mc.thePlayer.sendChatMessage(text);
            System.out.println(text);
            text = "";
        }
    }

}
