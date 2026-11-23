package industries.angelic.mutantcreatures.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
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
     * Where a grabbed target is held, feet at the returned y: centred between the hands of the devour clip, which
     * hold it up to the face, 1.45 blocks in front of the body and 3.15 blocks up.
     */
    public double[] holdPosition(Entity target) {
        float yaw = this.renderYawOffset * (float) Math.PI / 180.0F;
        double forward = 1.45D;
        return new double[] { this.posX - MathHelper.sin(yaw) * forward, this.posY + 3.15D - target.height * 0.5D,
            this.posZ + MathHelper.cos(yaw) * forward };
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
