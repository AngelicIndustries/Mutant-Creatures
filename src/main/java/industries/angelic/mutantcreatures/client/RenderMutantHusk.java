package industries.angelic.mutantcreatures.client;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thehippomaster.MutantCreatures.client.ModelMutantZombie;
import thehippomaster.MutantCreatures.client.RenderMutantZombie;

/** The mutant zombie's model and renderer, with the husk's colours. */
@SideOnly(Side.CLIENT)
public class RenderMutantHusk extends RenderMutantZombie {

    private static final ResourceLocation TEXTURE = new ResourceLocation("mutantcreatures", "textures/husk.png");

    public RenderMutantHusk() {
        super(new ModelMutantZombie(), 1.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }
}
