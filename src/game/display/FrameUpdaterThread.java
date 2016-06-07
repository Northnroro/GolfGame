package game.display;

import java.awt.image.BufferedImage;

import game.GolfGame;
import game.character.Ball;
import game.stage.HoleManage;

public class FrameUpdaterThread implements Runnable {
	private HoleManage hm;

	public FrameUpdaterThread(HoleManage hm) {
		this.hm = hm;
	}

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (hm) {
					DisplayFrame frame = hm.getFrame();
					if (hm.isFlying()) {
						Ball currentBall = hm.getGolfer()[hm.getCurrentTurn()]
								.getBall();
						frame.moveCamX(currentBall.getX() + 30
								* currentBall.getDx() - GolfGame.SCREEN_WIDTH
								/ 2);
						double posY = currentBall.getDy();
						if (posY < 0)
							posY = 0;
						frame.moveCamY(currentBall.getY() + 50 * posY
								- GolfGame.SCREEN_HEIGHT / 1.2);
					}
					if (frame.getMoveCamX() < 0) {
						frame.moveCamX(0);
					}
					BufferedImage thisHole = hm.getGolfCourse().getHoles()[hm
							.getCurrentHole()].getCurrentTerrain();
					if (frame.getMoveCamX() > thisHole.getWidth()
							- GolfGame.SCREEN_WIDTH) {
						frame.moveCamX(thisHole.getWidth()
								- GolfGame.SCREEN_WIDTH);
					}
					if (frame.getMoveCamY() > thisHole.getHeight()
							- GolfGame.SCREEN_HEIGHT) {
						frame.moveCamY(thisHole.getHeight()
								- GolfGame.SCREEN_HEIGHT);
					}
					if (frame.getMoveCamY() < 0) {
						frame.moveCamY(0);
					}
					double smooth = frame.getCamSmooth();
					double tempx = frame.getMoveCamX();
					double tempy = frame.getMoveCamY();
					frame.setCamX(frame.getCamX()
							+ (frame.getMoveCamX() - frame.getCamX()) / smooth);
					frame.setCamY(frame.getCamY()
							+ (frame.getMoveCamY() - frame.getCamY()) / smooth);
					frame.moveCamX(tempx);
					frame.moveCamY(tempy);
					frame.update();
				}
				Thread.sleep(15);
			}
		} catch (InterruptedException e) {
			System.out.println("Thread Interrupted");
		}
		System.out.println("Thread Finished");
	}

}
