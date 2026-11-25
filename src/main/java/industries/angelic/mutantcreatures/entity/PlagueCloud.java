package industries.angelic.mutantcreatures.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import thehippomaster.MutantCreatures.MutantZombie;

/**
 * The plague mutant zombie's retch where it lands: a low cloud that gives everything standing in it a dose of plague
 * each second. It shows only as particles and is never saved.
 */
public class PlagueCloud extends Entity {

    public static final double RADIUS = 1.5D;
    private static final double HEIGHT = 2.0D;
    private static final int LIFE = 120;

    private MutantZombie owner;

    public PlagueCloud(World world) {
        super(world);
        this.setSize(0.25F, 0.25F);
    }

    public PlagueCloud(MutantZombie owner, double x, double y, double z) {
        this(owner.worldObj);
        this.owner = owner;
        this.setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.ticksExisted > LIFE) {
            this.setDead();
            return;
        }
        if (this.worldObj.isRemote) {
            this.spawnParticles();
        } else if (this.ticksExisted % 20 == 0) {
            this.dose();
        }
    }

    private void dose() {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            this.posX - RADIUS,
            this.posY - 0.5D,
            this.posZ - RADIUS,
            this.posX + RADIUS,
            this.posY + HEIGHT,
            this.posZ + RADIUS);
        List<?> entities = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (Object object : entities) {
            EntityLivingBase living = (EntityLivingBase) object;
            if (this.owner != null ? this.owner.isAlly(living) : living instanceof MutantZombie) continue;
            double dx = living.posX - this.posX;
            double dz = living.posZ - this.posZ;
            if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
            PlagueEffects.dose(living, 1);
        }
    }

    /** Sickly green mist, with a fleck of red now and then, over the cloud's ground. */
    private void spawnParticles() {
        for (int i = 0; i < 4; i++) {
            double angle = this.rand.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(this.rand.nextDouble()) * RADIUS;
            boolean red = this.rand.nextInt(6) == 0;
            this.worldObj.spawnParticle(
                "mobSpell",
                this.posX + Math.cos(angle) * distance,
                this.posY + this.rand.nextDouble() * 1.2D,
                this.posZ + Math.sin(angle) * distance,
                red ? 0.45D : 0.5D,
                red ? 0.12D : 0.6D,
                red ? 0.08D : 0.18D);
        }
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
