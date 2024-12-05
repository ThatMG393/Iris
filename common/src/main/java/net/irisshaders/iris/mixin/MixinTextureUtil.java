package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.platform.TextureUtil;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureUtil.class)
public class MixinTextureUtil {
	@Inject(method = "generateTextureId", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V", shift = At.Shift.AFTER), cancellable = true)
	private static void generateTextureId(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(IrisRenderSystem.createTexture(GL46C.GL_TEXTURE_2D));
	}
}
