package game.stage;

import game.GolfGame;
import game.display.DisplayFrame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class HoleLoader {
	private static final String COURSE_LIST = "course_list.txt";

	private HoleLoader() {
	}

	public static GolfCourse[] load(DisplayFrame frame) {
		ArrayList<GolfCourse> course = new ArrayList<GolfCourse>();
		try {
			Scanner file = new Scanner(
					HoleLoader.class.getResourceAsStream(COURSE_LIST));
			ArrayList<GolfHole> hole = new ArrayList<GolfHole>();
			while (file.hasNext()) {
				String read = file.nextLine().trim();
				int count = 0;
				while (true) {
					try {
						hole.add(new GolfHole(HoleLoader.class
								.getResource("hole/" + read + "_Hole" + count
										+ ".png"), HoleLoader.class
								.getResource("hole/" + read + "_Hole" + count
										+ "_friction.png"), HoleLoader.class
								.getResourceAsStream("hole/" + read + "_Hole"
										+ count + "_config.txt"),
								HoleLoader.class.getResource("hole/" + read
										+ "_Hole" + count + "_background.png"),
								frame));
						count++;
					} catch (IOException | IllegalArgumentException e) {
						break;
					}
				}
			}
			course.add(new GolfCourse(hole.toArray(new GolfHole[hole.size()])));
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(frame, "Cannot find " + COURSE_LIST
					+ "(" + e.getMessage() + ")", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return course.toArray(new GolfCourse[course.size()]);
	}
}
