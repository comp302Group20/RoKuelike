package Domain;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer implements Serializable {
    private static final long serialVersionUID = 1L;
    private int timeRemaining; // in seconds
    private Timer timer;
    private boolean isPaused;

    public GameTimer(int initialTime) {
        this.timeRemaining = initialTime;
        this.isPaused = false;
    }

    public void start(Runnable onTimeUpdate, Runnable onTimeEnd) {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    timeRemaining--;
                    System.out.println("Timer tick: " + timeRemaining);  // Add this line
                    onTimeUpdate.run();

                    if (timeRemaining <= 0) {
                        timer.cancel();
                        onTimeEnd.run();
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
    }

    public void resume() {
        isPaused = false;
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void addTime(int seconds) {
        timeRemaining += seconds;
    }
}
