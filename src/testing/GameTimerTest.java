package Domain;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

/**
 * Test class for GameTimer.
 * Demonstrates various test scenarios:
 * 1. Normal start and countdown.
 * 2. Pausing and resuming the timer.
 * 3. Adding extra time during countdown.
 */
public class GameTimerTest {

    private GameTimer timer;
    private boolean timeEnded;
    private int updatesCount;

    @Before
    public void setUp() {
        timer = new GameTimer(5);
        timeEnded = false;
        updatesCount = 0;
    }

    @After
    public void tearDown() {
        timer.stop();
    }

    /**
     * Tests that the timer counts down normally from 5 to 0,
     * calling the update callback each second and eventually calling onTimeEnd.
     */
    @Test
    public void testStartNormal() throws InterruptedException {
        Runnable onTimeUpdate = () -> updatesCount++;
        Runnable onTimeEnd = () -> timeEnded = true;
        timer.start(onTimeUpdate, onTimeEnd);
        Thread.sleep(6000);
        assertTrue("Timer should call onTimeUpdate around 5 times", updatesCount >= 5);
        assertTrue("Timer should end after counting down to 0", timeEnded);
    }

    /**
     * Tests that pausing the timer prevents it from decrementing,
     * and resuming lets it continue.
     */
    @Test
    public void testPauseResume() throws InterruptedException {
        Runnable onTimeUpdate = () -> updatesCount++;
        Runnable onTimeEnd = () -> timeEnded = true;

        timer.start(onTimeUpdate, onTimeEnd);
        timer.pause();
        Thread.sleep(2000);
        assertEquals("Updates should not increment during pause", 0, updatesCount);
        assertFalse("Timer should not end while paused", timeEnded);
        timer.resume();
        Thread.sleep(3000);
        assertTrue("Updates should occur after resume", updatesCount >= 3);
        assertFalse("Timer should not be ended yet after only 3 seconds resumed", timeEnded);
    }

    /**
     * Tests that adding time during the countdown actually extends the timer.
     * For example, if we add 5 seconds after 2 seconds have passed,
     * it should not end as early as the original time would suggest.
     */
    @Test
    public void testAddTime() throws InterruptedException {
        Runnable onTimeUpdate = () -> updatesCount++;
        Runnable onTimeEnd = () -> timeEnded = true;
        timer.start(onTimeUpdate, onTimeEnd);
        Thread.sleep(2000);
        timer.addTime(5);
        Thread.sleep(6000);
        assertTrue("Timer should likely have ended or be about to end after 8 total seconds", timeEnded || (timer.getTimeRemaining() <= 1));
        assertTrue("Updates should be at least 7 or 8", updatesCount >= 7);
    }
}
