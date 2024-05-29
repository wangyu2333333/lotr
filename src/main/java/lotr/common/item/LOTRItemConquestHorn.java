package lotr.common.item;

import java.util.List;

import cpw.mods.fml.relauncher.*;
import lotr.common.*;
import lotr.common.entity.*;
import lotr.common.fac.*;
import lotr.common.world.spawning.LOTRInvasions;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class LOTRItemConquestHorn extends Item {
	@SideOnly(value = Side.CLIENT)
	public IIcon baseIcon;
	@SideOnly(value = Side.CLIENT)
	public IIcon overlayIcon;

	public LOTRItemConquestHorn() {
		setMaxStackSize(1);
		setCreativeTab(LOTRCreativeTabs.tabCombat);
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag) {
		LOTRInvasions type = LOTRItemConquestHorn.getInvasionType(itemstack);
		list.add(type.invasionName());
	}

	public boolean canUseHorn(ItemStack itemstack, World world, EntityPlayer entityplayer, boolean sendMessage) {
		if (LOTRDimension.getCurrentDimensionWithFallback(world) == LOTRDimension.UTUMNO) {
			if (sendMessage && !world.isRemote) {
				entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.conquestHornProtected"));
			}
			return false;
		}
		LOTRInvasions invasionType = LOTRItemConquestHorn.getInvasionType(itemstack);
		LOTRFaction invasionFaction = invasionType.invasionFaction;
		float alignmentRequired = 1500.0f;
		if (LOTRLevelData.getData(entityplayer).getAlignment(invasionFaction) >= alignmentRequired) {
			boolean blocked = false;
			if (LOTRBannerProtection.isProtected(world, entityplayer, LOTRBannerProtection.forFaction(invasionFaction), false)) {
				blocked = true;
			}
			if (LOTREntityNPCRespawner.isSpawnBlocked(entityplayer, invasionFaction)) {
				blocked = true;
			}
			if (blocked) {
				if (sendMessage && !world.isRemote) {
					entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.conquestHornProtected", invasionFaction.factionName()));
				}
				return false;
			}
			return true;
		}
		if (sendMessage && !world.isRemote) {
			LOTRAlignmentValues.notifyAlignmentNotHighEnough(entityplayer, alignmentRequired, invasionType.invasionFaction);
		}
		return false;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public int getColorFromItemStack(ItemStack itemstack, int pass) {
		if (pass == 0) {
			LOTRFaction faction = LOTRItemConquestHorn.getInvasionType(itemstack).invasionFaction;
			return faction.getFactionColor();
		}
		return 16777215;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int i, int pass) {
		return pass > 0 ? overlayIcon : baseIcon;
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		LOTRInvasions type = LOTRItemConquestHorn.getInvasionType(itemstack);
		if (type != null) {
			return StatCollector.translateToLocal(type.codeNameHorn());
		}
		return super.getItemStackDisplayName(itemstack);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack) {
		return EnumAction.bow;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemstack) {
		return 40;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (LOTRInvasions type : LOTRInvasions.values()) {
			ItemStack itemstack = new ItemStack(item);
			LOTRItemConquestHorn.setInvasionType(itemstack, type);
			list.add(itemstack);
		}
	}

	@Override
	public ItemStack onEaten(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		LOTRInvasions invasionType = LOTRItemConquestHorn.getInvasionType(itemstack);
		if (canUseHorn(itemstack, world, entityplayer, true)) {
			if (!world.isRemote) {
				LOTREntityInvasionSpawner invasion = new LOTREntityInvasionSpawner(world);
				invasion.setInvasionType(invasionType);
				invasion.isWarhorn = true;
				invasion.spawnsPersistent = true;
				invasion.setLocationAndAngles(entityplayer.posX, entityplayer.posY + 3.0, entityplayer.posZ, 0.0f, 0.0f);
				world.spawnEntityInWorld(invasion);
				invasion.startInvasion(entityplayer);
			}
			if (!entityplayer.capabilities.isCreativeMode) {
				--itemstack.stackSize;
			}
		}
		return itemstack;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer) {
		canUseHorn(itemstack, world, entityplayer, false);
		entityplayer.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
		return itemstack;
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public void registerIcons(IIconRegister iconregister) {
		baseIcon = iconregister.registerIcon(getIconString() + "_base");
		overlayIcon = iconregister.registerIcon(getIconString() + "_overlay");
	}

	@Override
	@SideOnly(value = Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	public static ItemStack createHorn(LOTRInvasions type) {
		ItemStack itemstack = new ItemStack(LOTRMod.conquestHorn);
		LOTRItemConquestHorn.setInvasionType(itemstack, type);
		return itemstack;
	}

	public static LOTRInvasions getInvasionType(ItemStack itemstack) {
		String s;
		LOTRInvasions invasionType = null;
		if (itemstack.getTagCompound() != null && itemstack.getTagCompound().hasKey("InvasionType")) {
			s = itemstack.getTagCompound().getString("InvasionType");
			invasionType = LOTRInvasions.forName(s);
		}
		if (invasionType == null && itemstack.getTagCompound() != null && itemstack.getTagCompound().hasKey("HornFaction")) {
			s = itemstack.getTagCompound().getString("HornFaction");
			invasionType = LOTRInvasions.forName(s);
		}
		if (invasionType == null) {
			invasionType = LOTRInvasions.HOBBIT;
		}
		return invasionType;
	}

	public static void setInvasionType(ItemStack itemstack, LOTRInvasions type) {
		if (itemstack.getTagCompound() == null) {
			itemstack.setTagCompound(new NBTTagCompound());
		}
		itemstack.getTagCompound().setString("InvasionType", type.codeName());
	}
}
