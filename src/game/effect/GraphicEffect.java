package game.effect;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import game.display.DisplayFrame;

public class GraphicEffect {
	private BufferedImage img;
	private boolean refCenter, onScreen;
	private int divide;
	private int playingFrame = 0;
	private double rotation = 0;
	private double x = 0, y = 0;
	private DisplayFrame frame;

	protected GraphicEffect(BufferedImage img, int i, boolean refCenter,
			DisplayFrame frame) {
		this(img, i, refCenter, false, frame);
	}

	protected GraphicEffect(BufferedImage img, int i, boolean refCenter,
			boolean onScreen, DisplayFrame frame) {
		if (i > 0) {
			this.img = img;
			this.divide = i;
			this.refCenter = refCenter;
			this.onScreen = onScreen;
			this.frame = frame;
		} else {
			try {
				throw new IllegalArgumentException(
						"divide must be greater than 0");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	public int getWidth() {
		return img.getWidth() / divide;
	}

	public int getHeight() {
		return img.getHeight();
	}

	public BufferedImage getImage() {
		return img.getSubimage(playingFrame * img.getWidth() / divide, 0,
				img.getWidth() / divide, img.getHeight());
	}

	public void nextFrame() {
		playingFrame = (playingFrame + 1) % divide;
	}

	public void resetFrame() {
		playingFrame = 0;
	}

	public void setRotation(double degree) {
		rotation = degree * Math.PI / 180;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public AffineTransform getAffineTransform() {
		// #####< WARNING : REVERSE ORDER >#####
		AffineTransform at = new AffineTransform();
		if (!onScreen) {
			at.translate(x - frame.getCamX(), y - frame.getCamY());
		} else {
			at.translate(x, y);
		}
		at.rotate(rotation);
		at.translate(-getWidth() / 2.0, -getHeight() / 2.0);
		if (!refCenter) {
			at.translate(0, -getHeight() / 2);
		}
		return at;
	}

	public boolean isOnScreen() {
		return onScreen;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getRotation() {
		return rotation * 180 / Math.PI;
	}
}
