package industries.angelic.mutantcreatures.entity;

import static org.junit.Assert.assertEquals;

import net.minecraft.potion.PotionEffect;

import org.junit.Test;

/** Doses of the plague stack as Special Mobs' do: one level higher each up to the cap, keeping the longer duration. */
public class PlagueEffectsTest {

    @Test
    public void dosesStackUpToTheCapAndKeepTheLongerDuration() {
        PotionEffect effect = PlagueEffects.stacked(null, 2, 160, 5);
        assertEquals(0, effect.getAmplifier());
        assertEquals(160, effect.getDuration());
        for (int dose = 1; dose < 10; dose++) {
            effect = PlagueEffects.stacked(effect, 2, 160, 5);
            assertEquals("level after " + (dose + 1) + " doses", Math.min(5, dose), effect.getAmplifier());
        }
        assertEquals(400, PlagueEffects.stacked(new PotionEffect(2, 400, 1), 2, 160, 5).getDuration());
        assertEquals(0, PlagueEffects.stacked(new PotionEffect(15, 100, 0), 15, 200, 0).getAmplifier());
    }
}
