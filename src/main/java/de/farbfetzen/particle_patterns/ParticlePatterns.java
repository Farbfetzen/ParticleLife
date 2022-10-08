package de.farbfetzen.particle_patterns;

import java.util.List;
import java.util.Random;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import processing.core.PApplet;
import processing.core.PVector;

public class ParticlePatterns extends PApplet {

    @Parameter(names = {"-s", "--seed"}, description = "The initial seed for the random number generator")
    private Long seed;
    @Parameter(names = {"-f", "--full-screen"}, description = "Run the app in full screen mode. Type ESC to exit.")
    private boolean fullScreenArg;
    @Parameter(names = {"-w", "--window-size"}, arity = 2, description = "The window width and height in pixels.")
    private List<Integer> windowSize = List.of(1024, 768);
    @Parameter(names = {"-h", "--help"}, description = "Display this help message.", help = true)
    private boolean showHelp;

    private final int backgroundColor = color(0, 0, 0);
    private final int[] colors = {
            color(255, 1, 1, 128f),
            color(1, 255, 255, 128f),
            color(1, 255, 1, 128f),
            color(255, 255, 1, 128f)
    };
    private final Particle[][] particlesGroups = new Particle[4][];
    private final float[][] gMatrix = new float[4][4];
    private float slipperiness;
    private float cutoffDistanceSquared;
    private final Random seedGenerator = new Random();

    public static void main(final String[] args) {
        PApplet.main(ParticlePatterns.class, args);
    }

    @Override
    public void settings() {
        if (args == null) {
            args = new String[0];
        }
        final var jc = JCommander.newBuilder().programName("ParticlePatterns").addObject(this).build();
        try {
            jc.parse(args);
        } catch (final ParameterException e) {
            System.out.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }
        if (showHelp) {
            jc.usage();
            System.exit(0);
        }
        if (seed == null) {
            seed = seedGenerator.nextLong();
        } else {
            seedGenerator.setSeed(seed);
        }
        if (fullScreenArg) {
            fullScreen();
        } else {
            width = windowSize.get(0);
            height = windowSize.get(1);
        }
    }

    @Override
    public void setup() {
        strokeWeight(5);
        blendMode(ADD);
        reset();
    }

    private void reset() {
        System.out.println("Seed: " + seed);
        randomSeed(seed);
        slipperiness = random(0.05f, 0.95f);  // high value equals low friction and vice versa
        final var cutoffDistance = constrain(randomGaussian() * 30 + 150, 50, 250);
        System.out.println("Cutoff distance: " + cutoffDistance);
        cutoffDistanceSquared = cutoffDistance * cutoffDistance;
        for (int i = 0; i < particlesGroups.length; i++) {
            final int n = (int) random(100, 500);
            final var particles = new Particle[n];
            for (int j = 0; j < n; j++) {
                final var position = new PVector(random(0, width), random(0, height));
                particles[j] = new Particle(position);
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
        updateParticles();
        background(backgroundColor);
        for (int i = 0; i < 4; i++) {
            stroke(colors[i]);
            for (final Particle particle : particlesGroups[i]) {
                point(particle.getPosition().x, particle.getPosition().y);
            }
        }
    }

    private void updateParticles() {
        for (int i = 0; i < particlesGroups.length; i++) {
            final var groupA = particlesGroups[i];
            for (int j = 0; j < particlesGroups.length; j++) {
                final var groupB = particlesGroups[j];
                updateVelocity(groupA, groupB, gMatrix[i][j]);
            }
        }
        for (final Particle[] particles : particlesGroups) {
            for (final Particle particle : particles) {
                particle.update(width, height, slipperiness);
            }
        }
    }

    private void updateVelocity(final Particle[] groupA, final Particle[] groupB, final float g) {
        for (final Particle a : groupA) {
            for (final Particle b : groupB) {
                final PVector distanceXY = PVector.sub(a.getPosition(), b.getPosition());
                final float distanceSquared = distanceXY.magSq();
                if (distanceSquared > 0 && distanceSquared < cutoffDistanceSquared) {
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
