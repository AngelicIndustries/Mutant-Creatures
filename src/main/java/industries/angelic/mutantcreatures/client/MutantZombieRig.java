package industries.angelic.mutantcreatures.client;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.model.ModelRenderer;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.model.GeoBone;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thehippomaster.MutantCreatures.MutantZombie;
import thehippomaster.MutantCreatures.client.ModelMutantZombie;

/**
 * Drives a GeckoLib model whose bones match the mutant zombie's parts with the original Java animation code.
 *
 * <p>
 * The geo bones carry the Java rest pose as their rotation, so the model looks right in Blockbench. Each frame the
 * Java model is posed and the difference to that rest pose is written into the bone snapshots. Java angles map to
 * Salamander's bone rotation with X and Y negated, and a Java rotation point offset maps to a Bedrock translation
 * with Y negated.
 */
@SideOnly(Side.CLIENT)
public final class MutantZombieRig {

    private final ModelMutantZombie model = new ModelMutantZombie();
    private final Map<String, ModelRenderer> parts = new LinkedHashMap<>();
    private final Map<String, float[]> restPoints = new LinkedHashMap<>();

    public MutantZombieRig() {
        part("pelvis", this.model.pelvis);
        part("waist", this.model.waist);
        part("chest", this.model.chest);
        part("head", this.model.head);
        part("villager_head", this.model.villagerHead);
        part("right_arm", this.model.arm1);
        part("left_arm", this.model.arm2);
        part("right_forearm", this.model.forearm1);
        part("left_forearm", this.model.forearm2);
        part("right_leg", this.model.leg1);
        part("left_leg", this.model.leg2);
        part("right_shin", this.model.foreleg1);
        part("left_shin", this.model.foreleg2);
    }

    private void part(String bone, ModelRenderer renderer) {
        this.parts.put(bone, renderer);
        this.restPoints.put(
            bone,
            new float[] { renderer.rotationPointX, renderer.rotationPointY, renderer.rotationPointZ });
    }

    public ModelMutantZombie model() {
        return this.model;
    }

    /** Poses the Java model for this frame and copies it into the pose. */
    public void apply(MutantZombie zombie, ModelPose pose, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float headPitch, float partialTicks) {
        this.model.pose(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F, partialTicks);
        copyTo(pose);
        boolean villager = zombie.getVillager();
        pose.get("head")
            .ifPresent(bone -> bone.skipRender(villager));
        pose.get("villager_head")
            .ifPresent(bone -> bone.skipRender(!villager));
    }

    /** Copies the Java model's current part angles and rotation points into the pose. */
    public void copyTo(ModelPose pose) {
        for (Map.Entry<String, ModelRenderer> entry : this.parts.entrySet()) {
            BoneSnapshot snapshot = pose.get(entry.getKey())
                .orElse(null);
            if (snapshot == null) continue;

            ModelRenderer part = entry.getValue();
            GeoBone bone = snapshot.getBone();
            float[] rest = this.restPoints.get(entry.getKey());
            snapshot.setRotation(
                -part.rotateAngleX - bone.baseRotX(),
                -part.rotateAngleY - bone.baseRotY(),
                part.rotateAngleZ - bone.baseRotZ());
            snapshot.setTranslation(
                part.rotationPointX - rest[0],
                rest[1] - part.rotationPointY,
                part.rotationPointZ - rest[2]);
        }
    }
}
