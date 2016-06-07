package game.stage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicBorders;

import game.GolfGame;
import game.character.Ball;
import game.character.Golfer;
import game.character.Player;
import game.display.BarThread;
import game.display.DemoThread;
import game.display.DisplayFrame;
import game.display.FrameUpdaterThread;
import game.effect.GraphicEffect;
import game.effect.graphics.ParticleEffect;

public class HoleManage {

	private GolfCourse golfCourse;
	private Golfer[] golfer;
	private int currentHole = -1;// -1
	private int currentTurn = 0;
	private boolean playing = true, flying = false, aiming = false;
	private double currAngle = -45;
	private DisplayFrame frame;
	private ArrayList<GraphicEffect> effect;
	private Thread thread;
	private Thread barThread;
	private int currentHoleTurn = 1;
	public boolean upPressed = false;
	public boolean downPressed = false;
	public boolean rightPressed = false;
	public boolean leftPressed = false;
	public boolean ctrlPressed = false;
	private Thread demoThread = null;
	private JFrame scb;

	public HoleManage(GolfCourse golfCourse, Golfer[] golfer,
			ArrayList<GraphicEffect> effect, DisplayFrame frame) {
		this.golfCourse = golfCourse;
		this.golfer = golfer;
		this.frame = frame;
		this.effect = effect;
		scb = new JFrame();
		scb.setUndecorated(true);
		update();
		scb.setVisible(true);
		frame.toFront();
	}

