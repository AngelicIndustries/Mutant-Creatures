package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.world.World;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import industries.angelic.mutantcreatures.compat.SpecialMobsCompat;
import industries.angelic.mutantcreatures.entity.ai.AIBrutishSweep;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Mutant form of Special Mobs' brutish zombie: two big teeth, a huge wooden sword and the brutish zombie's extra
 * armor. Its hits hurt through armor like the brutish zombie's Pain sword, it cleaves everything in front of it with
 * a sword sweep, and its roar raises brutish zombies.
 */
public class BrutishMutantZombie extends MutantZombie implements GeoEntity {

    public static final int SWEEP = 4;
    /** Health a hit takes past armor, as the brutish zombie's Pain sword does. */
    private static final float PAIN = 2.0F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BrutishMutantZombie(World world) {
        super(world);
        this.experienceValue = 35;
        this.tasks.addTask(1, new AIBrutishSweep(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth)
            .setBaseValue(180.0D);
    }

    /** The mutant zombie's own armor plus the brutish zombie's 10. */
    @Override
    public int getTotalArmorValue() {
        return super.getTotalArmorValue() + 10;
    }

    @Override
    public boolean attackEntityAsMob(net.minecraft.entity.Entity entity) {
        if (!this.worldObj.isRemote && this.currentAttackID == 0
            && this.onGround
            && this.rand.nextInt(3) == 0) {
            this.sendAttackPacket(SWEEP);
            return true;
        }
        return super.attackEntityAsMob(entity);
    }

    /** Takes a little health past armor, never the last of it. Creative players are left alone. */
    @Override
    public void onAttackHit(EntityLivingBase target) {
        if (!target.isEntityAlive() || target.getHealth() <= PAIN) return;
        if (target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.disableDamage) return;
        target.setHealth(target.getHealth() - PAIN);
    }

    /** Its roar raises Special Mobs' brutish zombies. */
    @Override
    public EntityZombie createMinion(double x, double y, double z) {
        EntityZombie minion = SpecialMobsCompat.createMinion("BrutishZombie", this, x, y, z);
        return minion != null ? minion : super.createMinion(x, y, z);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        super.dropFewItems(recentlyHit, looting);
        if (recentlyHit) {
            this.dropItem(Items.flint, 1 + this.rand.nextInt(2 + looting));
        }
    }

    @Override
    protected void dropRareDrop(int looting) {
        this.dropItem(Items.iron_ingot, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
