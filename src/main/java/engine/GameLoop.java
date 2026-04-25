package engine;

/**
 * GameLoop manages the core 60FPS thread timer.
 * It is responsible for driving the GameManager logic and triggering UI repaints.
 */
public class GameLoop implements Runnable {
    
    private Thread loopThread;
    private boolean isRunning = false;
    private GameManager gameManager;
    
    // Target 60 Frames Per Second
    private final double TARGET_FPS = 60.0;
    private final double TIME_PER_UPDATE = 1000000000 / TARGET_FPS; // In nanoseconds
    
    public GameLoop(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    /**
     * Starts the game loop on a new thread.
     */
    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;
        loopThread = new Thread(this, "GameLoopThread");
        loopThread.start();
    }
    
    /**
     * Stops the game loop safely.
     */
    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;
        try {
            loopThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        long previousTime = System.nanoTime();
        double accumulator = 0.0;
        
        while (isRunning) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - previousTime;
            previousTime = currentTime;
            accumulator += elapsedTime / TIME_PER_UPDATE;
            
            // Update game logic at consistent 60FPS intervals
            while (accumulator >= 1.0) {
                update();
                accumulator--;
            }
            
            // NOTE FOR JAZ/INTEGRATION:
            // This is where you would call gameStage.repaint() to update the graphics.
            // gameManager.requestRepaint(); 
            
            // Sleep slightly to prevent maxing out the CPU unnecessarily 
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * The core logic cycle called 60 times a second.
     */
    private void update() {
        if (gameManager != null) {
            gameManager.tick(); // Advance game state by one frame
        }
    }
}