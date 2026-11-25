package industries.angelic.mutantcreatures.entity.ai;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import industries.angelic.mutantcreatures.entity.BrutishMutantZombie;
import thehippomaster.MutantCreatures.MutantCreatures;

/**
 * The sword sweep: it winds up to the right and cleaves everything in a half circle in front of it, knocking it far
 * away. The ticks match the sweep clip.
 */
public class AIBrutishSweep extends EntityAIBase {

    private static final int SWING = 9;
    private static final int HIT = 13;
    private static final int END = 32;
    private static final double RANGE = 5.0D;

    private final BrutishMutantZombie zombie;
    private EntityLivingBase target;

    public AIBrutishSweep(BrutishMutantZombie zombie) {
        this.zombie = zombie;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.zombie.getAttackTarget();
        return this.target != null && this.zombie.currentAttackID == BrutishMutantZombie.SWEEP;
    }

    @Override
    public void startExecuting() {
        this.zombie.animTick = 0;
        this.zombie.getNavigator()
            .clearPathEntity();
        this.zombie.playSound("MutantCreatures:mutantzombie.gruntA", 0.3F, 0.7F);
    }

    @Override
    public boolean continueExecuting() {
        return this.zombie.deathTick <= 0 && this.zombie.animTick < END;
    }

    @Override
    public void updateTask() {
        int tick = this.zombie.animTick;
        if (tick < SWING) {
            this.zombie.getLookHelper()
                .setLookPositionWithEntity(this.target, 30.0F, 30.0F);
        }
        if (tick == SWING) this.zombie.playSound("random.bow", 1.5F, 0.4F);
        if (tick == HIT) this.cleave();
    }

    /** Hits everything within reach in the half circle in front of its body. */
    private void cleave() {
        float yaw = this.zombie.renderYawOffset * (float) Math.PI / 180.0F;
        double frontX = -MathHelper.sin(yaw);
        double frontZ = MathHelper.cos(yaw);
        List<?> entities = this.zombie.worldObj.getEntitiesWithinAABBExcludingEntity(
            this.zombie,
            this.zombie.boundingBox.expand(RANGE, 2.0D, RANGE));
        boolean hit = false;
        for (Object object : entities) {
            if (!(object instanceof EntityLivingBase) || this.zombie.isAlly((Entity) object)) continue;

            EntityLivingBase living = (EntityLivingBase) object;
            double dx = living.posX - this.zombie.posX;
            double dz = living.posZ - this.zombie.posZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > RANGE || dx * frontX + dz * frontZ < 0) continue;

            // Creative players, and mobs still shrugging off a recent hit, are not flung either.
            if (!living.attackEntityFrom(DamageSource.causeMobDamage(this.zombie), 10.0F)) continue;
            this.zombie.onAttackHit(living);
            double away = distance < 1.0E-4 ? 1 : distance;
            living.motionX = dx / away * 1.6D;
            living.motionY = 0.45D;
            living.motionZ = dz / away * 1.6D;
            if (living instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) living;
                MutantCreatures.sendPacketToAll(player, new S12PacketEntityVelocity(player));
            }
            hit = true;
        }
        this.zombie.playSound(hit ? "mob.zombie.woodbreak" : "mob.zombie.wood", 1.5F, 0.6F);
    }

    @Override
    public void resetTask() {
        this.zombie.sendAttackPacket(0);
        this.target = null;
    }
}
