package industries.angelic.mutantcreatures.compat;

import net.minecraft.entity.EntityLivingBase;

import cpw.mods.fml.common.Loader;
import industries.angelic.mutantcreatures.entity.HungryMutantZombie;
import thehippomaster.MutantCreatures.MutantCreatures;
import thehippomaster.MutantCreatures.item.ChemicalX;

/** Mutant forms of Special Mobs' variants. Special Mobs' classes are looked up by name, so it stays optional. */
public final class SpecialMobsCompat {

    public static final String MODID = "SpecialMobs";

    private SpecialMobsCompat() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    /** Lets Chemical X turn Special Mobs' variants into their mutant forms. */
    public static void registerMutants() {
        addMutant("toast.specialMobs.entity.zombie.EntityHungryZombie", HungryMutantZombie.class);
    }

    @SuppressWarnings("unchecked")
    private static void addMutant(String className, Class<? extends EntityLivingBase> mutant) {
        try {
            ChemicalX.addMutant((Class<? extends EntityLivingBase>) Class.forName(className), mutant);
        } catch (ClassNotFoundException e) {
            MutantCreatures.LOG.warn("Special Mobs class {} not found; Chemical X won't make its mutant", className);
        }
    }
}
