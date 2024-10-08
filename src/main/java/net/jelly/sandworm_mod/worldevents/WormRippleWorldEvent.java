package net.jelly.sandworm_mod.worldevents;

import net.jelly.sandworm_mod.registry.common.WorldEventRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.lodestar.lodestone.systems.worldevent.WorldEventInstance;

public class WormRippleWorldEvent extends WorldEventInstance {
    public Vec3 position;
    public int lifetime = 30;
    public int scale = 2;

    public int in = 100;
    public int out = 30;

    public WormRippleWorldEvent() {
        super(WorldEventRegistry.WORM_RIPPLE);
    }

    public WormRippleWorldEvent spawnRipple(Vec3 pos) {
        position = pos;
        return this;
    }

    @Override
    public void tick(Level level) {
        // shader implementation
//        if(lifetime <= in) {
//            fx.radius = lerp(0, 20, (float) lifetime / in);
//            fx.speed = lerp(0, 4, (float) lifetime / in);
//            fx.magnitude = lerp(0, 0.2f, (float) lifetime / in);
//            fx.frequency = lerp(0, 4, (float) lifetime / in);
//        }
//        else {
//            fx.radius = lerp(20, 0, (float) lifetime / (in+out));
//            fx.speed = lerp(4, 0, (float) (lifetime-in) / out);
//            fx.magnitude = lerp(0.2f, 0, (float) (lifetime-in) / out);
//            // fx.frequency = lerp(4, 0, (float) (lifetime-in) / out);
//        }
        if(this.level == null) return;

        if(lifetime <= 0) {
            this.end(level);
        }

        if (!this.level.isClientSide && this.lifetime > 0) {
            spawnSpiralParticles(this.position, this.lifetime);
        }
        this.lifetime--;

        super.tick(level);
    }

    private void spawnSpiralParticles(Vec3 pos, int lifetime) {
        for(int t = 0; t < 360; t++) {
            if (t % 10 == 0) {
                int theta = t - 12*lifetime;
                spawnParticle(new Vec3(pos.x + scale * (lifetime / 30.0d) * t / 20 * Math.cos(Math.toRadians(theta)), pos.y, pos.z + scale * (lifetime / 30.0d) * t / 20 * Math.sin(Math.toRadians(theta))));
            }
        }
    }

    private void spawnParticle(Vec3 pos) {
        int heightDecrement = 0;
        BlockPos blockPos = BlockPos.containing(pos.x, pos.y - 1, pos.z);
        BlockState blockState = level.getBlockState(blockPos);
        while(blockState.isAir()) {
            blockPos = BlockPos.containing(pos.x, pos.y - 1 - heightDecrement, pos.z);
            blockState = level.getBlockState(blockPos);
            heightDecrement++;
            if(heightDecrement >= 20) break;
        }

        BlockParticleOption blockParticle = new BlockParticleOption(ParticleTypes.BLOCK, blockState);

                ((ServerLevel) level).sendParticles(
                        blockParticle, // The particle option
                        pos.x,         // X position
                        blockPos.getY()+1,         // Y position
                        pos.z,         // Z position
                        1,            // Count of particles to spawn
                        0.2,           // X offset for particle spread
                        0.0,           // Y offset for particle spread
                        0.2,           // Z offset for particle spread
                        0.0            // Speed of the particle
                );
    }

    float lerp(float a, float b, float f)
    {
        return (float)(a * (1.0 - f)) + (b * f);
    }
}

