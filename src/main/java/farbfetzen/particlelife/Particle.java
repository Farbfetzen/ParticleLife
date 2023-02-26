package farbfetzen.particlelife;

import lombok.Getter;
import lombok.Setter;
import processing.core.PVector;

@Getter
@Setter
class Particle {

    private final int groupId;
    private PVector position;
    private PVector velocity;

    Particle(final int groupId, final PVector position, final PVector velocity) {
        this.groupId = groupId;
        this.position = position;
        this.velocity = velocity;
    }

    void update(final float deltaTime, final int maxX, final int maxY, final float slipperiness) {
        velocity.mult(slipperiness);
        position.add(PVector.mult(velocity, deltaTime));

        if (position.x < 0) {
            position.x = maxX - position.x;
        } else if (position.x >= maxX) {
            position.x = position.x - maxX;
        }
        if (position.y < 0) {
            position.y = maxY - position.y;
        } else if (position.y >= maxY) {
            position.y = position.y - maxY;
        }
    }

}
