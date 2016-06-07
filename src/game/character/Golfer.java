package game.character;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import game.display.DisplayFrame;
import game.effect.graphics.Animation;

public class Golfer implements Comparable<Golfer> {
	public static final double MAX_SPEED = 10;
	private Ball ball;
	private Animation ani;
	private ArrayList<Integer> score = new ArrayList<Integer>();
	private int point = 0;
	private int number;

	public Golfer(int i, DisplayFrame frame) {
		this.number = i;
		try {
			ani = new Animation(ImageIO.read(getClass().getResource(
					"player" + i + ".png")), 4, frame);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Cannot find " + "player" + i
					+ ".png", "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		ball = new Ball(frame);
	}

	public void update() {
		getAni().setX(getBall().getX());
		getAni().setY(getBall().getY());
	}

	@Override
	public int compareTo(Golfer that) {
		if (this.score.size() > 0 && that.score.size() > 0) {
			int thisS = 0;
			for (int i = 0; i < this.score.size(); i++) {
				thisS += this.score.get(i);
			}
			int thatS = 0;
			for (int i = 0; i < that.score.size(); i++) {
				thatS += that.score.get(i);
			}
			return thisS - thatS;
		}
		return 0;
	}

	public int[] getScore() {
		int out[] = new int[score.size()];
		for (int i = 0; i < score.size(); i++) {
			out[i] = score.get(i);
		}
		return out;
	}

	public ArrayList<Integer> getScoreAsList() {
		return score;
	}

	public boolean isHoleOut(int hole) {
		if (this.score.size() > hole)
			return true;
		return false;
	}

	public void setScore(int score) {
		this.score.add(score);
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public Ball getBall() {
		return this.ball;
	}

	public Animation getAni() {
		return ani;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

}
