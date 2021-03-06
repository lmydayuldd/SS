package granular_medium;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static final String STREAM_PATH = "octave/TP5/files/stream_particles=%d_L=%d_D=%d.txt";
    private static final String NEW_PARTICLES_PATH = "octave/TP5/files/particles_diff=%d_L=%d_D=%d.txt";
    private static final String CINETIC_ENERGY_PATH = "octave/TP5/files/cinetic_particles=%d_L=%d_D=%d.txt";

    public static void saveParticlesOverTime(Stats stats) {

        System.out.println("Saving times...");

        List<String> acumulatedStream = new ArrayList();
        List<String> newParticles = new ArrayList();

        double last = 0;
        for (double particles: stats.calculateAccumulatedStream()) {
            acumulatedStream.add(String.valueOf(particles));
            newParticles.add(String.valueOf(particles - last));
            last = particles;
        }

        writeTo(String.format(STREAM_PATH, (int)stats.getParameters().getL(), stats.getParticleCount(), (int)stats.getParameters().getD()), acumulatedStream);
        writeTo(String.format(NEW_PARTICLES_PATH, (int)stats.getParameters().getL(), stats.getParticleCount(), (int)stats.getParameters().getD()), newParticles);
    }

    public static void saveEnergy(Stats stats) {
        System.out.println("Saving energy...");
        List<String> energy = new ArrayList();

        for (double particles: stats.getEnergyOverTime()) {
            energy.add(String.valueOf(particles));
        }

        writeTo(String.format(CINETIC_ENERGY_PATH, (int)stats.getParameters().getL(), stats.getParticleCount(), (int)stats.getParameters().getD()), energy);
    }

    private static void writeTo(String fileName, List<String> lines) {
        Path path = Paths.get(fileName);
        try {
            Files.write(path, lines, Charset.forName("UTF-8"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
