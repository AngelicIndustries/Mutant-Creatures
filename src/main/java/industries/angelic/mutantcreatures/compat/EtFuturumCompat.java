package industries.angelic.mutantcreatures.compat;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;

import cpw.mods.fml.common.Loader;
import industries.angelic.mutantcreatures.entity.MutantHusk;
import thehippomaster.MutantCreatures.MutantCreatures;
import thehippomaster.MutantCreatures.MutantZombie;
import thehippomaster.MutantCreatures.item.ChemicalX;

/** Mutant forms of Et Futurum Requiem's mobs. Its classes are looked up by name, so it stays optional. */
public final class EtFuturumCompat {

    public static final String MODID = "etfuturum";

    private EtFuturumCompat() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(MODID);
    }

    /** An Et Futurum husk raised as a minion of the leader, or null if it can't be made. */
    public static EntityZombie createHuskMinion(MutantZombie leader, double x, double y, double z) {
        return MinionBehavior.raise(EntityList.createEntityByName(MODID + ".husk", leader.worldObj), leader, x, y, z);
    }

    /** Lets Chemical X turn Et Futurum's husk into a mutant husk. */
    @SuppressWarnings("unchecked")
    public static void registerMutants() {
        String husk = "ganymedes01.etfuturum.entities.EntityHusk";
        try {
            ChemicalX.addMutant((Class<? extends EntityLivingBase>) Class.forName(husk), MutantHusk.class);
        } catch (ClassNotFoundException e) {
            MutantCreatures.LOG.warn("Et Futurum class {} not found; Chemical X won't make mutant husks", husk);
        }
    }
}
