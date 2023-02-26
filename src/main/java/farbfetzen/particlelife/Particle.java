package farbfetzen.particlelife;

import lombok.Getter;
import lombok.Setter;
import processing.core.PVector;

@Getter
@Setter
class Particle {

    private final int groupId;
    private PVector position;
    private PVector velocity = new PVector();

    Particle(final int groupId, final PVector position) {
        this.groupId = groupId;
        this.position = position;
    }

    void update(final float deltaTime, final int maxX, final int maxY, final float slipperiness) {
        velocity.mult(slipperiness);
        position.add(PVector.mult(velocity, deltaTime));

        if (position.x < 0) {
            position.x = -position.x;
            velocity.x = -velocity.x;
        } else if (position.x >= maxX) {
            position.x = maxX * 2 - position.x;
            velocity.x = -velocity.x;
        }
        if (position.y < 0) {
            position.y = -position.y;
            velocity.y = -velocity.y;
        } else if (position.y >= maxY) {
            position.y = maxY * 2 - position.y;
            velocity.y = -velocity.y;
        }
    }
}
