package industries.angelic.mutantcreatures.client;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.FishingMutantZombie;
import industries.angelic.mutantcreatures.entity.MutantFishHook;

/** The bobber at twice vanilla's size, and the line sagging from the rod tip to it, both as vanilla draws them. */
@SideOnly(Side.CLIENT)
public class RenderMutantFishHook extends Render {

    private static final ResourceLocation PARTICLES = new ResourceLocation("textures/particle/particles.png");
    private static final int SEGMENTS = 16;

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        MutantFishHook hook = (MutantFishHook) entity;
        if (hook.getHooked() == null) renderBobber(x, y, z);

        FishingMutantZombie angler = hook.getAngler();
        if (angler == null) return;
        double[] tip = angler.rodTip(partialTicks);
        double hookX = hook.lastTickPosX + (hook.posX - hook.lastTickPosX) * partialTicks;
        double hookY = hook.lastTickPosY + (hook.posY - hook.lastTickPosY) * partialTicks;
        double hookZ = hook.lastTickPosZ + (hook.posZ - hook.lastTickPosZ) * partialTicks;
        double dx = tip[0] - hookX;
        double dy = tip[1] - hookY;
        double dz = tip[2] - hookZ;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(2.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINE_STRIP);
        tessellator.setColorOpaque_I(0);
        for (int i = 0; i <= SEGMENTS; i++) {
            float f = (float) i / SEGMENTS;
            tessellator.addVertex(x + dx * f, y + dy * (f * f + f) * 0.5D, z + dz * f);
        }
        tessellator.draw();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderBobber(double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        this.bindTexture(PARTICLES);
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5D, -0.5D, 0.0D, 0.0625D, 0.1875D);
        tessellator.addVertexWithUV(0.5D, -0.5D, 0.0D, 0.125D, 0.1875D);
        tessellator.addVertexWithUV(0.5D, 0.5D, 0.0D, 0.125D, 0.125D);
        tessellator.addVertexWithUV(-0.5D, 0.5D, 0.0D, 0.0625D, 0.125D);
        tessellator.draw();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return PARTICLES;
    }
}
