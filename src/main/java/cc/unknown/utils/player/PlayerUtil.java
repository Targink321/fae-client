package cc.unknown.utils.player;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Mouse;

import cc.unknown.utils.Loona;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

@UtilityClass
public class PlayerUtil implements Loona {

    public final List<Block> blacklist = Arrays.asList(Blocks.enchanting_table, Blocks.chest, Blocks.ender_chest,
            Blocks.trapped_chest, Blocks.anvil, Blocks.sand, Blocks.web, Blocks.torch,
            Blocks.crafting_table, Blocks.furnace, Blocks.waterlily, Blocks.dispenser,
            Blocks.stone_pressure_plate, Blocks.wooden_pressure_plate, Blocks.noteblock,
            Blocks.dropper, Blocks.tnt, Blocks.standing_banner, Blocks.wall_banner, Blocks.redstone_torch);
	
	public void send(final Object message, final Object... objects) {
		if (inGame()) {
			final String format = String.format(message.toString(), objects);
			mc.thePlayer.addChatMessage(new ChatComponentText("" + format));
		}
	}

	public boolean inGame() {
		return mc.thePlayer != null && mc.theWorld != null;
	}

	public boolean isMoving() {
		return mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F;
	}

	public boolean tryingToCombo() {
		return Mouse.isButtonDown(0) && Mouse.isButtonDown(1);
	}

	public boolean lookingAtPlayer(EntityPlayer v, EntityPlayer e, double m) {
		double deltaX = e.posX - v.posX;
		double deltaY = e.posY - v.posY + v.getEyeHeight();
		double deltaZ = e.posZ - v.posZ;
		double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		return distance < m;
	}

