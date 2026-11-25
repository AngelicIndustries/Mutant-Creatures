package industries.angelic.mutantcreatures.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;

import industries.angelic.mutantcreatures.entity.PlagueCloud;
import industries.angelic.mutantcreatures.entity.PlagueMutantZombie;

/**
 * Retches at a target three to eight blocks away: it rears back, lurches in and spews onto the ground at the
 * target's feet, leaving a plague cloud there. The ticks match the retch clip.
 */
public class AIPlagueRetch extends EntityAIBase {

    private static final int END = 40;
    /** Where the retch lands is fixed a little before it starts, so the spew heads there from the start. */
    private static final int AIM = PlagueMutantZombie.RETCH_START - 4;
    /** The cloud forms once the first of the spew has reached the ground. */
    private static final int LAND = PlagueMutantZombie.RETCH_START + 6;
    private static final double MIN_RANGE = 3.0D;
    private static final double MAX_RANGE = 8.0D;

    private final PlagueMutantZombie zombie;
    private EntityLivingBase target;
    private double aimX, aimY, aimZ;

    public AIPlagueRetch(PlagueMutantZombie zombie) {
        this.zombie = zombie;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.zombie.getAttackTarget();
        if (this.target == null || this.zombie.currentAttackID != 0
            || this.zombie.deathTick > 0
            || !this.zombie.onGround
            || this.zombie.retchCooldown > 0
            || this.zombie.getRNG()
                .nextInt(6) != 0) {
            return false;
        }
        double distance = this.zombie.getDistanceSqToEntity(this.target);
        return distance > MIN_RANGE * MIN_RANGE && distance < MAX_RANGE * MAX_RANGE
            && this.zombie.getEntitySenses()
                .canSee(this.target);
    }

    @Override
    public void startExecuting() {
        this.zombie.sendAttackPacket(PlagueMutantZombie.RETCH);
        this.zombie.animTick = 0;
        this.zombie.getNavigator()
            .clearPathEntity();
        this.zombie.playSound("MutantCreatures:mutantzombie.gruntA", 0.3F, 0.6F);
    }

    @Override
    public boolean continueExecuting() {
        return this.zombie.deathTick <= 0 && this.zombie.animTick < END;
    }

    @Override
    public void updateTask() {
        int tick = this.zombie.animTick;
        // Turns slower while spewing, so the spew trails a target that runs.
        float turn = tick < PlagueMutantZombie.RETCH_START ? 30.0F : 10.0F;
        this.zombie.getLookHelper()
            .setLookPositionWithEntity(this.target, turn, 30.0F);

        if (tick == AIM) this.aim();
        if (tick == PlagueMutantZombie.RETCH_START) {
            this.zombie.playSound("random.burp", 2.0F, 0.5F);
            this.zombie.playSound("mob.slime.big", 2.0F, 0.6F);
        }
        if (tick > PlagueMutantZombie.RETCH_START && tick < PlagueMutantZombie.RETCH_END && tick % 5 == 0) {
            this.zombie.playSound("mob.slime.small", 1.5F, 0.5F + this.zombie.getRNG()
                .nextFloat() * 0.2F);
        }
        if (tick == LAND) {
            this.zombie.worldObj
                .spawnEntityInWorld(new PlagueCloud(this.zombie, this.aimX, this.aimY, this.aimZ));
        }
    }

    /** Aims at the ground under the target's feet, a few blocks down at most. */
    private void aim() {
        this.aimX = this.target.posX;
        this.aimZ = this.target.posZ;
        int x = MathHelper.floor_double(this.aimX);
        int z = MathHelper.floor_double(this.aimZ);
        int y = MathHelper.floor_double(this.target.boundingBox.minY);
        for (int i = 0; i < 4 && this.zombie.worldObj.isAirBlock(x, y - 1, z); i++) y--;
        this.aimY = Math.min(this.target.boundingBox.minY, y);
        this.zombie.aimRetch(this.aimX, this.aimY, this.aimZ);
    }

    @Override
    public void resetTask() {
        this.zombie.sendAttackPacket(0);
        this.zombie.retchCooldown = 80 + this.zombie.getRNG()
            .nextInt(40);
        this.target = null;
    }
}
