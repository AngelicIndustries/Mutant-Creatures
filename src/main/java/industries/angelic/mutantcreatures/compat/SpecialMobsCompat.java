package industries.angelic.mutantcreatures.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;

import cpw.mods.fml.common.Loader;
import industries.angelic.mutantcreatures.entity.FireMutantZombie;
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
    }

    /**
     * A Special Mobs zombie of the given variant, set up as Special Mobs sets up a spawned one and made a minion of
     * the leader, or null if it can't be made.
     *
     * @param variant the Special Mobs entity name without its prefix, e.g. {@code HungryZombie}
     */
    public static EntityZombie createMinion(String variant, MutantZombie leader, double x, double y, double z) {
        Entity entity = EntityList.createEntityByName(MODID + "." + variant, leader.worldObj);
        if (!(entity instanceof EntityZombie) || !(entity instanceof MutantMinion)) return null;

        EntityZombie zombie = (EntityZombie) entity;
        // Placed first: spawn setup reads the difficulty where the zombie stands.
        zombie.setPosition(x, y, z);
        zombie.onSpawnWithEgg(null);
        // Raised zombies come up worn, as the mutant zombie's own minions do.
        zombie.setHealth(zombie.getMaxHealth() * (0.6F + 0.4F * leader.getRNG()
            .nextFloat()));
        ((MutantMinion) zombie).mutantcreatures$follow(leader);
        return zombie;
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
