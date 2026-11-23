package industries.angelic.mutantcreatures.entity.ai;

import java.util.List;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import industries.angelic.mutantcreatures.entity.HungryMutantZombie;

/**
 * Walks to food lying on the ground and eats it. With a target it only goes for food close by, so throwing food at
 * its feet distracts it for a moment. The ticks match the eat clip.
 */
public class AIHungryEat extends EntityAIBase {

    private static final int SWALLOW = 16;
    private static final int CRUMBS = 21;
    private static final int BURP = 27;
    private static final int END = 30;
    private static final int GIVE_UP = 200;

    private final HungryMutantZombie zombie;
    private EntityItem food;
    private ItemStack eaten;
    private boolean eating;
    private int ticks;

    public AIHungryEat(HungryMutantZombie zombie) {
        this.zombie = zombie;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        if (this.zombie.currentAttackID != 0 || this.zombie.deathTick > 0
            || !this.zombie.onGround
            || this.zombie.ticksExisted % 10 != 0) {
            return false;
        }
        this.food = this.findFood(this.zombie.getAttackTarget() == null ? 16.0D : 4.0D);
        return this.food != null;
    }

    private EntityItem findFood(double range) {
        EntityItem nearest = null;
        double best = range * range;
        List<?> items = this.zombie.worldObj
            .getEntitiesWithinAABB(EntityItem.class, this.zombie.boundingBox.expand(range, 4.0D, range));
        for (Object object : items) {
            EntityItem item = (EntityItem) object;
            ItemStack stack = item.getEntityItem();
            if (!item.isEntityAlive() || stack == null || !(stack.getItem() instanceof ItemFood)) continue;
            double distance = this.zombie.getDistanceSqToEntity(item);
            if (distance < best) {
                best = distance;
                nearest = item;
            }
        }
        return nearest;
    }

    @Override
    public void startExecuting() {
        this.eating = false;
        this.ticks = 0;
        this.zombie.getNavigator()
            .tryMoveToEntityLiving(this.food, 1.1D);
    }

    @Override
    public boolean continueExecuting() {
        if (this.zombie.deathTick > 0) return false;
        if (this.eating) return this.zombie.animTick < END;
        return this.food.isEntityAlive() && this.ticks < GIVE_UP && this.zombie.currentAttackID == 0;
    }

    @Override
    public void updateTask() {
        this.ticks++;
        this.zombie.getLookHelper()
            .setLookPositionWithEntity(this.food, 30.0F, 30.0F);

        if (!this.eating) {
            double dx = this.food.posX - this.zombie.posX;
            double dz = this.food.posZ - this.zombie.posZ;
            if (dx * dx + dz * dz < 2.4D * 2.4D) {
                this.eating = true;
                this.zombie.getNavigator()
                    .clearPathEntity();
                this.zombie.sendAttackPacket(HungryMutantZombie.EAT);
                this.zombie.animTick = 0;
            } else if (this.ticks % 20 == 0) {
                this.zombie.getNavigator()
                    .tryMoveToEntityLiving(this.food, 1.1D);
            }
            return;
        }

        if (this.zombie.animTick == SWALLOW && this.food.isEntityAlive()) {
            ItemStack stack = this.food.getEntityItem();
            this.eaten = stack.splitStack(Math.min(8, stack.stackSize));
            if (stack.stackSize <= 0) this.food.setDead();
            else this.food.setEntityItemStack(stack);
            this.zombie.eat(this.eaten);
            this.zombie.playChompSound();
        }
        if (this.zombie.animTick == CRUMBS && this.eaten != null) {
            this.zombie.dropCrumbs(this.eaten);
        }
        if (this.zombie.animTick == BURP) {
            this.zombie.playBurpSound();
        }
    }

    @Override
    public void resetTask() {
        if (this.eating) this.zombie.sendAttackPacket(0);
        this.eating = false;
        this.food = null;
        this.eaten = null;
        this.zombie.getNavigator()
            .clearPathEntity();
    }
}
