package quaternary.burninator;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Mod(modid = Burninator.MODID, name = Burninator.NAME, version = Burninator.VERSION)
@Mod.EventBusSubscriber
public class Burninator {
	public static final String MODID = "burninator";
	public static final String NAME = "Burninator";
	public static final String VERSION = "GRADLE:VERSION";
	public static final Logger LOGGER = LogManager.getLogger(NAME);
	
	private static Set<Block> hotBlocks = new HashSet<>();
	private static Set<Block> semiHotBlocks = new HashSet<>();
	
	@Mod.EventHandler
	public static void postinit(FMLPostInitializationEvent e) {
		updateBlockLists();
	}
	
	@SubscribeEvent
	public static void tick(TickEvent.WorldTickEvent e) {
		World world = e.world;
		if(world.isRemote) return;
		
		world.profiler.startSection(MODID);
		
		List<EntityLivingBase> entitiesToDamage = new ArrayList<>();
		
		Iterator<EntityLivingBase> enterator = e.world.getEntities(EntityLivingBase.class, (ent) -> 
			ent != null &&
			ent.onGround &&
			ent.isEntityAlive() &&
			!ent.isRiding() &&
			!ent.isImmuneToFire() &&
			!EnchantmentHelper.hasFrostWalkerEnchantment(ent)
		).iterator();
		
		while(enterator.hasNext()) {
			EntityLivingBase ent = enterator.next();
			
			Collection<Block> blocksToCheck = ent.isSneaking() ? hotBlocks : semiHotBlocks;
			
			//Basically just duplicate the Entity#move logic that leads to Block#onEntityWalk for magma blocks etc
			BlockPos belowPos = new BlockPos(
				MathHelper.floor(ent.posX),
				MathHelper.floor(ent.posY - 0.2),
				MathHelper.floor(ent.posZ)
			);
			
			Block block = world.getBlockState(belowPos).getBlock();
			
			if(blocksToCheck.contains(block)) {
				//Don't damage them now - if the entity dies from this attack, it can CME. Defer it.
				entitiesToDamage.add(ent);
			}
		}
		
		for(EntityLivingBase ent : entitiesToDamage) {
			ent.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
		}
		
		world.profiler.endSection();
	}
	
	@Config(modid = MODID)
	public static class ModConfig {
		@Config.Name("Hot Blocks")
		@Config.Comment("Blocks that burn entities that touch them (always)")
		public static String[] hotBlocks = new String[] {
			"minecraft:magma"
		};
		
		@Config.Name("Semi Hot Blocks")
		@Config.Comment("Blocks that burn entities that touch them (when they are not sneaking). Like magma blocks in vanilla.")
		public static String[] semiHotBlocks = new String[0];
	}
	
	@SubscribeEvent
	public static void configChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if(e.getModID().equals(MODID)) {
			ConfigManager.sync(MODID, Config.Type.INSTANCE);
			updateBlockLists();
		}
	}
	
	private static void updateBlockLists() {
		hotBlocks.clear();
		semiHotBlocks.clear();
		
		blah(hotBlocks, ModConfig.hotBlocks);
		blah(semiHotBlocks, ModConfig.semiHotBlocks);
		
		semiHotBlocks.addAll(hotBlocks);
	}
	
	private static void blah(Collection<Block> blockList, String[] resourceStrings) {
		blockList.clear();
		
		for(String s : resourceStrings) {
			ResourceLocation res = new ResourceLocation(s);
			if(ForgeRegistries.BLOCKS.containsKey(res)) {
				blockList.add(ForgeRegistries.BLOCKS.getValue(res));
			} else {
				LOGGER.warn("No block found with the name '{}', skipping", s);
			}
		}
	}
}