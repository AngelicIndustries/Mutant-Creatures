package industries.angelic.mutantcreatures.client;

import java.util.Map;

import org.joml.Vector3f;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.renderer.base.GeoModelRenderer;
import com.geckolib.renderer.base.GeoRenderTransform;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.MouthAttacker;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Tells a mutant zombie where it drew the mouth during its mouth attack, so what comes out of the mouth leaves the
 * drawn mouth: the fire mutant's flames, the plague mutant's retch.
 */
@SideOnly(Side.CLIENT)
public class MouthTrackingRenderer<T extends MutantZombie & GeoAnimatable & MouthAttacker>
    extends MutantZombieGeoRenderer<T> {

    /** The mouth on the zombie face, in head bone space: two pixels up from the chin, just in front of the face. */
    private static final Vector3f MOUTH = new Vector3f(0, 2 / 16f, -4.5f / 16f);

    private final int attack;

    /** @param attack the attack ID during which the mouth is tracked */
    public MouthTrackingRenderer(MutantZombieGeoModel<T> model, String clipPrefix, Map<Integer, String> clips,
        int attack) {
        super(model, clipPrefix, clips);
        this.attack = attack;
    }

    @Override
    protected void applyRenderPose(T zombie, ModelPose pose, float limbSwing, float limbSwingAmount, float netHeadYaw,
        float headPitch, float partialTicks) {
        super.applyRenderPose(zombie, pose, limbSwing, limbSwingAmount, netHeadYaw, headPitch, partialTicks);
        if (zombie.currentAttackID != this.attack || zombie.deathTick > 0) return;

        GeoRenderTransform head = GeoModelRenderer.captureTransforms(pose.model(), pose)
            .getBone(zombie.getVillager() ? "villager_head" : "head")
            .orElse(null);
        if (head == null) return;

        double[] mouth = modelToWorld(
            zombie,
            head.modelSpaceMatrix()
                .transformPosition(new Vector3f(MOUTH)),
            partialTicks);
        zombie.drawnMouth()
            .set(zombie, mouth[0], mouth[1], mouth[2]);
    }
}
