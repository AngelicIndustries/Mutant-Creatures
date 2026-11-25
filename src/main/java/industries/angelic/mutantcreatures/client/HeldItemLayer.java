package industries.angelic.mutantcreatures.client;

import java.nio.FloatBuffer;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A vanilla item, scaled up, held at a bone: drawn from its icon as vanilla draws a held item, with the enchantment
 * glint if asked for. The icon must run diagonally from the held end at its bottom left up to its top right, as
 * swords, tools and the fishing rod do. It is laid along the bone's -z with its flat to the sides and its grip on the
 * bone.
 */
@SideOnly(Side.CLIENT)
public class HeldItemLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    private static final ResourceLocation GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    /** Thickness of the item as vanilla draws it: one texel. */
    private static final float THICKNESS = 1 / 16F;
    private static final FloatBuffer MATRIX = BufferUtils.createFloatBuffer(16);

    private final String bone;
    private final Function<T, IIcon> icon;
    private final Matrix4f iconToBone;
    private final boolean glint;

    /**
     * @param size  width of the icon in blocks, before the renderer's scale
     * @param gripU the middle of the grip on the icon, in texels from its left
     * @param gripV the middle of the grip on the icon, in texels from its top
     */
    public HeldItemLayer(GeoRenderer<T> renderer, String bone, Function<T, IIcon> icon, float size, float gripU,
        float gripV, boolean glint) {
        super(renderer);
        this.bone = bone;
        this.icon = icon;
        this.iconToBone = iconToBone(size, gripU, gripV);
        this.glint = glint;
    }

    /**
     * From the icon's space, where (0, 0) is its bottom left corner and its width is one, to the bone's. The blade
     * runs diagonally up the icon from the grip; it is turned to lie along -z with its flat to the sides.
     */
    public static Matrix4f iconToBone(float size, float gripU, float gripV) {
        return new Matrix4f().scale(size)
            .rotateY((float) Math.toRadians(90))
            .rotateZ((float) Math.toRadians(-45))
            .translate(-gripU / 16, gripV / 16 - 1, THICKNESS / 2);
    }

    /** A texel on the icon, in texels from its top left as in the texture, in the bone's space. */
    public static Vector3f texelToBone(Matrix4f iconToBone, float u, float v) {
        return iconToBone.transformPosition(new Vector3f(u / 16, 1 - v / 16, -THICKNESS / 2));
    }

    @Override
    public void render(RenderPassInfo<T> info) {
        IIcon icon = this.icon.apply(info.animatable());
        if (icon == null) return;
        if (info.runAtBone(this.bone, () -> renderItem(info, icon))) {
            this.renderer.bindTexture(info.animatable());
        }
    }

    private void renderItem(RenderPassInfo<T> info, IIcon icon) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationItemsTexture);
        MATRIX.clear();
        this.iconToBone.get(MATRIX);
        GL11.glMultMatrix(MATRIX);
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
        if (this.glint) renderGlint(tessellator);
    }

    /** The enchantment glint, drawn over the item as vanilla draws it over a held item. */
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
