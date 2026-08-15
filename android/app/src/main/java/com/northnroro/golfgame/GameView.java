package com.northnroro.golfgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class GameView extends View {
    private static final float BASE_W = 800f;
    private static final float BASE_H = 600f;
    private static final String[] COMBO_NAMES = { "Shooting Star", "Tornado" };
    private static final String[] COMBO_TEXT = { "← → ↓ ↑ ↓ ↓", "↑ ↓ ← → ↑ ↑" };

    private enum Screen { TITLE, HOW_TO, CREDITS, PLAYING, FINISHED }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final ArrayList<Integer> comboInput = new ArrayList<>();
    private final boolean[] keyDown = new boolean[4];
    private final AndroidSound sound;
    private final Course[] courses = new Course[3];

    private Bitmap titleBg;
    private Bitmap titleFg;
    private Bitmap howToBg;
    private Bitmap howToFg;
    private Bitmap creditsBg;
    private Bitmap creditsFg;
    private Bitmap startButton;
    private Bitmap howToButton;
    private Bitmap creditsButton;
    private Bitmap homeButton;

    private PlayerState[] players;
    private Screen screen = Screen.TITLE;
    private String fatalError;
    private String popupText;
    private long popupUntil;
    private String winnerText;

    private int currentHole;
    private int currentTurn;
    private int currentShot;
    private boolean flying;
    private boolean meterActive;
    private boolean meterForward;
    private boolean powerLocked;
    private boolean cpuMeterControl;
    private double meter;
    private double lockedPower;
    private double cpuTargetPower;
    private double angleDegrees = -45.0;
    private long cpuActionAt;
    private double physicsAccumulatorMs;

    private double camX;
    private double camY;
    private double targetCamX;
    private double targetCamY;
    private float contentScale = 1f;
    private float contentLeft;
    private float contentTop;
    private float contentWidth;
    private float contentHeight;

    private float dragLastX;
    private float dragLastY;
    private boolean draggingCamera;
    private int heldDirection = -1;
    private long lastFrameNs;

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        sound = new AndroidSound(context);
        try {
            loadMenuAssets(context);
            for (int i = 0; i < courses.length; i++)
                courses[i] = new Course(context, i);
        } catch (Exception e) {
            fatalError = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        lastFrameNs = System.nanoTime();
    }

    private void loadMenuAssets(Context context) throws IOException {
        titleBg = loadBitmap(context, "game/menuImage/Title_bg.png");
        titleFg = loadBitmap(context, "game/menuImage/Title.png");
        howToBg = loadBitmap(context, "game/menuImage/HowTo_bg.png");
        howToFg = loadBitmap(context, "game/menuImage/HowTo.png");
        creditsBg = loadBitmap(context, "game/menuImage/Credits_bg.png");
        creditsFg = loadBitmap(context, "game/menuImage/Credits.png");
        startButton = loadBitmap(context, "game/menuImage/btn_start.png");
        howToButton = loadBitmap(context, "game/menuImage/btn_howTo.png");
        creditsButton = loadBitmap(context, "game/menuImage/btn_credit.png");
        homeButton = loadBitmap(context, "game/menuImage/btn_home.png");
    }

    private static Bitmap loadBitmap(Context context, String path) throws IOException {
        try (InputStream in = context.getAssets().open(path)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null)
                throw new IOException("Cannot decode " + path);
            return bitmap;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        contentScale = Math.min(w / BASE_W, h / BASE_H);
        contentWidth = BASE_W * contentScale;
        contentHeight = BASE_H * contentScale;
        contentLeft = (w - contentWidth) * 0.5f;
        contentTop = (h - contentHeight) * 0.5f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateFrame();
        canvas.drawColor(Color.BLACK);
        if (fatalError != null) {
            drawFatalError(canvas);
        } else {
            drawCurrentScreen(canvas);
        }
        postInvalidateOnAnimation();
    }

    private void updateFrame() {
        long now = System.nanoTime();
        double dtMs = (now - lastFrameNs) / 1_000_000.0;
        lastFrameNs = now;
        dtMs = Math.max(1.0, Math.min(50.0, dtMs));
        if (screen == Screen.PLAYING)
            updateGame(dtMs);
        updateParticles((long) dtMs);
    }

    private void drawCurrentScreen(Canvas canvas) {
        switch (screen) {
        case TITLE:
            drawTitle(canvas);
            break;
        case HOW_TO:
            drawHowTo(canvas);
            break;
        case CREDITS:
            drawCredits(canvas);
            break;
        case PLAYING:
            drawGame(canvas);
            break;
        case FINISHED:
            drawGame(canvas);
            drawFinishedOverlay(canvas);
            break;
        }
    }

    private void drawFatalError(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        canvas.drawText("Cannot start Golf Game", 40, 70, textPaint);
        textPaint.setTextSize(18f);
        canvas.drawText(fatalError, 40, 110, textPaint);
    }

    private void drawTitle(Canvas canvas) {
        canvas.save();
        enterBase(canvas);
        drawFullScreenBitmap(canvas, titleBg);
        canvas.drawBitmap(titleFg, 0, 0, paint);
        canvas.drawBitmap(startButton, 340, 260, paint);
        canvas.drawBitmap(howToButton, 355, 340, paint);
        canvas.drawBitmap(creditsButton, 355, 410, paint);
        drawSmallLabel(canvas, "Android native port", 18, 575, Color.WHITE);
        canvas.restore();
    }

    private void drawHowTo(Canvas canvas) {
        canvas.save();
        enterBase(canvas);
        drawFullScreenBitmap(canvas, howToBg);
        canvas.drawBitmap(howToFg, 0, 0, paint);
        canvas.drawBitmap(homeButton, 700, 10, paint);
        drawPanel(canvas, 18, 485, 560, 98, 0xCC111111);
        drawSmallLabel(canvas, "Android: D-pad = aim / combo, SHOT = Space", 32, 515, Color.WHITE);
        drawSmallLabel(canvas, "Tap SHOT once to start meter, again to lock power; then enter combo.",
                32, 542, Color.WHITE);
        drawSmallLabel(canvas, "Drag the course while idle to look around. Keyboard/controller also works.",
                32, 569, Color.WHITE);
        canvas.restore();
    }

    private void drawCredits(Canvas canvas) {
        canvas.save();
        enterBase(canvas);
        drawFullScreenBitmap(canvas, creditsBg);
        canvas.drawBitmap(creditsFg, 0, 0, paint);
        canvas.drawBitmap(homeButton, 700, 10, paint);
        drawSmallLabel(canvas, "Android port + material-aware CC0 SFX", 18, 580, Color.WHITE);
        canvas.restore();
    }

    private void startGame() {
        try {
            if (players != null) {
                for (PlayerState player : players)
                    player.recycle();
            }
            players = new PlayerState[] {
                    new PlayerState(getContext(), 0, false),
                    new PlayerState(getContext(), 1, false),
                    new PlayerState(getContext(), 2, true) };
            currentHole = 0;
            screen = Screen.PLAYING;
            winnerText = null;
            gotoHole(0);
        } catch (IOException e) {
            fatalError = e.getMessage();
        }
    }

    private void gotoHole(int hole) {
        currentHole = hole;
        currentTurn = 0;
        currentShot = 1;
        flying = false;
        cancelMeter();
        comboInput.clear();
        angleDegrees = -45.0;
        physicsAccumulatorMs = 0;
        Course course = courses[currentHole];
        for (PlayerState player : players) {
            player.ball.reset(course.startX, course.startY);
            player.animationFrame = 0;
        }
        centerCameraOn(players[0].ball, true);
        scheduleCpuIfNeeded();
        showPopup("Hole " + (currentHole + 1) + " / " + courses.length, 1200);
    }

    private void updateGame(double dtMs) {
        double dt = dtMs / 1000.0;
        long now = SystemClock.uptimeMillis();

        if (!flying && !meterActive && !currentPlayer().cpu) {
            if (directionHeld(3))
                angleDegrees -= 60.0 * dt;
            if (directionHeld(1))
                angleDegrees += 60.0 * dt;
            if (directionHeld(0))
                angleDegrees -= 25.0 * dt;
            if (directionHeld(2))
                angleDegrees += 25.0 * dt;
            angleDegrees = Math.max(-175.0, Math.min(-5.0, angleDegrees));
        }

        updateCpu(now);
        if (meterActive)
            updateMeter(dt);

        if (flying) {
            physicsAccumulatorMs += dtMs;
            int safety = 0;
            while (physicsAccumulatorMs >= 10.0 && flying && safety++ < 8) {
                physicsAccumulatorMs -= 10.0;
                BallState ball = currentPlayer().ball;
                BallState.Result result = ball.update(courses[currentHole], sound, angleDegrees);
                spawnBallEffects(ball);
                if (result != BallState.Result.MOVING)
                    finishShot(result);
            }
        }

        updateCamera(dtMs);
        spawnWeather(dtMs);
    }

    private PlayerState currentPlayer() {
        return players[currentTurn];
    }

    private boolean directionHeld(int direction) {
        return heldDirection == direction || keyDown[direction];
    }

    private void startMeter(boolean cpuControlled) {
        if (flying)
            return;
        meterActive = true;
        meterForward = true;
        powerLocked = false;
        cpuMeterControl = cpuControlled;
        meter = 0.0;
        lockedPower = 0.0;
        comboInput.clear();
    }

    private void cancelMeter() {
        meterActive = false;
        meterForward = true;
        powerLocked = false;
        cpuMeterControl = false;
        meter = 0.0;
        lockedPower = 0.0;
        comboInput.clear();
    }

    private void updateMeter(double dt) {
        double speed = meterForward ? 1.18 : 1.05;
        meter += (meterForward ? 1 : -1) * speed * dt;
        if (cpuMeterControl && !powerLocked && meter >= cpuTargetPower) {
            powerLocked = true;
            lockedPower = Math.max(0.08, Math.min(1.0, meter));
        }
        if (meter >= 1.0) {
            meter = 1.0;
            meterForward = false;
        }
        if (meter <= 0.0) {
            meter = 0.0;
            if (powerLocked)
                launchShot();
            else
                cancelMeter();
        }
    }

    private void pressShot() {
        if (screen != Screen.PLAYING || flying || currentPlayer().cpu)
            return;
        if (!meterActive) {
            startMeter(false);
        } else if (!powerLocked) {
            powerLocked = true;
            lockedPower = Math.max(0.08, Math.min(1.0, meter));
        }
    }

    private void addCombo(int direction) {
        if (screen == Screen.PLAYING && meterActive && powerLocked
                && !currentPlayer().cpu && comboInput.size() < 12) {
            comboInput.add(direction);
        }
    }

    private void launchShot() {
        PlayerState player = currentPlayer();
        double power = Math.max(0.08, Math.min(1.0, lockedPower));
        player.ball.launch(angleDegrees, power, comboInput, sound);
        sound.shot(power);
        player.animationFrame = 2;
        flying = true;
        physicsAccumulatorMs = 0;
        meterActive = false;
        cpuMeterControl = false;
        targetCamX = player.ball.x - BASE_W / 2.0;
        targetCamY = player.ball.y - BASE_H / 1.2;
    }

    private void scheduleCpuIfNeeded() {
        if (players != null && currentPlayer().cpu)
            cpuActionAt = SystemClock.uptimeMillis() + 850;
        else
            cpuActionAt = 0;
    }

    private void updateCpu(long now) {
        if (players == null || !currentPlayer().cpu || flying || meterActive)
            return;
        if (now < cpuActionAt)
            return;

        BallState ball = currentPlayer().ball;
        Course course = courses[currentHole];
        double vx = course.finishX - ball.x;
        double vy = course.finishY - ball.y;
        double distance = Math.hypot(vx, vy);
        double lift = Math.min(330.0, 80.0 + distance * 0.28);
        angleDegrees = Math.toDegrees(Math.atan2(vy - lift, vx));
        if (angleDegrees > -5)
            angleDegrees = -35;
        if (angleDegrees < -175)
            angleDegrees = -145;
        cpuTargetPower = Math.max(0.28, Math.min(1.0, 0.28 + distance / 1050.0));
        startMeter(true);
    }

    private void finishShot(BallState.Result result) {
        PlayerState player = currentPlayer();
        Course course = courses[currentHole];
        flying = false;
        player.animationFrame = 0;

        if (result == BallState.Result.OUT_OF_BOUNDS) {
            player.ball.reset(course.startX, course.startY);
            showPopup("OUT OF BOUNDS", 1000);
        } else if (result == BallState.Result.HOLED) {
            int relative = currentShot - course.par;
            player.recordScore(currentHole, relative);
            showPopup(player.label() + "  " + scoreName(relative, currentShot), 1300);
            spawnCelebration(player.ball.x, player.ball.y);
        }

        if (allPlayersHoled()) {
            if (currentHole + 1 >= courses.length) {
                finishGame();
            } else {
                gotoHole(currentHole + 1);
            }
            return;
        }
        advanceTurn();
    }

    private boolean allPlayersHoled() {
        for (PlayerState player : players) {
            if (!player.holeOut[currentHole])
                return false;
        }
        return true;
    }
    private void advanceTurn() {
        int guard = 0;
        do {
            currentTurn++;
            if (currentTurn >= players.length) {
                currentTurn = 0;
                currentShot++;
            }
            guard++;
        } while (players[currentTurn].holeOut[currentHole] && guard < players.length * 2);

        BallState ball = currentPlayer().ball;
        Course course = courses[currentHole];
        angleDegrees = course.finishX >= ball.x ? -45.0 : -135.0;
        cancelMeter();
        centerCameraOn(ball, false);
        scheduleCpuIfNeeded();
    }

    private void finishGame() {
        PlayerState winner = players[0];
        for (PlayerState player : players) {
            if (player.totalScore() < winner.totalScore())
                winner = player;
        }
        winnerText = winner.label() + " wins!  Total " + formatScore(winner.totalScore());
        screen = Screen.FINISHED;
        flying = false;
        cancelMeter();
    }

    private static String scoreName(int relative, int strokes) {
        if (strokes == 1)
            return "HOLE IN ONE!";
        if (relative <= -3)
            return "ALBATROSS " + formatScore(relative);
        if (relative == -2)
            return "EAGLE -2";
        if (relative == -1)
            return "BIRDIE -1";
        if (relative == 0)
            return "PAR";
        if (relative == 1)
            return "BOGEY +1";
        return formatScore(relative);
    }

    private static String formatScore(int score) {
        return score > 0 ? "+" + score : Integer.toString(score);
    }

    private void centerCameraOn(BallState ball, boolean immediate) {
        targetCamX = ball.x - BASE_W / 2.0;
        targetCamY = ball.y - BASE_H / 1.2;
        clampTargetCamera();
        if (immediate) {
            camX = targetCamX;
            camY = targetCamY;
        }
    }

    private void updateCamera(double dtMs) {
        if (players == null)
            return;
        BallState ball = currentPlayer().ball;
        if (flying) {
            targetCamX = ball.x + 30.0 * ball.dx - BASE_W / 2.0;
            double forwardY = Math.max(0.0, ball.dy);
            targetCamY = ball.y + 50.0 * forwardY - BASE_H / 1.2;
        } else if (!draggingCamera) {
            targetCamX = ball.x - BASE_W / 2.0;
            targetCamY = ball.y - BASE_H / 1.2;
        }
        clampTargetCamera();
        double blend = Math.min(1.0, dtMs / 110.0);
        camX += (targetCamX - camX) * blend;
        camY += (targetCamY - camY) * blend;
    }

    private void clampTargetCamera() {
        Course course = courses[currentHole];
        targetCamX = Math.max(0.0, Math.min(Math.max(0, course.width - BASE_W), targetCamX));
        targetCamY = Math.max(0.0, Math.min(Math.max(0, course.height - BASE_H), targetCamY));
    }

    private void updateParticles(long dtMs) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            if (!particles.get(i).update(dtMs))
                particles.remove(i);
        }
    }

    private void spawnWeather(double dtMs) {
        double seconds = dtMs / 1000.0;
        int count = currentHole == 1 ? 14 : (currentHole == 0 ? 8 : 5);
        for (int i = 0; i < count; i++) {
            if (random.nextDouble() > seconds)
                continue;
            double x = camX + random.nextDouble() * BASE_W;
            double y = camY - 25 - random.nextDouble() * 70;
            if (currentHole == 0) {
                particles.add(new Particle(x, y, random.nextDouble() - 0.5,
                        1.0 + random.nextDouble(), 0.015, 3f + random.nextFloat() * 4f,
                        0xBBE8EDFF, 3500, false));
            } else if (currentHole == 1) {
                particles.add(new Particle(x, y, -0.3, 7.0 + random.nextDouble() * 3.0,
                        0.0, 2f, 0x88FFFFFF, 1200, false));
            } else {
                particles.add(new Particle(x, y, random.nextDouble() * 0.6 - 0.3,
                        0.5 + random.nextDouble(), 0.01, 4f + random.nextFloat() * 3f,
                        0x99FFFFFF, 4500, false));
            }
        }
    }

    private void spawnBallEffects(BallState ball) {
        if (ball.shootingStar) {
            for (int i = 0; i < 2; i++) {
                particles.add(new Particle(ball.x, ball.y,
                        random.nextDouble() * 2.5 - 1.25,
                        random.nextDouble() * -2.0, 0.08,
                        2f + random.nextFloat() * 4f, 0xFFFFE066, 900, false));
            }
        }
        if (ball.tornado) {
            particles.add(new Particle(ball.x, ball.y,
                    random.nextDouble() * 3.0 - 1.5,
                    random.nextDouble() * 3.0 - 1.5, 0.0,
                    4f + random.nextFloat() * 5f, 0xAA66CCFF, 650, false));
        }
        if (ball.bouncedThisStep) {
            int color;
            if (ball.bounceFriction >= 0.80)
                color = 0xCCBDEBFF;
            else if (ball.bounceFriction <= 0.15)
                color = 0xCCC9A66B;
            else
                color = 0xCC8ED16B;
            for (int i = 0; i < 5; i++) {
                particles.add(new Particle(ball.x, ball.y,
                        random.nextDouble() * 3 - 1.5, -random.nextDouble() * 2,
                        0.10, 2f + random.nextFloat() * 3f, color, 500, false));
            }
        }
    }

    private void spawnCelebration(double x, double y) {
        for (int i = 0; i < 42; i++) {
            int color = Color.HSVToColor(220,
                    new float[] { random.nextFloat() * 360f, 0.65f, 1f });
            particles.add(new Particle(x, y,
                    random.nextDouble() * 8 - 4,
                    random.nextDouble() * -8 - 1,
                    0.28, 3f + random.nextFloat() * 5f, color, 1600, false));
        }
    }

    private void showPopup(String text, long durationMs) {
        popupText = text;
        popupUntil = SystemClock.uptimeMillis() + durationMs;
    }

    private void drawGame(Canvas canvas) {
        if (players == null)
            return;
        Course course = courses[currentHole];
        canvas.save();
        canvas.clipRect(contentLeft, contentTop,
                contentLeft + contentWidth, contentTop + contentHeight);
        enterBase(canvas);
        drawCourseBackground(canvas, course);

        canvas.save();
        canvas.translate((float) -camX, (float) -camY);
        canvas.drawBitmap(course.terrain, 0, 0, paint);
        drawTrajectory(canvas, course);
        drawWorldParticles(canvas);
        drawCurrentGolfer(canvas);
        drawBalls(canvas);
        canvas.restore();

        drawMeter(canvas);
        drawHud(canvas);
        canvas.restore();

        drawTouchControls(canvas);
        drawPopup(canvas);
    }

    private void drawCourseBackground(Canvas canvas, Course course) {
        double denomX = Math.max(1.0, course.width - BASE_W);
        double denomY = Math.max(1.0, course.height - BASE_H);
        float x = (float) (-camX * Math.max(0, course.background.getWidth() - BASE_W) / denomX);
        float y = (float) (-camY * Math.max(0, course.background.getHeight() - BASE_H) / denomY);
        canvas.drawBitmap(course.background, x, y, paint);
    }

    private void drawTrajectory(Canvas canvas, Course course) {
        if (flying || currentPlayer().cpu)
            return;
        double previewPower = meterActive
                ? (powerLocked ? lockedPower : Math.max(0.10, meter)) : 1.0;
        double a = Math.toRadians(angleDegrees);
        BallState preview = currentPlayer().ball.previewCopy(
                BallState.MAX_SPEED * previewPower * Math.cos(a),
                BallState.MAX_SPEED * previewPower * Math.sin(a));
        paint.setColor(0x99FFFFFF);
        for (int i = 0; i < 180; i++) {
            BallState.Result result = preview.update(course, null, angleDegrees);
            if (i % 6 == 0)
                canvas.drawCircle((float) preview.x, (float) preview.y, 3f, paint);
            if (result != BallState.Result.MOVING)
                break;
        }
    }

    private void drawWorldParticles(Canvas canvas) {
        for (Particle particle : particles) {
            if (particle.screenSpace)
                continue;
            paint.setColor(particle.color);
            canvas.drawCircle((float) particle.x, (float) particle.y, particle.size, paint);
        }
    }

    private void drawCurrentGolfer(Canvas canvas) {
        PlayerState player = currentPlayer();
        int frameWidth = player.spriteSheet.getWidth() / 4;
        int frame = meterActive ? (powerLocked ? 2 : 1) : player.animationFrame;
        frame = Math.max(0, Math.min(3, frame));
        Rect src = new Rect(frame * frameWidth, 0,
                (frame + 1) * frameWidth, player.spriteSheet.getHeight());
        float left = (float) player.ball.x - frameWidth / 2f;
        float top = (float) player.ball.y - player.spriteSheet.getHeight();
        RectF dst = new RectF(left, top, left + frameWidth,
                top + player.spriteSheet.getHeight());
        canvas.drawBitmap(player.spriteSheet, src, dst, paint);
    }

    private void drawBalls(Canvas canvas) {
        for (int i = 0; i < players.length; i++) {
            PlayerState player = players[i];
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFF2F2FF);
            canvas.drawCircle((float) player.ball.x, (float) player.ball.y, 5.5f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(i == currentTurn ? 2.5f : 1.5f);
            paint.setColor(i == currentTurn ? 0xFFFFE45C : 0xFF4A4A60);
            canvas.drawCircle((float) player.ball.x, (float) player.ball.y, 5.5f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawMeter(Canvas canvas) {
        if (!meterActive)
            return;
        drawPanel(canvas, 70, 495, 660, 92, 0xD9222840);
        paint.setColor(0xFF07132D);
        canvas.drawRoundRect(new RectF(98, 548, 702, 570), 11, 11, paint);
        paint.setColor(powerLocked ? 0xFFFFD45A : 0xFF8FD4FF);
        canvas.drawRoundRect(new RectF(100, 550,
                100 + (float) (600.0 * meter), 568), 9, 9, paint);
        paint.setColor(0xFFFFFFFF);
        float markerX = 100 + (float) (600.0 * meter);
        canvas.drawRect(markerX - 3, 543, markerX + 3, 575, paint);

        String status = powerLocked
                ? "POWER LOCKED  " + Math.round(lockedPower * 100) + "%  • Enter combo now"
                : "Tap SHOT again to lock power";
        drawSmallLabel(canvas, status, 96, 522, Color.WHITE);
        if (!comboInput.isEmpty())
            drawSmallLabel(canvas, "Input: " + comboSequenceText(), 390, 522, 0xFFFFE57D);
    }

    private String comboSequenceText() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < comboInput.size(); i++) {
            if (i > 0)
                out.append(' ');
            out.append(directionSymbol(comboInput.get(i)));
        }
        return out.toString();
    }

    private static String directionSymbol(int direction) {
        switch (direction) {
        case 0: return "↑";
        case 1: return "→";
        case 2: return "↓";
        case 3: return "←";
        default: return "?";
        }
    }

    private void drawHud(Canvas canvas) {
        Course course = courses[currentHole];
        drawPanel(canvas, 10, 10, 400, 146, 0xD9000000);
        drawSmallLabel(canvas, "HOLE " + (currentHole + 1) + "/" + courses.length
                + "   PAR " + course.par + "   SHOT " + currentShot,
                22, 34, Color.WHITE);
        drawSmallLabel(canvas, "TURN " + currentPlayer().label()
                + "   ANGLE " + Math.round(angleDegrees) + "°",
                22, 58, 0xFFFFE278);

        float y = 82;
        for (int i = 0; i < players.length; i++) {
            PlayerState player = players[i];
            int color = i == currentTurn ? 0xFFFFE278 : Color.WHITE;
            drawSmallLabel(canvas, player.label() + "  " + scoreSummary(player),
                    22, y, color);
            y += 22;
        }

        drawPanel(canvas, 425, 10, 365, 116, 0xD9222230);
        drawSmallLabel(canvas, "COMBOS — after 2nd SHOT", 438, 34, 0xFFFFE278);
        drawSmallLabel(canvas, COMBO_NAMES[0] + ":  " + COMBO_TEXT[0],
                438, 60, Color.WHITE);
        drawSmallLabel(canvas, COMBO_NAMES[1] + ":  " + COMBO_TEXT[1],
                438, 84, Color.WHITE);
        drawSmallLabel(canvas, "Release/re-tap repeated directions", 438, 108, 0xFFB8C6D9);
    }

    private String scoreSummary(PlayerState player) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < player.scores.length; i++) {
            if (i > 0)
                out.append("  ");
            int score = player.scores[i];
            out.append(score == Integer.MIN_VALUE ? "—" : formatScore(score));
        }
        out.append("   Σ ").append(formatScore(player.totalScore()));
        return out.toString();
    }

    private void drawTouchControls(Canvas canvas) {
        if (screen != Screen.PLAYING)
            return;
        float r = controlRadius();
        float dcx = dpadCenterX(r);
        float dcy = dpadCenterY(r);
        float step = r * 0.92f;
        float br = r * 0.46f;
        drawCircleButton(canvas, dcx, dcy - step, br, "↑", heldDirection == 0);
        drawCircleButton(canvas, dcx + step, dcy, br, "→", heldDirection == 1);
        drawCircleButton(canvas, dcx, dcy + step, br, "↓", heldDirection == 2);
        drawCircleButton(canvas, dcx - step, dcy, br, "←", heldDirection == 3);

        float scx = shotCenterX(r);
        float scy = dcy;
        drawCircleButton(canvas, scx, scy, r * 0.78f,
                powerLocked ? "COMBO" : "SHOT", false);
        drawMenuButton(canvas);
    }

    private void drawCircleButton(Canvas canvas, float cx, float cy, float radius,
            String label, boolean active) {
        paint.setColor(active ? 0xDDFFD54F : 0xB9222B38);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, radius * 0.06f));
        paint.setColor(active ? 0xFFFFFFFF : 0xCCDCE7F3);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.FILL);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(Math.max(16f, radius * 0.48f));
        textPaint.setColor(active ? Color.BLACK : Color.WHITE);
        canvas.drawText(label, cx, cy - (textPaint.ascent() + textPaint.descent()) / 2f,
                textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawMenuButton(Canvas canvas) {
        RectF menu = menuRect();
        paint.setColor(0xBB202733);
        canvas.drawRoundRect(menu, 12, 12, paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(17f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("MENU", menu.centerX(), menu.centerY()
                - (textPaint.ascent() + textPaint.descent()) / 2f, textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawPopup(Canvas canvas) {
        if (popupText == null || SystemClock.uptimeMillis() >= popupUntil)
            return;
        float width = Math.min(getWidth() - 40f, 520f);
        RectF box = new RectF((getWidth() - width) / 2f, getHeight() * 0.42f,
                (getWidth() + width) / 2f, getHeight() * 0.42f + 72f);
        paint.setColor(0xDD111722);
        canvas.drawRoundRect(box, 18, 18, paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(26f);
        textPaint.setColor(0xFFFFE278);
        canvas.drawText(popupText, box.centerX(), box.centerY()
                - (textPaint.ascent() + textPaint.descent()) / 2f, textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawFinishedOverlay(Canvas canvas) {
        paint.setColor(0xCC000000);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFFFFE278);
        textPaint.setTextSize(42f);
        canvas.drawText(winnerText == null ? "Game finished" : winnerText,
                getWidth() / 2f, getHeight() / 2f - 25f, textPaint);
        textPaint.setTextSize(22f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Tap anywhere to return to the title",
                getWidth() / 2f, getHeight() / 2f + 30f, textPaint);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void enterBase(Canvas canvas) {
        canvas.translate(contentLeft, contentTop);
        canvas.scale(contentScale, contentScale);
    }

    private void drawFullScreenBitmap(Canvas canvas, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, null, new RectF(0, 0, BASE_W, BASE_H), paint);
    }

    private void drawPanel(Canvas canvas, float x, float y, float w, float h, int color) {
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(x, y, x + w, y + h), 12, 12, paint);
    }

    private void drawSmallLabel(Canvas canvas, String text, float x, float y, int color) {
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextSize(16f);
        textPaint.setColor(color);
        canvas.drawText(text, x, y, textPaint);
    }

    private float controlRadius() {
        return Math.max(44f, Math.min(72f, getHeight() * 0.075f));
    }

    private float dpadCenterY(float r) {
        return getHeight() - r * 1.65f;
    }

    private float dpadCenterX(float r) {
        float leftGutter = Math.max(0f, contentLeft);
        if (leftGutter >= r * 2.8f)
            return leftGutter * 0.5f;
        return contentLeft + r * 1.55f;
    }

    private float shotCenterX(float r) {
        float contentRight = contentLeft + contentWidth;
        float rightGutter = Math.max(0f, getWidth() - contentRight);
        if (rightGutter >= r * 2.3f)
            return contentRight + rightGutter * 0.5f;
        return contentRight - r * 1.35f;
    }

    private RectF menuRect() {
        return new RectF(getWidth() - 112f, 18f, getWidth() - 18f, 62f);
    }

    private float baseX(float x) {
        return (x - contentLeft) / Math.max(0.001f, contentScale);
    }

    private float baseY(float y) {
        return (y - contentTop) / Math.max(0.001f, contentScale);
    }

    private boolean insideBase(float x, float y) {
        return x >= contentLeft && x <= contentLeft + contentWidth
                && y >= contentTop && y <= contentTop + contentHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int index = event.getActionIndex();
        float x = event.getX(index);
        float y = event.getY(index);

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (handleTouchDown(x, y))
                return true;
        }

        if (action == MotionEvent.ACTION_MOVE && draggingCamera) {
            float mx = event.getX(0);
            float my = event.getY(0);
            targetCamX -= (mx - dragLastX) / Math.max(0.001f, contentScale);
            targetCamY -= (my - dragLastY) / Math.max(0.001f, contentScale);
            dragLastX = mx;
            dragLastY = my;
            clampTargetCamera();
            camX = targetCamX;
            camY = targetCamY;
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            heldDirection = -1;
            draggingCamera = false;
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            int dir = directionAt(x, y);
            if (dir == heldDirection)
                heldDirection = -1;
        }
        return true;
    }

    private boolean handleTouchDown(float x, float y) {
        if (screen == Screen.FINISHED) {
            sound.click();
            screen = Screen.TITLE;
            return true;
        }
        if (screen == Screen.TITLE || screen == Screen.HOW_TO || screen == Screen.CREDITS)
            return handleMenuTouch(x, y);
        if (screen != Screen.PLAYING)
            return false;

        if (menuRect().contains(x, y)) {
            sound.click();
            cancelMeter();
            flying = false;
            screen = Screen.TITLE;
            return true;
        }

        int direction = directionAt(x, y);
        if (direction >= 0) {
            if (heldDirection != direction && meterActive && powerLocked)
                addCombo(direction);
            heldDirection = direction;
            return true;
        }
        if (shotAt(x, y)) {
            pressShot();
            return true;
        }
        if (insideBase(x, y) && !flying && !meterActive) {
            draggingCamera = true;
            dragLastX = x;
            dragLastY = y;
            return true;
        }
        return false;
    }

    private boolean handleMenuTouch(float x, float y) {
        if (!insideBase(x, y))
            return false;
        float bx = baseX(x);
        float by = baseY(y);
        if (screen == Screen.TITLE) {
            if (bx >= 340 && bx <= 779 && by >= 260 && by <= 357) {
                sound.click();
                startGame();
                return true;
            }
            if (bx >= 355 && bx <= 725 && by >= 340 && by <= 420) {
                sound.click();
                screen = Screen.HOW_TO;
                return true;
            }
            if (bx >= 355 && bx <= 725 && by >= 410 && by <= 490) {
                sound.click();
                screen = Screen.CREDITS;
                return true;
            }
        } else if (bx >= 690 && bx <= 800 && by >= 0 && by <= 110) {
            sound.click();
            screen = Screen.TITLE;
            return true;
        }
        return false;
    }

    private int directionAt(float x, float y) {
        float r = controlRadius();
        float cx = dpadCenterX(r);
        float cy = dpadCenterY(r);
        float step = r * 0.92f;
        float br = r * 0.62f;
        if (distance(x, y, cx, cy - step) <= br)
            return 0;
        if (distance(x, y, cx + step, cy) <= br)
            return 1;
        if (distance(x, y, cx, cy + step) <= br)
            return 2;
        if (distance(x, y, cx - step, cy) <= br)
            return 3;
        return -1;
    }

    private boolean shotAt(float x, float y) {
        float r = controlRadius();
        return distance(x, y, shotCenterX(r), dpadCenterY(r)) <= r * 0.92f;
    }

    private static float distance(float ax, float ay, float bx, float by) {
        return (float) Math.hypot(ax - bx, ay - by);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int direction = directionForKey(keyCode);
        if (direction >= 0) {
            if (!keyDown[direction] && meterActive && powerLocked)
                addCombo(direction);
            keyDown[direction] = true;
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_SPACE
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_BUTTON_A)
                && event.getRepeatCount() == 0) {
            pressShot();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            handleBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int direction = directionForKey(keyCode);
        if (direction >= 0) {
            keyDown[direction] = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private static int directionForKey(int keyCode) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_W:
            return 0;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_D:
            return 1;
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_S:
            return 2;
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_A:
            return 3;
        default:
            return -1;
        }
    }

    boolean handleBack() {
        if (screen == Screen.TITLE)
            return false;
        sound.click();
        cancelMeter();
        flying = false;
        screen = Screen.TITLE;
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sound.release();
        if (players != null) {
            for (PlayerState player : players)
                player.recycle();
        }
        for (Course course : courses) {
            if (course != null)
                course.recycle();
        }
    }
}
