package industries.angelic.mutantcreatures.client;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

import industries.angelic.mutantcreatures.entity.FireMutantZombie;

/**
 * The server burns a cone from FireMutantZombie's mouth point along where its head looks, and the client sends the
 * flames the same way out of the drawn mouth. This checks that while it breathes the server's mouth point is the
 * model's, and that the face points roughly where it looks, so the flames don't leave the face sideways. The face
 * turns about a quarter less than the look: the original model splits a head turn between its tilted chest and head.
 */
public class FireMutantZombieBreathTest {

    @Test
    public void mouthIsWhereTheServerBurnsFromAndTheFaceLooksAlong() throws Exception {
        BakedGeoModel geo = MutantZombieVariantModelTest.load("fire_mutant_zombie");
        Animation breath;
        try (Reader reader = Files.newBufferedReader(
            Paths.get("src/main/resources/assets/mutantcreatures/geckolib/animations/fire_mutant_zombie.animation.json"),
            StandardCharsets.UTF_8)) {
            breath = new GeckoLibGsonLoader().loadAnimations(reader)
                .getAnimation("animation.fire_mutant_zombie.breath");
        }
        double forward = constant("MOUTH_FORWARD");
        double up = constant("MOUTH_UP");

        FireMutantZombie zombie = new FireMutantZombie(null);
        MutantZombieRig rig = new MutantZombieRig();
        for (float lookYaw : new float[] { 0, 40, -40 }) {
            for (float lookPitch : new float[] { 0, 20 }) {
                for (int tick = FireMutantZombie.BREATH_START; tick < FireMutantZombie.BREATH_END; tick++) {
                    zombie.currentAttackID = FireMutantZombie.BREATH;
                    zombie.animTick = tick;
                    ModelPose pose = ModelPose.create(geo);
                    rig.apply(zombie, pose, 0, 0, 0, lookYaw, lookPitch, 0);
                    pose.apply(AnimationProcessor.evaluate(breath, tick / 20.0, MolangContext.EMPTY), true);
                    Matrix4f head = GeoModelRenderer.captureTransforms(geo, pose)
                        .bones()
                        .get("head")
                        .modelSpaceMatrix();
                    Vector3f facing = head.transformDirection(new Vector3f(0, 0, -1))
                        .normalize();
                    // Model space: front is -z, and +x is the mob's left, so a look to its left is a positive yaw.
                    double yaw = Math.toDegrees(Math.atan2(facing.x, -facing.z));
                    double pitch = Math.toDegrees(Math.asin(-facing.y));
                    String at = " at tick " + tick + ", look " + lookYaw + "/" + lookPitch;
                    assertEquals("face yaw" + at, lookYaw, yaw, 12);
                    assertEquals("face pitch" + at, lookPitch, pitch, 12);

                    if (lookYaw == 0 && lookPitch == 0) {
                        Vector3f mouth = head.transformPosition(new Vector3f(0, 2 / 16f, -4.5f / 16f));
                        assertEquals("mouth forward" + at, forward, -mouth.z * 1.3, 0.12);
                        assertEquals("mouth up" + at, up, (mouth.y + 0.01) * 1.3, 0.12);
                    }
                }
            }
        }
    }

    private static double constant(String name) throws Exception {
        Field field = FireMutantZombie.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getDouble(null);
    }
}
