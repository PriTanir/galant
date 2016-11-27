/**
 * The purpose of this class is to set up and manage synchronization of
 * display and algorithm from the point of view of the "main program",
 * treating the algorithm execution as a worker thread. It is responsible for
 * firing up the algorithm and keeping track of the relationship between the
 * display state and the algorithm state. It is also responsible for
 * signaling the algorithm to terminate and then terminating the thread.
 * @see edu.ncsu.csc.Galant.algorithm.AlgorithmSynchronizer for the worker
 * thread end of the synchronization.
 */

package edu.ncsu.csc.Galant.algorithm;

import java.lang.Thread;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.GraphDispatch;
import edu.ncsu.csc.Galant.gui.window.GraphWindow;

public class AlgorithmExecutor {

    /**
     * the amount of time to wait between polling the algorithm, in
     * milliseconds
     */
    final int WAIT_TIME = 10;

    /**
     * amount of time to wait before concluding that the algorithm is in an
     * infinite loop
     */
    final int BUSY_WAIT_TIME_LIMIT = 5000;

    private Algorithm algorithm;
    private AlgorithmSynchronizer synchronizer;
    /**
     * Needs to be public so that it can be interrupted (not clear that it
     * helps)
     */
    public Thread algorithmThread;
    private int algorithmState;
    private int displayState;

    /**
     * true if an error or infinite loop occurred during execution
     */
    public boolean infiniteLoop = false;

    /**
     * true if a GalantException is thrown during algorithm execution
     */
    public boolean exceptionThrown = false;

    /**
     * Makes a note of the algorithm and its synchronizer and creates a
     * thread to run the algorithm
     */
    public AlgorithmExecutor(Algorithm algorithm,
                             AlgorithmSynchronizer synchronizer) {
        this.algorithm = algorithm;
        this.synchronizer = synchronizer;
        this.algorithmThread = new Thread(algorithm);
		algorithmThread.setName("Execution thread");
        this.infiniteLoop = false;
        this.exceptionThrown = false;
    }

    /**
     * Starts the algorithm thread and causes it to execute the first step.
     * @todo not clear if we want the first step to execute
     */
    public void startAlgorithm() {
        algorithmState = displayState = 0;
        algorithmThread.start();
        incrementDisplayState();
    }

    /**
     * Informs the algorithm that it should terminate and then terminates the
     * thread.
     */
    public synchronized void stopAlgorithm() {
        System.out.println("-> stopAlgorithm");
        GraphDispatch dispatch = GraphDispatch.getInstance();
        dispatch.setAnimationMode(false);
        synchronized ( synchronizer ) {
            synchronizer.stop();
            notifyAll();
        }
        System.out.println("algorithm thread notified");
        try {
            if ( ! infiniteLoop && ! synchronizer.exceptionThrown() )
                algorithmThread.join();
            System.out.println("beyond joining algorithm thread");
        }
        catch (InterruptedException e) {
            System.out.println("Synchronization problem in stopAlgorithm()");
        }
        algorithmState = displayState = 0;
        System.out.println("<- stopAlgorithm");
    }


    /**
     * Needed so that graph elements can record their modifications based on
     * current algorithm state.
     */
    public int getAlgorithmState() { return algorithmState; }

    /**
     * Needed for code that relies on knowing what the current display is showing
     */
    public int getDisplayState() { return displayState; }

    /**
     * Called whenever user interaction requests a step forward; the
     * algorithm is then responsible for calling incrementAlgorithmState() to
     * put the algorithm in sync with the display; this is done in
     * pauseExecution() in the AlgorithmSynchronizer.
     *
     * @todo needs to check if there's a query window open before releasing
     * control to the algorithm via synchronizer.notify(); ideally should
     * also display something on the status bar or throw an exception; the
     * detection part is complicated, however
     */
    public synchronized void incrementDisplayState() {
        System.out.println("-> incrementDisplayState, displayState = "
                           + displayState
                           + ", algorithmState = " + algorithmState);
        GraphDispatch dispatch = GraphDispatch.getInstance();
        if ( displayState == algorithmState
             && ! synchronizer.algorithmFinished()
             && ! synchronizer.stopped()
             && ! synchronizer.exceptionThrown() ) {
            displayState++;
            algorithmState++;

            // wake up the algorithmThread, have it do something
            synchronized ( synchronizer ) {
                synchronizer.notify();
            }
            int timeInBusyWait = 0;
            do {
                try {
                    Thread.sleep(WAIT_TIME);
                    timeInBusyWait += WAIT_TIME;
                    System.out.println("timeInBusyWait = " + timeInBusyWait);
                } catch (InterruptedException e) {
                    System.out.printf("Error occured while trying to wait");
                    e.printStackTrace(System.out);
                }
            } while ( ! synchronizer.stepFinished()
                      && ! synchronizer.stopped()
                      //                      && ! algorithmThread.interrupted()
                      && ! synchronizer.exceptionThrown()
                      && ! exceptionThrown
                      && timeInBusyWait < BUSY_WAIT_TIME_LIMIT );
            System.out.println(" in incrementDisplayState"
                               + ", stepFinished = "
                               + synchronizer.stepFinished()
                               + ", stopped = " + synchronizer.stopped()
                               + ", infiniteLoop = " + infiniteLoop
                               + ", exceptionThrown = " + exceptionThrown
                               + ", synchronizer.exceptionThrown = "
                               + synchronizer.exceptionThrown() );
            if ( timeInBusyWait >= BUSY_WAIT_TIME_LIMIT ) {
                System.out.println("busy wait time limit exceeded");
                infiniteLoop = true;
            }
        }
        else if ( displayState < algorithmState ) {
            displayState++;
        }
        System.out.println(" in incrementDisplayState, infiniteLoop = "
                           + infiniteLoop
                           + ", exceptionThrown = " + exceptionThrown
                           + ", synchronizer.exceptionThrown = "
                           + synchronizer.exceptionThrown() );
        if ( infiniteLoop || synchronizer.exceptionThrown() ) {
            // need to let window know that algorithm was terminated due
            // to unusual circumstances so that appropriate message will
            // appear on the status bar
            dispatch.getGraphWindow().performDone();
        }
        System.out.println("<- incrementDisplayState, displayState = "
                           + displayState
                           + ", algorithmState = " + algorithmState);
    }

    /**
     * Called when user requests a step back
     */
    public void decrementDisplayState() {
        if ( displayState >= 0 ) displayState--;
    }

    /**
     * True if it's possible to step forward
     */
    public boolean hasNextState() {
        if ( algorithmState > displayState ) return true;
		if ( ! synchronizer.algorithmFinished() ) return true;
        return false;
	}

    /**
     * true if it's possible to step back
     * @todo not clear whether the lower bound is 1 or 0
     */
    public boolean hasPreviousState() {
        return (displayState > 1);
    }

    public void printStates() {
        System.out.printf("displayState = %d, algorithmState = %d\n",
                          displayState, algorithmState);
    }
}

//  [Last modified: 2016 11 27 at 20:52:27 GMT]
