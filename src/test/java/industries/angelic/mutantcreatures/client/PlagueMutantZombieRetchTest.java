package industries.angelic.mutantcreatures.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.Test;

import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.renderer.base.GeoModelRenderer;

import industries.angelic.mutantcreatures.entity.PlagueMutantZombie;

/**
 * The spew leaves the drawn mouth, or PlagueMutantZombie's mouth point when the mouth hasn't been drawn. This checks
 * that while it spews that point is the model's mouth, and that the face looks down at the ground ahead.
 */
public class PlagueMutantZombieRetchTest {

    @Test
    public void mouthIsWhereTheSpewLeavesAndTheFaceLooksDownAhead() throws Exception {
        BakedGeoModel geo = MutantZombieVariantModelTest.load("plague_mutant_zombie");
        Animation retch;
        try (Reader reader = Files.newBufferedReader(
            Paths.get(
                "src/main/resources/assets/mutantcreatures/geckolib/animations/plague_mutant_zombie.animation.json"),
            StandardCharsets.UTF_8)) {
            retch = new GeckoLibGsonLoader().loadAnimations(reader)
                .getAnimation("animation.plague_mutant_zombie.retch");
        }
        double forward = constant("MOUTH_FORWARD");
        double up = constant("MOUTH_UP");

        PlagueMutantZombie zombie = new PlagueMutantZombie(null);
        MutantZombieRig rig = new MutantZombieRig();
        for (int tick = PlagueMutantZombie.RETCH_START; tick < PlagueMutantZombie.RETCH_END; tick++) {
            zombie.currentAttackID = PlagueMutantZombie.RETCH;
            zombie.animTick = tick;
            ModelPose pose = ModelPose.create(geo);
            rig.apply(zombie, pose, 0, 0, 0, 0, 0, 0);
            pose.apply(AnimationProcessor.evaluate(retch, tick / 20.0, MolangContext.EMPTY), true);
            Matrix4f head = GeoModelRenderer.captureTransforms(geo, pose)
                .bones()
                .get("head")
                .modelSpaceMatrix();
            Vector3f facing = head.transformDirection(new Vector3f(0, 0, -1))
                .normalize();
            double pitch = Math.toDegrees(Math.asin(-facing.y));
            Vector3f mouth = head.transformPosition(new Vector3f(0, 2 / 16f, -4.5f / 16f));
            double mouthForward = -mouth.z * 1.3, mouthUp = (mouth.y + 0.01) * 1.3;
            String at = String.format(
                Locale.ROOT,
                " at tick %d: mouth %.2f forward, %.2f up, face %.0f degrees down",
                tick,
                mouthForward,
                mouthUp,
                pitch);
            assertTrue("face looks down ahead" + at, pitch > 10 && pitch < 60);
            assertEquals("mouth forward" + at, forward, mouthForward, 0.15);
            assertEquals("mouth up" + at, up, mouthUp, 0.15);
        }
    }

    private static double constant(String name) throws Exception {
        Field field = PlagueMutantZombie.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getDouble(null);
    }
}
