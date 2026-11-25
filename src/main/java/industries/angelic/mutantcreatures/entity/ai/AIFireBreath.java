package industries.angelic.mutantcreatures.entity.ai;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

import industries.angelic.mutantcreatures.entity.FireMutantZombie;

/**
 * Breathes fire at a target three to nine blocks away: it rears back, then sweeps a cone of flame that sets
 * everything in it alight. Water puts the breath out. The ticks match the breath clip.
 */
public class AIFireBreath extends EntityAIBase {

    private static final int END = 50;
    private static final double RANGE = 8.0D;
    /** Cosine of the cone's half angle, about 30 degrees. */
    private static final double CONE = 0.86D;

    private final FireMutantZombie zombie;
    private EntityLivingBase target;

    public AIFireBreath(FireMutantZombie zombie) {
        this.zombie = zombie;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.zombie.getAttackTarget();
        if (this.target == null || this.zombie.currentAttackID != 0
            || this.zombie.deathTick > 0
            || !this.zombie.onGround
            || this.zombie.isWet()
            || this.zombie.getRNG()
                .nextInt(30) != 0) {
            return false;
        }
        double distance = this.zombie.getDistanceSqToEntity(this.target);
        return distance > 3 * 3 && distance < 9 * 9
            && this.zombie.getEntitySenses()
                .canSee(this.target);
    }

    @Override
    public void startExecuting() {
        this.zombie.sendAttackPacket(FireMutantZombie.BREATH);
        this.zombie.animTick = 0;
        this.zombie.getNavigator()
            .clearPathEntity();
        this.zombie.playSound("mob.blaze.breathe", 1.5F, 0.6F);
    }

    @Override
    public boolean continueExecuting() {
        return this.zombie.deathTick <= 0 && this.zombie.animTick < END && !this.zombie.isWet();
    }

    @Override
    public void updateTask() {
        int tick = this.zombie.animTick;
        // Turns slower while breathing, so the cone sweeps after a target that runs.
        float turn = tick < FireMutantZombie.BREATH_START ? 30.0F : 10.0F;
        this.zombie.getLookHelper()
            .setLookPositionWithEntity(this.target, turn, 30.0F);

        if (tick == FireMutantZombie.BREATH_START) {
            this.zombie.playSound("mob.ghast.fireball", 2.0F, 0.6F);
        }
        if (tick >= FireMutantZombie.BREATH_START && tick < FireMutantZombie.BREATH_END) {
            if (tick % 5 == 0) this.zombie.playSound("fire.fire", 2.0F, 0.6F + this.zombie.getRNG()
                .nextFloat() * 0.3F);
            if (tick % 3 == 0) this.burn(tick % 6 == 0);
        }
    }

    /** Sets everything in the cone alight, hurting it every other time. */
    private void burn(boolean hurt) {
        double[] mouth = this.zombie.mouthPosition();
        Vec3 look = this.zombie.headLook();
        List<?> entities = this.zombie.worldObj.getEntitiesWithinAABBExcludingEntity(
            this.zombie,
            this.zombie.boundingBox.expand(RANGE + 2, RANGE, RANGE + 2));
        for (Object object : entities) {
            if (!(object instanceof EntityLivingBase) || this.zombie.isAlly((Entity) object)) continue;

            EntityLivingBase living = (EntityLivingBase) object;
            double dx = living.posX - mouth[0];
            double dy = living.posY + living.height * 0.5D - mouth[1];
            double dz = living.posZ - mouth[2];
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > RANGE || distance < 1.0E-4) continue;
            if ((dx * look.xCoord + dy * look.yCoord + dz * look.zCoord) / distance < CONE) continue;

            living.setFire(6);
            if (hurt) {
                living.attackEntityFrom(
                    DamageSource.causeMobDamage(this.zombie)
                        .setFireDamage(),
                    3.0F);
            }
        }
    }

    @Override
    public void resetTask() {
        this.zombie.sendAttackPacket(0);
        this.target = null;
    }
}
