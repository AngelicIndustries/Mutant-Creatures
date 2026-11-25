package industries.angelic.mutantcreatures.client;

import java.util.Map;

import net.minecraft.init.Items;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.geckolib.animation.state.ModelPose;
import com.geckolib.renderer.base.GeoModelRenderer;
import com.geckolib.renderer.base.GeoRenderTransform;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.FishingMutantZombie;

/**
 * Holds the vanilla fishing rod, scaled up, at the rod bone: the rod with its line at rest, the bare rod while the
 * line is out. While it is out, tells the zombie where it drew the rod tip, so the line leaves the tip.
 */
@SideOnly(Side.CLIENT)
public class FishingMutantZombieRenderer extends MutantZombieGeoRenderer<FishingMutantZombie> {

    /** Width of the rod's icon in blocks, before the renderer's scale. */
    static final float ROD_SIZE = 1.4F;
    /** The middle of the grip and the tip on the rod's icons, in texels from the top left. */
    static final float GRIP_U = 4.0F, GRIP_V = 13.0F, TIP_U = 14.5F, TIP_V = 1.5F;
    static final Matrix4f ROD = HeldItemLayer.iconToBone(ROD_SIZE, GRIP_U, GRIP_V);

    public FishingMutantZombieRenderer(MutantZombieGeoModel<FishingMutantZombie> model, String clipPrefix,
        Map<Integer, String> clips) {
        super(model, clipPrefix, clips);
        addRenderLayer(
            new HeldItemLayer<>(
                this,
                "rod",
                zombie -> zombie.isLineOut() ? Items.fishing_rod.func_94597_g()
                    : Items.fishing_rod.getIconFromDamage(0),
                ROD_SIZE,
                GRIP_U,
                GRIP_V,
                false));
    }

    @Override
    protected void applyRenderPose(FishingMutantZombie zombie, ModelPose pose, float limbSwing,
        float limbSwingAmount, float netHeadYaw, float headPitch, float partialTicks) {
        super.applyRenderPose(zombie, pose, limbSwing, limbSwingAmount, netHeadYaw, headPitch, partialTicks);
        if (!zombie.isLineOut()) return;

        GeoRenderTransform rod = GeoModelRenderer.captureTransforms(pose.model(), pose)
            .getBone("rod")
            .orElse(null);
        if (rod == null) return;

        Vector3f tip = HeldItemLayer.texelToBone(ROD, TIP_U, TIP_V);
        double[] world = modelToWorld(
            zombie,
            rod.modelSpaceMatrix()
                .transformPosition(tip),
            partialTicks);
        zombie.drawnRodTip()
            .set(zombie, world[0], world[1], world[2]);
    }
}
