package industries.angelic.mutantcreatures.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import industries.angelic.mutantcreatures.compat.SpecialMobsCompat;
import industries.angelic.mutantcreatures.entity.ai.AIHungryDevour;
import industries.angelic.mutantcreatures.entity.ai.AIHungryEat;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Mutant form of Special Mobs' hungry zombie, with its belly eaten away down to the ribs and spine. It grabs its
 * target, lifts it to its face and bites it, eating food out of a player's inventory to heal, and it walks over to
 * food lying on the ground to eat it. With no stomach, what it eats falls out through its belly.
 *
 * <p>
 * Animation runs on the mutant zombie's attack ID and tick, synced by its attack packet; the client plays the
 * matching GeckoLib clips. There are no GeckoLib controllers.
 */
public class HungryMutantZombie extends MutantZombie implements GeoEntity {

    public static final int DEVOUR = 4;
    public static final int EAT = 5;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public HungryMutantZombie(World world) {
        super(world);
        this.experienceValue = 35;
        this.tasks.addTask(1, new AIHungryDevour(this));
        this.tasks.addTask(1, new AIHungryEat(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth)
            .setBaseValue(160.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
            .setBaseValue(0.29D);
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if (!this.worldObj.isRemote && this.currentAttackID == 0
            && this.onGround
            && entity instanceof EntityLivingBase
            && entity.ridingEntity == null) {
            // Hungrier when hurt.
            int chance = this.getHealth() < this.getMaxHealth() / 2 ? 2 : 3;
            if (this.rand.nextInt(chance) == 0) {
                this.sendAttackPacket(DEVOUR);
                return true;
            }
        }
        return super.attackEntityAsMob(entity);
    }

    /**
     * Where its hands are on each tick of the devour clip, in blocks in front of and above its feet, between the two
     * hands. Taken from the model posed by the clip, so a grabbed target moves with the arms.
     */
    private static final float[] GRIP_FORWARD = {
        0.33F, 0.56F, 0.79F, 1.02F, 1.22F, 1.40F, 1.55F, 1.66F, 1.74F, 1.77F, 1.76F, 1.70F, 1.59F, 1.44F, 1.26F,
        1.26F, 1.31F, 1.36F, 1.41F, 1.40F, 1.36F, 1.32F, 1.28F, 1.26F, 1.26F, 1.26F, 1.31F, 1.36F, 1.41F, 1.40F,
        1.36F, 1.32F, 1.28F, 1.26F, 1.26F, 1.26F, 1.31F, 1.36F, 1.41F, 1.40F, 1.36F, 1.32F, 1.28F, 1.26F, 1.26F,
        1.36F, 1.36F, 1.23F, 1.61F, 1.76F, 1.66F, 1.34F, 0.83F, 0.74F, 0.64F, 0.54F, 0.43F, 0.41F, 0.38F, 0.36F,
        0.33F };
    private static final float[] GRIP_UP = {
        0.73F, 0.76F, 0.84F, 0.95F, 1.10F, 1.29F, 1.50F, 1.74F, 2.00F, 2.27F, 2.55F, 2.65F, 2.81F, 2.98F, 3.13F,
        3.13F, 3.04F, 2.95F, 2.86F, 2.87F, 2.95F, 3.03F, 3.10F, 3.13F, 3.13F, 3.13F, 3.04F, 2.95F, 2.86F, 2.87F,
        2.95F, 3.03F, 3.10F, 3.13F, 3.13F, 3.13F, 3.04F, 2.95F, 2.86F, 2.87F, 2.95F, 3.03F, 3.10F, 3.13F, 3.13F,
        3.33F, 3.52F, 3.70F, 3.14F, 2.50F, 1.85F, 1.27F, 0.84F, 0.76F, 0.70F, 0.65F, 0.61F, 0.63F, 0.66F, 0.69F,
        0.73F };

    /** Where a grabbed target is held on this tick of the devour: centred in its hands, feet at the returned y. */
    public double[] holdPosition(Entity target, int tick) {
        int i = Math.max(0, Math.min(GRIP_FORWARD.length - 1, tick));
        float yaw = this.renderYawOffset * (float) Math.PI / 180.0F;
        return new double[] { this.posX - MathHelper.sin(yaw) * GRIP_FORWARD[i],
            this.posY + GRIP_UP[i] - target.height * 0.5D, this.posZ + MathHelper.cos(yaw) * GRIP_FORWARD[i] };
    }

    /** Takes up to 8 of a random food from the player's inventory, or null if they carry none. */
    public ItemStack takeFood(EntityPlayer player) {
        ItemStack[] inventory = player.inventory.mainInventory;
        List<Integer> food = new ArrayList<>();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].getItem() instanceof ItemFood) food.add(i);
        }
        if (food.isEmpty()) return null;

        int slot = food.get(this.rand.nextInt(food.size()));
        ItemStack taken = inventory[slot].splitStack(Math.min(8, inventory[slot].stackSize));
        if (inventory[slot].stackSize <= 0) inventory[slot] = null;
        return taken;
    }

    /** Eats a stack of food and heals by its food value. */
    public void eat(ItemStack stack) {
        this.heal(((ItemFood) stack.getItem()).func_150905_g(stack) * stack.stackSize);
    }

    /** With no stomach, what it eats falls out through the hole in its belly. */
    public void dropCrumbs(ItemStack food) {
        if (!(this.worldObj instanceof WorldServer)) return;
        float yaw = this.renderYawOffset * (float) Math.PI / 180.0F;
        // The belly sits half a block behind the middle of the body, 1.6 blocks up.
        double back = 0.45D;
        ((WorldServer) this.worldObj).func_147487_a(
            "iconcrack_" + Item.getIdFromItem(food.getItem()),
            this.posX + MathHelper.sin(yaw) * back,
            this.posY + 1.6D,
            this.posZ - MathHelper.cos(yaw) * back,
            24,
            0.3D,
            0.15D,
            0.3D,
            0.05D);
    }

    public void playChompSound() {
        this.worldObj.playSoundAtEntity(this, "random.eat", 1.0F, 0.5F + this.rand.nextFloat() * 0.2F);
    }

    public void playBurpSound() {
        this.worldObj.playSoundAtEntity(this, "random.burp", 1.0F, 0.5F + this.rand.nextFloat() * 0.1F);
    }

    /** Its roar raises Special Mobs' hungry zombies. */
    @Override
    public EntityZombie createMinion(double x, double y, double z) {
        EntityZombie minion = SpecialMobsCompat.createMinion("HungryZombie", this, x, y, z);
        return minion != null ? minion : super.createMinion(x, y, z);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        super.dropFewItems(recentlyHit, looting);
        if (recentlyHit) {
            this.dropItem(Items.apple, 1 + this.rand.nextInt(2 + looting));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
