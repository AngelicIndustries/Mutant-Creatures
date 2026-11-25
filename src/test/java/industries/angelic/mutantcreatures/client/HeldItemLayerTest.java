package industries.angelic.mutantcreatures.client;

import static org.junit.Assert.assertEquals;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.Test;

/**
 * A held item lies along its bone's -z with the middle of its grip on the bone, so the bone's pose aims it. The
 * fishing line leaves from the rod tip the same transform gives.
 */
public class HeldItemLayerTest {

    @Test
    public void swordGripIsOnTheBoneAndTheBladeRunsAlongMinusZ() {
        Matrix4f sword = HeldItemLayer.iconToBone(1.5F, 3.25F, 12.75F);
        assertNear(new Vector3f(0, 0, 0), HeldItemLayer.texelToBone(sword, 3.25F, 12.75F));
        // The tip at the icon's top right corner, a diagonal of the grip's.
        Vector3f tip = HeldItemLayer.texelToBone(sword, 16, 0);
        float length = 1.5F * (16 - 3.25F) / 16 * (float) Math.sqrt(2);
        assertNear(new Vector3f(0, 0, -length), tip);
    }

    @Test
    public void rodTipIsAheadOfTheGripAlongMinusZ() {
        Vector3f grip = HeldItemLayer.texelToBone(
            FishingMutantZombieRenderer.ROD,
            FishingMutantZombieRenderer.GRIP_U,
            FishingMutantZombieRenderer.GRIP_V);
        Vector3f tip = HeldItemLayer.texelToBone(
            FishingMutantZombieRenderer.ROD,
            FishingMutantZombieRenderer.TIP_U,
            FishingMutantZombieRenderer.TIP_V);
        assertNear(new Vector3f(0, 0, 0), grip);
        // The rod runs a little off the icon's diagonal, so its tip is a little off -z.
        double length = Math.hypot(
            FishingMutantZombieRenderer.TIP_U - FishingMutantZombieRenderer.GRIP_U,
            FishingMutantZombieRenderer.GRIP_V - FishingMutantZombieRenderer.TIP_V) / 16
            * FishingMutantZombieRenderer.ROD_SIZE;
        assertEquals("tip to the side", 0, tip.x, 0.01);
        assertEquals("tip up or down", 0, tip.y, 0.1);
        assertEquals("tip ahead", -length, tip.z, 0.01);
    }

    private static void assertNear(Vector3f expected, Vector3f actual) {
        assertEquals("x", expected.x, actual.x, 1.0E-4);
        assertEquals("y", expected.y, actual.y, 1.0E-4);
        assertEquals("z", expected.z, actual.z, 1.0E-4);
    }
}
