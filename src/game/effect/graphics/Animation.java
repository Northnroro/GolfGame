package game.effect.graphics;

import java.awt.image.BufferedImage;

import game.display.DisplayFrame;
import game.effect.GraphicEffect;

public class Animation extends GraphicEffect {

	public Animation(BufferedImage img, DisplayFrame frame) {
		this(img, 1, false, frame);
	}

	public Animation(BufferedImage img, int divide, DisplayFrame frame) {
		super(img, divide, false, frame);
	}

	public Animation(BufferedImage img, boolean refCenter, DisplayFrame frame) {
		this(img, 1, refCenter, frame);
	}

	public Animation(BufferedImage img, int divide, boolean refCenter,
			DisplayFrame frame) {
		this(img, divide, refCenter, false, frame);
	}

	public Animation(BufferedImage img, int divide, boolean refCenter,
			boolean onScreen, DisplayFrame frame) {
		super(img, divide, refCenter, onScreen, frame);
	}

}
