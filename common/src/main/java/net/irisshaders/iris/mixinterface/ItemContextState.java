package net.irisshaders.iris.mixinterface;

import net.minecraft.world.item.ItemStack;

public interface ItemContextState {
	void setDisplayStack(ItemStack itemStack);

	ItemStack getDisplayStack();
}
