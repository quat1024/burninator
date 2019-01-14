package quaternary.dangerousmagma;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = DangerousMagma.MODID, name = DangerousMagma.NAME, version = DangerousMagma.VERSION)
@Mod.EventBusSubscriber
public class DangerousMagma {
	public static final String MODID = "dangerousmagma";
	public static final String NAME = "Dangerous Magma";
	public static final String VERSION = "GRADLE:VERSION";
	
	@SubscribeEvent
	public static void playertick(TickEvent.PlayerTickEvent e) {
		EntityPlayer player = e.player;
		World world = player.world;
		//Return on remote world, if the player is not sneaking, or if the player is flying or riding
		//(since vanilla logic handles the non-sneaking case and ignores flying and riding players)
		if(world.isRemote || !player.isSneaking() || player.capabilities.isFlying || player.isRiding()) return;
		
		//Basically just duplicate the Entity#move logic that leads to Block#onEntityWalk
		BlockPos belowPos = new BlockPos(
			MathHelper.floor(player.posX),
			MathHelper.floor(player.posY - 0.2),
			MathHelper.floor(player.posZ)
		);
		
		Block block = world.getBlockState(belowPos).getBlock();
		
		if(block == Blocks.MAGMA) {
			//Damages the player
			block.onEntityWalk(world, belowPos, player);
		}
	}
}