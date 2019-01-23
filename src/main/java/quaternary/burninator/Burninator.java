package quaternary.burninator;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
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
	
	private static Set<ResourceLocation> entityIds = new HashSet<>();
	private static EnumListMode entityMode = EnumListMode.IGNORED;
	
	private static ResourceLocation FAKE_PLAYER_RESOURCELOCATION = new ResourceLocation("minecraft", "player");
	private static ResourceLocation UNKNOWN_RESOURCELOCATION = new ResourceLocation(MODID, "unknown");
	
	@Mod.EventHandler
	public static void postinit(FMLPostInitializationEvent e) {
		readConfig();
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
			!EnchantmentHelper.hasFrostWalkerEnchantment(ent) &&
			(
				entityMode == EnumListMode.IGNORED ||
				((entityMode == EnumListMode.WHITELIST) == entityIds.contains(getEntityResloc(ent)))
			)
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
	
	private static ResourceLocation getEntityResloc(Entity ent) {
		//Guess who doesn't *actually* have an entity id
		if(ent instanceof EntityPlayer) {
			return FAKE_PLAYER_RESOURCELOCATION;
		} else {
			EntityEntry yeet = EntityRegistry.getEntry(ent.getClass());
			return yeet == null ? UNKNOWN_RESOURCELOCATION : yeet.getRegistryName();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	
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
		
		@Config.Name("Entity List")
		@Config.Comment({
			"A list of entity IDs, like 'minecraft:horse' (as in /summon).",
			"Players are 'minecraft:player'.",
			"Also see 'Entity List Mode' for how to make this list actually have an effect on the game!"
		})
		public static String[] entityList = new String[0];
		
		@Config.Name("Entity List Mode")
		@Config.Comment({
			"How the game will handle the entity list.",
			"IGNORED: All entities will burn.",
			"WHITELIST: Only selected entities will burn on the blocks you choose.",
			"BLACKLIST: All entities except the selected entities will burn on the blocks you choose."
		})
		public static EnumListMode listMode = EnumListMode.IGNORED;
	}
	
	@SubscribeEvent
	public static void configChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if(e.getModID().equals(MODID)) {
			ConfigManager.sync(MODID, Config.Type.INSTANCE);
			readConfig();
		}
	}
	
	private static void readConfig() {
		blah(hotBlocks, ModConfig.hotBlocks);
		blah(semiHotBlocks, ModConfig.semiHotBlocks);
		semiHotBlocks.addAll(hotBlocks);
		
		entityIds.clear();
		
		for(String s : ModConfig.entityList) {
			ResourceLocation res = new ResourceLocation(s);
			if(FAKE_PLAYER_RESOURCELOCATION.equals(res) || ForgeRegistries.ENTITIES.containsKey(res)) {
				entityIds.add(res);
			} else {
				LOGGER.warn("No entity found with the name '{}', skipping", s);
			}
		}
		
		entityMode = ModConfig.listMode;
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
	
	public enum EnumListMode {
		IGNORED,
		WHITELIST,
		@SuppressWarnings("unused")
		BLACKLIST
	}
}