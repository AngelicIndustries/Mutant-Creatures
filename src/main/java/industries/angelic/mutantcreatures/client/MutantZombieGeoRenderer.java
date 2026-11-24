package industries.angelic.mutantcreatures.client;

import java.util.Map;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.Animation;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.renderer.GeoEntityRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Renders a mutant zombie variant with a GeckoLib model. The mutant zombie's own animation code poses the shared
 * bones through {@link MutantZombieRig}; the variant's own attacks are clips, sampled at the attack's tick so they
 * stay in step with the server.
 */
@SideOnly(Side.CLIENT)
public class MutantZombieGeoRenderer<T extends MutantZombie & GeoAnimatable> extends GeoEntityRenderer<T> {

    private final MutantZombieRig rig = new MutantZombieRig();
    private final String clipPrefix;
    private final Map<Integer, String> clips;

    /**
     * @param clipPrefix animation name prefix, e.g. {@code animation.hungry_mutant_zombie.}
     * @param clips      clip name for each of the variant's attack IDs
     */
    public MutantZombieGeoRenderer(MutantZombieGeoModel<T> model, String clipPrefix, Map<Integer, String> clips) {
        super(model, 1.5F);
        this.clipPrefix = clipPrefix;
        this.clips = clips;
        withScale(1.3F);
    }

    @Override
    protected void applyRenderPose(T zombie, ModelPose pose, float limbSwing, float limbSwingAmount, float netHeadYaw,
        float headPitch, float partialTicks) {
        float age = zombie.ticksExisted + partialTicks;
        this.rig.apply(zombie, pose, limbSwing, limbSwingAmount, age, netHeadYaw, headPitch, partialTicks);
        if (zombie.deathTick > 0) return;

        String clip = this.clips.get(zombie.currentAttackID);
        Animation animation = clip == null ? null
            : getGeoModel().getBakedAnimations(zombie)
                .getAnimation(this.clipPrefix + clip);
        if (animation == null) return;

        double time = Math.min((zombie.animTick + partialTicks) / 20.0, animation.length());
        pose.apply(AnimationProcessor.evaluate(animation, time, MolangContext.EMPTY), true);
    }

    /**
     * Salamander 0.0.2's GeoEntityRenderer shows every entity's name, as for players. Use RenderLiving's rule for
     * mobs instead: only a custom name, and only while looked at unless always shown. Salamander has this after
     * 0.0.2; drop the override once a newer Salamander is required.
     */
    @Override
    protected boolean func_110813_b(EntityLivingBase living) {
        EntityLiving mob = (EntityLiving) living;
        return super.func_110813_b(mob) && (mob.getAlwaysRenderNameTagForRender()
            || mob.hasCustomNameTag() && mob == this.renderManager.field_147941_i);
    }

    /** Fades out while it vanishes for good, as the mutant zombie does. */
    @Override
    public int getRenderColor(T zombie, float partialTicks) {
        if (zombie.vanishTick <= 0) return 0xFFFFFFFF;
        float alpha = 1.0F - (zombie.vanishTick + partialTicks) / zombie.maxVanishTime() * 0.6F;
        return (int) (MathHelper.clamp_float(alpha, 0, 1) * 255) << 24 | 0xFFFFFF;
    }

    /** The mutant zombie's fall and get-up, in place of the vanilla death tip-over. */
    @Override
    protected void rotateCorpse(EntityLivingBase living, float ageInTicks, float bodyYaw, float partialTicks) {
        GL11.glRotatef(180.0F - bodyYaw, 0.0F, 1.0F, 0.0F);
        MutantZombie zombie = (MutantZombie) living;
        int pitch = Math.min(20, zombie.deathTick);
        boolean reviving = false;
        if (zombie.deathTick > zombie.maxDeathTime() - 40) {
            pitch = zombie.maxDeathTime() - zombie.deathTick;
            reviving = true;
        }
        if (pitch > 0) {
            float f = reviving ? (pitch - partialTicks) / 40.0F * 1.6F : (pitch + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = Math.min(1.0F, MathHelper.sqrt_float(f));
            GL11.glRotatef(f * 80.0F, -1.0F, 0.0F, 0.0F);
        }
    }
}
