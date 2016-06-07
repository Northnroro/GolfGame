package game.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import game.GolfGame;
import game.character.Golfer;
import game.character.Player;
import game.character.Computer;
import game.debug.StackTrace;
import game.effect.GraphicEffect;
import game.effect.graphics.Animation;
import game.effect.graphics.ParticleEffect;
import game.stage.GolfCourse;
import game.stage.GolfHole;
import game.stage.HoleLoader;
import game.stage.HoleManage;

public class DisplayFrame extends JFrame {
	private Golfer[] golfer;
	private ArrayList<GraphicEffect> effect;
	private GolfCourse golfCourse;
	private HoleManage holeManage;
	private ArrayList<Integer> optionSequence = new ArrayList<Integer>();
	private double camX, camY, moveCamX, moveCamY, camSmooth = 10.0;

	private ImageIcon icon;
	private JLabel label;
	private JPanel panelTitle;
	private JPanel panelHowTo;
	private JPanel panelCredit;
	private BufferedImage img = new BufferedImage(GolfGame.SCREEN_WIDTH,
			GolfGame.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private BufferedImage bgCredit = new BufferedImage(GolfGame.SCREEN_WIDTH,
			GolfGame.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private BufferedImage bgTitle = new BufferedImage(GolfGame.SCREEN_WIDTH,
			GolfGame.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private BufferedImage bgHowTo = new BufferedImage(GolfGame.SCREEN_WIDTH,
			GolfGame.SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);

	public DisplayFrame() {
		super("<<" + GolfGame.GAME_NAME + ">>");
		try {
			bgCredit = ImageIO.read(getClass().getClassLoader().getResource(
					"game/menuImage/Credits_bg.png"));
			bgTitle = ImageIO.read(getClass().getClassLoader().getResource(
					"game/menuImage/Title_bg.png"));
			bgHowTo = ImageIO.read(getClass().getClassLoader().getResource(
					"game/menuImage/HowTo_bg.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setSize(GolfGame.SCREEN_WIDTH, GolfGame.SCREEN_HEIGHT);
		setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 4);
		icon = new ImageIcon(img);
		setLabel(new JLabel(icon));
		// ---------- PANEL TITLE ------------ //
		setPanelTitle(new JPanel(null));
		getPanelTitle().setSize(GolfGame.SCREEN_WIDTH, GolfGame.SCREEN_HEIGHT);
		// JButton start = new JButton("Start Game");
		// JButton howTo = new JButton("How To Play");
		// JButton credit = new JButton("Credit");

		getLabel().add(getPanelTitle());
		getLabel().validate();

		ImageIcon IconStart = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_start.png"));
		ImageIcon IconStartOver = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_start_over.png"));
		JButton start = new JButton(IconStart);
		start.setBounds(340, 260, 439, 97);
		start.setBorder(BorderFactory.createEmptyBorder());
		start.setContentAreaFilled(false);
		start.setRolloverIcon(IconStartOver);
		getPanelTitle().add(start);

		ImageIcon IconHowTo = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_howTo.png"));
		ImageIcon IconHowToOver = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_howTo_over.png"));
		JButton howTo = new JButton(IconHowTo);
		howTo.setBounds(355, 340, 370, 80);
		howTo.setBorder(BorderFactory.createEmptyBorder());
		howTo.setContentAreaFilled(false);
		howTo.setRolloverIcon(IconHowToOver);
		getPanelTitle().add(howTo);

		ImageIcon IconCredit = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_credit.png"));
		ImageIcon IconCreditOver = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_credit_over.png"));
		JButton credit = new JButton(IconCredit);
		credit.setBounds(355, 410, 370, 80);
		credit.setBorder(BorderFactory.createEmptyBorder());
		credit.setContentAreaFilled(false);
		credit.setRolloverIcon(IconCreditOver);
		getPanelTitle().add(credit);

		// --------------- LISTENER -----------------//
		final DisplayFrame frame = this;
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				// TEST
				// label.remove(panelTitle);
				golfer = new Golfer[] { new Player(0, frame),
						new Player(1, frame), new Computer(2, frame) };
				effect = new ArrayList<GraphicEffect>();
				holeManage = new HoleManage(golfCourse, golfer, effect, frame);

				optionSequence.add(0);
				optionSequence.add(0);
				optionSequence.add(0);

				getLabel().remove(getPanelTitle());
			}
		});
		howTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				optionSequence.add(1);
				getLabel().remove(getPanelTitle());
				getLabel().add(panelHowTo);
				getLabel().validate();
			}
		});
		credit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				optionSequence.add(2);
				getLabel().remove(getPanelTitle());
				getLabel().add(panelCredit);
				getLabel().validate();
			}
		});
		// for (int i = 0; i < 18; i++) {
		// if (i == 7)
		// panelTitle.add(start);
		// else if (i == 10)
		// panelTitle.add(howTo);
		// else if (i == 13)
		// panelTitle.add(credit);
		// else
		// panelTitle.add(new JLabel());
		// }
		ImageIcon imgTitle = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/Title.png"));
		JLabel imgLabelTitle = new JLabel();
		imgLabelTitle.setIcon(imgTitle);
		imgLabelTitle.setBounds(0, 0, imgTitle.getIconWidth(),
				imgTitle.getIconHeight());
		getPanelTitle().add(imgLabelTitle);

		getPanelTitle().setOpaque(false);
		// ************************************** //

		// ---------- PANEL HOW TO PLAY ------------ //

		panelHowTo = new JPanel(null);
		panelHowTo.setSize(GolfGame.SCREEN_WIDTH, GolfGame.SCREEN_HEIGHT);
		ImageIcon homeIconHowTo = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_home.png"));
		ImageIcon homeIconHowToOver = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_home_over.png"));
		JButton homeBtnHowTo = new JButton(homeIconHowTo);
		homeBtnHowTo.setBounds(700, 10, 90, 90);
		homeBtnHowTo.setBorder(BorderFactory.createEmptyBorder());
		homeBtnHowTo.setContentAreaFilled(false);
		homeBtnHowTo.setRolloverIcon(homeIconHowToOver);
		panelHowTo.add(homeBtnHowTo);
		homeBtnHowTo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				clear();
				optionSequence.clear();
				getLabel().remove(panelHowTo);
				getLabel().add(getPanelTitle());
				getLabel().validate();
			}
		});

		ImageIcon imgHowTo = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/HowTo.png"));
		JLabel imgLabelHowTo = new JLabel();
		imgLabelHowTo.setIcon(imgHowTo);
		imgLabelHowTo.setBounds(0, 0, imgHowTo.getIconWidth(),
				imgHowTo.getIconHeight());
		// imgLabel.setText("XXX");
		panelHowTo.add(imgLabelHowTo);

		panelHowTo.setOpaque(false);

		// ***************************************** //

		// ---------- PANEL CREDIT ------------ //

		// panelCredit = new JPanel(new BorderLayout());
		panelCredit = new JPanel(null);
		panelCredit.setSize(GolfGame.SCREEN_WIDTH, GolfGame.SCREEN_HEIGHT);
		ImageIcon homeIcon = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_home.png"));
		ImageIcon homeIconOver = new ImageIcon(getClass().getClassLoader()
				.getResource("game/menuImage/btn_home_over.png"));
		JButton homeBtnCredit = new JButton(homeIcon);
		homeBtnCredit.setBounds(700, 10, 90, 90);
		homeBtnCredit.setBorder(BorderFactory.createEmptyBorder());
		homeBtnCredit.setContentAreaFilled(false);
		homeBtnCredit.setRolloverIcon(homeIconOver);
		// homeBtnCredit.setLocation(200, 200);
		panelCredit.add(homeBtnCredit);
		homeBtnCredit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				clear();
				optionSequence.clear();
				getLabel().remove(panelCredit);
				getLabel().add(getPanelTitle());
				getLabel().validate();
			}
		});

		ImageIcon img = new ImageIcon(getClass().getClassLoader().getResource(
				"game/menuImage/Credits.png"));
		JLabel imgLabel = new JLabel();
		imgLabel.setIcon(img);
		imgLabel.setBounds(0, 0, img.getIconWidth(), img.getIconHeight());
		// imgLabel.setText("XXX");
		panelCredit.add(imgLabel);

		// panelCredit.add(topRowCredit, BorderLayout.NORTH);
		panelCredit.setOpaque(false);

		// ***************************************** //

		// Test
		// label.add(panelTitle);
		// label.remove(panelTitle);
		// End Test
		getContentPane().add(getLabel());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		final JFrame temp = this;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				toFront();
				if (JOptionPane.showConfirmDialog(temp,
						"Do you really want to close the game?", null,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			// TODO DELETE THIS FUNCTION
			public void mouseClicked(MouseEvent e) {
				if (holeManage != null && holeManage.ctrlPressed) {
					holeManage.getGolfer()[holeManage.getCurrentTurn()]
							.getBall().setX(e.getX() + camX);
					holeManage.getGolfer()[holeManage.getCurrentTurn()]
							.getBall().setY(e.getY() + camY);
				}
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (holeManage != null && holeManage.getCurrentHole() >= 0) {
					if (!holeManage.isFlying()) {
						synchronized (holeManage) {
							BufferedImage currentHole = holeManage
									.getGolfCourse().getHoles()[holeManage
									.getCurrentHole()].getCurrentTerrain();
							moveCamX((currentHole.getWidth() - GolfGame.SCREEN_WIDTH)
									* e.getPoint().x / GolfGame.SCREEN_WIDTH);
							moveCamY((currentHole.getHeight() - GolfGame.SCREEN_HEIGHT)
									* e.getPoint().y / GolfGame.SCREEN_HEIGHT);
						}
					}
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			BarThread bt;

			@Override
			public void keyPressed(KeyEvent e) {
				// System.out.println("OK");
				if (holeManage != null) {
					if (holeManage.isAiming()) {
						if (KeyEvent.getKeyText(e.getKeyCode()).equals("Space")) {
							bt.setPressed(true);
						} else if (bt.isPressed()) {
							if (KeyEvent.getKeyText(e.getKeyCode())
									.equals("Up")) {
								bt.addCombo(0b0);
							}
							if (KeyEvent.getKeyText(e.getKeyCode()).equals(
									"Right")) {
								bt.addCombo(0b1);
							}
							if (KeyEvent.getKeyText(e.getKeyCode()).equals(
									"Down")) {
								bt.addCombo(0b10);
							}
							if (KeyEvent.getKeyText(e.getKeyCode()).equals(
									"Left")) {
								bt.addCombo(0b11);
							}
						}
					} else if (!holeManage.isFlying()) {
						if (KeyEvent.getKeyText(e.getKeyCode()).equals("Space")) {
							holeManage.setAiming(true);
							bt = new BarThread(holeManage);
							Thread t = new Thread(bt);
							holeManage.setBarThread(t);
							t.start();
						}
					}
					if (KeyEvent.getKeyText(e.getKeyCode()).equals("Up")) {
						holeManage.upPressed = true;
					} else if (KeyEvent.getKeyText(e.getKeyCode()).equals(
							"Down")) {
						holeManage.downPressed = true;
					}
					if (KeyEvent.getKeyText(e.getKeyCode()).equals("Right")) {
						holeManage.rightPressed = true;
					} else if (KeyEvent.getKeyText(e.getKeyCode()).equals(
							"Left")) {
						holeManage.leftPressed = true;
					}
					if (KeyEvent.getKeyText(e.getKeyCode()).equals("Ctrl")) {
						holeManage.ctrlPressed = true;
					}
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Up")) {
					holeManage.upPressed = false;
				} else if (KeyEvent.getKeyText(e.getKeyCode()).equals("Down")) {
					holeManage.downPressed = false;
				}
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Right")) {
					holeManage.rightPressed = false;
				} else if (KeyEvent.getKeyText(e.getKeyCode()).equals("Left")) {
					holeManage.leftPressed = false;
				}
				if (KeyEvent.getKeyText(e.getKeyCode()).equals("Ctrl")) {
					holeManage.ctrlPressed = false;
				}
			};
		});
		clear();
		setResizable(false);
		// setVisible(true);
	}

	public void clear() {
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(255, 255, 255, 255));
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
	}

	private void draw() {
		// label.remove(panelTitle);
		// label.remove(panelHowTo);
		// label.remove(panelCredit);
		setFocusable(true);
		Graphics2D g = img.createGraphics();
		if (optionSequence.size() == 3 && holeManage.getCurrentHole() >= 0) {
			if (holeManage.isPlaying()) {
				GolfHole h = holeManage.getGolfCourse().getHoles()[holeManage
						.getCurrentHole()];
				g.drawImage(
						h.getBackground(),
						(int) (-getCamX()
								* (h.getBackground().getWidth() - GolfGame.SCREEN_WIDTH) / (h
								.getCurrentTerrain().getWidth() - GolfGame.SCREEN_WIDTH)),
						(int) (-getCamY()
								* (h.getBackground().getHeight() - GolfGame.SCREEN_HEIGHT) / (h
								.getCurrentTerrain().getHeight() - GolfGame.SCREEN_HEIGHT)),
						null);
				for (int i = -GolfHole.LAYER_NUM + 1; i < 0; i++) {
					g.drawImage(
							h.getCurrentTerrain(i),
							(int) (-getCamX()
									* (h.getCurrentTerrain(i).getWidth() - GolfGame.SCREEN_WIDTH) / (h
									.getCurrentTerrain().getWidth() - GolfGame.SCREEN_WIDTH)),
							(int) (-getCamY()
									* (h.getCurrentTerrain(i).getHeight() - GolfGame.SCREEN_HEIGHT) / (h
									.getCurrentTerrain().getHeight() - GolfGame.SCREEN_HEIGHT)),
							null);
				}
				Animation a = holeManage.getGolfer()[holeManage
						.getCurrentTurn()].getAni();
				g.drawImage(a.getImage(), a.getAffineTransform(), null);
				for (Golfer i : holeManage.getGolfer()) {
					Animation a1 = i.getBall().getAni();
					g.drawImage(a1.getImage(), a1.getAffineTransform(), null);
				}
				for (GraphicEffect i : holeManage.getEffectAsArray()) {
					if (i != null && !i.isOnScreen())
						if (i instanceof Animation)
							g.drawImage(i.getImage(), i.getAffineTransform(),
									null);
						else {
							if (((ParticleEffect) i).next()) {
								g.drawImage(i.getImage(),
										i.getAffineTransform(), null);
							} else {
								holeManage.getEffect().remove(i);
							}
						}
				}
				for (int i = 0; i < GolfHole.LAYER_NUM; i++) {
					g.drawImage(
							h.getCurrentTerrain(i),
							(int) (-getCamX()
									* (h.getCurrentTerrain(i).getWidth() - GolfGame.SCREEN_WIDTH) / (h
									.getCurrentTerrain().getWidth() - GolfGame.SCREEN_WIDTH)),
							(int) (-getCamY()
									* (h.getCurrentTerrain(i).getHeight() - GolfGame.SCREEN_HEIGHT) / (h
									.getCurrentTerrain().getHeight() - GolfGame.SCREEN_HEIGHT)),
							null);
				}
				synchronized (holeManage.getEffect()) {
					for (GraphicEffect i : holeManage.getEffectAsArray()) {
						if (i != null && i.isOnScreen())
							if (i instanceof Animation)
								g.drawImage(i.getImage(),
										i.getAffineTransform(), null);
							else {
								if (((ParticleEffect) i).next()) {
									g.drawImage(i.getImage(),
											i.getAffineTransform(), null);
								} else {
									holeManage.getEffect().remove(i);
								}
							}
					}
				}
			} else {
				// TODO Scoreboard

			}
		} else if (optionSequence.size() == 0) {
			// TEST
			// optionSequence.add(0);
			// optionSequence.add(0);
			// optionSequence.add(0);
			// System.out.println("XXX");

			// StackTrace.printStackTrace();
			g.drawImage(bgTitle, (int) (-getCamX()
					* (bgTitle.getWidth() - GolfGame.SCREEN_WIDTH) / bgTitle
					.getWidth()), (int) (-getCamY()
					* (bgTitle.getHeight() - GolfGame.SCREEN_HEIGHT) / bgTitle
					.getHeight()), null);
			// label.add(panelTitle);
			// label.validate();
		} else if (optionSequence.size() == 1) {
			// System.out.println("OPTION SEQUENCE SIZE 1");
			if (optionSequence.get(0) == 1) { // How To Play
				// System.out.println("HOW TO PLAY");
				g.drawImage(
						bgHowTo,
						(int) (-getCamX()
								* (bgHowTo.getWidth() - GolfGame.SCREEN_WIDTH) / bgHowTo
								.getWidth()),
						(int) (-getCamY()
								* (bgHowTo.getHeight() - GolfGame.SCREEN_HEIGHT) / bgHowTo
								.getHeight()), null);
				// label.add(panelHowTo);
				// label.validate();
			} else if (optionSequence.get(0) == 2) { // Credit
				g.drawImage(
						bgCredit,
						(int) (-getCamX()
								* (bgCredit.getWidth() - GolfGame.SCREEN_WIDTH) / bgCredit
								.getWidth()),
						(int) (-getCamY()
								* (bgCredit.getHeight() - GolfGame.SCREEN_HEIGHT) / bgCredit
								.getHeight()), null);
				// GolfHole h = holeManage.getGolfCourse().getHoles()[0];
				// g.drawImage(
				// h.getBackground(),
				// (int) (-getCamX()
				// * (h.getBackground().getWidth() - GolfGame.SCREEN_WIDTH) / (h
				// .getCurrentTerrain().getWidth() - GolfGame.SCREEN_WIDTH)),
				// (int) (-getCamY()
				// * (h.getBackground().getHeight() - GolfGame.SCREEN_HEIGHT) /
				// (h
				// .getCurrentTerrain().getHeight() - GolfGame.SCREEN_HEIGHT)),
				// null);
				// label.add(panelCredit);
				// label.validate();
			}
		}
		getContentPane().repaint();
	}

	public void update() {
		clear();
		draw();
	}

	public void showScoreBoard() {
		// TODO
		holeManage.setPlaying(false);
	}

	public void hideScoreBoard() {
		// TODO
		holeManage.setPlaying(true);
	}

	public void showHowToPlay() {
		// TODO

	}

	public void showCredit() {
		// TODO

	}

	public double getCamX() {
		if (holeManage == null) {
			camX += (MouseInfo.getPointerInfo().getLocation().getX() - getX() - camX) / 10.0;
			if (camX < 0)
				camX = 0;
			if (MouseInfo.getPointerInfo().getLocation().getX() - getX() > getWidth())
				camX = getWidth();
			return camX;
		}
		return camX;
	}

	public void setCamX(double camX) {
		this.camX = camX;
		this.moveCamX = camX;
	}

	public double getCamY() {
		if (holeManage == null) {
			camY += (MouseInfo.getPointerInfo().getLocation().getY() - getY() - camY) / 10.0;
			if (camY < 0)
				camY = 0;
			return camY;
		}
		return camY;
	}

	public void setCamY(double camY) {
		this.camY = camY;
		this.moveCamY = camY;
	}

	public void moveCamX(double camX) {
		this.moveCamX = camX;
	}

	public void moveCamY(double camY) {
		this.moveCamY = camY;
	}

	public Golfer[] getGolfer() {
		return golfer;
	}

	public void setGolfer(Golfer[] golfer) {
		this.golfer = golfer;
	}

	public ArrayList<GraphicEffect> getEffect() {
		return effect;
	}

	public void setEffect(ArrayList<GraphicEffect> effect) {
		this.effect = effect;
	}

	public GolfCourse getGolfCourse() {
		return golfCourse;
	}

	public void setGolfCourse(GolfCourse golfCourse) {
		this.golfCourse = golfCourse;
	}

	public HoleManage getHoleManage() {
		return holeManage;
	}

	public void setHoleManage(HoleManage holeManage) {
		this.holeManage = holeManage;
	}

	public double getCamSmooth() {
		return camSmooth;
	}

	public double getMoveCamX() {
		return moveCamX;
	}

	public double getMoveCamY() {
		return moveCamY;
	}

	public int getOptionSeqNo() {
		return optionSequence.size();
	}

	public ArrayList<Integer> getOptionSequence() {
		return optionSequence;
	}

	public void setOptionSequence(ArrayList<Integer> optionSequence) {
		this.optionSequence = optionSequence;
	}

	public JLabel getLabel() {
		return label;
	}

	public void setLabel(JLabel label) {
		this.label = label;
	}

	public JPanel getPanelTitle() {
		return panelTitle;
	}

	public void setPanelTitle(JPanel panelTitle) {
		this.panelTitle = panelTitle;
	}
}
