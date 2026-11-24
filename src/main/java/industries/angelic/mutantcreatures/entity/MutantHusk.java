package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import industries.angelic.mutantcreatures.compat.EtFuturumCompat;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * The mutant zombie as a husk, from the desert: its slam and leap always leave the target hungry, as a husk's hit
 * does. With Et Futurum Requiem, Chemical X makes one from its husk and its roar raises husks. Husks have no
 * villager form, so neither does it.
 */
public class MutantHusk extends MutantZombie {

    public MutantHusk(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.updateObject(21, (byte) 0);
    }

    @Override
    public void setVillager() {}

    /** With Et Futurum Requiem, its roar raises husks. */
    @Override
    public EntityZombie createMinion(double x, double y, double z) {
        EntityZombie minion = EtFuturumCompat.isLoaded() ? EtFuturumCompat.createHuskMinion(this, x, y, z) : null;
        return minion != null ? minion : super.createMinion(x, y, z);
    }

    @Override
    public void onAttackHit(EntityLivingBase target) {
        target.addPotionEffect(new PotionEffect(Potion.hunger.id, 200, 1));
    }
}
