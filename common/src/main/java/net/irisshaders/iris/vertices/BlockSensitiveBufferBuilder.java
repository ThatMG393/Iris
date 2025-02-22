package net.irisshaders.iris.vertices;

public interface BlockSensitiveBufferBuilder {
	void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ);

	void overrideBlock(int block);

	void restoreBlock();

	void endBlock();

    void ignoreMidBlock(boolean b);
}
