package game.stage;

import game.GolfGame;
import game.character.Ball;
import game.display.DisplayFrame;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class GolfHole {
	public static final int LAYER_NUM = 3;
	private BufferedImage terrain;
	private BufferedImage friction;
	private BufferedImage background;
	private boolean isDestroyed[][];
	private int par;
	private Point start, finish;
	private BufferedImage tempCurrentTerrain,
			tempCurrentTerrainBack[] = new BufferedImage[LAYER_NUM],
			tempCurrentTerrainFront[] = new BufferedImage[LAYER_NUM];
	private DisplayFrame frame;

	public GolfHole(URL f, URL g, InputStream h, URL i, DisplayFrame frame)
			throws IOException, IllegalArgumentException {
		this.frame = frame;
		terrain = ImageIO.read(f);
		friction = ImageIO.read(g);
		background = ImageIO.read(i);
		Scanner file = new Scanner(h);
		par = file.nextInt();
		int temp1 = file.nextInt();
		int temp2 = file.nextInt();
		start = new Point(temp1, temp2);
		temp1 = file.nextInt();
		temp2 = file.nextInt();
		finish = new Point(temp1, temp2);
		repair();
	}

	public void repair() {
		isDestroyed = new boolean[friction.getHeight()][friction.getWidth()];
		for (int i = 0; i < friction.getHeight(); i++) {
			for (int j = 0; j < friction.getWidth(); j++) {
				if (friction.getRGB(j, i) >>> 24 <= 10) {
					isDestroyed[i][j] = true;
				} else {
					isDestroyed[i][j] = false;
				}
			}
		}
		for (int i = -Ball.BALL_SIZE + 1; i < Ball.BALL_SIZE; i++) {
			for (int j = Ball.BALL_SIZE / 2; finish.y + j >= 0; j--) {
				if (!isDestroyed[finish.y + j][finish.x + i]) {
					isDestroyed[finish.y + j][finish.x + i] = true;
				} else {
					break;
				}
			}
		}
		updateCurrentTerrain();
	}

	public BufferedImage getCurrentTerrain(int depth) {
		if (depth >= 0)
			return tempCurrentTerrainFront[depth];
		return tempCurrentTerrainBack[-depth];
	}

	public BufferedImage getCurrentTerrain() {
		return tempCurrentTerrain;
	}

	private void updateCurrentTerrain() {
		tempCurrentTerrain = new BufferedImage(terrain.getWidth(),
				terrain.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage tempCurrentTerrainMostFront = new BufferedImage(
				terrain.getWidth(), terrain.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage tempCurrentTerrainMostBack = new BufferedImage(
				terrain.getWidth(), terrain.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		LinkedList<Point> q = new LinkedList<Point>();
		boolean isSet[][] = new boolean[isDestroyed.length][isDestroyed[0].length];
		for (int i = 0; i < tempCurrentTerrain.getHeight(); i++) {
			for (int j = 0; j < tempCurrentTerrain.getWidth(); j++) {
				if (!isDestroyed[i][j]) {
					int sumx = 0;
					int sumy = 0;
					for (int a = -3; a <= 3; a++) {
						for (int b = -3; b <= 3; b++) {
							int chkx = b + j;
							int chky = a + i;
							if (chkx >= 0 && chky >= 0
									&& chkx < isDestroyed[i].length
									&& chky < isDestroyed.length) {
								if (isDestroyed[chky][chkx]) {
									sumx += b;
									sumy += a;
								}
							}
						}
					}
					int count = 0;
					for (int a = -1; a <= 1; a++) {
						for (int b = -1; b <= 1; b++) {
							int chkx = b + j;
							int chky = a + i;
							if (chkx >= 0 && chky >= 0
									&& chkx < isDestroyed[i].length
									&& chky < isDestroyed.length) {
								if (isDestroyed[chky][chkx]) {
									count++;
								}
							}
						}
					}
					if (count == 0) {
						sumx = 0;
						sumy = 0;
						tempCurrentTerrainMostFront.setRGB(j, i,
								terrain.getRGB(j, i));
						tempCurrentTerrainMostBack.setRGB(j, i,
								terrain.getRGB(j, i));
					} else {
						tempCurrentTerrainMostFront.setRGB(j, i, new Color(0,
								0, 0, 255).getRGB());
						tempCurrentTerrainMostBack.setRGB(j, i, new Color(0, 0,
								0, 150).getRGB());
					}
					if (sumx != 0 && sumy != 0) {
						Color c = new Color(terrain.getRGB(j, i));
						tempCurrentTerrain.setRGB(
								j,
								i,
								new Color(Math.min(255, Math.max(c.getRed() + 5
										* sumx - sumy, 0)),
										Math.min(
												255,
												Math.max(c.getGreen() + 5
														* sumx - sumy, 0)),
										Math.min(
												255,
												Math.max(c.getBlue() + 5 * sumx
														- sumy, 0))).getRGB());
						q.add(new Point(j, i));
						isSet[i][j] = true;
					}
				} else {
					Color c = new Color(terrain.getRGB(j, i));
					if (terrain.getRGB(j, i) >>> 24 > 0) {
						tempCurrentTerrain.setRGB(
								j,
								i,
								new Color(c.getRed() * 3 / 4,
										c.getGreen() * 3 / 4,
										c.getBlue() * 3 / 4, (int) ((terrain
												.getRGB(j, i) >>> 24) / 1.5))
										.getRGB());
					}
				}
			}
		}
		while (!q.isEmpty()) {
			Point p = q.removeFirst();
			int px = p.x, py = p.y;
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					px = p.x - 1;
					py = p.y;
				} else if (i == 1) {
					px = p.x;
					py = p.y - 1;
				} else if (i == 2) {
					px = p.x + 1;
					py = p.y;
				} else if (i == 3) {
					px = p.x;
					py = p.y + 1;
				}
				if (px >= 0 && px < isSet[0].length && py >= 0
						&& py < isSet.length && !isSet[py][px]
						&& !isDestroyed[py][px]) {
					isSet[py][px] = true;
					tempCurrentTerrain.setRGB(px, py,
							tempCurrentTerrain.getRGB(p.x, p.y));
					tempCurrentTerrainMostBack.setRGB(px, py,
							tempCurrentTerrain.getRGB(p.x, p.y));
					q.add(new Point(px, py));
				}
			}
		}
		for (int i = 0; i < LAYER_NUM; i++) {

			tempCurrentTerrainFront[i] = new BufferedImage(
					(int) (tempCurrentTerrain.getWidth() * 80.0 / (80.0 - i)),
					(int) (tempCurrentTerrain.getHeight() * 80.0 / (80.0 - i)),
					BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(80.0 / (80.0 - i), 80.0 / (80.0 - i));
			AffineTransformOp scaleOp = new AffineTransformOp(at,
					AffineTransformOp.TYPE_BILINEAR);
			if (i < LAYER_NUM - 1) {
				scaleOp.filter(tempCurrentTerrain, tempCurrentTerrainFront[i]);
			} else {
				scaleOp.filter(tempCurrentTerrainMostFront,
						tempCurrentTerrainFront[i]);
			}
			tempCurrentTerrainBack[i] = new BufferedImage(
					(int) (tempCurrentTerrain.getWidth() * 100.0 / (100.0 + i)),
					(int) (tempCurrentTerrain.getHeight() * 100.0 / (100.0 + i)),
					BufferedImage.TYPE_INT_ARGB);
			at = new AffineTransform();
			at.scale(100.0 / (100.0 + i), 100.0 / (100.0 + i));
			scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			if (i < LAYER_NUM - 1) {
				scaleOp.filter(tempCurrentTerrain, tempCurrentTerrainBack[i]);
			} else {
				scaleOp.filter(tempCurrentTerrainMostBack,
						tempCurrentTerrainBack[i]);
			}
		}
	}

	public int getPar() {
		return par;
	}

	public Point getStart() {
		return start;
	}

	public Point getFinish() {
		return finish;
	}

	public double isHit(int checkx, int checky) {
		if (isDestroyed[checky][checkx]) {
			return 1.0;
		}
		return new Color(friction.getRGB(checkx, checky)).getRed() / 255.0;
	}

	public BufferedImage getBackground() {
		return background;
	}
}
