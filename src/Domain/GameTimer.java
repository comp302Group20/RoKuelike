package Domain;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer implements Serializable {
    private static final long serialVersionUID = 1L;
    private int timeRemaining; // in seconds
    private Timer timer;
    private boolean isPaused;
    private Runnable onTimeUpdate;
    private Runnable onTimeEnd;

    public GameTimer(int initialTime) {
        this.timeRemaining = initialTime;
        this.isPaused = false;
    }

    public void start(Runnable onTimeUpdate, Runnable onTimeEnd) {
        this.onTimeUpdate = onTimeUpdate;
        this.onTimeEnd = onTimeEnd;

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    if (timeRemaining > 0) {
                        timeRemaining--;
                        System.out.println("Timer tick: " + timeRemaining);

                        if (onTimeUpdate != null) {
                            onTimeUpdate.run();
                        }
                    }

                    if (timeRemaining <= 0) {
                        stop();
                        if (onTimeEnd != null) {
                            onTimeEnd.run();
                        }
                    }
                }
            }
        }, 0, 1000);
    }

    public double getRemainingTimePercentage(int totalTime) {
        return (timeRemaining * 100.0) / totalTime;
    }

    public void pause() {
        isPaused = true;
        System.out.println("Timer paused at: " + timeRemaining);
    }

    public void resume() {
        isPaused = false;
        System.out.println("Timer resumed at: " + timeRemaining);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void addTime(int seconds) {
        timeRemaining += seconds;
    }

    public boolean isPaused() {
        return isPaused;
    }
}