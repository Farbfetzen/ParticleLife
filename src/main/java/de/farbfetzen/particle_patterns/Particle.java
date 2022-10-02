package de.farbfetzen.particle_patterns;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Particle {

    private float positionX;
    private float positionY;
    private float velocityX;
    private float velocityY;

    Particle(final float positionX, final float positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    void update(final int maxX, final int maxY, final float slipperiness) {
        velocityX *= slipperiness;
        velocityY *= slipperiness;

        positionX += velocityX;
        positionY += velocityY;

        if (positionX < 0) {
            positionX = -positionX;
            velocityX = -velocityX;
        } else if (positionX >= maxX) {
            positionX = maxX * 2 - positionX;
            velocityX = -velocityX;
        }
        if (positionY < 0) {
            positionY = -positionY;
            velocityY = -velocityY;
        } else if (positionY >= maxY) {
            positionY = maxY * 2 - positionY;
            velocityY = -velocityY;
        }
    }
}
