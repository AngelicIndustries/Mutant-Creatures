package industries.angelic.mutantcreatures.client;

import net.minecraft.util.ResourceLocation;

import com.geckolib.model.GeoModel;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import industries.angelic.mutantcreatures.entity.HungryMutantZombie;

@SideOnly(Side.CLIENT)
public class HungryMutantZombieModel extends GeoModel<HungryMutantZombie> {

    public static final ResourceLocation MODEL = new ResourceLocation("mutantcreatures", "hungry_mutant_zombie");
    public static final ResourceLocation TEXTURE = new ResourceLocation(
        "mutantcreatures",
        "textures/hungry_zombie.png");

    @Override
    public ResourceLocation getModelResource(HungryMutantZombie zombie) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HungryMutantZombie zombie) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HungryMutantZombie zombie) {
        return MODEL;
    }
}
