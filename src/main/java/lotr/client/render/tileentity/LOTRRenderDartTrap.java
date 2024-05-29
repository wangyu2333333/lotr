package lotr.client.render.tileentity;

import org.lwjgl.opengl.GL11;

import lotr.common.tileentity.LOTRTileEntityDartTrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class LOTRRenderDartTrap extends TileEntitySpecialRenderer {
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.thePlayer.capabilities.isCreativeMode && mc.gameSettings.showDebugInfo && Minecraft.isGuiEnabled()) {
			GL11.glPushMatrix();
			GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX, -TileEntityRendererDispatcher.staticPlayerY, -TileEntityRendererDispatcher.staticPlayerZ);
			GL11.glDepthMask(false);
			GL11.glDisable(3553);
			GL11.glDisable(2896);
			GL11.glDisable(2884);
			GL11.glDisable(3042);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			AxisAlignedBB range = ((LOTRTileEntityDartTrap) tileentity).getTriggerRange();
			RenderGlobal.drawOutlinedBoundingBox(range, 16777215);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL11.glEnable(3553);
			GL11.glEnable(2896);
			GL11.glEnable(2884);
			GL11.glDisable(3042);
			GL11.glDepthMask(true);
			GL11.glPopMatrix();
		}
	}
}
