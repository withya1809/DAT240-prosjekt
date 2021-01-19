package no.uis.tools;

/**
 * Simple structural class that executes some code every x milliseconds.
 * @author Alan Rostem
 */
public class TickExecution {
    private long fixedDeltaTime;
    private Runnable tickCallback;
    private Thread thread;

    /**
     * Class constructor
     * @param milliseconds long - Time between each execution
     * @param callback Runnable - Lambda expression containing the executable code
     * @author Alan Rostem
     */
    public TickExecution(long milliseconds, Runnable callback) {
        fixedDeltaTime = milliseconds;
        tickCallback = callback;
        thread = new Thread(() -> {
            try {
                while (true) {
                    callback.run();
                    Thread.sleep(fixedDeltaTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Starts the ticking.
     * @author Alan Rostem
     */
    public void execute() {
        thread.start();
    }

    /**
     * Stops the execution abruptly. This is not recommended since it depends on some deprecated Java Thread code.
     * @see java.lang.Thread
     * @author Alan Rostem
     */
    public void terminate() {
        thread.stop();
    }
}
