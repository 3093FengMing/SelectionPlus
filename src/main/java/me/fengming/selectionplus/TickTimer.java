package me.fengming.selectionplus;

public class TickTimer {
    public int tick;
    private boolean running;
    public boolean reverse;

    public TickTimer() {
        this.tick = 0;
        this.running = false;
    }

    public void add() {
        if (running) {
            if (reverse) {
                tick--;
            } else tick++;
        }
    }

    public void start() {
        tick = 0;
        running = true;
    }

    public void stop() {
        running = false;
        tick = 0;
    }

    public void reset() {
        tick = 0;
    }

}
