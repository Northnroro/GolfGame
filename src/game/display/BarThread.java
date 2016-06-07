package game.display;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import game.GolfGame;
import game.character.Golfer;
import game.effect.graphics.Animation;
import game.stage.HoleManage;

public class BarThread implements Runnable {
	private HoleManage hm;
	private static BufferedImage bar = null;
	private static Graphics2D g2;
	private boolean pressed;
	private ArrayList<Integer> comboList = new ArrayList<Integer>();
	private static BufferedImage arrowImg = null;
	private int numIconDrawn = 0;

	public BarThread(HoleManage hm) {
		this.hm = hm;
		if (bar == null) {
			bar = new BufferedImage(700, 100, BufferedImage.TYPE_INT_ARGB);
			g2 = bar.createGraphics();
			g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND));
			clearBar();
		}
		if (arrowImg == null) {
			try {
				arrowImg = ImageIO.read(getClass().getResource("arrow.png"));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(hm.getFrame(),
						"Cannot find arrow.png" + "(" + e.getMessage() + ")",
						"ERROR", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}

	private static void clearBar() {
		g2.setComposite(AlphaComposite.Clear);
		g2.setColor(new Color(0, 0, 0, 0));
		g2.fillRect(0, 0, 700, 100);
		g2.setComposite(AlphaComposite.SrcOver);
		g2.setColor(new Color(150, 150, 200, 150));
		g2.fillRoundRect(0, 0, 700, 100, 40, 40);
		g2.setColor(new Color(0, 0, 100, 100));
		g2.drawRoundRect(0, 0, 700, 100, 40, 40);
	}

	private static void drawBarInside(double percent, double slidePercent,
			ArrayList<Integer> comboList, int numIconDrawn) {
		g2.setComposite(AlphaComposite.Clear);
		g2.setColor(new Color(150, 150, 200, 150));
		g2.fillRect(40, 60, 620, 40);
		g2.setComposite(AlphaComposite.SrcOver);
		g2.fillRect(40, 60, 620, 40);
		g2.setColor(new Color(0, 25, 100, 255));
		g2.fillRoundRect(50, 70, 600, 20, 10, 10);
		g2.setColor(new Color(150, 200, 255, 255));
		g2.fillRoundRect(50, 70, (int) (600 * percent), 20, 10, 10);
		g2.setColor(new Color(0, 0, 50, 255));
		g2.drawRoundRect(50, 70, 600, 20, 10, 10);
		g2.setColor(new Color(240, 250, 255, 255));
		g2.fillRect((int) (50 + 600 * slidePercent - 7), 70 - 5, (int) 14, 30);
		g2.setColor(new Color(0, 0, 0, 255));
		g2.drawRect((int) (50 + 600 * slidePercent - 7), 70 - 5, 14, 30);
		while (numIconDrawn < comboList.size()) {
			AffineTransform at = new AffineTransform();
			at.translate(50 + numIconDrawn * (arrowImg.getWidth() + 5), 30);
			for (int i = 0; i < comboList.get(numIconDrawn); i++) {
				at.rotate(Math.PI / 2);
			}
			at.translate(-arrowImg.getWidth() / 2, -arrowImg.getHeight() / 2);
			g2.drawImage(arrowImg, at, null);
			numIconDrawn++;
		}
	}

	@Override
	public void run() {
		Animation ge = new Animation(bar, 1, false, true, hm.getFrame());
		ge.setX(GolfGame.SCREEN_WIDTH / 2);
		ge.setY(GolfGame.SCREEN_HEIGHT - 20);
		hm.getEffect().add(ge);
		double percent = 0.0;
		double slidePercent = 0.0;
		boolean front = true;
		pressed = false;
		try {
			Thread demoThread = null;
			while (true) {

				if (front) {
					slidePercent += 0.015;
					if (slidePercent >= 1) {
						slidePercent = 1;
						front = false;
					}
				} else {
					slidePercent -= 0.01;
					if (slidePercent <= 0) {
						synchronized (hm.getEffect()) {
							hm.getEffect().remove(ge);
						}
						if (pressed) {
							Golfer g = hm.getGolfer()[hm.getCurrentTurn()];
							g.getAni().nextFrame();
							Thread.sleep(100);
							g.getAni().nextFrame();
							// -----------------------------------
							g.getBall().setDx(
									Golfer.MAX_SPEED
											* percent
											* Math.cos(hm.getCurrAngle()
													* Math.PI / 180));
							g.getBall().setDy(
									Golfer.MAX_SPEED
											* percent
											* Math.sin(hm.getCurrAngle()
													* Math.PI / 180));
							g.getBall().setCombo(comboList);
							// -----------------------------------
							hm.setFlying(true);
							Thread.sleep(100);
							g.getAni().nextFrame();
							Thread.sleep(100);
							g.getAni().resetFrame();
						}
						break;
					}
				}
				if (!pressed) {
					percent = slidePercent;
				}
				synchronized (hm) {
				if (demoThread == null || !demoThread.isAlive()) {
					demoThread = new Thread(new DemoThread(
							hm.getGolfer()[hm.getCurrentTurn()].getBall(),
							Golfer.MAX_SPEED
									* percent
									* Math.cos(hm.getCurrAngle() * Math.PI
											/ 180), Golfer.MAX_SPEED
									* percent
									* Math.sin(hm.getCurrAngle() * Math.PI
											/ 180), hm.getEffect(),
							hm.getGolfCourse().getHoles()[hm.getCurrentHole()],
							hm.getFrame()));
					demoThread.start();
				}
				
					drawBarInside(percent, slidePercent, comboList,
							numIconDrawn);
				}
				Thread.sleep(20);
			}
		} catch (InterruptedException e) {

		} finally {
			hm.setAiming(false);
			clearBar();
		}
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public void addCombo(int i) {
		if (comboList.size() < 12)
			comboList.add(i);
	}

}
