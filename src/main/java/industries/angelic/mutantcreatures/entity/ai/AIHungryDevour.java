package industries.angelic.mutantcreatures.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

import industries.angelic.mutantcreatures.entity.HungryMutantZombie;
import thehippomaster.MutantCreatures.MutantCreatures;

/**
 * Grabs the target, lifts it to the face for three bites and flings it away. The ticks match the devour clip.
 */
public class AIHungryDevour extends EntityAIBase {

    private static final int GRAB = 10;
    private static final int FLING = 46;
    private static final int END = 60;

    private final HungryMutantZombie zombie;
    private EntityLivingBase target;
    private EntityLivingBase held;

    public AIHungryDevour(HungryMutantZombie zombie) {
        this.zombie = zombie;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        this.target = this.zombie.getAttackTarget();
        return this.target != null && this.zombie.currentAttackID == HungryMutantZombie.DEVOUR;
    }

    @Override
    public void startExecuting() {
        this.zombie.animTick = 0;
        this.held = null;
        this.zombie.getNavigator()
            .clearPathEntity();
        this.zombie.worldObj.playSoundAtEntity(
            this.zombie,
            "MutantCreatures:mutantzombie.gruntA",
            0.3F,
            0.7F + this.zombie.getRNG()
                .nextFloat() * 0.2F);
    }

    @Override
    public boolean continueExecuting() {
        return this.zombie.deathTick <= 0 && this.zombie.animTick < END;
    }

    @Override
    public void updateTask() {
        int tick = this.zombie.animTick;
        if (this.held == null && tick < FLING) {
            this.zombie.getLookHelper()
                .setLookPositionWithEntity(this.target, 30.0F, 30.0F);
        }

        if (tick == GRAB && this.target.isEntityAlive() && this.target.ridingEntity == null) {
            double[] hold = this.zombie.holdPosition(this.target);
            double dx = hold[0] - this.target.posX;
            double dz = hold[2] - this.target.posZ;
            if (dx * dx + dz * dz < 2.5D * 2.5D && Math.abs(hold[1] - this.target.posY) < 2.5D) {
                this.held = this.target;
            }
        }

        if (this.held != null) {
            if (!this.held.isEntityAlive()) {
                this.held = null;
                return;
            }
            if (tick == 18 || tick == 28 || tick == 38) {
                this.bite();
            }
            if (tick >= FLING) {
                this.fling();
                this.held = null;
                return;
            }
            this.pull();
        }
    }

    /** Pulls the held target to the face; for players the velocity is sent each tick, as their client moves them. */
    private void pull() {
        double[] hold = this.zombie.holdPosition(this.held);
        this.held.motionX = (hold[0] - this.held.posX) * 0.5D;
        this.held.motionY = (hold[1] - this.held.posY) * 0.5D + 0.08D;
        this.held.motionZ = (hold[2] - this.held.posZ) * 0.5D;
        this.held.fallDistance = 0;
        sendVelocity(this.held);
    }

    private void bite() {
        this.held.attackEntityFrom(DamageSource.causeMobDamage(this.zombie), 5.0F);
        this.zombie.playChompSound();
        ItemStack food = this.held instanceof EntityPlayer ? this.zombie.takeFood((EntityPlayer) this.held) : null;
        if (food != null) {
            this.zombie.eat(food);
            this.zombie.dropCrumbs(food);
            this.zombie.playBurpSound();
        } else {
            // Nothing to eat in the pockets, so it eats the target instead.
            this.zombie.heal(2.0F);
            this.held.addPotionEffect(new PotionEffect(Potion.hunger.id, 200, 1));
        }
    }

    private void fling() {
        float yaw = this.zombie.renderYawOffset * (float) Math.PI / 180.0F;
        this.held.attackEntityFrom(DamageSource.causeMobDamage(this.zombie), 6.0F);
        this.held.motionX = -MathHelper.sin(yaw) * 1.4D;
        this.held.motionY = 0.7D;
        this.held.motionZ = MathHelper.cos(yaw) * 1.4D;
        sendVelocity(this.held);
        this.zombie.worldObj.playSoundAtEntity(
            this.zombie,
            "MutantCreatures:mutantzombie.gruntB",
            0.3F,
            0.8F + this.zombie.getRNG()
                .nextFloat() * 0.4F);
    }

    private static void sendVelocity(EntityLivingBase living) {
        if (living instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) living;
            MutantCreatures.sendPacketToAll(player, new S12PacketEntityVelocity(player));
        }
    }

    @Override
    public void resetTask() {
        this.zombie.sendAttackPacket(0);
        this.held = null;
        this.target = null;
    }
}