	private void update() {
		Container c = scb.getContentPane();
		c.removeAll();
		c.setLayout(new BorderLayout());
		int currpar = 0;
		if (currentHole >= 0 && currentHole < golfCourse.getHoles().length) {
			currpar = golfCourse.getHoles()[currentHole].getPar();
		}
		int shownum = currentHole + 1;
		if (shownum >= golfCourse.getHoles().length)
			shownum = golfCourse.getHoles().length;
		JLabel label = new JLabel("Hole : " + (shownum) + "/"
				+ golfCourse.getHoles().length + " | Par : " + currpar);
		label.setFont(new Font("Arial", Font.BOLD, 30));
		label.setOpaque(true);
		label.setBackground(new Color(255, 100, 100));
		label.setForeground(new Color(255, 255, 255));
		c.add(label, BorderLayout.NORTH);
		label = new JLabel("Shot : " + (currentHoleTurn));
		label.setFont(new Font("Arial", Font.BOLD, 30));
		label.setOpaque(true);
		label.setBackground(new Color(100, 200, 100));
		label.setForeground(new Color(255, 255, 255));
		c.add(label, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setOpaque(true);
		panel.setBackground(new Color(0, 0, 0));
		panel.setForeground(new Color(255, 255, 255));
		c.add(panel, BorderLayout.SOUTH);
		for (int i = 0; i < golfer.length; i++) {
			JPanel row = new JPanel(new FlowLayout());
			row.setOpaque(false);
			JLabel l2 = new JLabel((golfer[i] instanceof Player ? "Player#"
					: "CPU#") + (golfer[i].getNumber() + 1));
			l2.setFont(new Font("Arial", Font.BOLD, 25));
			l2.setOpaque(true);
			l2.setForeground(new Color(255, 255, 255));
			if (i != currentTurn)
				l2.setBackground(new Color(100, 100, 200 + i % 2 * 55));
			else {
				l2.setBackground(new Color(250, 250, 100));
				l2.setForeground(new Color(0, 0, 0));
			}
			row.add(l2);
			for (int j = 0; j < golfCourse.getHoles().length; j++) {
				if (j < golfer[i].getScoreAsList().size()) {
					l2 = new JLabel(String.format(" %+d ",
							golfer[i].getScore()[j]));
					l2.setFont(new Font("Arial", Font.BOLD, 25));
					l2.setOpaque(true);
					l2.setBackground(new Color(100, 100, 150 + (i + j) % 2 * 55
							+ i % 2 * 50));
					l2.setForeground(new Color(255, 255, 255));
					l2.setSize(30, 30);
				} else {
					l2 = new JLabel("---");
					l2.setFont(new Font("Arial", Font.BOLD, 25));
					l2.setOpaque(true);
					l2.setBackground(new Color(50, 50, 100 + (i + j) % 2 * 55
							+ i % 2 * 50));
					l2.setForeground(new Color(255, 255, 255));
					l2.setSize(30, 30);
				}
				row.add(l2);
			}
			panel.add(row, BorderLayout.NORTH);
			JPanel temp = new JPanel();
			temp.setLayout(new BorderLayout());
			temp.setOpaque(false);
			panel.add(temp, BorderLayout.CENTER);
			panel = temp;
		}
		scb.pack();
		scb.setBounds(frame.getX() - scb.getWidth() - 15, frame.getY(),
				scb.getWidth(), scb.getHeight());
	}

	public boolean run() {
		if (currentHole < 0) {
			gotoNextHole();
		}
		thread = new Thread(new FrameUpdaterThread(this));
		thread.start();
		while (playing) {
			update();
			if (flying) {
				if (golfer[currentTurn].getBall().update(
						golfCourse.getHoles()[currentHole])) {
					try {

						if (golfer[currentTurn].getBall().isInHole(
								golfCourse.getHoles()[currentHole])) {
							int point = currentHoleTurn
									- golfCourse.getHoles()[currentHole]
											.getPar();
							golfer[currentTurn].setScore(point);
							// TODO in hole
							BufferedImage scoreImg = null;
							try {
								scoreImg = ImageIO.read(ParticleEffect.class
										.getResource("score_"
												+ (currentHoleTurn == 1 ? "x"
														: point) + ".png"));
							} catch (Exception e) {
								scoreImg = new BufferedImage(200, 200,
										BufferedImage.TYPE_INT_ARGB);
								Graphics2D g2d = scoreImg.createGraphics();
								g2d.setFont(new Font("Arial", Font.BOLD, 60));
								g2d.setColor(new Color(255, 0, 0));
								g2d.drawString("+" + point, 50, 150);
							}
							ParticleEffect scorep = new ParticleEffect(
									scoreImg, true, frame, 1000);
							scorep.setX(GolfGame.SCREEN_WIDTH / 2);
							scorep.setY(GolfGame.SCREEN_HEIGHT / 2);
							scorep.setDy(-0.5);
							effect.add(scorep);
							Thread.sleep(1000);
						}
						synchronized (this) {
							if (++currentTurn >= golfer.length) {
								currentTurn = 0;
								currentHoleTurn++;
							}
							int count = 0;
							while (golfer[currentTurn].isHoleOut(currentHole)) {
								if (++currentTurn >= golfer.length) {
									currentTurn = 0;
									currentHoleTurn++;
									count++;
									System.out.println("Finish "
											+ currentHoleTurn);
									if (count >= 2) {
										System.out.println("FINISHED");
										gotoNextHole();
										if (currentHole == golfCourse
												.getHoles().length) {
											// TODO prepare for finishing
											Arrays.sort(golfer);
											update();
											JFrame winFrame = new JFrame();
											winFrame.setUndecorated(true);
											winFrame.setSize(500, 60);
											JLabel winL = new JLabel(
													(golfer[0] instanceof Player ? "Player#"
															: "CPU#")
															+ (golfer[0]
																	.getNumber() + 1)
															+ " is the winner!");
											winL.setFont(new Font("Arial",
													Font.BOLD, 35));
											winL.setBackground(new Color(0, 0,
													0));
											winL.setOpaque(true);
											winL.setHorizontalAlignment(JLabel.CENTER);
											winL.setForeground(new Color(255,
													255, 0));
											winFrame.add(winL);
											winFrame.setBounds(
													frame.getX()
															+ (frame.getWidth() - winFrame
																	.getWidth())
															/ 2,
													frame.getY()
															+ (frame.getHeight() - winFrame
																	.getHeight())
															/ 2, winFrame
															.getWidth(),
													winFrame.getHeight());
											final JFrame autoClose = winFrame;
											new Thread(new Runnable() {
												long timer = System
														.currentTimeMillis() + 3000;

												@Override
												public void run() {
													// TODO Auto-generated
													// method stub
													while (System
															.currentTimeMillis() < timer) {
														try {
															autoClose.setBounds(
																	frame.getX()
																			+ (frame.getWidth() - autoClose
																					.getWidth())
																			/ 2
																			+ (int) (Math
																					.random() * 5 - 2),
																	frame.getY()
																			+ (frame.getHeight() - autoClose
																					.getHeight())
																			/ 2
																			+ (int) (Math
																					.random() * 5 - 2),
																	autoClose
																			.getWidth(),
																	autoClose
																			.getHeight());
															Thread.sleep(100);
														} catch (InterruptedException e) {
														}
													}
													autoClose.setVisible(false);
													scb.setVisible(false);
												}
											}).start();
											winFrame.setVisible(true);
											winFrame.setAlwaysOnTop(true);
											return true;
										}
										break;
									}
								}
							}
							golfer[currentTurn].update();
							frame.moveCamX(golfer[currentTurn].getAni().getX()
									- GolfGame.SCREEN_WIDTH / 2);
							frame.moveCamY(golfer[currentTurn].getAni().getY()
									- GolfGame.SCREEN_HEIGHT / 1.2);

							flying = false;
						}
						Thread.sleep(200);
					} catch (InterruptedException e) {

					}
				}
			} else if (aiming) {

			} else {
				if (rightPressed) {
					if (currAngle < -100 && currAngle >= -190) {
						currAngle += (-90 - currAngle) * 2;
					}
				}
				if (leftPressed) {
					if (currAngle >= -80 && currAngle <= 10) {
						currAngle += (-90 - currAngle) * 2;
					}
				}
				if (upPressed) {
					if (currAngle > -70 && currAngle <= 10) {
						currAngle -= 1;
					} else if (currAngle < -110 && currAngle >= -190) {
						currAngle += 1;
					}
				}
				if (downPressed) {
					if (currAngle > -80 && currAngle < 0) {
						currAngle += 1;
					} else if (currAngle < -100 && currAngle > -180) {
						currAngle -= 1;
					}
				}
				// DEMO
				if (demoThread == null || !demoThread.isAlive()) {
					demoThread = new Thread(new DemoThread(
							this.getGolfer()[this.getCurrentTurn()].getBall(),
							Golfer.MAX_SPEED
									* 1.0
									* Math.cos(this.getCurrAngle() * Math.PI
											/ 180), Golfer.MAX_SPEED
									* 1.0
									* Math.sin(this.getCurrAngle() * Math.PI
											/ 180), this.getEffect(),
							this.getGolfCourse().getHoles()[this
									.getCurrentHole()], this.getFrame()));
					demoThread.start();
				}
				//
				golfer[currentTurn].update();
			}

			if (currentHole == 0) {
				for (int i = 0; i < 1 * Math.random() - 0.2; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					ParticleEffect snowEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 2000 + 3000));
					snowEffectP.setX(Math.random()
							* golfCourse.getHoles()[currentHole]
									.getCurrentTerrain().getWidth());
					snowEffectP.setY(-50);
					snowEffectP.setDx(Math.random() * 0.5 - 0.25);
					snowEffectP.setDy(Math.random() * 1.0 - 0.5);
					snowEffectP.setDDx(Math.random() * 0.014 - 0.007);
					snowEffectP.setDDy(Ball.GRAVITY / 2);

					g2d.setColor(new Color(205, 205, 255,
							(int) (Math.random() * 105) + 100));
					int size = (int) (Math.random() * 10) + 4;
					for (int j = 0; j < 10 * Math.random(); j++) {
						int sx = (int) (Math.random() * effectImage.getWidth());
						int sy = (int) (Math.random() * effectImage.getHeight());
						g2d.drawLine(sx, sy, (int) (sx + snowEffectP.getDx()),
								sy + size);
					}

					frame.getEffect().add(snowEffectP);
				}
			} else if (currentHole == 1) {
				for (int i = 0; i < 1 * Math.random() - 0.9; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(255, 255, 255, 50));
					int size = (int) (Math.random() * 10) + 4;
					for (int j = 0; j < 30 * Math.random(); j++) {
						g2d.drawLine(15, 15, (int) (15 + Math.random() * 2
								* size - size), (int) (15 + Math.random() * 2
								* size - size));
					}

					ParticleEffect snowEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 5000 + 10000));
					snowEffectP.setX(Math.random()
							* golfCourse.getHoles()[currentHole]
									.getCurrentTerrain().getWidth());
					snowEffectP.setY(-50);
					snowEffectP.setDx(Math.random() * 2.0 - 1.0);
					snowEffectP.setDy(Math.random() * 1.0 - 0.5);
					snowEffectP.setDDx(Math.random() * 0.05 - 0.025);
					snowEffectP.setDDy(Ball.GRAVITY / 20);
					snowEffectP.setDr(Math.random() * 10.0 - 5.0);
					frame.getEffect().add(snowEffectP);
				}
			} else if (currentHole == 2) {
				for (int i = 0; i < 1 * Math.random() - 0.7; i++) {
					BufferedImage effectImage = new BufferedImage(30, 30,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D g2d = effectImage.createGraphics();

					g2d.setColor(new Color(255, 255, 255, 100));
					int size = (int) (Math.random() * 4) + 4;
					for (int j = 0; j < 10 * Math.random(); j++) {
						g2d.fillOval(15 + (int) (Math.random() * size / 2),
								15 + (int) (Math.random() * size / 2), size,
								size);
					}

					ParticleEffect snowEffectP = new ParticleEffect(
							effectImage, false, frame,
							(int) (Math.random() * 5000 + 10000));
					snowEffectP.setX(Math.random()
							* golfCourse.getHoles()[currentHole]
									.getCurrentTerrain().getWidth());
					snowEffectP.setY(-50);
					snowEffectP.setDx(Math.random() * 0.5 - 0.25);
					snowEffectP.setDy(Math.random() * 1.0 - 0.5);
					snowEffectP.setDDx(Math.random() * 0.01 - 0.005);
					snowEffectP.setDDy(Ball.GRAVITY / 15);
					snowEffectP.setDr(Math.random() * 10.0 - 5.0);
					frame.getEffect().add(snowEffectP);
				}
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {

			}
		}
		thread.interrupt();
		if (currentHole == golfCourse.getHoles().length) {
			return true;
		}
		return false;
	}

	private void gotoNextHole() {
		synchronized (this) {
			flying = false;
			aiming = false;
			currentHole++;
			currentHoleTurn = 1;
			currentTurn = 0;
			if (currentHole >= golfCourse.getHoles().length) {
				thread.interrupt();
				update();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
				return;
			}
		}
		for (Golfer i : golfer) {
			i.getBall().setX(golfCourse.getHoles()[currentHole].getStart().x);
			i.getBall().setY(golfCourse.getHoles()[currentHole].getStart().y);
		}
		golfer[currentTurn].update();
		frame.moveCamX(golfer[currentTurn].getAni().getX()
				- GolfGame.SCREEN_WIDTH / 2);
		frame.moveCamY(golfer[currentTurn].getAni().getY()
				- GolfGame.SCREEN_HEIGHT / 1.2);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {

		}
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public GolfCourse getGolfCourse() {
		return golfCourse;
	}

	public Golfer[] getGolfer() {
		return golfer;
	}

	public int getCurrentHole() {
		return currentHole;
	}

	public int getCurrentTurn() {
		return currentTurn;
	}

	public DisplayFrame getFrame() {
		return frame;
	}

	public boolean isFlying() {
		return flying;
	}

	public Thread getThread() {
		return thread;
	}

	public boolean isAiming() {
		return aiming;
	}

	public void setAiming(boolean aiming) {
		this.aiming = aiming;
	}

	public Thread getBarThread() {
		return barThread;
	}

	public void setBarThread(Thread barThread) {
		this.barThread = barThread;
	}

	public ArrayList<GraphicEffect> getEffect() {
		return effect;
	}

	public GraphicEffect[] getEffectAsArray() {
		synchronized (effect) {
			try {
				return effect.toArray(new GraphicEffect[effect.size()]);
			} catch (Exception e) {

			}
			return new GraphicEffect[0];
		}
	}

	public void setEffect(ArrayList<GraphicEffect> effect) {
		this.effect = effect;
	}

	public void setFlying(boolean flying) {
		this.flying = flying;
	}

	public double getCurrAngle() {
		return currAngle;
	}

	public void setCurrAngle(double currAngle) {
		this.currAngle = currAngle;
	}
}
