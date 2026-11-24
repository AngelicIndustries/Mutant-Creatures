package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import thehippomaster.MutantCreatures.MutantZombie;

/**
 * The mutant zombie as a husk, from the desert: its slam and leap always leave the target hungry, as a husk's hit
 * does. Chemical X makes one from Et Futurum Requiem's husk. Husks have no villager form, so neither does it.
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

    @Override
    public void onAttackHit(EntityLivingBase target) {
        target.addPotionEffect(new PotionEffect(Potion.hunger.id, 200, 1));
    }
}
