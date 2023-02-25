package farbfetzen.particlelife;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import processing.core.PApplet;
import processing.core.PVector;

public class ParticleLife extends PApplet {

    @Parameter(names = {"-s", "--seed"}, description = "The initial seed for the random number generator")
    private Long seed;
    @Parameter(names = {"-f", "--full-screen"}, description = "Run the app in full screen mode. Type ESC to exit.")
    private boolean fullScreenArg;
    @Parameter(names = {"-w", "--window-size"}, arity = 2, description = "The window width and height in pixels.")
    private List<Integer> windowSize = List.of(1024, 768);
    @Parameter(names = {"-h", "--help"}, description = "Display this help message.", help = true)
    private boolean showHelp;

    private final int numberOfGroups = 4;
    private final int backgroundColor = color(0, 0, 0);
    private final int[] colors = {
            color(255, 1, 1, 128f),
            color(1, 255, 255, 128f),
            color(1, 255, 1, 128f),
            color(255, 255, 1, 128f)
    };
    private final List<Particle> particles = new ArrayList<>();
    private final float[][] gMatrix = new float[numberOfGroups][numberOfGroups];
    private float slipperiness;
    private float cutoffDistanceSquared;
    private final Random seedGenerator = new Random();
    private float distanceMean;
    private float distanceSd;
    private float distanceMax;

    public static void main(final String[] args) {
        PApplet.main(ParticleLife.class, args);
    }

    @Override
    public void settings() {
        if (args == null) {
            args = new String[0];
        }
        final var jc = JCommander.newBuilder().programName("ParticleLife").addObject(this).build();
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
        final var meanDimensions = (width + height) / 2f;
        distanceMean = meanDimensions / 6;
        distanceSd = meanDimensions / 30;
        distanceMax = distanceMean * 2;
        strokeWeight(5);
        blendMode(ADD);
        reset();
    }

    private void reset() {
        System.out.println("Seed: " + seed);
        randomSeed(seed);
        slipperiness = random(0.05f, 0.95f);  // high value equals low friction and vice versa
        final var cutoffDistance = constrain(randomGaussian() * distanceSd + distanceMean, 20, distanceMax);
        System.out.println("Cutoff distance: " + cutoffDistance);
        cutoffDistanceSquared = cutoffDistance * cutoffDistance;
        particles.clear();
        for (int i = 0; i < numberOfGroups; i++) {
            final int numberOfParticlesInGroup = (int) random(100, 500);
            for (int j = 0; j < numberOfParticlesInGroup; j++) {
                final var position = new PVector(random(0, width), random(0, height));
                particles.add(new Particle(i, position));
            }
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
        for (final Particle particle : particles) {
            stroke(colors[particle.getGroupId()]);
            point(particle.getPosition().x, particle.getPosition().y);
        }
    }

    private void updateParticles() {
        for (int a = 0; a < particles.size() - 1; a++) {
            for (int b = a + 1; b < particles.size(); b++) {
                final Particle pA = particles.get(a);
                final Particle pB = particles.get(b);
                final PVector distanceXY = PVector.sub(pA.getPosition(), pB.getPosition());
                final float distanceSquared = distanceXY.magSq();
                if (distanceSquared > 0 && distanceSquared < cutoffDistanceSquared) {
                    final float distance = sqrt(distanceSquared);
                    final int groupA = pA.getGroupId();
                    final int groupB = pB.getGroupId();
                    final float forceAToB = gMatrix[groupA][groupB] / distance;
                    pA.getVelocity().add(distanceXY.mult(forceAToB));
                    final float forceBToA = gMatrix[groupB][groupA] / distance;
                    pB.getVelocity().add(distanceXY.mult(forceBToA));
                }
            }
        }
        for (final Particle particle : particles) {
            particle.update(width, height, slipperiness);
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'r') {
            reset();
        } else if (key == 'n') {
            seed = seedGenerator.nextLong();
            reset();
        } else if (key == ' ') {
            if (isLooping()) {
                noLoop();
            } else {
                loop();
            }
        }
    }

}
