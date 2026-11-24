package industries.angelic.mutantcreatures.mixins.late.etfuturum;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ganymedes01.etfuturum.entities.EntityHusk;
import industries.angelic.mutantcreatures.compat.MinionBehavior;
import industries.angelic.mutantcreatures.compat.MutantMinion;
import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Et Futurum's husks raised by a mutant husk's roar act as its minions; see {@link MinionBehavior}. EntityHusk
 * doesn't override saving or drops, so this adds those overrides to it.
 */
@Mixin(EntityHusk.class)
public abstract class MixinEntityHusk extends EntityZombie implements MutantMinion {

    @Unique
    private boolean mutantcreatures$minion;
    @Unique
    private MutantZombie mutantcreatures$leader;

    private MixinEntityHusk(World world) {
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

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        if (!this.mutantcreatures$minion || MinionBehavior.dropsLoot(this)) super.dropFewItems(recentlyHit, looting);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (this.mutantcreatures$minion) tag.setBoolean(MinionBehavior.NBT_KEY, true);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.mutantcreatures$minion = tag.getBoolean(MinionBehavior.NBT_KEY);
    }
}
