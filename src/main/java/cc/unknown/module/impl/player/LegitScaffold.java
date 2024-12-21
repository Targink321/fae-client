package cc.unknown.module.impl.player;

import org.lwjgl.input.Keyboard;

import cc.unknown.event.impl.EventLink;
import cc.unknown.event.impl.move.MoveInputEvent;
import cc.unknown.event.impl.move.PreMotionEvent;
import cc.unknown.event.impl.other.ClickGuiEvent;
import cc.unknown.event.impl.render.RenderEvent;
import cc.unknown.module.impl.Module;
import cc.unknown.module.impl.api.Category;
import cc.unknown.module.impl.api.ModuleInfo;
import cc.unknown.module.setting.impl.BooleanValue;
import cc.unknown.module.setting.impl.DoubleSliderValue;
import cc.unknown.module.setting.impl.SliderValue;
import cc.unknown.utils.client.Cold;
import cc.unknown.utils.player.PlayerUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;

@ModuleInfo(name = "LegitScaffold", category = Category.Player)
public class LegitScaffold extends Module {
	private SliderValue shiftTime = new SliderValue("Shift Time", 140, 5, 200, 5);
	private SliderValue shiftMutiplier = new SliderValue("Shift speed multiplier", 0.3, 0.2, 1, 0.05);
    private BooleanValue pitchCheck = new BooleanValue("Pitch Check", false);
	private DoubleSliderValue pitchRange = new DoubleSliderValue("Pitch Range", 70, 85, 0, 90, 1);
    private BooleanValue onlyGround = new BooleanValue("Only Ground", false);
    private BooleanValue holdShift = new BooleanValue("Hold Shift", false);
    private BooleanValue slotSwap = new BooleanValue("Block Switching", true);
    private BooleanValue blocksOnly = new BooleanValue("Blocks Only", true);
    private BooleanValue backwards = new BooleanValue("Backwards Movement Only", true);

    private boolean shouldBridge, isShifting = false;
    private int ticks;
    private int lastSlot;
    private Cold shiftTimer = new Cold(0);

	public LegitScaffold() {
		this.registerSetting(shiftTime, shiftMutiplier, pitchCheck, pitchRange, onlyGround, holdShift, slotSwap, blocksOnly, backwards);
	}

	@EventLink
	public void onGui(ClickGuiEvent e) {
		this.setSuffix("- [" + shiftTime.getInputToInt() + " ms]");
	}
	
	@Override
	public void onEnable() {
		lastSlot = -1;		
	}
	
	@Override
	public void onDisable() {
		setSneak(false);
		if (PlayerUtil.playerOverAir()) {
			setSneak(false);
		}

		mc.thePlayer.inventory.currentItem = lastSlot;
		shouldBridge = false;
	}

	@EventLink
	public void onMoveInput(MoveInputEvent e) {
        if (shouldBridge && ticks <= 2) {
            e.setSneakMultiplier(shiftMutiplier.getInput());
        }
	}
	
	@EventLink
	public void onSuicide(PreMotionEvent e) {
        if (mc.currentScreen != null) return;
        if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) return;
        
        if (PlayerUtil.playerOverAir() && (!onlyGround.isToggled() || mc.thePlayer.onGround) && mc.thePlayer.motionY < 0.1) {
        	shiftTimer.reset();
        }
        
        boolean shift = shiftTime.getInputToInt() > 0;
        
		if (blocksOnly.isToggled()) {
			ItemStack i = mc.thePlayer.getHeldItem();
			if (i == null || !(i.getItem() instanceof ItemBlock)) {
				if (isShifting) {
					isShifting = false;
					setSneak(false);
				}
				return;
			}
		}
        
        if (backwards.isToggled() && (mc.thePlayer.movementInput.moveForward > 0) && (mc.thePlayer.movementInput.moveStrafe == 0) || mc.thePlayer.movementInput.moveForward >= 0) {
            shouldBridge = false;
            return;
        }
        
		if (pitchCheck.isToggled() && mc.thePlayer.rotationPitch < pitchRange.getInputMinToFloat() || mc.thePlayer.rotationPitch > pitchRange.getInputMaxToFloat()) {
			shouldBridge = false;
			if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
				shouldBridge = true;
			}
			return;
		}
        
        if (mc.thePlayer.onGround) {
			if (PlayerUtil.playerOverAir()) {
				if (shift) {
					shiftTimer.setMs(randomInt(shiftTime.getInputToInt(),
							(int) (shiftTime.getInputToInt() + 0.1)));
					shiftTimer.reset();
				}

				isShifting = true;
				setSneak(true);
				shouldBridge = true;
			} else if (mc.thePlayer.isSneaking() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())
					&& holdShift.isToggled()) {
				isShifting = false;
				shouldBridge = false;
				setSneak(false);
			} else if (holdShift.isToggled() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
				isShifting = false;
				shouldBridge = false;
				setSneak(false);
			} else if (mc.thePlayer.isSneaking()
					&& (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) && holdShift.isToggled())
					&& (!shift || shiftTimer.hasFinished())) {
				isShifting = false;
				setSneak(false);
				shouldBridge = true;
			} else if (mc.thePlayer.isSneaking() && !holdShift.isToggled() && (!shift || shiftTimer.hasFinished())) {
				isShifting = false;
				setSneak(false);
				shouldBridge = true;
			}
		} else if (shouldBridge && mc.thePlayer.capabilities.isFlying) {
			setSneak(false);
			shouldBridge = false;
		} else if (shouldBridge && PlayerUtil.playerOverAir()) {
			isShifting = true;
			setSneak(true);
		} else {
			isShifting = false;
			setSneak(false);
		}

	}
	
	@EventLink
	public void onRender(RenderEvent e) {
		if (!PlayerUtil.inGame() && !e.is3D()) return;
		
        if (lastSlot == -1) {
        	lastSlot = mc.thePlayer.inventory.currentItem;
        }
        
		final int slot = PlayerUtil.findBlock();
		
        if (slot == -1) {
            return;
        }
        
        if (slotSwap.isToggled() && shouldSkipBlockCheck()) {
        	mc.thePlayer.inventory.currentItem = slot;
        }

		if (mc.currentScreen == null || mc.thePlayer.getHeldItem() == null) return;
	}
	
	private boolean shouldSkipBlockCheck() {
		ItemStack heldItem = mc.thePlayer.getHeldItem();
		return heldItem == null || !(heldItem.getItem() instanceof ItemBlock);
	}
	
	private void setSneak(boolean sneak) {
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), sneak);		
	}
	
	private int randomInt(int x, int v) {
		return (int) (Math.random() * (x - v) + v);
	}
}
