package agency.highlysuspect.burninator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class Init implements ModInitializer {
	public static final String MODID = "burninator";
	
	public static final Tag<Block> VERY_HOT = TagRegistry.block(new Identifier(MODID, "very_hot"));
	public static final Tag<Block> HOT = TagRegistry.block(new Identifier(MODID, "hot"));
	public static final Tag<Block> REVERSE_HOT = TagRegistry.block(new Identifier(MODID, "reverse_hot"));
	
	public static final Tag<EntityType<?>> IGNORED_ENTITIES = TagRegistry.entityType(new Identifier(MODID, "ignored_entities"));
	public static final Tag<EntityType<?>> ONLY_ENTITIES = TagRegistry.entityType(new Identifier(MODID, "only_entities"));
	
	@Override
	public void onInitialize() {
		//Does nothing on its own, but those tags ^ get registered in the static initializer.
	}
}
