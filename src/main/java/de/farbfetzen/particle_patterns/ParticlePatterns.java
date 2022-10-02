package de.farbfetzen.particle_patterns;

import java.util.Random;

import processing.core.PApplet;

public class ParticlePatterns extends PApplet {

    private static final int CANVAS_WIDTH = 1024;
    private static final int CANVAS_HEIGHT = 768;
    private static final int DISTANCE_SQUARED_CUTOFF = 100 * 100;
    private static final Random seedGenerator = new Random();
    private static Long seed;

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

    public static void main(final String[] args) {
        int i = 0;
        while (i < args.length) {
            if ("-s".equals(args[i])) {
                seed = Long.parseLong(args[++i]);
            }
            i++;
        }
        PApplet.main(ParticlePatterns.class);
    }

    private static void nextSeed() {
        seed = seedGenerator.nextLong();
    }

    @Override
    public void settings() {
        size(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    @Override
    public void setup() {
        strokeWeight(5);
        reset(seed == null);
    }

    private void reset(final boolean newSeed) {
        // TODO: Print all interesting values to the console.
        if (newSeed) {
            nextSeed();
        }
        System.out.println("Seed: " + seed);
        randomSeed(seed);
        slipperiness = random(0.05f, 0.95f);  // high value equals low friction and vice versa

        for (int i = 0; i < particlesGroups.length; i++) {
            final int n = (int) random(50, 500);
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
                point(particle.getPositionX(), particle.getPositionY());
            }
        }
    }

    private static void updateVelocity(final Particle[] groupA, final Particle[] groupB, final float g) {
        // TODO: Take delta time into account?
        for (final Particle a : groupA) {
            for (final Particle b : groupB) {
                final float dx = a.getPositionX() - b.getPositionX();
                final float dy = a.getPositionY() - b.getPositionY();
                final float distanceSquared = dx * dx + dy * dy;
                if (distanceSquared > 0 && distanceSquared < DISTANCE_SQUARED_CUTOFF) {
                    final float distance = sqrt(distanceSquared);
                    final float force = g / distance;
                    a.setVelocityX(a.getVelocityX() + force * dx);
                    a.setVelocityY(a.getVelocityY() + force * dy);
                }
            }
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'r') {
            reset(false);
        } else if (key == 'n') {
            reset(true);
        }
    }

}
