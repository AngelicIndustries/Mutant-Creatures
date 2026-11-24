package industries.angelic.mutantcreatures.client;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;

import org.joml.Vector3f;
import org.junit.Test;

import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.renderer.base.GeoModelRenderer;
import com.geckolib.renderer.base.GeoRenderTransform;

import industries.angelic.mutantcreatures.entity.HungryMutantZombie;

/**
 * The server holds a devoured target where the hands are in the devour clip, from tables in HungryMutantZombie. This
 * checks the tables against the model posed by the clip; when the clip changes, paste the printed arrays in.
 */
public class HungryMutantZombieGripTest {

    @Test
    public void gripTablesFollowTheDevourClip() throws Exception {
        BakedGeoModel geo = MutantZombieVariantModelTest.load("hungry_mutant_zombie");
        Animation devour;
        try (Reader reader = Files.newBufferedReader(
            Paths.get(
                "src/main/resources/assets/mutantcreatures/geckolib/animations/hungry_mutant_zombie.animation.json"),
            StandardCharsets.UTF_8)) {
            devour = new GeckoLibGsonLoader().loadAnimations(reader)
                .getAnimation("animation.hungry_mutant_zombie.devour");
        }
        float[] forward = table("GRIP_FORWARD");
        float[] up = table("GRIP_UP");

        HungryMutantZombie zombie = new HungryMutantZombie(null);
        MutantZombieRig rig = new MutantZombieRig();
        StringBuilder expectedForward = new StringBuilder();
        StringBuilder expectedUp = new StringBuilder();
        boolean matches = forward.length == up.length;
        for (int tick = 0; tick < forward.length; tick++) {
            zombie.currentAttackID = HungryMutantZombie.DEVOUR;
            zombie.animTick = tick;
            ModelPose pose = ModelPose.create(geo);
            rig.apply(zombie, pose, 0, 0, 0, 0, 0, 0);
            pose.apply(AnimationProcessor.evaluate(devour, tick / 20.0, MolangContext.EMPTY), true);
            Map<String, GeoRenderTransform> bones = GeoModelRenderer.captureTransforms(geo, pose)
                .bones();
            // The grip: two pixels up from the end of each forearm, in blocks at the renderer's 1.3 scale.
            Vector3f right = bones.get("right_forearm")
                .modelSpaceMatrix()
                .transformPosition(new Vector3f(0, -14 / 16f, 0));
            Vector3f left = bones.get("left_forearm")
                .modelSpaceMatrix()
                .transformPosition(new Vector3f(0, -14 / 16f, 0));
            float f = -(right.z + left.z) / 2 * 1.3F;
            float u = ((right.y + left.y) / 2 + 0.01F) * 1.3F;
            expectedForward.append(String.format(Locale.ROOT, "%.2fF, ", f));
            expectedUp.append(String.format(Locale.ROOT, "%.2fF, ", u));
            matches &= Math.abs(forward[tick] - f) < 0.011F && Math.abs(up[tick] - u) < 0.011F;
        }
        assertTrue(
            "GRIP tables no longer match the devour clip:\nGRIP_FORWARD " + expectedForward
                + "\nGRIP_UP "
                + expectedUp,
            matches);
    }

    private static float[] table(String name) throws Exception {
        Field field = HungryMutantZombie.class.getDeclaredField(name);
        field.setAccessible(true);
        return (float[]) field.get(null);
    }
}