	public double fovFromEntity(Entity en) {
		return ((double) (mc.thePlayer.rotationYaw - fovToEntity(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
	}
	

    public double PitchFromEntity(Entity en, float f) {
        return (double) (mc.thePlayer.rotationPitch - pitchToEntity(en, f));
    }

    public float pitchToEntity(Entity ent, float f) {
        double x = mc.thePlayer.getDistanceToEntity(ent);
        double y = mc.thePlayer.posY - (ent.posY + f);
        double pitch = (((Math.atan2(x, y) * 180.0D) / Math.PI));
        return (float) (90 - pitch);
    }

	public float fovToEntity(Entity ent) {
		double x = ent.posX - mc.thePlayer.posX;
		double z = ent.posZ - mc.thePlayer.posZ;
		double yaw = Math.atan2(x, z) * 57.2957795D;
		return (float) (yaw * -1.0D);
	}

	public boolean fov(Entity entity, float fov) {
		fov = (float) ((double) fov * 0.5D);
		double v = ((double) (mc.thePlayer.rotationYaw - fovToEntity(entity)) % 360.0D + 540.0D) % 360.0D - 180.0D;
		return v > 0.0D && v < (double) fov || (double) (-fov) < v && v < 0.0D;
	}

	public boolean playerOverAir() {
		return mc.theWorld.isAirBlock(new BlockPos(MathHelper.floor_double(mc.thePlayer.posX),
				MathHelper.floor_double(mc.thePlayer.posY - 1.0D), MathHelper.floor_double(mc.thePlayer.posZ)));
	}

	public boolean isBlockUnder(int offset) {
		for (int i = (int) (mc.thePlayer.posY - offset); i > 0; i--) {
			BlockPos pos = new BlockPos(mc.thePlayer.posX, i, mc.thePlayer.posZ);
			if (!(mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir))
				return true;
		}
		return false;
	}

	public boolean isHoldingWeapon() {
		if (mc.thePlayer.getCurrentEquippedItem() == null) {
			return false;
		} else {
			Item item = mc.thePlayer.getCurrentEquippedItem().getItem();
			return item instanceof ItemSword || item instanceof ItemAxe;
		}
	}

	public double getDirection() {
		float moveYaw = mc.thePlayer.rotationYaw;
		if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing == 0f) {
			moveYaw += (mc.thePlayer.moveForward > 0) ? 0 : 180;
		} else if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing != 0f) {
			if (mc.thePlayer.moveForward > 0)
				moveYaw += (mc.thePlayer.moveStrafing > 0) ? -45 : 45;
			else
				moveYaw -= (mc.thePlayer.moveStrafing > 0) ? -45 : 45;
			moveYaw += (mc.thePlayer.moveForward > 0) ? 0 : 180;
		} else if (mc.thePlayer.moveStrafing != 0f && mc.thePlayer.moveForward == 0f) {
			moveYaw += (mc.thePlayer.moveStrafing > 0) ? -90 : 90;
		}
		return Math.floorMod((int) moveYaw, 360);
	}

	public ItemStack getBestSword() {
		int size = mc.thePlayer.inventoryContainer.getInventory().size();
		ItemStack lastSword = null;
		for (int i = 0; i < size; i++) {
			ItemStack stack = mc.thePlayer.inventoryContainer.getInventory().get(i);
			if (stack != null && stack.getItem() instanceof ItemSword)
				if (lastSword == null) {
					lastSword = stack;
				} else if (isBetterSword(stack, lastSword)) {
					lastSword = stack;
				}
		}
		return lastSword;
	}

    public double getFov(final double posX, final double posZ) {
        return getFov(mc.thePlayer.rotationYaw, posX, posZ);
    }

    public double getFov(final float yaw, final double posX, final double posZ) {
        double angle = (yaw - angle(posX, posZ)) % 360.0;
        return MathHelper.wrapAngleTo180_double(angle);
    }
    
    public float angle(final double n, final double n2) {
        return (float) (Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }
    
	public ItemStack getBestAxe() {
		int size = mc.thePlayer.inventoryContainer.getInventory().size();
		ItemStack lastAxe = null;
		for (int i = 0; i < size; i++) {
			ItemStack stack = mc.thePlayer.inventoryContainer.getInventory().get(i);
			if (stack != null && stack.getItem() instanceof ItemAxe)
				if (lastAxe == null) {
					lastAxe = stack;
				} else if (isBetterTool(stack, lastAxe, Blocks.planks)) {
					lastAxe = stack;
				}
		}
		return lastAxe;
	}

	public ItemStack getBestPickaxe() {
		int size = mc.thePlayer.inventoryContainer.getInventory().size();
		ItemStack lastPickaxe = null;
		for (int i = 0; i < size; i++) {
			ItemStack stack = mc.thePlayer.inventoryContainer.getInventory().get(i);
			if (stack != null && stack.getItem() instanceof ItemPickaxe)
				if (lastPickaxe == null) {
					lastPickaxe = stack;
				} else if (isBetterTool(stack, lastPickaxe, Blocks.stone)) {
					lastPickaxe = stack;
				}
		}
		return lastPickaxe;
	}

	public boolean isBetterTool(ItemStack better, ItemStack than, Block versus) {
		return (getToolDigEfficiency(better, versus) > getToolDigEfficiency(than, versus));
	}

	public boolean isBetterSword(ItemStack better, ItemStack than) {
		return (getSwordDamage((ItemSword) better.getItem(), better) > getSwordDamage((ItemSword) than.getItem(),
				than));
	}

	public float getSwordDamage(ItemSword sword, ItemStack stack) {
		float base = sword.getMaxDamage();
		return base + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25F;
	}

	public float getToolDigEfficiency(ItemStack stack, Block block) {
		float f = stack.getStrVsBlock(block);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
			if (i > 0)
				f += (i * i + 1);
		}
		return f;
	}

    public void sendClick(final int button, final boolean state) {
        final int keyBind = button == 0 ? mc.gameSettings.keyBindAttack.getKeyCode() : mc.gameSettings.keyBindUseItem.getKeyCode();

        KeyBinding.setKeyBindState(keyBind, state);

        if (state) {
            KeyBinding.onTick(keyBind);
        }
    }
    
    public int findBlock() {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock &&  blacklist.stream().noneMatch(block -> block.equals(((ItemBlock) itemStack.getItem()).getBlock())) && itemStack.stackSize > 0) {
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }
}
