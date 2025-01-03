package net.irisshaders.iris.platform;

import net.irisshaders.iris.Iris;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.nio.file.Path;

public class IrisForgeHelpers implements IrisPlatformHelpers {
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}

	boolean HAS_CAMO = isModLoaded("cable_facades");

	@Override
	public String getVersion() {
		return LoadingModList.get().getModFileById(Iris.MODID).versionString();
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public int compareVersions(String currentVersion, String semanticVersion) throws Exception {
		return new DefaultArtifactVersion(currentVersion).compareTo(new DefaultArtifactVersion(semanticVersion));
	}

	@Override
	public KeyMapping registerKeyBinding(KeyMapping keyMapping) {
		IrisForgeMod.KEYLIST.add(keyMapping);
		return keyMapping;
	}

	@Override
	public boolean useELS() {
		return true;
	}

	// TODO find a way to do this without breaking Cable Facades...
	@Override
	public BlockState getBlockAppearance(BlockAndTintGetter level, BlockState state, Direction cullFace, BlockPos pos) {
		return state;
	}
}
