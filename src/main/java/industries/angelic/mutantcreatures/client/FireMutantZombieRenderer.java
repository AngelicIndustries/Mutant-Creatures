package industries.angelic.mutantcreatures.client;

import java.util.Map;

import org.joml.Vector3f;

import com.geckolib.animation.state.ModelPose;
import com.geckolib.renderer.base.GeoModelRenderer;
import com.geckolib.renderer.base.GeoRenderTransform;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.FireMutantZombie;

/** Tells the fire mutant zombie where it drew the mouth while it breathes fire, so the flames leave the mouth. */
@SideOnly(Side.CLIENT)
public class FireMutantZombieRenderer extends MutantZombieGeoRenderer<FireMutantZombie> {

    /** The mouth on the zombie face, in head bone space: two pixels up from the chin, just in front of the face. */
    private static final Vector3f MOUTH = new Vector3f(0, 2 / 16f, -4.5f / 16f);

    public FireMutantZombieRenderer(MutantZombieGeoModel<FireMutantZombie> model, String clipPrefix,
        Map<Integer, String> clips) {
        super(model, clipPrefix, clips);
    }

    @Override
    protected void applyRenderPose(FireMutantZombie zombie, ModelPose pose, float limbSwing, float limbSwingAmount,
        float netHeadYaw, float headPitch, float partialTicks) {
        super.applyRenderPose(zombie, pose, limbSwing, limbSwingAmount, netHeadYaw, headPitch, partialTicks);
        if (zombie.currentAttackID != FireMutantZombie.BREATH || zombie.deathTick > 0) return;

        GeoRenderTransform head = GeoModelRenderer.captureTransforms(pose.model(), pose)
            .getBone("head")
            .orElse(null);
        if (head == null) return;

        double[] mouth = modelToWorld(
            zombie,
            head.modelSpaceMatrix()
                .transformPosition(new Vector3f(MOUTH)),
            partialTicks);
        zombie.setDrawnMouth(mouth[0], mouth[1], mouth[2]);
    }
}
