package com.example.flappybird_prm391;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView_SurfaceView extends SurfaceView implements Runnable {

//    GameThread gameThread;
    SurfaceHolder holder; // Current screen holder
    GameEngine gameEngine; // The game itself
    Thread thread; // Game running thread
    boolean isRunning = true; // Flag to detect whether the thread is running or not
    long startTime, loopTime; // Loop start time and loop duration
    private final int SCREEN_DELAY_MILIS = 20; // Screen refresh rate count in milisecond


    public GameView_SurfaceView(Context context) {
        super(context);
        this.holder = getHolder();
        // Add this as surfaceHolder callback object.
//        holder.addCallback(this);
        setWillNotDraw(false);
        setFocusable(true);
        Random random = new Random();
        setBackgroundResource(random.nextBoolean() ? R.drawable.background_day : R.drawable.background_night);
        gameEngine = new GameEngine(context, context.getResources());
//        gameThread = new GameThread(context, context.getResources(), holder);
    }

//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        invalidate();
//        if(!gameThread.isRunning()){
////            gameThread = new GameThread(holder);
//            gameThread.start();
//        }else{
//            gameThread.start();
//        }
//    }

//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        gameEngine.update(canvas);
    }

//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        if(gameThread.isRunning()){
//            gameThread.setIsRunning(false);
//            boolean retry = true;
//            while(retry){
//                try{
//                    gameThread.join();
//                    retry = false;
//                } catch(InterruptedException e) {
//
//                }
//            }
//        }
//    }

    int count = 0;

    @Override
    public void run() {
        // Looping until the boolean is false

        while(isRunning){
            count++;
            if(!holder.getSurface().isValid()){
                continue;
            }
            startTime = SystemClock.uptimeMillis();
            //locking the canvas (not working right now), using invalidate to redraw instead
            invalidate();
//            Canvas canvas;
//            if (holder.getSurface().isValid()) {
//                canvas = holder.lockCanvas();
//                if (canvas != null) {
//                    synchronized (holder){
//                        gameEngine.drawScreen(canvas);
//                        holder.unlockCanvasAndPost(canvas);
//                    }
//                }
//            }
            //loop time
            loopTime = SystemClock.uptimeMillis() - startTime;
            // Pausing here to make sure we update the right amount per second
            if(loopTime < SCREEN_DELAY_MILIS){
                try{
                    Thread.sleep(SCREEN_DELAY_MILIS - loopTime);
                }catch(InterruptedException e){
                    Log.e("Interrupted","Interrupted while sleeping");
                }
            }
        }
    }

    public void pause(){
        isRunning = false;
        while(true){
            try{
                thread.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            break;
        }
        thread = null;
    }

    public void resume(){
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        // Catch on tap screen
        if(action == MotionEvent.ACTION_DOWN){
            // Keeps the bird not flying out the screen
//            System.out.println("Screen pressed, bird:" + gameThread.getGameEngine().getBird().getX() + " - " + gameThread.getGameEngine().getBird().getY());
            if(gameEngine.getBird().getY() > -gameEngine.getBird().getHeight()) {
                gameEngine.getBird().setVelocity(35);
            }
            if(!gameEngine.isPlaying()){
                gameEngine.setPlaying(true);
                gameEngine.getSound().playSwoosh();
            }
        }
        return true;
    }
}