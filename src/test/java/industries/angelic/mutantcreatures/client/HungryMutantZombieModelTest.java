package industries.angelic.mutantcreatures.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.util.ResourceLocation;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.Test;

import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.renderer.base.GeoModelRenderer;

import thehippomaster.MutantCreatures.client.ModelMutantZombie;

/**
 * The hungry mutant zombie's geo model shares every part with the mutant zombie's Java model and adds a spine. These
 * tests check that the shared parts come out vertex for vertex and UV for UV the same, at rest and posed by the rig.
 */
public class HungryMutantZombieModelTest {

    static final Path MODEL = Paths
        .get("src/main/resources/assets/mutantcreatures/geckolib/models/hungry_mutant_zombie.geo.json");
    /** Bones the Java model doesn't have. */
    static final Set<String> NEW_BONES = new HashSet<>(Arrays.asList("spine"));

    static BakedGeoModel load() throws Exception {
        try (Reader reader = Files.newBufferedReader(MODEL, StandardCharsets.UTF_8)) {
            return new GeckoLibGsonLoader()
                .loadModel(new ResourceLocation("mutantcreatures", "hungry_mutant_zombie"), reader);
        }
    }

    @Test
    public void restPoseMatchesJavaModel() throws Exception {
        BakedGeoModel geo = load();
        MutantZombieRig rig = new MutantZombieRig();
        rig.model()
            .setAngles();
        compare(geo, rig);
    }

    @Test
    public void rigCopiesArbitraryJavaPoses() throws Exception {
        BakedGeoModel geo = load();
        Random random = new Random(42);
        for (int i = 0; i < 20; i++) {
            MutantZombieRig rig = new MutantZombieRig();
            ModelMutantZombie model = rig.model();
            for (ModelRenderer part : parts(model)) {
                part.rotateAngleX = (random.nextFloat() - 0.5F) * 3;
                part.rotateAngleY = (random.nextFloat() - 0.5F) * 3;
                part.rotateAngleZ = (random.nextFloat() - 0.5F) * 3;
            }
            model.pelvis.rotationPointY += (random.nextFloat() - 0.5F) * 40;
            model.pelvis.rotationPointX += (random.nextFloat() - 0.5F) * 4;
            compare(geo, rig);
        }
    }

    static List<ModelRenderer> parts(ModelMutantZombie model) {
        return Arrays.asList(
            model.pelvis,
            model.waist,
            model.chest,
            model.head,
            model.villagerHead,
            model.arm1,
            model.arm2,
            model.forearm1,
            model.forearm2,
            model.leg1,
            model.leg2,
            model.foreleg1,
            model.foreleg2);
    }

    private static void compare(BakedGeoModel geo, MutantZombieRig rig) throws Exception {
        ModelPose pose = ModelPose.create(geo);
        rig.copyTo(pose);

        Map<String, Integer> expected = new HashMap<>();
        javaQuads(rig.model().pelvis, new Matrix4f(), expected);

        Map<String, Integer> actual = new HashMap<>();
        List<float[]> vertices = new ArrayList<>();
        GeoModelRenderer.render(
            geo,
            pose,
            bone -> !NEW_BONES.contains(bone.name()),
            (x, y, z, u, v, nx, ny, nz, r, g, b, a) -> vertices.add(new float[] { x * 16, y * 16, z * 16, u, v }),
            1,
            1,
            1,
            1);
        for (int i = 0; i < vertices.size(); i += 4) {
            actual.merge(key(vertices.subList(i, i + 4)), 1, Integer::sum);
        }

        assertTrue("no geometry compared", expected.size() > 50);
        List<String> missing = new ArrayList<>();
        for (String quad : expected.keySet()) if (!actual.containsKey(quad)) missing.add(quad);
        List<String> extra = new ArrayList<>();
        for (String quad : actual.keySet()) if (!expected.containsKey(quad)) extra.add(quad);
        assertEquals(
            "missing " + missing.subList(0, Math.min(3, missing.size())) + "\nextra "
                + extra.subList(0, Math.min(3, extra.size())),
            0,
            missing.size() + extra.size());
        assertEquals(expected, actual);
    }

    /** Java quads in Salamander's model space: (-x, 24 - y, z) in pixels. */
    private static void javaQuads(ModelRenderer part, Matrix4f parent, Map<String, Integer> out) throws Exception {
        Matrix4f matrix = new Matrix4f(parent).translate(part.rotationPointX, part.rotationPointY, part.rotationPointZ)
            .rotateZ(part.rotateAngleZ)
            .rotateY(part.rotateAngleY)
            .rotateX(part.rotateAngleX);
        Field quadList = ModelBox.class.getDeclaredField("quadList");
        quadList.setAccessible(true);
        for (Object box : part.cubeList) {
            for (TexturedQuad quad : (TexturedQuad[]) quadList.get(box)) {
                List<float[]> vertices = new ArrayList<>();
                for (PositionTextureVertex vertex : quad.vertexPositions) {
                    Vector4f p = matrix.transform(
                        new Vector4f(
                            (float) vertex.vector3D.xCoord,
                            (float) vertex.vector3D.yCoord,
                            (float) vertex.vector3D.zCoord,
                            1));
                    vertices.add(new float[] { -p.x, 24 - p.y, p.z, vertex.texturePositionX, vertex.texturePositionY });
                }
                out.merge(key(vertices), 1, Integer::sum);
            }
        }
        if (part.childModels != null) {
            for (Object child : part.childModels) {
                javaQuads((ModelRenderer) child, matrix, out);
            }
        }
    }

    private static String key(List<float[]> quad) {
        List<String> corners = new ArrayList<>();
        for (float[] v : quad) {
            corners.add(
                String.format(
                    Locale.ROOT,
                    "(%.1f %.1f %.1f | %.4f %.4f)",
                    zero(v[0]),
                    zero(v[1]),
                    zero(v[2]),
                    zero(v[3]),
                    zero(v[4])));
        }
        corners.sort(String::compareTo);
        return String.join(" ", corners);
    }

    private static float zero(float value) {
        return Math.abs(value) < 0.005F ? 0 : value;
    }
}
