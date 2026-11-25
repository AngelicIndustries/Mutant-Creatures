package industries.angelic.mutantcreatures.compat;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;

import cpw.mods.fml.common.Loader;
import industries.angelic.mutantcreatures.entity.BrutishMutantZombie;
import industries.angelic.mutantcreatures.entity.FireMutantZombie;
import industries.angelic.mutantcreatures.entity.FishingMutantZombie;
import industries.angelic.mutantcreatures.entity.HungryMutantZombie;
import thehippomaster.MutantCreatures.MutantCreatures;
import thehippomaster.MutantCreatures.MutantZombie;
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
        addMutant("toast.specialMobs.entity.zombie.EntityFireZombie", FireMutantZombie.class);
        addMutant("toast.specialMobs.entity.zombie.EntityBrutishZombie", BrutishMutantZombie.class);
        addMutant("toast.specialMobs.entity.zombie.EntityFishingZombie", FishingMutantZombie.class);
    }

    /**
     * A Special Mobs zombie of the given variant raised as a minion of the leader, or null if it can't be made.
     *
     * @param variant the Special Mobs entity name without its prefix, e.g. {@code HungryZombie}
     */
    public static EntityZombie createMinion(String variant, MutantZombie leader, double x, double y, double z) {
        return MinionBehavior.raise(EntityList.createEntityByName(MODID + "." + variant, leader.worldObj), leader, x, y, z);
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
