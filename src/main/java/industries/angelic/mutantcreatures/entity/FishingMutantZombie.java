package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

import industries.angelic.mutantcreatures.compat.SpecialMobsCompat;
import industries.angelic.mutantcreatures.entity.ai.AIFishingCast;
import thehippomaster.MutantCreatures.MutantCreatures;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Mutant form of Special Mobs' fishing zombie: a plain mutant zombie with a huge fishing rod. It casts its hook at
 * targets a few blocks off and yanks whatever it hooks over to land just in front of it, where its ground slam
 * follows. Its roar raises fishing zombies.
 */
public class FishingMutantZombie extends MutantZombie implements GeoEntity {

    public static final int CAST = 4;
    public static final int REEL = 5;
    /** The tick of the cast at which the whip throws the hook. */
    public static final int CAST_RELEASE = 10;
    private static final int LINE_OUT_WATCHER = 22;
    /** How far in front of it a yanked target lands, about where its slam's shockwave starts. */
    private static final double LAND = 3.0D;
    /**
     * Where the rod tip is, from the model, in blocks ahead of, above and to the right of its feet: as the whip throws
     * the hook, and while it holds the line out.
     */
    private static final double[] RELEASE_TIP = { 2.9D, 2.8D, 1.2D };
    private static final double[] HOLD_TIP = { 2.9D, 1.9D, 0.2D };

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private MutantFishHook hook;
    /** Ticks until it can cast again. */
    public int castCooldown;
    private final DrawnPoint drawnRodTip = new DrawnPoint();

    public FishingMutantZombie(World world) {
        super(world);
        this.experienceValue = 35;
        this.tasks.addTask(1, new AIFishingCast(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(LINE_OUT_WATCHER, (byte) 0);
    }

    /** A little slower than the mutant zombie, as the fishing zombie is than a zombie. */
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
            .setBaseValue(0.26D * 0.9D);
    }

    public MutantFishHook getHook() {
        return this.hook;
    }

    public void setHook(MutantFishHook hook) {
        this.hook = hook;
        this.dataWatcher.updateObject(LINE_OUT_WATCHER, (byte) (hook != null ? 1 : 0));
    }

    /** Whether its hook is out, on both sides. */
    public boolean isLineOut() {
        return this.dataWatcher.getWatchableObjectByte(LINE_OUT_WATCHER) != 0;
    }

    /**
     * Starts one of its attacks from the start, on both sides. Clients restart an attack's clock only when they are
     * told the attack ended, so they are told that first.
     */
    public void startAttack(int id) {
        this.sendAttackPacket(0);
        this.sendAttackPacket(id);
        this.animTick = 0;
    }

    /**
     * Flings a hooked target over to land just in front of it. Thrown up at 0.5, a mob or player is in the air for
     * about 13 ticks and covers about 8 times its starting speed across the ground.
     */
    public void yank(Entity target) {
        double dx = this.posX - target.posX;
        double dz = this.posZ - target.posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 1.0E-4) return;
        double speed = Math.max(0.0D, distance - LAND) / 8.0D;
        target.motionX = dx / distance * speed;
        target.motionY = 0.5D;
        target.motionZ = dz / distance * speed;
        target.onGround = false;
        target.isAirBorne = true;
        if (target instanceof EntityPlayerMP) {
            MutantCreatures.sendPacketToAll((EntityPlayerMP) target, new S12PacketEntityVelocity(target));
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.worldObj.isRemote) return;
        if (this.castCooldown > 0) this.castCooldown--;
        if (this.hook != null && this.hook.isDead) this.setHook(null);
    }

    @Override
    public void setDead() {
        super.setDead();
        if (this.hook != null) this.hook.setDead();
    }

    /** The rod tip as the renderer last drew it. */
    public DrawnPoint drawnRodTip() {
        return this.drawnRodTip;
    }

    /** Where the line leaves the rod: the tip as last drawn, or about where it is while the line is out. */
    public double[] rodTip(float partialTicks) {
        return this.drawnRodTip.get(this, this.fromFeet(HOLD_TIP, partialTicks));
    }

    /** Where the whip throws the hook from. */
    public double[] releasePoint() {
        return this.fromFeet(RELEASE_TIP, 1.0F);
    }

    /** A point given in blocks ahead of, above and to the right of its feet, turned the way its body faces. */
    private double[] fromFeet(double[] offset, float partialTicks) {
        float yaw = (this.prevRenderYawOffset + (this.renderYawOffset - this.prevRenderYawOffset) * partialTicks)
            * (float) Math.PI
            / 180.0F;
        double sin = MathHelper.sin(yaw);
        double cos = MathHelper.cos(yaw);
        return new double[] {
            this.lastTickPosX + (this.posX - this.lastTickPosX) * partialTicks - sin * offset[0] - cos * offset[2],
            this.lastTickPosY + (this.posY - this.lastTickPosY) * partialTicks + offset[1],
            this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * partialTicks + cos * offset[0] - sin * offset[2] };
    }

    /** Its roar raises Special Mobs' fishing zombies. */
    @Override
    public EntityZombie createMinion(double x, double y, double z) {
        EntityZombie minion = SpecialMobsCompat.createMinion("FishingZombie", this, x, y, z);
        return minion != null ? minion : super.createMinion(x, y, z);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        super.dropFewItems(recentlyHit, looting);
        if (recentlyHit) {
            this.dropItem(Items.fish, 1 + this.rand.nextInt(3 + looting));
        }
    }

    @Override
    protected void dropRareDrop(int looting) {
        this.dropItem(Items.fishing_rod, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
