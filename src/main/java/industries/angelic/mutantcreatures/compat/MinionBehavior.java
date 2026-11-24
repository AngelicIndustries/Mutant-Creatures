package industries.angelic.mutantcreatures.compat;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.MathHelper;

import thehippomaster.MutantCreatures.MutantZombie;

/**
 * The mutant zombie's minion behavior, for other mods' zombies its roar raises. Their mixins keep the state and call
 * in here: a raised zombie keeps near its leader, takes its target, never turns on a mutant zombie, and drops loot
 * only a third of the time. Whether a zombie is a minion is saved under {@link #NBT_KEY}; after a reload it finds a
 * leader nearby again.
 */
public final class MinionBehavior {

    public static final String NBT_KEY = "MutantCreaturesMinion";

    private MinionBehavior() {}

    /** One tick of a minion's behavior, on the server. Returns its leader afterwards, or null if it has none. */
    public static MutantZombie update(EntityZombie zombie, MutantZombie leader) {
        EntityLivingBase target = zombie.getAttackTarget();
        // A mutant zombie's ground slam hits its own minions; they don't hold it against it.
        if (target != null && (!target.isEntityAlive() || target instanceof MutantZombie)) zombie.setAttackTarget(null);
        if (zombie.getAITarget() instanceof MutantZombie) zombie.setRevengeTarget(null);

        if (leader != null && (!leader.isEntityAlive() || zombie.getDistanceSqToEntity(leader) > 24 * 24)) {
            leader = null;
        }
        if (leader == null) {
            zombie.detachHome();
            List<?> nearby = zombie.worldObj
                .getEntitiesWithinAABB(MutantZombie.class, zombie.boundingBox.expand(16.0D, 16.0D, 16.0D));
            if (!nearby.isEmpty()) leader = (MutantZombie) nearby.get(
                zombie.getRNG()
                    .nextInt(nearby.size()));
        } else {
            zombie.setHomeArea(
                MathHelper.floor_double(leader.posX),
                MathHelper.floor_double(leader.boundingBox.minY),
                MathHelper.floor_double(leader.posZ),
                16);
            if (zombie.getAttackTarget() == null && leader.getAttackTarget() != null) {
                zombie.setAttackTarget(leader.getAttackTarget());
                zombie.setRevengeTarget(leader.getAttackTarget());
            }
        }
        return leader;
    }

    /** Whether a minion drops its loot this time. */
    public static boolean dropsLoot(EntityZombie zombie) {
        return zombie.getRNG()
            .nextInt(3) == 0;
    }

    /**
     * Sets up another mod's zombie as that mod sets up a spawned one, worn as raised zombies come up, and makes it
     * a minion of the leader. Returns null if the entity isn't a zombie the minion mixins reach.
     */
    public static EntityZombie raise(Object entity, MutantZombie leader, double x, double y, double z) {
        if (!(entity instanceof EntityZombie) || !(entity instanceof MutantMinion)) return null;

        EntityZombie zombie = (EntityZombie) entity;
        // Placed first: spawn setup reads the difficulty where the zombie stands.
        zombie.setPosition(x, y, z);
        zombie.onSpawnWithEgg(null);
        zombie.setHealth(
            zombie.getMaxHealth() * (0.6F + 0.4F * leader.getRNG()
                .nextFloat()));
        ((MutantMinion) zombie).mutantcreatures$follow(leader);
        return zombie;
    }
}
