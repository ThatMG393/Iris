package net.irisshaders.iris.mixin;

import net.irisshaders.iris.NeoLambdas;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderType.class)
public class MixinRenderType {
	@Unique
	private static final RenderStateShard.ShaderStateShard TRANSLUCENT_PARTICLE_SHADER = new RenderStateShard.ShaderStateShard(ShaderAccess.TRANSLUCENT_PARTICLE_SHADER);
	private static final RenderStateShard.ShaderStateShard WEATHER_SHADER = new RenderStateShard.ShaderStateShard(ShaderAccess.WEATHER_SHADER);

	@Redirect(method = {"method_65225", NeoLambdas.NEO_PARTICLE }, require = 1, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderType;PARTICLE_SHADER:Lnet/minecraft/client/renderer/RenderStateShard$ShaderStateShard;"))
	private static RenderStateShard.ShaderStateShard getTranslucentParticleShader() {
		return TRANSLUCENT_PARTICLE_SHADER;
	}

	@Redirect(method = {"method_65228", NeoLambdas.NEO_WEATHER_TYPE }, require = 1, at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderType;PARTICLE_SHADER:Lnet/minecraft/client/renderer/RenderStateShard$ShaderStateShard;"))
	private static RenderStateShard.ShaderStateShard getWeatherShader() {
		return WEATHER_SHADER;
	}
}
