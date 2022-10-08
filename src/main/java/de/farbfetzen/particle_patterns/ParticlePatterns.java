package de.farbfetzen.particle_patterns;

import java.util.Random;

import processing.core.PApplet;
import processing.core.PVector;

public class ParticlePatterns extends PApplet {

    private static final int CANVAS_WIDTH = 1024;
    private static final int CANVAS_HEIGHT = 768;
    private static final int DISTANCE_SQUARED_CUTOFF = 100 * 100;

    private final int backgroundColor = color(0, 0, 0);
    private float slipperiness;
    private final int[] colors = {
            color(255, 0, 0, 128f),
            color(0, 255, 255, 128f),
            color(0, 255, 0, 128f),
            color(255, 255, 0, 128f)
    };
    private final Particle[][] particlesGroups = new Particle[4][];
    private final float[][] gMatrix = new float[4][4];
    private final Random seedGenerator = new Random();
    private long seed;

    public static void main(final String[] args) {
        PApplet.main(ParticlePatterns.class, args);
    }

    private void handleArgs() {
        boolean customSeed = false;
        if (args != null) {
            int i = 0;
            while (i < args.length) {
                if ("-s".equals(args[i])) {
                    seed = Long.parseLong(args[++i]);
                    seedGenerator.setSeed(seed);
                    customSeed = true;
                }
                i++;
            }
        }
        if (args == null || !customSeed) {
            seed = seedGenerator.nextLong();
        }
    }

    @Override
    public void settings() {
        size(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    @Override
    public void setup() {
        handleArgs();
        strokeWeight(5);
        reset();
    }

    private void reset() {
        // TODO: Print all interesting values to the console.
        System.out.println("Seed: " + seed);
        randomSeed(seed);
        slipperiness = random(0.05f, 0.95f);  // high value equals low friction and vice versa

        for (int i = 0; i < particlesGroups.length; i++) {
            final int n = (int) random(100, 1000);
            final var particles = new Particle[n];
            for (int j = 0; j < n; j++) {
                particles[j] = new Particle(random(0, CANVAS_WIDTH), random(0, CANVAS_HEIGHT));
            }
            particlesGroups[i] = particles;
        }

        for (int i = 0; i < gMatrix.length; i++) {
            for (int j = 0; j < gMatrix.length; j++) {
                gMatrix[i][j] = random(-1, 1);
            }
        }
    }

    @Override
    public void draw() {
        for (int i = 0; i < particlesGroups.length; i++) {
            final var groupA = particlesGroups[i];
            for (int j = 0; j < particlesGroups.length; j++) {
                final var groupB = particlesGroups[j];
                updateVelocity(groupA, groupB, gMatrix[i][j]);
            }
        }
        for (final Particle[] particles : particlesGroups) {
            for (final Particle particle : particles) {
                particle.update(CANVAS_WIDTH, CANVAS_HEIGHT, slipperiness);
            }
        }
        background(backgroundColor);
        for (int i = 0; i < 4; i++) {
            stroke(colors[i]);
            for (final Particle particle : particlesGroups[i]) {
                point(particle.getPosition().x, particle.getPosition().y);
            }
        }
    }

    private static void updateVelocity(final Particle[] groupA, final Particle[] groupB, final float g) {
        for (final Particle a : groupA) {
            for (final Particle b : groupB) {
                final PVector distanceXY = PVector.sub(a.getPosition(), b.getPosition());
                final float distanceSquared = distanceXY.magSq();
                if (distanceSquared > 0 && distanceSquared < DISTANCE_SQUARED_CUTOFF) {
                    final float distance = sqrt(distanceSquared);
                    final float force = g / distance;
                    a.getVelocity().add(distanceXY.mult(force));
                }
            }
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'r') {
            reset();
        } else if (key == 'n') {
            seed = seedGenerator.nextLong();
            reset();
        }
    }

}
