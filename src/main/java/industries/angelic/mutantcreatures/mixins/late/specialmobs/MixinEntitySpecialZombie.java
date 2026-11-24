package industries.angelic.mutantcreatures.mixins.late.specialmobs;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import industries.angelic.mutantcreatures.compat.MinionBehavior;
import industries.angelic.mutantcreatures.compat.MutantMinion;
import thehippomaster.MutantCreatures.MutantZombie;
import toast.specialMobs.entity.zombie.Entity_SpecialZombie;

/** Special Mobs' zombies raised by a mutant zombie's roar act as its minions; see {@link MinionBehavior}. */
@Mixin(Entity_SpecialZombie.class)
public abstract class MixinEntitySpecialZombie extends EntityZombie implements MutantMinion {

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
        if (this.mutantcreatures$minion && !this.worldObj.isRemote) {
            this.mutantcreatures$leader = MinionBehavior.update(this, this.mutantcreatures$leader);
        }
    }

    @Inject(method = "dropFewItems", at = @At("HEAD"), cancellable = true)
    private void mutantcreatures$dropLess(boolean recentlyHit, int looting, CallbackInfo ci) {
        if (this.mutantcreatures$minion && !MinionBehavior.dropsLoot(this)) ci.cancel();
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void mutantcreatures$writeMinion(NBTTagCompound tag, CallbackInfo ci) {
        if (this.mutantcreatures$minion) tag.setBoolean(MinionBehavior.NBT_KEY, true);
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void mutantcreatures$readMinion(NBTTagCompound tag, CallbackInfo ci) {
        this.mutantcreatures$minion = tag.getBoolean(MinionBehavior.NBT_KEY);
    }
}
