package agency.highlysuspect.burninator.mixin;

import agency.highlysuspect.burninator.Init;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(
		method = "move",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void uwuSteppies(MovementType no, Vec3d noo, CallbackInfo nooo, Vec3d noooo, BlockPos nooooo, BlockState state) {
		Entity thi$ = (Entity) (Object) this;
		if(thi$.world.isClient) return;
		
		//IJ's static analysis makes a mistake here. It's smart enough to see through the double-cast trick.
		//It thinks thi$ is never an instance of LivingEntity, since it's clearly an instance of EntityMixin.
		//But it's not smart enough to see through Mixin and realize that this is a false assumption.
		//noinspection ConstantConditions
		if(!(thi$ instanceof LivingEntity)) return;
		
		EntityType<?> type = thi$.getType();
		
		if(!Init.ONLY_ENTITIES.values().isEmpty() && !type.isIn(Init.ONLY_ENTITIES)) return;
		if(type.isIn(Init.IGNORED_ENTITIES)) return;
		
		LivingEntity living = (LivingEntity) thi$;
		if(living.isFireImmune() || EnchantmentHelper.hasFrostWalker(living)) return;
		
		if(state.isIn(Init.VERY_HOT) || (living.isSneaking() && state.isIn(Init.HOT)) || (!living.isSneaking() && state.isIn(Init.REVERSE_HOT))) {
			living.damage(DamageSource.HOT_FLOOR, 1);
		}
	}
}
