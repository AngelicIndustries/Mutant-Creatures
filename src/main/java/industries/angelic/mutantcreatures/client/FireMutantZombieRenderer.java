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

        Vector3f mouth = head.modelSpaceMatrix()
            .transformPosition(new Vector3f(MOUTH));

        // Model space to the world as the renderer draws it: scaled, raised by its ground offset and turned by
        // 180 degrees minus the body's yaw.
        float bodyYaw = zombie.prevRenderYawOffset + (zombie.renderYawOffset - zombie.prevRenderYawOffset) * partialTicks;
        double angle = Math.toRadians(180.0F - bodyYaw);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double mx = mouth.x * this.scaleWidth;
        double my = (mouth.y + 0.01F) * this.scaleHeight;
        double mz = mouth.z * this.scaleWidth;
        double x = zombie.lastTickPosX + (zombie.posX - zombie.lastTickPosX) * partialTicks;
        double y = zombie.lastTickPosY + (zombie.posY - zombie.lastTickPosY) * partialTicks;
        double z = zombie.lastTickPosZ + (zombie.posZ - zombie.lastTickPosZ) * partialTicks;
        zombie.setDrawnMouth(x + mx * cos + mz * sin, y + my, z - mx * sin + mz * cos);
    }
}
