package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * Special Mobs' plague: each dose is one random effect, and doses of the same effect stack, one level higher each up
 * to a cap, keeping the longer duration.
 */
public final class PlagueEffects {

    /** Effect, seconds and highest level (0 is I) of each dose, as Special Mobs has them. */
    private static final int[][] DOSES = { { Potion.moveSlowdown.id, 8, 5 }, { Potion.confusion.id, 10, 2 },
        { Potion.blindness.id, 10, 0 }, { Potion.hunger.id, 8, 1 }, { Potion.weakness.id, 10, 6 },
        { Potion.poison.id, 6, 1 } };

    private PlagueEffects() {}

    public static void dose(EntityLivingBase target, int doses) {
        for (int i = 0; i < doses; i++) {
            int[] dose = DOSES[target.getRNG()
                .nextInt(DOSES.length)];
            PotionEffect existing = target.getActivePotionEffect(Potion.potionTypes[dose[0]]);
            target.addPotionEffect(stacked(existing, dose[0], dose[1] * 20, dose[2]));
        }
    }

    /** A dose on top of what the target already has of it. */
    static PotionEffect stacked(PotionEffect existing, int potion, int duration, int maxLevel) {
        if (existing == null) return new PotionEffect(potion, duration, 0);
        return new PotionEffect(
            potion,
            Math.max(duration, existing.getDuration()),
            Math.min(maxLevel, existing.getAmplifier() + 1));
    }
}
