package industries.angelic.mutantcreatures.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.BrutishMutantZombie;

/**
 * The vanilla wooden sword, scaled up, in the brutish mutant zombie's right fist. It glints, as the brutish zombie's
 * sword does with its Pain enchantment. The sword bone sits in the fist, and the sword runs along its -z.
 */
@SideOnly(Side.CLIENT)
public class BrutishSwordLayer extends GeoRenderLayer<BrutishMutantZombie> {

    private static final ResourceLocation GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    /** Width of the sword's icon in blocks, before the renderer's scale. */
    private static final float SIZE = 1.5F;
    /** The middle of the grip, in texels from the icon's bottom left corner, right and up alike. */
    private static final float GRIP = 3.25F;
    /** Thickness of the item as vanilla draws it: one texel. */
    private static final float THICKNESS = 1 / 16F;

    public BrutishSwordLayer(GeoRenderer<BrutishMutantZombie> renderer) {
        super(renderer);
    }

    @Override
    public void render(RenderPassInfo<BrutishMutantZombie> info) {
        if (info.runAtBone("sword", () -> renderSword(info))) {
            this.renderer.bindTexture(info.animatable());
        }
    }

    private static void renderSword(RenderPassInfo<BrutishMutantZombie> info) {
        IIcon icon = Items.wooden_sword.getIconFromDamage(0);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationItemsTexture);
        GL11.glScalef(SIZE, SIZE, SIZE);
        // The blade runs diagonally up the icon from the grip at the bottom left. Lay it along -z with the flat of the
        // blade to the sides, and the grip's middle on the bone.
        GL11.glRotatef(90, 0, 1, 0);
        GL11.glRotatef(-45, 0, 0, 1);
        GL11.glTranslatef(-GRIP / 16, -GRIP / 16, THICKNESS / 2);
        GL11.glColor4f(info.red(), info.green(), info.blue(), info.alpha());
        // With the UVs in this order the icon's bottom left corner is at (0, 0), and its front face is not mirrored.
        Tessellator tessellator = Tessellator.instance;
        ItemRenderer.renderItemIn2D(
            tessellator,
            icon.getMinU(),
            icon.getMinV(),
            icon.getMaxU(),
            icon.getMaxV(),
            icon.getIconWidth(),
            icon.getIconHeight(),
            THICKNESS);
        renderGlint(tessellator);
    }

    /** The enchantment glint, drawn over the sword as vanilla draws it over a held item. */
    private static void renderGlint(Tessellator tessellator) {
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        int blendSource = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int blendDestination = GL11.glGetInteger(GL11.GL_BLEND_DST);

        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glColor4f(0.38F, 0.19F, 0.608F, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        for (int pass = 0; pass < 2; pass++) {
            long period = pass == 0 ? 3000L : 4873L;
            float shift = Minecraft.getSystemTime() % period / (float) period * 8.0F;
            GL11.glPushMatrix();
            GL11.glScalef(0.125F, 0.125F, 0.125F);
            GL11.glTranslatef(pass == 0 ? shift : -shift, 0.0F, 0.0F);
            GL11.glRotatef(pass == 0 ? -50.0F : 10.0F, 0.0F, 0.0F, 1.0F);
            ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, THICKNESS);
            GL11.glPopMatrix();
        }
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glBlendFunc(blendSource, blendDestination);
        GeoRenderer.setEnabled(GL11.GL_BLEND, blend);
        GeoRenderer.setEnabled(GL11.GL_LIGHTING, lighting);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }
}
