package game;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import game.character.Golfer;
import game.character.Player;
import game.display.DisplayFrame;
import game.effect.GraphicEffect;
import game.stage.GolfCourse;
import game.stage.HoleLoader;
import game.stage.HoleManage;

public class GolfGame {
	public static final String GAME_NAME = "MyGolfGame";
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 600;
	private DisplayFrame frame;

	// private HoleManage hm;

	public GolfGame() {
		frame = new DisplayFrame();
		JFrame loader = new JFrame();
		loader.setUndecorated(true);
		loader.setAlwaysOnTop(true);
		Thread t = null;
		try {
			final BufferedImage loaderImage = ImageIO.read(GolfGame.class
					.getResource("splash_screen.png"));
			JLabel loaderLabel = new JLabel(new ImageIcon(loaderImage));
			loader.setBounds(
					(Toolkit.getDefaultToolkit().getScreenSize().width - loaderImage
							.getWidth()) / 2,
					(Toolkit.getDefaultToolkit().getScreenSize().height - loaderImage
							.getHeight()) / 2, loaderImage.getWidth(),
					loaderImage.getHeight());
			JPanel allLoad = new JPanel(null);
			final JLabel loadingText = new JLabel("Loading...");
			loadingText.setFont(new Font("Arial", Font.BOLD, 25));
			loaderLabel.setBounds(0, 0, loaderImage.getWidth(),
					loaderImage.getHeight());
			loadingText.setBounds(0, 0, 130, 50);
			loadingText.setHorizontalAlignment(JLabel.CENTER);
			allLoad.add(loadingText);
			allLoad.add(loaderLabel);
			loader.getContentPane().add(allLoad);
			loadingText.setLocation(
					loaderImage.getWidth() - loadingText.getWidth(),
					loaderImage.getHeight() - loadingText.getHeight());
			t = new Thread(new Runnable() {
				int count = 0;

				@Override
				public void run() {
					try {
						while (true) {
							String dot = "";
							for (int i = 0; i < count % 4; i++) {
								dot += ".";
							}
							loadingText.setText("Loading" + dot);
							Thread.sleep(200);
							count++;
						}
					} catch (InterruptedException e) {
					}
				}
			});
			t.start();
			loader.setVisible(true);
		} catch (IOException e) {

		}
		GolfCourse golfCourse[] = HoleLoader.load(frame);
		frame.setGolfCourse(golfCourse[0]);
		loader.setVisible(false);
		if (t != null)
			t.interrupt();
		frame.setVisible(true);
	}

	public void run() throws InterruptedException {
		if (frame.getHoleManage() != null) {
			if (frame.getHoleManage().run()) {
				frame.setHoleManage(null);
				frame.getOptionSequence().clear();
				frame.clear();
				frame.getOptionSequence().clear();
				frame.getLabel().add(frame.getPanelTitle());
				frame.getLabel().validate();
			}
			Thread.sleep(100);
		} else {
			frame.update();
			Thread.sleep(20);
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		GolfGame gg = new GolfGame();
		while (true) {
			gg.run();
		}
		// while (true) {
		// frame.update();
		// Thread.sleep(1000);
		// }
	}

}
