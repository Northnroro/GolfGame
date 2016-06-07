package game.effect.graphics;

import java.awt.image.BufferedImage;

import game.display.DisplayFrame;
import game.effect.GraphicEffect;

public class ParticleEffect extends GraphicEffect {
	private long timeLeft;
	private double dx;
	private double dy;
	private double dr;
	private double ddx, ddy;

	public ParticleEffect(BufferedImage img, boolean onScreen,
			DisplayFrame frame, int time) {
		this(img, 1, onScreen, frame, time);
	}

	public ParticleEffect(BufferedImage img, int i, boolean os,
			DisplayFrame frame, int time) {
		super(img, i, true, os, frame);
		this.timeLeft = time + System.currentTimeMillis();
	}

	public boolean next() {
		if (this.timeLeft < System.currentTimeMillis()) {
			return false;
		}
		this.dx += this.ddx;
		this.dy += this.ddy;
		this.setRotation(this.getRotation() + this.dr);
		this.setX(this.getX() + this.dx);
		this.setY(this.getY() + this.dy);
		return true;
	}

	public double getDx() {
		return dx;
	}

	public double getDy() {
		return dy;
	}

	public void setDx(double dx) {
		this.dx = dx;
	}

	public void setDy(double dy) {
		this.dy = dy;
	}

	public void setDDx(double ddx) {
		this.ddx = ddx;
	}

	public void setDDy(double ddy) {
		this.ddy = ddy;
	}

	public void setDr(double dr) {
		this.dr = dr;
	}
}
