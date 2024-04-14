package cc.unknown.module.impl.combat;

import static cc.unknown.utils.helpers.MathHelper.randomInt;

import java.util.function.Supplier;
import java.util.stream.Stream;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.move.LivingUpdateEvent;
import cc.unknown.event.impl.other.ClickGuiEvent;
import cc.unknown.event.impl.player.StrafeEvent;
import cc.unknown.module.impl.Module;
import cc.unknown.module.impl.api.Category;
import cc.unknown.module.impl.api.Register;
import cc.unknown.module.setting.impl.DoubleSliderValue;
import cc.unknown.module.setting.impl.ModeValue;
import cc.unknown.module.setting.impl.SliderValue;
import cc.unknown.utils.player.PlayerUtil;

@Register(name = "JumpReset", category = Category.Combat)
public class JumpReset extends Module {
	private ModeValue mode = new ModeValue("Mode", "Normal", "Reset", "Hit", "Tick", "Normal");
	private SliderValue chance = new SliderValue("Chance", 100, 0, 100, 1);
	private DoubleSliderValue tickTicks = new DoubleSliderValue("Ticks", 3, 4, 0, 20, 1);
	private DoubleSliderValue hitHits = new DoubleSliderValue("Hits", 3, 4, 0, 20, 1);

	private int limit = 0;
	private boolean reset = false;

	public JumpReset() {
		this.registerSetting(mode, chance, tickTicks, hitHits);
	}

	@EventLink
	public void onGui(ClickGuiEvent e) {
		this.setSuffix(mode.getMode());
	}

	@EventLink
	public void onLiving(LivingUpdateEvent e) {
		if (PlayerUtil.inGame()) {
			if (mode.is("Tick") || mode.is("Hit")) {
				double packetDirection = Math.atan2(mc.thePlayer.motionX, mc.thePlayer.motionZ);
				double degreePlayer = PlayerUtil.getDirection();
				double degreePacket = Math.floorMod((int) Math.toDegrees(packetDirection), 360);
				double angle = Math.abs(degreePacket + degreePlayer);
				double threshold = 120.0;
				angle = Math.floorMod((int) angle, 360);
				boolean inRange = angle >= 180 - threshold / 2 && angle <= 180 + threshold / 2;
				if (inRange) {
					reset = true;
				}
			}

			if (mode.is("Reset")) {
				if (mc.thePlayer.hurtTime >= 1 && mc.thePlayer.hurtTime < 6) {
					double multi = 1.2224324, min = 0.1, max = 0.5;
					if ((Math.abs(mc.thePlayer.motionX) > min && Math.abs(mc.thePlayer.motionZ) > min) && (Math.abs(mc.thePlayer.motionX) < max && Math.abs(mc.thePlayer.motionZ) < max)) {
						mc.thePlayer.motionX /= multi;
						mc.thePlayer.motionZ /= multi;
					}
				}
			}
			
			if (mode.is("Normal") && mc.thePlayer.fallDistance > 2.5F) {
                if(mc.currentScreen == null) {
                    if (mc.thePlayer.hurtTime >= 8) {
                        mc.gameSettings.keyBindJump.pressed = mc.thePlayer.isSprinting();
                        if(mc.thePlayer.hurtTime == 8) {
                            mc.gameSettings.keyBindJump.pressed = false;
                        }
                    }
                }
			}
		}
	}

	@EventLink
	public void onStrafe(StrafeEvent e) {
		if (PlayerUtil.inGame()) {
			if (checkLiquids() || !applyChance())
				return;

			if (mode.is("Ticks") || mode.is("Hits") && reset) {
				if (!mc.gameSettings.keyBindJump.pressed && shouldJump() && mc.thePlayer.isSprinting()
						&& mc.thePlayer.hurtTime == 9 && mc.thePlayer.fallDistance > 2.5F) {
					mc.gameSettings.keyBindJump.pressed = true;
					limit = 0;
				}
				reset = false;
				return;
			}

			switch (mode.getMode()) {
			case "Ticks": {
				limit++;
			}
				break;

			case "Hits": {
				if (mc.thePlayer.hurtTime == 9) {
					limit++;
				}
			}
				break;
			}
		}
	}

	private boolean shouldJump() {
		switch (mode.getMode()) {
		case "Ticks": {
			return limit >= randomInt(tickTicks.getInputMinToInt(), tickTicks.getInputMaxToInt() + 0.1);
		}
		case "Hits": {
			return limit >= randomInt(hitHits.getInputMinToInt(), hitHits.getInputMaxToInt() + 0.1);
		}
		default:
			return false;
		}
	}

	private boolean checkLiquids() {
		if (mc.thePlayer == null || mc.theWorld == null) {
			return false;
		}
		return Stream.<Supplier<Boolean>>of(mc.thePlayer::isInLava, mc.thePlayer::isBurning, mc.thePlayer::isInWater,
				() -> mc.thePlayer.isInWeb).map(Supplier::get).anyMatch(Boolean.TRUE::equals);
	}

	private boolean applyChance() {
		Supplier<Boolean> chanceCheck = () -> {
			return chance.getInput() != 100.0D && Math.random() >= chance.getInput() / 100.0D;
		};

		return Stream.of(chanceCheck).map(Supplier::get).anyMatch(Boolean.TRUE::equals);
	}
}
