package industries.angelic.mutantcreatures.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import industries.angelic.mutantcreatures.compat.SpecialMobsCompat;

/** Mixins into other mods, each loaded only when its mod is. */
@LateMixin
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.MutantCreatures.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (loadedMods.contains(SpecialMobsCompat.MODID)) mixins.add("specialmobs.MixinEntitySpecialZombie");
        return mixins;
    }
}
