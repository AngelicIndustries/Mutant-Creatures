package industries.angelic.mutantcreatures.compat;

import thehippomaster.MutantCreatures.MutantZombie;

/**
 * Another mod's zombie raised by a mutant zombie's roar, given the behavior of the mutant zombie's own minions by a
 * mixin: it follows its leader, takes its target and never turns on it.
 */
public interface MutantMinion {

    /** Makes this zombie a minion of the given mutant zombie. */
    void mutantcreatures$follow(MutantZombie leader);

    /** Its leader, or null when it isn't a minion or has lost its leader. */
    MutantZombie mutantcreatures$leader();
}
