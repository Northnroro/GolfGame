package game.character;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.border.StrokeBorder;

import game.GolfGame;
import game.display.DisplayFrame;
import game.effect.graphics.Animation;
import game.effect.graphics.ParticleEffect;
import game.stage.GolfHole;
import game.stage.HoleLoader;

public class Ball {
	public static final int BALL_SIZE = 11, NUM_CHECK_BOUND = 10;
	public static final double GRAVITY = 0.2, STOP_DIFFERENCE = 3.5;
	public static final int COMBO_COMBINATION[] = new int[] { 0b110110001010,
			0b001011010000 };// 0:LRDUDD(ShootingStar) 1:UDLRUU(Tornado)
	private Animation ani;
	private double checkBound[][] = new double[NUM_CHECK_BOUND][2];// x, y
	private double x, y, dx, dy, dr;
	private double differencePosition = 0;
	private boolean combo[] = new boolean[COMBO_COMBINATION.length];
	private DisplayFrame frame;

	public Ball(DisplayFrame frame) {
		BufferedImage img = new BufferedImage(BALL_SIZE, BALL_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setBackground(new Color(0, 0, 0, 0));
		g.setColor(new Color(230, 230, 255));
		g.fillOval(0, 0, BALL_SIZE, BALL_SIZE);
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				if (img.getRGB(j, i) >>> 24 != 0) {
					if (Math.random() < 0.1) {
						img.setRGB(j, i, new Color(150, 150, 200).getRGB());
					}
				}
			}
		}
		g.setColor(new Color(0, 0, 0));
		g.setStroke(new BasicStroke(2.0f));
		g.drawOval(0, 0, BALL_SIZE, BALL_SIZE);
		for (int i = 0; i < NUM_CHECK_BOUND; i++) {
			checkBound[i][0] = (BALL_SIZE
					* Math.cos(2 * Math.PI * i / NUM_CHECK_BOUND) / 2);
			checkBound[i][1] = (BALL_SIZE
					* Math.sin(2 * Math.PI * i / NUM_CHECK_BOUND) / 2);
		}
		ani = new Animation(img, true, frame);
		this.frame = frame;
		// setX(150);
		// setY(415);
	}

	public boolean update(GolfHole gh) {// true : finish
		dy += GRAVITY;
		setX(this.x + this.dx);
		setY(this.y + this.dy);
		Object result[] = getBounceAngle(gh);
		if (result != null) {
			double newAngle = (double) result[0];
			double fric = (double) result[1];
			double currSpeed = Math.hypot(dx, dy);
			currSpeed *= fric;
			double addRotate = 0;// test
			for (int i = 0; i < 20; i++) {
				Object obj[] = getBounceAngle(gh);
				if (obj != null) {
					double cos = Math.cos((double) obj[0]);
					double sin = Math.sin((double) obj[0]);
					setX(x - cos);
					setY(y - sin);
				} else {
					break;
				}
			}
			dx = -currSpeed * Math.cos(newAngle);
			dy = -currSpeed * Math.sin(newAngle);
			if (combo[0] && !combo[1]) {
				// TODO
				dx = 0;
				dy = 0;
				combo[0] = false;
				for (int i = 0; i < 100; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(205 + (int) (Math.random() * 50),
							205 + (int) (Math.random() * 50), (int) (Math
									.random() * 200)));
					int size = (int) (Math.random() * 7) + 2;
					g2d.setStroke(new BasicStroke(2));
					for (int j = 0; j < 20 * Math.random(); j++) {
						g2d.drawLine(15, 15, (int) (15 + Math.random() * 2
								* size - size), (int) (15 + Math.random() * 2
								* size - size));
					}

					ParticleEffect ballEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 8000 + 3000));
					ballEffectP.setX(getX());
					ballEffectP.setY(getY());
					ballEffectP.setDx(Math.random() * 2.0 - 1.0);
					ballEffectP.setDy(Math.random() * 2.0 - 2.5);
					ballEffectP.setDDy(Ball.GRAVITY / 10);
					frame.getEffect().add(ballEffectP);
				}
			}
			if (combo[1]) {
				// TODO
				double curspd = Math.hypot(dx, dy) * 1.5;
				dx = curspd
						* Math.cos(frame.getHoleManage().getCurrAngle()
								* Math.PI / 180);
				dy = curspd
						* Math.sin(frame.getHoleManage().getCurrAngle()
								* Math.PI / 180);
				combo[1] = false;
				for (int i = 0; i < 100; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(50 + (int) (Math.random() * 150),
							50 + (int) (Math.random() * 150), 155 + (int) (Math
									.random() * 100), 100));
					int size = (int) (Math.random() * 7) + 2;
					g2d.setStroke(new BasicStroke(2));
					for (int j = 0; j < 5 * Math.random(); j++) {
						g2d.drawOval(15, 15, (int) (15 + Math.random() * 2
								* size - size), (int) (15 + Math.random() * 2
								* size - size));
					}

					ParticleEffect ballEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 1000 + 500));
					ballEffectP.setX(getX());
					ballEffectP.setY(getY());
					ballEffectP.setDx(Math.random() * 6.0 - 3.0);
					ballEffectP.setDy(Math.random() * 6.0 - 3.0);
					frame.getEffect().add(ballEffectP);
				}
			}
		} else {
			boolean hasAnyCombo = false;
			for (int i = 0; i < combo.length; i++) {
				hasAnyCombo |= combo[i];
			}
			if (!hasAnyCombo) {
				if (frame != null) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(105 + (int) (Math.random() * 150),
							105 + (int) (Math.random() * 150),
							105 + (int) (Math.random() * 150)));
					int size = (int) (Math.random() * 5) + 1;
					g2d.fillOval(15, 15, size, size);
					g2d.setColor(new Color(255, 255, 255, 200));
					g2d.drawOval(15, 15, size, size);

					ParticleEffect ballEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 2000 + 1000));
					ballEffectP.setX(getX());
					ballEffectP.setY(getY());
					ballEffectP.setDx(Math.random() * 1.0 - 0.5);
					ballEffectP.setDy(Math.random() * 1.0 - 0.5);
					ballEffectP.setDDy(Ball.GRAVITY / 4);
					frame.getEffect().add(ballEffectP);
				}
			}
			if (combo[0]) {
				// TODO
				BufferedImage effectImage = new BufferedImage(30, 30,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = effectImage.createGraphics();

				g2d.setColor(new Color(205 + (int) (Math.random() * 50),
						205 + (int) (Math.random() * 50),
						(int) (Math.random() * 200)));
				int size = (int) (Math.random() * 7) + 2;
				g2d.setStroke(new BasicStroke(2));
				for (int j = 0; j < 20 * Math.random(); j++) {
					g2d.drawLine(15, 15,
							(int) (15 + Math.random() * 2 * size - size),
							(int) (15 + Math.random() * 2 * size - size));
				}

				ParticleEffect ballEffectP = new ParticleEffect(effectImage,
						false, frame, (int) (Math.random() * 3000 + 3000));
				ballEffectP.setX(getX());
				ballEffectP.setY(getY());
				ballEffectP.setDx(Math.random() * 3.0 - 1.5);
				ballEffectP.setDy(Math.random() * 3.0 - 3.5);
				ballEffectP.setDDy(Ball.GRAVITY / 3);
				frame.getEffect().add(ballEffectP);
			}
			if (combo[1]) {
				// TODO
				BufferedImage effectImage = new BufferedImage(30, 30,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = effectImage.createGraphics();

				g2d.setColor(new Color(50 + (int) (Math.random() * 150),
						50 + (int) (Math.random() * 150), 155 + (int) (Math
								.random() * 100), 100));
				int size = (int) (Math.random() * 7) + 2;
				g2d.setStroke(new BasicStroke(2));
				for (int j = 0; j < 5 * Math.random(); j++) {
					g2d.drawOval(15, 15,
							(int) (15 + Math.random() * 2 * size - size),
							(int) (15 + Math.random() * 2 * size - size));
				}

				ParticleEffect ballEffectP = new ParticleEffect(effectImage,
						false, frame, (int) (Math.random() * 1000 + 500));
				ballEffectP.setX(getX());
				ballEffectP.setY(getY());
				ballEffectP.setDx(Math.random() * 2.0 - 1.0);
				ballEffectP.setDy(Math.random() * 2.0 - 1.0);
				frame.getEffect().add(ballEffectP);
			}
		}
		if (getY() > gh.getCurrentTerrain().getHeight() + 10) {

			if (frame != null) {
				for (int i = 0; i < 200; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(55 + (int) (Math.random() * 50),
							55 + (int) (Math.random() * 50), 55 + (int) (Math
									.random() * 200),
							(int) (Math.random() * 100 + 155)));
					int size = (int) (Math.random() * 10) + 1;
					g2d.fillOval(15, 15, size, size);
					g2d.setColor(new Color(50, 50, 255, 200));
					g2d.drawOval(15, 15, size, size);

					ParticleEffect ballEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 3000 + 1000));
					ballEffectP.setX(getX());
					ballEffectP.setY(getY());
					ballEffectP.setDx(Math.random() * 2.0 - 1.0);
					ballEffectP.setDy(Math.random() * 25.0 - 25.5);
					// ballEffectP.setDDx(Math.random() * 0.2 - 0.1);
					ballEffectP.setDDy(Ball.GRAVITY * 2);
					frame.getEffect().add(ballEffectP);
				}
			}

			setX(gh.getStart().x);
			setY(gh.getStart().y);
			dx = 0;
			dy = 0;
			if (frame != null) {
				BufferedImage scoreImg = null;
				try {
					scoreImg = ImageIO.read(ParticleEffect.class
							.getResource("score_ob.png"));
				} catch (Exception e) {
					scoreImg = new BufferedImage(200, 200,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = scoreImg.createGraphics();
					g2d.setFont(new Font("Arial", Font.BOLD, 60));
					g2d.setColor(new Color(255, 0, 0));
					g2d.drawString("OB", 50, 150);
				}
				ParticleEffect scorep = new ParticleEffect(scoreImg, true,
						frame, 1000);
				scorep.setX(GolfGame.SCREEN_WIDTH / 2);
				scorep.setY(GolfGame.SCREEN_HEIGHT / 2);
				scorep.setDy(0.5);
				frame.getEffect().add(scorep);

				for (int i = 0; i < 20; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(55 + (int) (Math.random() * 50),
							55 + (int) (Math.random() * 50), 55 + (int) (Math
									.random() * 200),
							(int) (Math.random() * 200 + 55)));
					int size = (int) (Math.random() * 10) + 1;
					g2d.fillOval(15, 15, size, size);
					g2d.setColor(new Color(50, 50, 255, 200));
					g2d.drawOval(15, 15, size, size);

					ParticleEffect ballEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 1000 + 500));
					ballEffectP.setX(getX());
					ballEffectP.setY(getY());
					ballEffectP.setDx(Math.random() * 1 - 0.5);
					ballEffectP.setDy(Math.random() * 3.0 - 3.5);
					ballEffectP.setDDx(Math.random() * 0.1 - 0.05);
					ballEffectP.setDDy(Ball.GRAVITY / 4);
					frame.getEffect().add(ballEffectP);
				}
			}
		}
		if (isStop()) {// <<<<< test
			this.dy = 0;
			this.dx = 0;
			if (isInHole(gh)) {
				if (frame != null) {
					for (int i = 0; i < 100; i++) {
						BufferedImage effectImage = new BufferedImage(30, 30,
								BufferedImage.TYPE_INT_ARGB);
						Graphics2D g2d = effectImage.createGraphics();

						g2d.setColor(new Color(
								55 + (int) (Math.random() * 200),
								55 + (int) (Math.random() * 200),
								55 + (int) (Math.random() * 200)));
						int size = (int) (Math.random() * 5) + 5;
						g2d.fillOval(15, 15, size, size);
						g2d.setColor(new Color(255, 255, 255, 200));
						g2d.drawOval(15, 15, size, size);

						ParticleEffect ballEffectP = new ParticleEffect(
								effectImage, false, frame,
								(int) (Math.random() * 3000 + 1000));
						ballEffectP.setX(getX());
						ballEffectP.setY(getY());
						// ballEffectP.setDx(Math.random() * 0.6 - 0.3);
						ballEffectP.setDy(Math.random() * 9.0 - 9.5);
						ballEffectP.setDDx(Math.random() * 0.05 - 0.025);
						ballEffectP.setDDy(Ball.GRAVITY / 2);
						frame.getEffect().add(ballEffectP);
					}
				}
			}
			return true;
		}
		return false;
	}

	public boolean isStop() {
		if (differencePosition < STOP_DIFFERENCE) {
			return true;
		}
		return false;
	}

	private Object[] getBounceAngle(GolfHole gh) {// [0]:Angle [1]:Friction
		Object result[] = getHitAngle(gh);
		if (result == null) {
			return null;
		}
		double xComp = (double) result[0];
		double yComp = (double) result[1];
		double fric = (double) result[2];
		double oriAngle = Math.atan2(dy, dx);
		double hitAngle = Math.atan2(yComp, xComp);
		double newAngle = 2 * hitAngle - oriAngle;
		return new Object[] { newAngle, fric };
	}

	private Object[] getHitAngle(GolfHole gh) {// return Angle in Rad (0 at +x
												// and
												// PI/2 at +y) , null : not hit
		int numHit = 0;
		double xComp = 0, yComp = 0, fric = 0;
		for (double p[] : checkBound) {
			int checkx = (int) Math.round(p[0] + x);
			int checky = (int) Math.round(p[1] + y);
			if (checkx >= 0 && checkx < gh.getCurrentTerrain().getWidth()
					&& checky >= 0
					&& checky < gh.getCurrentTerrain().getHeight()) {
				double friction = gh.isHit(checkx, checky);
				if (friction < 1.0 - 1e-5) {
					numHit++;
					xComp += p[0];
					yComp += p[1];
					fric += friction;
				}
			}
		}
		if (numHit == 0) {
			return null;
		}
		return new Object[] {
				xComp,
				yComp,
				1.0
						- (1.0 - fric / numHit)
						* Math.abs(Math.cos(Math.atan2(dy, dx)
								- Math.atan2(yComp, xComp))) };
	}

	public double[][] getCheckBound() {
		return checkBound;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.differencePosition += Math.abs(x - this.x);
		this.differencePosition /= 1.1;
		ani.setX(x);
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.differencePosition += Math.abs(y - this.y);
		this.differencePosition /= 1.1;
		ani.setY(y);
		this.y = y;
	}

	public Animation getAni() {
		return ani;
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

	public void setCombo(ArrayList<Integer> comboList) {
		setCombo();
		if (comboList.size() >= 6) {
			int code = 0;
			for (int i = 0; i < 6; i++) {
				code = code << 2;
				code += comboList.get(i);
			}
			int pos = -1;
			for (int i = 0; i < COMBO_COMBINATION.length; i++) {
				if (code == COMBO_COMBINATION[i]) {
					pos = i;
					break;
				}
			}
			if (pos >= 0) {
				this.combo[pos] = true;
				System.out.println("1 : " + pos);
				if (comboList.size() >= 12) {
					code = 0;
					for (int i = 0; i < 6; i++) {
						code = code << 2;
						code += comboList.get(6 + i);
					}
					pos = -1;
					for (int i = 0; i < COMBO_COMBINATION.length; i++) {
						if (code == COMBO_COMBINATION[i]) {
							pos = i;
							break;
						}
					}
					if (pos >= 0) {
						this.combo[pos] = true;
						System.out.println("2 : " + pos);
					}
				}
			}
		}
	}

	public void setCombo() {
		this.combo = new boolean[COMBO_COMBINATION.length];
	}

	public boolean isInHole(GolfHole gh) {
		if (Math.hypot(this.x - gh.getFinish().x, this.y - gh.getFinish().y) < 55) {
			return true;
		}
		return false;
	}
}
