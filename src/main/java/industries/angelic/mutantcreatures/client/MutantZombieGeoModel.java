package industries.angelic.mutantcreatures.client;

import net.minecraft.util.ResourceLocation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A mutant zombie variant's geo model, animations and texture, all named after the variant. */
@SideOnly(Side.CLIENT)
public class MutantZombieGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

    private final ResourceLocation model;
    private final ResourceLocation texture;

    /**
     * @param name    the geo model and animation file name, e.g. {@code hungry_mutant_zombie}
     * @param texture the texture file name under {@code textures/}, e.g. {@code hungry_zombie}
     */
    public MutantZombieGeoModel(String name, String texture) {
        this.model = new ResourceLocation("mutantcreatures", name);
        this.texture = new ResourceLocation("mutantcreatures", "textures/" + texture + ".png");
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return this.model;
    }
}
