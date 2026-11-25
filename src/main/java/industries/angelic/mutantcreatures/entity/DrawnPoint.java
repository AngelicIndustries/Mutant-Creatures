package industries.angelic.mutantcreatures.entity;

import net.minecraft.entity.Entity;

/**
 * A point on a mob's model where the renderer last drew it, for effects that must leave the drawn model, such as
 * flames from a mouth or a line from a rod tip. It goes stale a couple of ticks after the mob was last drawn.
 */
public final class DrawnPoint {

    private double[] point;
    private int tick;

    public void set(Entity entity, double x, double y, double z) {
        this.point = new double[] { x, y, z };
        this.tick = entity.ticksExisted;
    }

    /** The point as last drawn, or {@code fallback} when the mob hasn't been drawn lately. */
    public double[] get(Entity entity, double[] fallback) {
        return this.point != null && entity.ticksExisted - this.tick <= 2 ? this.point : fallback;
    }
}
