package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import industries.angelic.mutantcreatures.compat.SpecialMobsCompat;
import industries.angelic.mutantcreatures.entity.ai.AIPlagueRetch;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Mutant form of Special Mobs' plague zombie: sickly and covered in sores, and faster than the mutant zombie. Its
 * hits give two doses of plague, it retches a plague cloud onto the ground at targets a few blocks away, and its roar
 * raises plague zombies.
 */
public class PlagueMutantZombie extends MutantZombie implements GeoEntity, MouthAttacker {

    public static final int RETCH = 4;
    /** Ticks of the retch attack during which it spews: from when the retch clip has lurched in. */
    public static final int RETCH_START = 12;
    public static final int RETCH_END = 30;
    /** Where the retch lands, synced so the spew heads there. */
    private static final int AIM_X_WATCHER = 22;
    private static final int AIM_Y_WATCHER = 23;
    private static final int AIM_Z_WATCHER = 24;
    /** Where the mouth is while it retches, from the model: blocks in front of and above its feet. */
    private static final double MOUTH_FORWARD = 1.2D;
    private static final double MOUTH_UP = 1.9D;
    /** The spew's speed toward where it lands, in blocks per tick. */
    private static final double SPEW_SPEED = 0.5D;
    /** The fall per tick of slime ball crumbs, which the spew is aimed up to make up for. */
    private static final double CRUMB_GRAVITY = 0.04D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final DrawnPoint drawnMouth = new DrawnPoint();
    /** Ticks until it can retch again. */
    public int retchCooldown;

    public PlagueMutantZombie(World world) {
        super(world);
        this.experienceValue = 35;
        this.tasks.addTask(1, new AIPlagueRetch(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(AIM_X_WATCHER, 0.0F);
        this.dataWatcher.addObject(AIM_Y_WATCHER, 0.0F);
        this.dataWatcher.addObject(AIM_Z_WATCHER, 0.0F);
    }

    /** Faster than the mutant zombie, as the plague zombie is than a zombie. */
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
            .setBaseValue(0.26D * 1.3D);
    }

    @Override
    public void onAttackHit(EntityLivingBase target) {
        PlagueEffects.dose(target, 2);
    }

    /** Sets where the coming retch lands. */
    public void aimRetch(double x, double y, double z) {
        this.dataWatcher.updateObject(AIM_X_WATCHER, (float) x);
        this.dataWatcher.updateObject(AIM_Y_WATCHER, (float) y);
        this.dataWatcher.updateObject(AIM_Z_WATCHER, (float) z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.isEntityAlive()) return;
        if (!this.worldObj.isRemote) {
            if (this.retchCooldown > 0) this.retchCooldown--;
        } else if (this.currentAttackID == RETCH && this.animTick >= RETCH_START && this.animTick < RETCH_END) {
            this.spawnSpew();
        }
    }

    /**
     * Slime ball crumbs arcing from the mouth as last drawn to where the retch lands, with sickly green mist along
     * the way.
     */
    private void spawnSpew() {
        double[] from = this.drawnMouth.get(this, this.mouthPosition());
        double dx = this.dataWatcher.getWatchableObjectFloat(AIM_X_WATCHER) - from[0];
        double dy = this.dataWatcher.getWatchableObjectFloat(AIM_Y_WATCHER) - from[1];
        double dz = this.dataWatcher.getWatchableObjectFloat(AIM_Z_WATCHER) - from[2];
        double ticks = Math.max(1.0D, Math.sqrt(dx * dx + dy * dy + dz * dz) / SPEW_SPEED);
        String crumb = "iconcrack_" + Item.getIdFromItem(Items.slime_ball);
        for (int i = 0; i < 4; i++) {
            this.worldObj.spawnParticle(
                crumb,
                from[0],
                from[1],
                from[2],
                dx / ticks + this.rand.nextGaussian() * 0.04D,
                dy / ticks + 0.5D * CRUMB_GRAVITY * ticks + this.rand.nextGaussian() * 0.04D,
                dz / ticks + this.rand.nextGaussian() * 0.04D);
        }
        double along = this.rand.nextDouble();
        this.worldObj.spawnParticle(
            "mobSpell",
            from[0] + dx * along,
            from[1] + dy * along,
            from[2] + dz * along,
            0.5D,
            0.6D,
            0.18D);
    }

    @Override
    public DrawnPoint drawnMouth() {
        return this.drawnMouth;
    }

    /** Where the mouth is while it retches, turned the way its body faces. */
    public double[] mouthPosition() {
        float yaw = this.renderYawOffset * (float) Math.PI / 180.0F;
        return new double[] { this.posX - MathHelper.sin(yaw) * MOUTH_FORWARD, this.posY + MOUTH_UP,
            this.posZ + MathHelper.cos(yaw) * MOUTH_FORWARD };
    }

    /** Its roar raises Special Mobs' plague zombies. */
    @Override
    public EntityZombie createMinion(double x, double y, double z) {
        EntityZombie minion = SpecialMobsCompat.createMinion("PlagueZombie", this, x, y, z);
        return minion != null ? minion : super.createMinion(x, y, z);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        super.dropFewItems(recentlyHit, looting);
        if (recentlyHit) {
            for (int i = 1 + this.rand.nextInt(3 + looting); i > 0; i--) {
                this.dropItem(Item.getItemFromBlock(this.rand.nextBoolean() ? Blocks.brown_mushroom : Blocks.red_mushroom), 1);
            }
        }
    }

    @Override
    protected void dropRareDrop(int looting) {
        this.dropItem(Items.fermented_spider_eye, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
