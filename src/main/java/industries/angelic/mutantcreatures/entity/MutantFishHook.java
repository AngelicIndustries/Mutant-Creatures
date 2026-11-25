package industries.angelic.mutantcreatures.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * The fishing mutant zombie's hook. It flies in an arc and latches onto the first mob or player it hurts, following
 * it until the angler reels it in. On a block or in water it stops where it is. It is never saved.
 */
public class MutantFishHook extends Entity {

    private static final int ANGLER_WATCHER = 20;
    private static final int HOOKED_WATCHER = 21;
    private static final int STOPPED_WATCHER = 22;
    private static final double GRAVITY = 0.03D;
    private static final double DRAG = 0.99D;
    /** Damage the hook does when it bites, which also decides whether it holds. */
    private static final float BITE = 2.0F;
    /** How far the hook may get from its angler before it snaps. */
    private static final double SNAP = 32.0D;

    private FishingMutantZombie angler;
    private Entity hooked;
    /** A mob the hook glanced off, which it passes through from then on. */
    private Entity glanced;

    public MutantFishHook(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        // The line runs from the rod to the hook, so it must be drawn while the hook itself is out of view.
        this.ignoreFrustumCheck = true;
    }

    /**
     * A hook cast from {@code (x, y, z)} at a target: it flies at {@code speed} across the ground and is aimed up
     * enough to come down on the target's chest.
     */
    public MutantFishHook(FishingMutantZombie angler, double x, double y, double z, Entity target, double speed) {
        this(angler.worldObj);
        this.angler = angler;
        this.dataWatcher.updateObject(ANGLER_WATCHER, angler.getEntityId());
        this.setPosition(x, y, z);
        double dx = target.posX - x;
        double dy = target.posY + target.height * 0.6D - y;
        double dz = target.posZ - z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double ticks = Math.max(1.0D, distance / speed);
        this.motionX = dx / ticks;
        this.motionZ = dz / ticks;
        this.motionY = dy / ticks + 0.5D * GRAVITY * ticks;
        angler.setHook(this);
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(ANGLER_WATCHER, -1);
        this.dataWatcher.addObject(HOOKED_WATCHER, -1);
        this.dataWatcher.addObject(STOPPED_WATCHER, (byte) 0);
    }

    public FishingMutantZombie getAngler() {
        if (this.angler == null && this.worldObj.isRemote) {
            Entity entity = this.worldObj.getEntityByID(this.dataWatcher.getWatchableObjectInt(ANGLER_WATCHER));
            if (entity instanceof FishingMutantZombie) this.angler = (FishingMutantZombie) entity;
        }
        return this.angler;
    }

    /** What the hook has latched onto, on both sides. */
    public Entity getHooked() {
        if (this.worldObj.isRemote) {
            int id = this.dataWatcher.getWatchableObjectInt(HOOKED_WATCHER);
            return id == -1 ? null : this.worldObj.getEntityByID(id);
        }
        return this.hooked;
    }

    /** Whether it has stopped on a block or in water. */
    public boolean isStopped() {
        return this.dataWatcher.getWatchableObjectByte(STOPPED_WATCHER) != 0;
    }

    private void setHooked(Entity entity) {
        this.hooked = entity;
        this.dataWatcher.updateObject(HOOKED_WATCHER, entity != null ? entity.getEntityId() : -1);
    }

    private void stop(double x, double y, double z) {
        this.setPosition(x, y, z);
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.dataWatcher.updateObject(STOPPED_WATCHER, (byte) 1);
    }

    @Override
    public void onUpdate() {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();

        if (!this.worldObj.isRemote) {
            if (this.angler == null || this.angler.isDead
                || this.angler.getHook() != this
                || this.getDistanceSqToEntity(this.angler) > SNAP * SNAP) {
                this.setDead();
                return;
            }
            if (this.hooked != null && (this.hooked.isDead || this.hooked.worldObj != this.worldObj)) {
                this.setHooked(null);
                this.stop(this.posX, this.posY, this.posZ);
            }
        }

        Entity hooked = this.getHooked();
        if (hooked != null) {
            this.setPosition(hooked.posX, hooked.boundingBox.minY + hooked.height * 0.6D, hooked.posZ);
            return;
        }
        if (this.isStopped()) return;

        if (!this.worldObj.isRemote) {
            if (this.isInWater()) {
                this.stop(this.posX, this.posY, this.posZ);
                return;
            }
            if (this.hit()) return;
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        this.motionX *= DRAG;
        this.motionY = this.motionY * DRAG - GRAVITY;
        this.motionZ *= DRAG;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /** Checks what it hits along this tick's flight. Returns whether it stopped or latched on. */
    private boolean hit() {
        Vec3 from = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
        Vec3 to = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        MovingObjectPosition block = this.worldObj.rayTraceBlocks(from, to);
        from = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
        to = block != null ? Vec3.createVectorHelper(block.hitVec.xCoord, block.hitVec.yCoord, block.hitVec.zCoord)
            : Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        List<?> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(
            this,
            this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ)
                .expand(1.0D, 1.0D, 1.0D));
        for (Object object : entities) {
            Entity entity = (Entity) object;
            if (!entity.canBeCollidedWith() || entity == this.glanced || this.angler.isAlly(entity)) continue;
            AxisAlignedBB box = entity.boundingBox.expand(0.3D, 0.3D, 0.3D);
            MovingObjectPosition intercept = box.calculateIntercept(from, to);
            if (intercept == null) continue;
            double distance = from.distanceTo(intercept.hitVec);
            if (distance < nearestDistance) {
                nearest = entity;
                nearestDistance = distance;
            }
        }

        if (nearest != null) {
            // It holds only if it bites: creative players and mobs still shrugging off a hit shake it off.
            if (nearest.attackEntityFrom(DamageSource.causeThrownDamage(this, this.angler), BITE)) {
                this.setHooked(nearest);
                this.playSound("random.bow", 1.0F, 1.6F);
                return true;
            }
            this.glanced = nearest;
            this.motionX *= -0.1D;
            this.motionZ *= -0.1D;
            return false;
        }
        if (block != null) {
            this.stop(block.hitVec.xCoord, block.hitVec.yCoord, block.hitVec.zCoord);
            return true;
        }
        return false;
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!this.worldObj.isRemote && this.angler != null && this.angler.getHook() == this) this.angler.setHook(null);
    }

    @Override
    public float getShadowSize() {
        return 0.0F;
    }

    @Override
    public boolean isInRangeToRenderDist(double distanceSq) {
        return distanceSq < 64.0D * 64.0D;
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound tag) {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {}
}
