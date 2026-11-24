package industries.angelic.mutantcreatures.mixins.late.specialmobs;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import industries.angelic.mutantcreatures.compat.MutantMinion;
import thehippomaster.MutantCreatures.MutantZombie;
import toast.specialMobs.entity.zombie.Entity_SpecialZombie;

/**
 * Gives Special Mobs' zombies raised by a mutant zombie's roar the mutant zombie's own minion behavior: they keep
 * near their leader, take its target, never turn on a mutant zombie, and drop loot only a third of the time. Whether
 * a zombie is a minion is saved with it; after a reload it finds a leader nearby again.
 */
@Mixin(Entity_SpecialZombie.class)
public abstract class MixinEntitySpecialZombie extends EntityZombie implements MutantMinion {

    @Unique
    private static final String mutantcreatures$MINION = "MutantCreaturesMinion";

    @Unique
    private boolean mutantcreatures$minion;
    @Unique
    private MutantZombie mutantcreatures$leader;

    private MixinEntitySpecialZombie(World world) {
        super(world);
    }

    @Override
    public void mutantcreatures$follow(MutantZombie leader) {
        this.mutantcreatures$minion = true;
        this.mutantcreatures$leader = leader;
    }

    @Override
    public MutantZombie mutantcreatures$leader() {
        return this.mutantcreatures$leader;
    }

    @Inject(method = "onLivingUpdate", at = @At("TAIL"))
    private void mutantcreatures$followLeader(CallbackInfo ci) {
        if (!this.mutantcreatures$minion || this.worldObj.isRemote) return;

        EntityLivingBase target = this.getAttackTarget();
        // A mutant zombie's ground slam hits its own minions; they don't hold it against it.
        if (target != null && (!target.isEntityAlive() || target instanceof MutantZombie)) this.setAttackTarget(null);
        if (this.getAITarget() instanceof MutantZombie) this.setRevengeTarget(null);

        MutantZombie leader = this.mutantcreatures$leader;
        if (leader != null && (!leader.isEntityAlive() || this.getDistanceSqToEntity(leader) > 24 * 24)) {
            leader = null;
        }
        if (leader == null) {
            this.detachHome();
            List<?> nearby = this.worldObj
                .getEntitiesWithinAABB(MutantZombie.class, this.boundingBox.expand(16.0D, 16.0D, 16.0D));
            if (!nearby.isEmpty()) leader = (MutantZombie) nearby.get(this.rand.nextInt(nearby.size()));
        } else {
            this.setHomeArea(
                MathHelper.floor_double(leader.posX),
                MathHelper.floor_double(leader.boundingBox.minY),
                MathHelper.floor_double(leader.posZ),
                16);
            if (this.getAttackTarget() == null && leader.getAttackTarget() != null) {
                this.setAttackTarget(leader.getAttackTarget());
                this.setRevengeTarget(leader.getAttackTarget());
            }
        }
        this.mutantcreatures$leader = leader;
    }

    @Inject(method = "dropFewItems", at = @At("HEAD"), cancellable = true)
    private void mutantcreatures$dropLess(boolean recentlyHit, int looting, CallbackInfo ci) {
        if (this.mutantcreatures$minion && this.rand.nextInt(3) != 0) ci.cancel();
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void mutantcreatures$writeMinion(NBTTagCompound tag, CallbackInfo ci) {
        if (this.mutantcreatures$minion) tag.setBoolean(mutantcreatures$MINION, true);
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void mutantcreatures$readMinion(NBTTagCompound tag, CallbackInfo ci) {
        this.mutantcreatures$minion = tag.getBoolean(mutantcreatures$MINION);
    }
}
