package industries.angelic.mutantcreatures.client;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import org.joml.Vector3f;
import org.junit.Test;

import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.renderer.base.GeoModelRenderer;

import industries.angelic.mutantcreatures.entity.FishingMutantZombie;

/**
 * The server throws the hook from FishingMutantZombie's release point, and the line falls back to its hold point
 * when the rod tip hasn't been drawn. This checks both against the rod tip in the model, at the cast's release and
 * while it holds the line out, so the hook leaves the drawn tip.
 */
public class FishingMutantZombieRodTest {

    @Test
    public void releaseAndHoldPointsAreTheModelsRodTip() throws Exception {
        assertTip("RELEASE_TIP", FishingMutantZombie.CAST_RELEASE / 20.0);
        assertTip("HOLD_TIP", 10.0);
    }

    private static void assertTip(String name, double time) throws Exception {
        BakedGeoModel geo = MutantZombieVariantModelTest.load("fishing_mutant_zombie");
        Animation cast;
        try (Reader reader = Files.newBufferedReader(
            Paths.get(
                "src/main/resources/assets/mutantcreatures/geckolib/animations/fishing_mutant_zombie.animation.json"),
            StandardCharsets.UTF_8)) {
            cast = new GeckoLibGsonLoader().loadAnimations(reader)
                .getAnimation("animation.fishing_mutant_zombie.cast");
        }
        FishingMutantZombie zombie = new FishingMutantZombie(null);
        zombie.currentAttackID = FishingMutantZombie.CAST;
        ModelPose pose = ModelPose.create(geo);
        new MutantZombieRig().apply(zombie, pose, 0, 0, 0, 0, 0, 0);
        pose.apply(AnimationProcessor.evaluate(cast, Math.min(time, cast.length()), MolangContext.EMPTY), true);
        Vector3f tip = GeoModelRenderer.captureTransforms(geo, pose)
            .bones()
            .get("rod")
            .modelSpaceMatrix()
            .transformPosition(
                HeldItemLayer.texelToBone(
                    FishingMutantZombieRenderer.ROD,
                    FishingMutantZombieRenderer.TIP_U,
                    FishingMutantZombieRenderer.TIP_V));
        // Model space to blocks from its feet, as the renderer draws it: front is -z, +x is its left.
        double forward = -tip.z * 1.3, up = (tip.y + 0.01) * 1.3, right = -tip.x * 1.3;

        Field field = FishingMutantZombie.class.getDeclaredField(name);
        field.setAccessible(true);
        double[] expected = (double[]) field.get(null);
        String model = String.format(Locale.ROOT, "%s from the model: { %.1fD, %.1fD, %.1fD }", name, forward, up, right);
        assertEquals(model, expected[0], forward, 0.15);
        assertEquals(model, expected[1], up, 0.15);
        assertEquals(model, expected[2], right, 0.15);
    }
}
