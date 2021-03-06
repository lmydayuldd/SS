package pedestrian_dynamics;

import pedestrian_dynamics.models.Particle;
import pedestrian_dynamics.models.State;
import pedestrian_dynamics.simulation.ParticleGenerator;
import pedestrian_dynamics.simulation.Simulation;
import pedestrian_dynamics.ui.NullPrinter;
import pedestrian_dynamics.ui.ParticlesLeftPrinter;
import pedestrian_dynamics.ui.Printer;
import pedestrian_dynamics.ui.UiPrinter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private static final int PARTICLE_COUNT = 200;
    private static final double DESIRED_SPEED = 1.2;

    private static final int FRAMES_PER_SECOND = 60;
    private static final double DELTA_TIME = 1e-3;

    private static final int[] CONTROLS = new int[]{3, 2, 3, 4};
    private static final int[] CONTROLS_MAX_DELAY = new int[]{1, 1, 1, 4};
    private static final int[] MAX_PEOPLE_PER_SECTION = new int[]{200, 200, 200, 200};

    private static final boolean VISUAL = false;
    private static final boolean MULTIPLE = false;

    public static void main(String[] args) {
        try {
            if (MULTIPLE) {
                runMultiple(5);
//                for (int i = 1; i <= 5; i++)
//                    runAll(0.8, 6.1, 0.2, i);
            } else {
                Parameters parameters = new Parameters(0, PARTICLE_COUNT, DESIRED_SPEED, DELTA_TIME, CONTROLS,
                        CONTROLS_MAX_DELAY, MAX_PEOPLE_PER_SECTION);
                List<Particle> particles = ParticleGenerator.generateParticles(parameters);
                List<Particle> staticParticles = ParticleGenerator.generateStaticParticles(parameters);
                State initialState = new State(
                        parameters, ParticleGenerator.generateHorizontalWalls(parameters), particles, staticParticles,
                        parameters.getD());

                if (VISUAL) {
                    Printer printer = new UiPrinter();
                    run(initialState, printer, parameters);
                } else {
                    makeMultipleRuns();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // A single run
    private static Stats run(State initialState, Printer printer, Parameters parameters) {
        Stats stats = new Stats(parameters);
        double time = 0;
        int steps = 0;
        int printStep = stepsToPrint();
        State currentState = initialState;

        double nextSwitch = 1;
        while (currentState.getParticleCount() > 0) {
            currentState = Simulation.updateState(currentState, parameters);

            stats.update(currentState, time);

            if (steps % printStep == 0) {
                printer.updateState(currentState);
//                for (Double aDouble : stats.getTimesToLeave()) {
//                    System.out.print(aDouble + " ");
//                }
//                System.out.println();
//                System.out.println(stats.getTotalTime());
            }

            steps++;
            time += parameters.getDeltaTime();

            if (time > nextSwitch) {
                nextSwitch += 2;
                //currentState = currentState.updatingStaticParticlesRadius();
            }
        }
        System.out.println(stats.getTimesToLeave());
        stats.print();
        return stats;
    }

    // It will run all the velocities of the simulation, each one a timesPerVelocity times
    // Every run of the same velocity will be in multiple threads but wait until they finish for next
    private static void runMultiple(int timesPerVelocity) throws InterruptedException {
        double startingVelocity = 0.8;
        double finalVelocity = 6.1;
        double deltaVelocity = 0.2;

        double currentVelocity = startingVelocity;

        while (currentVelocity < finalVelocity) {
            runMultiThread(currentVelocity, timesPerVelocity);
            currentVelocity += deltaVelocity;
        }
    }

    private static void makeMultipleRuns() throws InterruptedException {
        int runs = 5;
        ExecutorService executors = Executors.newFixedThreadPool(runs);
        IntStream.rangeClosed(1, runs).forEach(runCase -> executors.submit(() -> run(runCase)));
        executors.shutdown();
        executors.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    private static void run(int runCase) {
        System.out.println("Running case " + runCase);
        Printer printer = new ParticlesLeftPrinter();
        Parameters parameters;
        int peopleInSection = 200;
//        switch (runCase) {
//            case 1:
//                parameters = new Parameters(runCase, PARTICLE_COUNT, DESIRED_SPEED, DELTA_TIME, new int[] {2, 3, 4},
//                        new int[] {1, 1, 4}, new int[] {peopleInSection, peopleInSection, peopleInSection});
//                break;
//            case 2:
//                parameters = new Parameters(runCase, PARTICLE_COUNT, DESIRED_SPEED, DELTA_TIME, new int[] {3, 4},
//                        new int[] {1, 4}, new int[] {peopleInSection, peopleInSection});
//                break;
//            case 3:
//                parameters = new Parameters(runCase, PARTICLE_COUNT, DESIRED_SPEED, DELTA_TIME, new int[] {4},
//                        new int[] {4}, new int[] {peopleInSection});
//                break;
//            default:
//                throw new IllegalStateException();
//        }
        parameters = new Parameters(runCase, PARTICLE_COUNT, DESIRED_SPEED, DELTA_TIME, new int[] {3, 2, 3, 4},
                new int[] {1, 1, 1, 4}, new int[] {peopleInSection, peopleInSection, peopleInSection, peopleInSection});
        List<Particle> particles = ParticleGenerator.generateParticles(parameters);
        List<Particle> staticParticles = ParticleGenerator.generateStaticParticles(parameters);
        State initialState = new State(
                parameters, ParticleGenerator.generateHorizontalWalls(parameters), particles, staticParticles,
                parameters.getD());
        run(initialState, printer, parameters);
    }

    // Run N (times param) threads of the simulation with given speed at the same time
    private static void runMultiThread(double speed, int times) throws InterruptedException {
        System.out.println(String.format("---------- Calculating for desired speed: %s ----------", speed));
        ExecutorService executors = Executors.newFixedThreadPool(times);
        Printer printer = new NullPrinter();
        Parameters parameters = new Parameters(0, PARTICLE_COUNT, speed, DELTA_TIME, CONTROLS,
                CONTROLS_MAX_DELAY, MAX_PEOPLE_PER_SECTION);

        IntStream.rangeClosed(1, times).forEach(t -> {
            executors.submit(() -> {
                State initialState = new State(
                        parameters,
                        ParticleGenerator.generateHorizontalWalls(parameters),
                        ParticleGenerator.generateParticles(parameters),
                        ParticleGenerator.generateStaticParticles(parameters),
                        parameters.getD());
                Stats stats = run(initialState, printer, parameters);
                write(speed, t, stats.getTimesToLeave());
                System.out.println(String.format("Run number %d: ", t));
            });
        });

        executors.shutdown();
        executors.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    // Run a thread for every single simulation at the same time, one run each
    private static void runAll(double speedStart, double speedEnd, double step, int label) throws InterruptedException {
        double currentVelocity = speedStart;
        int threads = (int) (((speedEnd - speedStart) / step) + 2);
        ExecutorService executors = Executors.newFixedThreadPool(threads);
        while (currentVelocity < speedEnd) {
            Printer printer = new NullPrinter();
            Parameters parameters = new Parameters(0, PARTICLE_COUNT, currentVelocity, DELTA_TIME, CONTROLS,
                    CONTROLS_MAX_DELAY, MAX_PEOPLE_PER_SECTION);

            final double cur = currentVelocity;
            executors.submit(() -> {
                State initialState = new State(
                        parameters,
                        ParticleGenerator.generateHorizontalWalls(parameters),
                        ParticleGenerator.generateParticles(parameters),
                        ParticleGenerator.generateStaticParticles(parameters),
                        parameters.getD());
                Stats stats = run(initialState, printer, parameters);
                write(cur, label, stats.getTimesToLeave());
                System.out.println(String.format("^ Run for %.2f", cur));
            });

            currentVelocity += step;
        }
        executors.shutdown();
        executors.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    private static int stepsToPrint() {
        // How many steps we need to get the desired fps with the delta time given
        return (int) (1 / DELTA_TIME) / FRAMES_PER_SECOND;
    }

    private static void write(double velocity, int sample, List<Double> particlesTime) {

        List<String> lines = particlesTime.stream().map(String::valueOf).collect(Collectors.toList());
        writeTo(OUTPUT_PATH + String.format("%.2f", velocity) + "_" + sample + ".txt", lines);
    }

    private static void writeTo(String fileName, List<String> lines) {
        Path path = Paths.get(fileName);
        try {
            Files.write(path, lines, Charset.forName("UTF-8"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static final String OUTPUT_PATH = "octave/TP6/files/";
}
