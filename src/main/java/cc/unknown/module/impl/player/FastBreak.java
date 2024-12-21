package cc.unknown.module.impl.player;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.move.PreUpdateEvent;
import cc.unknown.module.impl.Module;
import cc.unknown.module.impl.api.Category;
import cc.unknown.module.impl.api.ModuleInfo;
import cc.unknown.module.setting.impl.SliderValue;

@ModuleInfo(name = "FastBreak", category = Category.Player)
public class FastBreak extends Module {
    private final SliderValue speed = new SliderValue("Speed", 50, 0, 100, 1);

    public FastBreak() {
    	this.registerSetting(speed);
    }
    
	@EventLink
	public void onPreUpdate(PreUpdateEvent event) {
        mc.playerController.blockHitDelay = 0;
        
        if (mc.playerController.curBlockDamageMP > 1 - speed.getInput() / 100f && mc.playerController.curBlockDamageMP < 0.99f) {
            mc.playerController.curBlockDamageMP = 0.99f;
        }
	}

}
