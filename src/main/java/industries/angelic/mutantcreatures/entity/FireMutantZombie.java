package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import industries.angelic.mutantcreatures.compat.SpecialMobsCompat;
import industries.angelic.mutantcreatures.entity.ai.AIFireBreath;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Mutant form of Special Mobs' fire zombie: charred, always aflame and immune to fire. Its hits set targets alight,
 * it breathes fire at targets a few blocks away, and its roar raises Special Mobs' fire zombies. Water hurts it,
 * and while it lies dead water is what finishes it; fire does not.
 */
public class FireMutantZombie extends MutantZombie implements GeoEntity {

    public static final int BREATH = 4;
    /** Ticks of the breath attack during which it breathes fire: from when the breath clip has the face level. */
    public static final int BREATH_START = 15;
    public static final int BREATH_END = 40;
    /** Where the mouth is while it breathes, from the model: blocks in front of and above its feet. */
    private static final double MOUTH_FORWARD = 1.1D;
    private static final double MOUTH_UP = 2.35D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    /** The mouth in the world as last drawn; set by the renderer. */
    private double[] drawnMouth;
    private int drawnMouthTick;

    public FireMutantZombie(World world) {
        super(world);
        this.experienceValue = 35;
        this.isImmuneToFire = true;
        this.getNavigator()
            .setAvoidsWater(true);
        this.tasks.addTask(1, new AIFireBreath(this));
    }

    /** Always aflame while alive and dry, as Special Mobs' fire zombie is. */
    @Override
    public boolean isBurning() {
        return this.isEntityAlive() && !this.isWet();
    }

    /**
     * Its fire shows as flames rising off it. The vanilla fire overlay would cover the whole model at this size, and
     * its glowing cracks with it.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    /** Fire keeps it going; doused while it lies dead, it crumbles away. */
    @Override
    protected boolean isVanishing() {
        return this.isWet();
    }

    @Override
    public void onAttackHit(EntityLivingBase target) {
        target.setFire(8);
    }

    /** Its roar raises Special Mobs' fire zombies. */
    @Override
    public EntityZombie createMinion(double x, double y, double z) {
        EntityZombie minion = SpecialMobsCompat.createMinion("FireZombie", this, x, y, z);
        return minion != null ? minion : super.createMinion(x, y, z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.isEntityAlive()) return;

        if (this.isWet()) {
            if (!this.worldObj.isRemote && this.ticksExisted % 10 == 0) {
                this.attackEntityFrom(DamageSource.drown, 1.0F);
                this.playSound("random.fizz", 0.6F, 1.6F + this.rand.nextFloat() * 0.4F);
            }
            if (this.worldObj.isRemote) {
                this.spawnAround("cloud", 2, 1.5D);
            }
        } else if (this.worldObj.isRemote) {
            this.spawnAround("flame", 2, 2.8D);
            if (this.rand.nextInt(3) == 0) this.spawnAround("largesmoke", 1, 2.6D);
            if (this.currentAttackID == BREATH && this.animTick >= BREATH_START && this.animTick < BREATH_END) {
                this.spawnBreath();
            }
        }
    }

    /** Particles rising from its upper body, from {@code height} down to half of it. */
    private void spawnAround(String particle, int count, double height) {
        for (int i = 0; i < count; i++) {
            this.worldObj.spawnParticle(
                particle,
                this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
                this.posY + height * (0.5D + this.rand.nextDouble() * 0.5D),
                this.posZ + (this.rand.nextDouble() - 0.5D) * this.width,
                0.0D,
                0.04D,
                0.0D);
        }
    }

    /**
     * Flames streaming out of the mouth as last drawn, along where its head looks, which is where the server burns.
     * The face follows the look only roughly: the original model splits a head turn between its tilted chest and
     * head, so the face turns a quarter less than the look.
     */
    private void spawnBreath() {
        double[] from = this.drawnMouth != null && this.ticksExisted - this.drawnMouthTick <= 2 ? this.drawnMouth
            : this.mouthPosition();
        Vec3 look = this.breathDirection();
        double[] along = { look.xCoord, look.yCoord, look.zCoord };
        for (int i = 0; i < 8; i++) {
            double speed = 0.45D + this.rand.nextDouble() * 0.35D;
            this.worldObj.spawnParticle(
                i % 4 == 0 ? "smoke" : "flame",
                from[0],
                from[1],
                from[2],
                along[0] * speed + this.rand.nextGaussian() * 0.06D,
                along[1] * speed + this.rand.nextGaussian() * 0.06D,
                along[2] * speed + this.rand.nextGaussian() * 0.06D);
        }
    }

    /** Called by the renderer with where it drew the mouth. */
    public void setDrawnMouth(double x, double y, double z) {
        this.drawnMouth = new double[] { x, y, z };
        this.drawnMouthTick = this.ticksExisted;
    }

    /** Where the mouth is while it breathes, turned the way its head looks. */
    public double[] mouthPosition() {
        float yaw = this.rotationYawHead * (float) Math.PI / 180.0F;
        return new double[] { this.posX - MathHelper.sin(yaw) * MOUTH_FORWARD, this.posY + MOUTH_UP,
            this.posZ + MathHelper.cos(yaw) * MOUTH_FORWARD };
    }

    /**
     * Where its head looks. Entity.getLook uses the body's yaw, which for a mob is where it walks, not where it
     * looks.
     */
    public Vec3 breathDirection() {
        float yaw = this.rotationYawHead * (float) Math.PI / 180.0F;
        float pitch = this.rotationPitch * (float) Math.PI / 180.0F;
        return Vec3.createVectorHelper(
            -MathHelper.sin(yaw) * MathHelper.cos(pitch),
            -MathHelper.sin(pitch),
            MathHelper.cos(yaw) * MathHelper.cos(pitch));
    }

    /** Snowballs always hurt it, as they do Special Mobs' fire zombie. */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getSourceOfDamage() instanceof EntitySnowball) amount = Math.max(2.0F, amount);
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        super.dropFewItems(recentlyHit, looting);
        if (recentlyHit) {
            this.dropItem(Items.coal, 1 + this.rand.nextInt(2 + looting));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
