package Domain;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A time management class that counts down from a given initial time and triggers events when time updates or ends.
 */
public class GameTimer implements Serializable {
    private static final long serialVersionUID = 1L;
    private int timeRemaining;
    private Timer timer;
    private boolean isPaused;
    private Runnable onTimeUpdate;
    private Runnable onTimeEnd;

    /**
     * Constructs a GameTimer with a specified initial time.
     * @param initialTime the initial number of seconds for the timer
     */
    public GameTimer(int initialTime) {
        this.timeRemaining = initialTime;
        this.isPaused = false;
    }

    /**
     * Starts the timer, regularly decrementing the timeRemaining and invoking the specified callbacks.
     * @param onTimeUpdate a callback invoked when time decreases each second
     * @param onTimeEnd a callback invoked when the time runs out
     */
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

    /**
     * Computes the remaining time as a percentage of a total time value.
     * @param totalTime the total time for comparison
     * @return a percentage (0-100) of time left
     */
    public double getRemainingTimePercentage(int totalTime) {
        return (timeRemaining * 100.0) / totalTime;
    }

    /**
     * Pauses the timer so it no longer decrements.
     */
    public void pause() {
        isPaused = true;
        System.out.println("Timer paused at: " + timeRemaining);
    }

    /**
     * Resumes the timer, allowing it to continue decrementing each second.
     */
    public void resume() {
        isPaused = false;
        System.out.println("Timer resumed at: " + timeRemaining);
    }

    /**
     * Stops the timer completely, preventing further schedule tasks.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Provides the current number of seconds left on the timer.
     * @return the time remaining in seconds
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Adds more seconds to the timer.
     * @param seconds the number of seconds to add
     */
    public void addTime(int seconds) {
        timeRemaining += seconds;
    }

    /**
     * Checks if the timer is currently paused.
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }
}
