package industries.angelic.mutantcreatures.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import industries.angelic.mutantcreatures.entity.FishingMutantZombie;
import industries.angelic.mutantcreatures.entity.MutantFishHook;

/**
 * Casts the hook at a target four to sixteen blocks away, waits for it to land, then reels in: a hooked target is
 * yanked over to land in front of it, and its ground slam follows. The ticks match the cast and reel clips.
 */
public class AIFishingCast extends EntityAIBase {

    private static final int RELEASE = FishingMutantZombie.CAST_RELEASE;
    /** How long it waits for the hook to land before it reels in anyway. */
    private static final int WAIT = 50;
    private static final int YANK = 5;
    private static final int REEL_END = 14;
    private static final double MIN_RANGE = 4.0D;
    private static final double MAX_RANGE = 16.0D;
    /** The hook's speed across the ground, in blocks per tick. */
    private static final double HOOK_SPEED = 1.4D;
    /** A yanked target this close when the reel ends gets slammed. */
    private static final double SLAM_RANGE = 8.0D;

    private final FishingMutantZombie zombie;
    private EntityLivingBase target;
    private Entity yanked;

    public AIFishingCast(FishingMutantZombie zombie) {
        this.zombie = zombie;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.zombie.getAttackTarget();
        if (this.target == null || this.zombie.currentAttackID != 0
            || this.zombie.deathTick > 0
            || !this.zombie.onGround
            || this.zombie.castCooldown > 0
            || this.zombie.getHook() != null
            || this.zombie.getRNG()
                .nextInt(4) != 0) {
            return false;
        }
        double distance = this.zombie.getDistanceSqToEntity(this.target);
        return distance > MIN_RANGE * MIN_RANGE && distance < MAX_RANGE * MAX_RANGE
            && this.zombie.getEntitySenses()
                .canSee(this.target);
    }

    @Override
    public void startExecuting() {
        this.yanked = null;
        this.zombie.startAttack(FishingMutantZombie.CAST);
        this.zombie.getNavigator()
            .clearPathEntity();
        this.zombie.playSound("MutantCreatures:mutantzombie.gruntA", 0.3F, 0.7F);
    }

    @Override
    public boolean continueExecuting() {
        if (this.zombie.deathTick > 0) return false;
        if (this.zombie.currentAttackID == FishingMutantZombie.CAST) return this.zombie.animTick < RELEASE + WAIT;
        return this.zombie.currentAttackID == FishingMutantZombie.REEL && this.zombie.animTick < REEL_END;
    }

    @Override
    public void updateTask() {
        int tick = this.zombie.animTick;
        MutantFishHook hook = this.zombie.getHook();
        if (this.zombie.currentAttackID == FishingMutantZombie.CAST) {
            if (tick <= RELEASE && this.target.isEntityAlive()) {
                this.zombie.getLookHelper()
                    .setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            }
            if (tick == RELEASE) {
                this.cast();
            } else if (tick > RELEASE && (hook == null || hook.getHooked() != null || hook.isStopped())) {
                this.reel();
            } else if (tick == RELEASE + WAIT - 1) {
                this.reel();
            }
            return;
        }

        Entity hooked = hook != null ? hook.getHooked() : null;
        if (hooked != null) {
            this.zombie.getLookHelper()
                .setLookPositionWithEntity(hooked, 30.0F, 30.0F);
        }
        if (tick == YANK) {
            if (hooked != null) {
                this.zombie.yank(hooked);
                this.yanked = hooked;
                this.zombie.playSound("random.bow", 1.5F, 0.5F);
            }
            if (hook != null) hook.setDead();
        }
    }

    private void cast() {
        double[] from = this.zombie.releasePoint();
        MutantFishHook hook = new MutantFishHook(this.zombie, from[0], from[1], from[2], this.target, HOOK_SPEED);
        this.zombie.worldObj.spawnEntityInWorld(hook);
        this.zombie.playSound("random.bow", 1.0F, 0.4F);
    }

    private void reel() {
        this.zombie.startAttack(FishingMutantZombie.REEL);
    }

    @Override
    public void resetTask() {
        MutantFishHook hook = this.zombie.getHook();
        if (hook != null) hook.setDead();
        this.zombie.castCooldown = 40 + this.zombie.getRNG()
            .nextInt(20);
        // The slam follows a yank: the target is in the air, coming down in front of it.
        if (this.yanked != null && this.yanked == this.target
            && this.yanked.isEntityAlive()
            && this.zombie.getDistanceSqToEntity(this.yanked) < SLAM_RANGE * SLAM_RANGE
            && this.zombie.onGround) {
            this.zombie.startAttack(1);
        } else {
            this.zombie.sendAttackPacket(0);
        }
        this.target = null;
        this.yanked = null;
    }
}
