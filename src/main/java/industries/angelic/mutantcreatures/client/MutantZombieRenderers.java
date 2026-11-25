package industries.angelic.mutantcreatures.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Items;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.BrutishMutantZombie;
import industries.angelic.mutantcreatures.entity.FireMutantZombie;
import industries.angelic.mutantcreatures.entity.FishingMutantZombie;
import industries.angelic.mutantcreatures.entity.HungryMutantZombie;
import industries.angelic.mutantcreatures.entity.PlagueMutantZombie;

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

    /** It holds the vanilla wooden sword, scaled up, glinting as the brutish zombie's enchanted one does. */
    public static MutantZombieGeoRenderer<BrutishMutantZombie> brutish() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(BrutishMutantZombie.SWEEP, "sweep");
        MutantZombieGeoRenderer<BrutishMutantZombie> renderer = new MutantZombieGeoRenderer<>(
            new MutantZombieGeoModel<>("brutish_mutant_zombie", "brutish_zombie"),
            "animation.brutish_mutant_zombie.",
            clips);
        renderer.addRenderLayer(
            new HeldItemLayer<>(
                renderer,
                "sword",
                zombie -> Items.wooden_sword.getIconFromDamage(0),
                1.5F,
                3.25F,
                12.75F,
                true));
        return renderer;
    }

    /** A plain mutant zombie with the vanilla fishing rod, scaled up. */
    public static MutantZombieGeoRenderer<FishingMutantZombie> fishing() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(FishingMutantZombie.CAST, "cast");
        clips.put(FishingMutantZombie.REEL, "reel");
        return new FishingMutantZombieRenderer(
            new MutantZombieGeoModel<>("fishing_mutant_zombie", "zombie"),
            "animation.fishing_mutant_zombie.",
            clips);
    }

    /** Its retch leaves the drawn mouth. */
    public static MutantZombieGeoRenderer<PlagueMutantZombie> plague() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(PlagueMutantZombie.RETCH, "retch");
        return new MouthTrackingRenderer<>(
            new MutantZombieGeoModel<>("plague_mutant_zombie", "plague_zombie"),
            "animation.plague_mutant_zombie.",
            clips,
            PlagueMutantZombie.RETCH);
    }

    /** Its cracks, eyes and coals glow through fire_zombie_glowmask.png. */
    public static MutantZombieGeoRenderer<FireMutantZombie> fire() {
        Map<Integer, String> clips = new HashMap<>();
        clips.put(FireMutantZombie.BREATH, "breath");
        MutantZombieGeoRenderer<FireMutantZombie> renderer = new MouthTrackingRenderer<>(
            new MutantZombieGeoModel<>("fire_mutant_zombie", "fire_zombie"),
            "animation.fire_mutant_zombie.",
            clips,
            FireMutantZombie.BREATH);
        renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer));
        return renderer;
    }
}
