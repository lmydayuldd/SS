package off_lattice;

import cell_index_method.exercises.Exercise;
import cell_index_method.models.Particle;
import cell_index_method.models.State;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class OffLatticeFileManager {

    private List<String> graphDataSet;
    private int frameNumber;

    public OffLatticeFileManager() {
        frameNumber = 1;
        graphDataSet = new LinkedList<>();
    }

    public void generateFrame(State state, String fileNameFormat) {
        List<String> lines = new LinkedList<>();
        lines.add(String.valueOf(state.getParticleCount()));
        lines.add("");
        state.getParticles().forEach(particle -> addParticleLine(lines, particle));
        Exercise.writeTo(String.format(fileNameFormat, frameNumber), lines);
        frameNumber++;
    }

    public List<Particle> getParticles(String fileName) {
        List<String> lines = Exercise.readFrom(fileName);
        List<Particle> particles = new LinkedList<>();
        if (lines == null) {
            return particles;
        }
        int particlesCount = Integer.valueOf(lines.get(0));
        // Starts in 2 because first line is a comment (XYZ format)
        for (int i = 2; i < particlesCount + 2; i++) {
            Scanner lineScanner = new Scanner(lines.get(i));
            int id = lineScanner.nextInt();
            double x = lineScanner.nextDouble();
            double y = lineScanner.nextDouble();
            double angle = lineScanner.nextDouble();
            int red = lineScanner.nextInt();
            int green = lineScanner.nextInt();
            int blue = lineScanner.nextInt();
            particles.add(new Particle(id, x, y,
                    OffLatticeParameters.PARTICLES_RADIUS, OffLatticeParameters.SPEED, angle));
        }
        return particles;
    }

    private void addParticleLine(List<String> lines, Particle particle) {
        Color color = particle.getColorForAngle();
        lines.add(particle.getId() + " "
                + particle.getX() + " "
                + particle.getY() + " "
                + particle.getAngle() + " "
                + color.getRed() + " "
                + color.getGreen() + " "
                + color.getBlue());
    }

    public void saveDataForState(State state) {
        graphDataSet.add(frameNumber + "\t" + state.polarization());
    }

    public void printGraphDataSet(State state, String filename) {
        graphDataSet.add(0, state.getEta() + " " + state.getDensity());
        Exercise.writeTo(filename, graphDataSet);
    }

}
