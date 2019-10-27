package com.example.flappybird_prm391;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.Display;

import com.example.flappybird_prm391.model.Bird;
import com.example.flappybird_prm391.model.Ground;
import com.example.flappybird_prm391.model.Pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    /**
     * Game sound
     */
    private SoundController sound;

    /**
     * String number -> screen display number generator
     */
    NumberDisplayController numberDisplay;

    /**
     * Device display
     */
    private Display display;

    /**
     * Coordinate of screen bottom
     */
    private int screenHeight;
    /**
     * Coordinate of screen right
     */
    private int screenWidth;

    /**
     * Bird object
     */
    private Bird bird;

    /**
     * Pipe objects
     */
    private Pipe[] topPipe, bottomPipe;

    /**
     * Offset thats define how much pipes can move up and down
     */
    private int minPipeOffset, maxPipeOffset;

    /**
     * Random for calculate pipe move up down value and some random resources to be loaded
     */
    private Random random;

    /**
     * Game state
     */
    private boolean playing = false, gameover = false;

    /**
     * Score
     */
    private float SCORE_X;
    private float SCORE_Y;
    private int score = 0;
    private List<Bitmap> drawingScore;

    private int nextPipe = 0;

    /**
     * Ground object
     */
    private Ground ground;

    /**
     * Game setting !!!
     */
    // World gravity
    private final int GRAVITY = 3;
    // Careful when setting GAP value (must be < maxPipeOffset)
    private final int PIPE_GAP = 400;
    // Distance of 2 pipes, based on screen witdh
    private float PIPE_DISTANCE_SCREENWITDH_RELATIVE = 0.75f;
    // 3 pair of pipes is just fine
    private final int PIPE_NUMBER = 3;
    // Pipe moving speed
    private final int PIPE_VELOCITY = 6;

    /**
     * Pipe distance
     */
    private int pipe_distance;

    public GameEngine(Context context, Resources resources) {
        // Initialize sound
        sound = new SoundController(context);
        // Initialize display number generator
        numberDisplay = new NumberDisplayController(resources);
        drawingScore = numberDisplay.bigNum2Display(String.valueOf(score));
        // Initialize screen
        random = new Random();
        display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        SCORE_X = screenHeight * 0.1f;
        SCORE_Y = screenWidth * 0.15f;
        pipe_distance = Math.round(screenWidth * PIPE_DISTANCE_SCREENWITDH_RELATIVE);
        // Initialize pipe objects
        minPipeOffset = Math.round(screenHeight/4f);
        maxPipeOffset = Math.round(screenHeight/2.5f);
        // Resource images size might not compatible with users divice so we need to calculate scale for resizing
        float pipeScale = getPipeResizeScale(resources);
        topPipe = new Pipe[PIPE_NUMBER];
        bottomPipe = new Pipe[PIPE_NUMBER];
        for(int i = 0; i < PIPE_NUMBER; i++){
            topPipe[i] = new Pipe(resources, true, pipeScale);
            bottomPipe[i] = new Pipe(resources, false, pipeScale);
            topPipe[i].setX(screenWidth + i*pipe_distance);
            topPipe[i].setY(minPipeOffset + random.nextInt(maxPipeOffset - minPipeOffset + 1) - topPipe[i].getHeight());
            bottomPipe[i].setX(screenWidth + i*pipe_distance);
            bottomPipe[i].setY(topPipe[i].getY() + topPipe[i].getHeight() + PIPE_GAP);
        }
        // Initialize bird object
        // Resource images size might not compatible with users divice so we need to calculate scale for resizing
        float birdScale = getBirdResizeScale(resources);
        bird = new Bird(resources, birdScale);
        bird.setX(screenWidth/2 - bird.getWidth()/2);
        bird.setY(screenHeight/2 - bird.getHeight()/2);
        //Initialize ground object
        float groundScale = getGroundResizeScale(resources);
        ground = new Ground(resources, groundScale);
        ground.setY(screenHeight - ground.getHeight());
    }

    public void update(Canvas canvas){
        if(playing){
            // Calculate bird position
            bird.setVelocity(bird.getVelocity() - GRAVITY);
            bird.setY(bird.getY() - bird.getVelocity());
            // Calculate ground position
            ground.setX(ground.getX() - ground.getVelocity());
            if(ground.getX() < -(ground.getWidth()/2)){
                ground.setX(0);
            }
            // Calculate pipes position
            for(int i = 0; i < PIPE_NUMBER; i++) {
                topPipe[i].setX(topPipe[i].getX() - PIPE_VELOCITY);
                bottomPipe[i].setX(bottomPipe[i].getX() - PIPE_VELOCITY);
                // if pipe moved out the screen so put it back to started position
                // reset its Y coordinate too
                if(topPipe[i].getX() < -topPipe[i].getWidth()){
                    topPipe[i].setX(topPipe[i].getX() + (PIPE_NUMBER * pipe_distance));
                    bottomPipe[i].setX(bottomPipe[i].getX() + (PIPE_NUMBER * pipe_distance));
                    topPipe[i].setY(minPipeOffset + random.nextInt(maxPipeOffset - minPipeOffset + 1) - topPipe[i].getHeight());
                    bottomPipe[i].setY(topPipe[i].getY() + topPipe[i].getHeight() + PIPE_GAP);
                }
            }
            // Die if bird touch the ground or pipe
            if(bird.getY() > screenHeight - ground.getHeight() - bird.getHeight()
                    || (bird.getX() >= (bottomPipe[nextPipe].getX() - bird.getWidth())
                        && bird.getX() <= (bottomPipe[nextPipe].getX() + bottomPipe[0].getWidth() - bird.getWidth())
                        && (bird.getY() >= (bottomPipe[nextPipe].getY() - bird.getHeight()) || bird.getY() <= (topPipe[nextPipe].getY() + topPipe[0].getHeight())))) {
                playing = false;
                sound.playHit();
            }
            // Score counting
            if(bird.getX() > topPipe[nextPipe].getX() + bird.getWidth()/2){
                if(nextPipe == 2){
                    nextPipe = 0;
                } else {
                    nextPipe++;
                }
                score++;
                sound.playPoint();
            }
        }
        // Draw bird
        canvas.drawBitmap(bird.getFrame()[bird.getCurrentFrame()],
                bird.getX(),
                bird.getY(),
                null);
        // Draw pipes
        for(int i = 0; i < PIPE_NUMBER; i++){
            canvas.drawBitmap(topPipe[i].getFrame(), topPipe[i].getX(), topPipe[i].getY() , null);
            canvas.drawBitmap(bottomPipe[i].getFrame(), bottomPipe[i].getX(), bottomPipe[i].getY(), null);
        }
        // Draw ground
        canvas.drawBitmap(ground.getFrame(),
                ground.getX(),
                ground.getY(),
                null);
        // Draw score
        drawingScore = numberDisplay.bigNum2Display(String.valueOf(score));
        for(int i = 0; i < drawingScore.size(); i++){
            canvas.drawBitmap(drawingScore.get(i), SCORE_X, SCORE_Y + i*30 , null);
        }
    }

    /**
     * Calculate pipe resize scale number based on pipe height and screen height (pHeight = 4/5 sHeight)
     * @return scale
     */
    private float getPipeResizeScale(Resources resources){
        int rawPipeHeight = BitmapFactory.decodeResource(resources, R.drawable.pipe_green_bottom).getHeight();
        return (screenHeight/1.25f) / rawPipeHeight;
    }

    /**
     * Calculate bird resize scale number based on pipe height and screen height (bHeight = 1/20 sHeight)
     * @return scale
     */
    private float getBirdResizeScale(Resources resources){
        int rawBirdHeight = BitmapFactory.decodeResource(resources, R.drawable.yellowbird_midflap).getHeight();
        return (screenHeight/20.0f) / rawBirdHeight;
    }

    /**
     * Calculate bird resize scale number based on pipe height and screen height (gHeight = 1/5 sHeight)
     * @return scale
     */
    private float getGroundResizeScale(Resources resources){
        int rawGroundHeight = BitmapFactory.decodeResource(resources, R.drawable.base).getHeight();
        return (screenHeight/5.0f) / rawGroundHeight;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isGameover() {
        return gameover;
    }

    public void setGameover(boolean gameover) {
        this.gameover = gameover;
    }

    public Bird getBird() {
        return bird;
    }

    public void setBird(Bird bird) {
        this.bird = bird;
    }

    public SoundController getSound() {
        return sound;
    }
}
