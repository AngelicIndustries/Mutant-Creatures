package industries.angelic.mutantcreatures.client;

import java.util.HashMap;
import java.util.Map;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.BrutishMutantZombie;
import industries.angelic.mutantcreatures.entity.FireMutantZombie;
import industries.angelic.mutantcreatures.entity.HungryMutantZombie;

/** Renderers for the GeckoLib mutant zombie variants. */
@SideOnly(Side.CLIENT)
public final class MutantZombieRenderers {

    private MutantZombieRenderers() {}

    public static MutantZombieGeoRenderer<HungryMutantZombie> hungry() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(HungryMutantZombie.DEVOUR, "devour");
        clips.put(HungryMutantZombie.EAT, "eat");
        return new MutantZombieGeoRenderer<>(
            new MutantZombieGeoModel<>("hungry_mutant_zombie", "hungry_zombie"),
            "animation.hungry_mutant_zombie.",
            clips);
    }

    /** It holds the vanilla wooden sword, scaled up. */
    public static MutantZombieGeoRenderer<BrutishMutantZombie> brutish() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(BrutishMutantZombie.SWEEP, "sweep");
        MutantZombieGeoRenderer<BrutishMutantZombie> renderer = new MutantZombieGeoRenderer<>(
            new MutantZombieGeoModel<>("brutish_mutant_zombie", "brutish_zombie"),
            "animation.brutish_mutant_zombie.",
            clips);
        renderer.addRenderLayer(new BrutishSwordLayer(renderer));
        return renderer;
    }

    /** Its cracks, eyes and coals glow through fire_zombie_glowmask.png. */
    public static MutantZombieGeoRenderer<FireMutantZombie> fire() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(FireMutantZombie.BREATH, "breath");
        FireMutantZombieRenderer renderer = new FireMutantZombieRenderer(
            new MutantZombieGeoModel<>("fire_mutant_zombie", "fire_zombie"),
            "animation.fire_mutant_zombie.",
            clips);
        renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer));
        return renderer;
    }
}
