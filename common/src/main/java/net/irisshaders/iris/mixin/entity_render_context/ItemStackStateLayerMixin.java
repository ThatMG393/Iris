package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.mixinterface.ItemContextState;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SolidBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class ItemStackStateLayerMixin {
	@Shadow(aliases = "field_55345")
	private ItemStackRenderState parentState;

	@Inject(method = "render", at = @At("HEAD"))
	private void onRender(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci, @Share("lastBState") LocalIntRef ref) {
		ref.set(CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
		iris$setupId(((ItemContextState) parentState).getDisplayStack());
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void onRenderEnd(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci, @Share("lastBState") LocalIntRef ref) {
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(ref.get());
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
	}

	@Unique
	private void iris$setupId(ItemStack pItemRenderer0) {
		if (WorldRenderingSettings.INSTANCE.getItemIds() == null) return;

		if (pItemRenderer0.getItem() instanceof BlockItem blockItem && !(pItemRenderer0.getItem() instanceof SolidBucketItem)) {
			if (WorldRenderingSettings.INSTANCE.getBlockStateIds() == null) return;

			CapturedRenderingState.INSTANCE.setCurrentBlockEntity(1);

			//System.out.println(WorldRenderingSettings.INSTANCE.getBlockStateIds().getInt(blockItem.getBlock().defaultBlockState()));
			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getBlockStateIds().getOrDefault(blockItem.getBlock().defaultBlockState(), 0));
		} else {
			ResourceLocation location = BuiltInRegistries.ITEM.getKey(pItemRenderer0.getItem());

			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getItemIds().applyAsInt(new NamespacedId(location.getNamespace(), location.getPath())));
		}
	}
}
