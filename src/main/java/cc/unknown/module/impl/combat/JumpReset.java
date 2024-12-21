package cc.unknown.module.impl.combat;

import java.util.concurrent.ThreadLocalRandom;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.move.MoveInputEvent;
import cc.unknown.event.impl.move.PreMotionEvent;
import cc.unknown.module.impl.Module;
import cc.unknown.module.impl.api.Category;
import cc.unknown.module.impl.api.ModuleInfo;
import cc.unknown.module.setting.impl.BooleanValue;
import cc.unknown.module.setting.impl.SliderValue;
import cc.unknown.utils.player.PlayerUtil;

@ModuleInfo(name = "JumpReset", category = Category.Combat)
public class JumpReset extends Module {

	private final BooleanValue onlyClick = new BooleanValue("Only Click", false);
	private final SliderValue chance = new SliderValue("Chance", 100, 0, 100, 1);
	
	private int ticksSinceVelocity;
	
	public JumpReset() {
		this.registerSetting(onlyClick, chance);
	}
	
	@EventLink
	public void onMoveInput(MoveInputEvent event) {
	    if (shouldSkipUpdate()) return;

	    double chanceValue = chance.getInputToInt();
	    double randomFactor = getRandomFactor(chanceValue);

	    if (!shouldPerformAction(chanceValue, randomFactor)) return;
	    
		if (PlayerUtil.isMoving() && mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround && mc.thePlayer.motionY > 0 && ticksSinceVelocity <= 14) {
			event.setJump(true);
		}
	}
	
	@EventLink
	public void onPreMotion(PreMotionEvent event) {
		if (mc.thePlayer.hurtTime == 9 && mc.thePlayer.motionY / 8000.0D > 0.1 && Math.hypot(mc.thePlayer.motionZ / 8000.0D, mc.thePlayer.motionX / 8000.0D) > 0.2) {
			ticksSinceVelocity++;
		}
	}
	
	private boolean shouldSkipUpdate() {
	    return onlyClick.isToggled() && !mc.thePlayer.isSwingInProgress;
	}

	private double getRandomFactor(double chanceValue) {
	    return Math.abs(Math.sin(System.nanoTime() * Double.doubleToLongBits(chanceValue))) * 100.0;
	}

	private boolean shouldPerformAction(double chanceValue, double randomFactor) {
	    return chanceValue >= 100.0D || ThreadLocalRandom.current().nextDouble(100.0D + randomFactor) < chanceValue;
	}
}
