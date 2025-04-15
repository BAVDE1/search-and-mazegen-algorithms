package common;

public abstract class Runner {
    public int opNum = 0;
    public int frameNum = 0;
    public int opFrameNum = 0;

    public boolean useFPO = true;
    public int framesPerOp = 1;
    public int opPerFrames = 1;

    private boolean running = false;
    private boolean paused = false;
    private boolean complete = false;

    public void start() {
        running = true;
    }

    public void pause() {
        paused = true;
    }

    public void reset() {
        opNum = 0;
        frameNum = 0;
        opFrameNum = 0;

        running = false;
        paused = false;
        complete = false;
    }

    public void nextFrame() {
        if (complete || !running || paused) return;
        frameNum++;
        opFrameNum++;

        // instant complete
        if (framesPerOp == 0 || opPerFrames == 0) {
            while (!complete) performOperation();
        }

        // frames between operations
        if (useFPO && opFrameNum == framesPerOp) performOperation();

        // multiple operations per frame
        if (!useFPO) {
            int i = 0;
            while (i < opPerFrames && !complete) {
                i++;
                performOperation();
            }
        }
    }

    public void performOperation() {
        opNum++;
        opFrameNum = 0;
    };
}
