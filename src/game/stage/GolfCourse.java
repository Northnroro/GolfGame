package game.stage;

import java.util.ArrayList;

public class GolfCourse {
	private ArrayList<GolfHole> holes = new ArrayList<GolfHole>();

	public GolfCourse(GolfHole h[]) {
		for (GolfHole i : h) {
			holes.add(i);
		}
	}

	public GolfHole[] getHoles() {
		return holes.toArray(new GolfHole[holes.size()]);
	}
}
