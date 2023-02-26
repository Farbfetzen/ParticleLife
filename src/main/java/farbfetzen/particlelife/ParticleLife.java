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

    private static final int NUMBER_OF_GROUPS = 4;
    private static final int POINT_SIZE = 5;
    private static final int DISTANCE_MIN = POINT_SIZE * 5;
    // A positive g results in a repulsive force.
    private static final float G_AT_0 = 1;
    private static final float SLOPE_0_TO_MIN = -G_AT_0 / DISTANCE_MIN;

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
    private final List<Particle> particles = new ArrayList<>();
    private float slipperiness;
    private float cutoffDistanceSquared;
    private final Random seedGenerator = new Random();
    private float distanceDistributionMean;
    private float distanceDistributionSd;
    private float distanceDistributionMax;
    private float distanceMax;
    private float distanceMid;
    private final float[][] slopeMinToMid = new float[NUMBER_OF_GROUPS][NUMBER_OF_GROUPS];
    private final float[][] slopeMidToMax = new float[NUMBER_OF_GROUPS][NUMBER_OF_GROUPS];
    private int previousMillis = 0;
    private float halfWidth;
    private float halfHeight;

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
        distanceDistributionMean = meanDimensions / 6;
        distanceDistributionSd = meanDimensions / 30;
        distanceDistributionMax = distanceDistributionMean * 2;
        halfWidth = width / 2f;
        halfHeight = height / 2f;
        strokeWeight(POINT_SIZE);
        blendMode(ADD);
        reset();
    }

    private void reset() {
        System.out.println("Seed: " + seed);
        randomSeed(seed);
        // high slipperiness equals low friction and vice versa
        slipperiness = constrain(randomGaussian() * 0.15f + 0.5f, 0.1f, 0.9f);
        distanceMax = constrain(
                randomGaussian() * distanceDistributionSd + distanceDistributionMean,
                20,
                distanceDistributionMax
        );
        cutoffDistanceSquared = distanceMax * distanceMax;
        final float halfDistance = (distanceMax - DISTANCE_MIN) / 2;
        distanceMid = DISTANCE_MIN + halfDistance;
        for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
            for (int j = 0; j < NUMBER_OF_GROUPS; j++) {
                final float g = random(-1, 1);
                slopeMinToMid[i][j] = g / halfDistance;
                slopeMidToMax[i][j] = -g / halfDistance;
            }
        }

        particles.clear();
        for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
            final int numberOfParticlesInGroup = (int) random(100, 500);
            for (int j = 0; j < numberOfParticlesInGroup; j++) {
                final var position = new PVector(random(0, width), random(0, height));
                final var velocity = PVector.random2D(this).mult(100);
                particles.add(new Particle(i, position, velocity));
            }
        }
    }

    @Override
    public void draw() {
        final int currentMillis = millis();
        // Time since last frame in seconds.
        final float deltaTime = (currentMillis - previousMillis) / 1000f;
        previousMillis = currentMillis;
        updateParticles(deltaTime);
        background(backgroundColor);
        for (final Particle particle : particles) {
            stroke(colors[particle.getGroupId()]);
            point(particle.getPosition().x, particle.getPosition().y);
        }
    }

    private void updateParticles(final float deltaTime) {
        for (int i = 0; i < particles.size() - 1; i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                final Particle pA = particles.get(i);
                final Particle pB = particles.get(j);
                final PVector distanceXY = getClosestDistance(pA, pB);
                final float distanceSquared = distanceXY.magSq();
                if (distanceSquared < cutoffDistanceSquared) {
                    final float distance = distanceSquared > 0 ? sqrt(distanceSquared) : Float.MIN_VALUE;
                    final float[] forces = getForces(distance, pA.getGroupId(), pB.getGroupId());
                    pA.getVelocity().add(distanceXY.mult(forces[0]));
                    pB.getVelocity().add(distanceXY.mult(forces[1]));
                }
            }
        }
        for (final Particle particle : particles) {
            particle.update(deltaTime, width, height, slipperiness);
        }
    }

    // TODO: Check this method with a unit test.
    private PVector getClosestDistance(final Particle pA, final Particle pB) {
        final float closestX;
        final float closestY;
        final float xDistanceOnScreen = pB.getPosition().x - pA.getPosition().x;
        final float yDistanceOnScreen = pB.getPosition().y - pA.getPosition().y;
        // Depending on the sign of the differences I can reduce the number of comparisons.
        // I also can check x and y independently.
        if (abs(xDistanceOnScreen) <= halfWidth) {
            closestX = xDistanceOnScreen;
        } else if (xDistanceOnScreen >= 0) {
            // left
            closestX = xDistanceOnScreen - width;
        } else {
            // right
            closestX = xDistanceOnScreen + width;
        }
        if (abs(yDistanceOnScreen) < halfHeight) {
            closestY = yDistanceOnScreen;
        } else if (yDistanceOnScreen >= 0) {
            // top
            closestY = yDistanceOnScreen - height;
        } else {
            // bottom
            closestY = yDistanceOnScreen + height;
        }
        return new PVector(closestX, closestY);
    }

    private float[] getForces(final float distance, final int groupA, final int groupB) {
        // Positive force = repulsion, negative force = attraction.
        if (distance <= DISTANCE_MIN) {
            final float f = G_AT_0 + SLOPE_0_TO_MIN * distance;
            return new float[]{f, f};
        }
        if (distance <= distanceMid) {
            final float d = distance - DISTANCE_MIN;
            return new float[]{d * slopeMinToMid[groupA][groupB], d * slopeMinToMid[groupB][groupA]};
        }
        final float d = distance - distanceMax;
        return new float[]{d * slopeMidToMax[groupA][groupB], d * slopeMidToMax[groupB][groupA]};
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
