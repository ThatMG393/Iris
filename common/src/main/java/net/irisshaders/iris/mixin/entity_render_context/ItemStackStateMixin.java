package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.mixinterface.ItemContextState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.class)
public class ItemStackStateMixin implements ItemContextState {
	@Unique
	private ItemStack iris_displayStack;

	@Override
	public void setDisplayStack(ItemStack itemStack) {
		this.iris_displayStack = itemStack;
	}

	@Override
	public ItemStack getDisplayStack() {
		return iris_displayStack;
	}

	@Inject(method = "clear", at = @At("HEAD"))
	private void clearDisplayStack(CallbackInfo ci) {
		this.iris_displayStack = null;
	}
}
