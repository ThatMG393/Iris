package net.irisshaders.iris.mixin.texture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.mixinterface.AbstractTextureExtended;
import net.irisshaders.iris.pbr.TextureTracker;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractTexture.class)
public abstract class MixinAbstractTexture implements AbstractTextureExtended {
	@Shadow
	protected int id;

	@Shadow
	public abstract void bind();

	@Shadow
	private int minFilter;

	@Shadow
	private int magFilter;

	@WrapOperation(method = "getId()I", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", remap = false))
	private int iris$afterGenerateId(Operation<Integer> original) {
		int id = original.call();
		TextureTracker.INSTANCE.trackTexture(id, (AbstractTexture) (Object) this);
		return id;
	}

	@Override
	public void setNearestFilter() {
		RenderSystem.assertOnRenderThreadOrInit();
		int min;
		int mag;
		boolean mipmap = minFilter >= 0x2700;
		min = mipmap ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_NEAREST;
		mag = GL11.GL_NEAREST;

		boolean bl3 = this.minFilter != min;
		boolean bl4 = this.magFilter != mag;
		if (bl4 || bl3) {
			this.bind();
			if (bl3) {
				GlStateManager._texParameter(3553, 10241, min);
				this.minFilter = min;
			}

			if (bl4) {
				GlStateManager._texParameter(3553, 10240, mag);
				this.magFilter = mag;
			}
		}
	}
}
