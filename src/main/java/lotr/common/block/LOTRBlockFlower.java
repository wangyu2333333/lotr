package lotr.common.block;

import lotr.common.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;

public class LOTRBlockFlower extends BlockBush {
	public LOTRBlockFlower() {
		this(Material.plants);
	}

	public LOTRBlockFlower(Material material) {
		super(material);
		setCreativeTab(LOTRCreativeTabs.tabDeco);
		setHardness(0.0f);
		setStepSound(Block.soundTypeGrass);
	}

	@Override
	public int getRenderType() {
		return LOTRMod.proxy.getFlowerRenderID();
	}

	public Block setFlowerBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
		return this;
	}
}
