package game.display;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import game.character.Ball;
import game.effect.GraphicEffect;
import game.effect.graphics.Animation;
import game.stage.GolfHole;

public class DemoThread implements Runnable {
	private static BufferedImage img = null;
	private Ball demoBall;
	private ArrayList<GraphicEffect> ge;
	private ArrayList<GraphicEffect> age = new ArrayList<GraphicEffect>();
	private GolfHole gh;
	private DisplayFrame frame;

	public DemoThread(Ball ball, double dx, double dy,
			ArrayList<GraphicEffect> ge, GolfHole gh, DisplayFrame frame) {
		if (img == null) {
			img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = img.createGraphics();
			g2.setBackground(new Color(0, 0, 0, 0));
			g2.setColor(new Color(255, 255, 255, 150));
			g2.fillOval(0, 0, 10, 10);
			g2.setComposite(AlphaComposite.Clear);
			g2.fillOval(2, 2, 6, 6);
			g2.setComposite(AlphaComposite.SrcOver);
			g2.setColor(new Color(255, 255, 255, 100));
			g2.fillOval(2, 2, 6, 6);
		}
		demoBall = new Ball(null);
		demoBall.setDx(dx);
		demoBall.setDy(dy);
		demoBall.setCombo();
		demoBall.setX(ball.getX());
		demoBall.setY(ball.getY());
		this.ge = ge;
		this.gh = gh;
		this.frame = frame;
	}

	@Override
	public void run() {
		try {
			int count = 0;
			while (!demoBall.update(gh)) {
				if (count++ % 5 == 0) {
					Animation ani = new Animation(img, true, frame);
					ani.setX(demoBall.getX());
					ani.setY(demoBall.getY());
					age.add(ani);
					ge.add(ani);
				}
				if (count > 1000)
					break;
			}
			Thread.sleep(50);
		} catch (InterruptedException e) {

		} finally {
			synchronized (ge) {
				ge.removeAll(age);
			}
		}
	}

}
