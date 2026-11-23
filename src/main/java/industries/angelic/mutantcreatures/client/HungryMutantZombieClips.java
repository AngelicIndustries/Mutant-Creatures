package industries.angelic.mutantcreatures.client;

import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.loading.math.MolangContext;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.HungryMutantZombie;

/**
 * Plays the hungry mutant zombie's own attacks on top of the pose from {@link MutantZombieRig}. Clips are sampled
 * at the attack's tick, so they stay in step with the server's bite ticks. The mutant zombie's attacks come from
 * the rig and need no clips.
 */
@SideOnly(Side.CLIENT)
public final class HungryMutantZombieClips {

    private static final String PREFIX = "animation.hungry_mutant_zombie.";

    private HungryMutantZombieClips() {}

    /** @param attackTicks attack tick plus partial ticks */
    public static void apply(BakedAnimations animations, ModelPose pose, int attackId, float attackTicks) {
        String clip = attackClip(attackId);
        if (clip == null) return;

        Animation animation = animations.getAnimation(PREFIX + clip);
        if (animation == null) return;

        double time = Math.min(attackTicks / 20.0, animation.length());
        pose.apply(AnimationProcessor.evaluate(animation, time, MolangContext.EMPTY), true);
    }

    static String attackClip(int attackId) {
        switch (attackId) {
            case HungryMutantZombie.DEVOUR:
                return "devour";
            case HungryMutantZombie.EAT:
                return "eat";
            default:
                return null;
        }
    }
}
