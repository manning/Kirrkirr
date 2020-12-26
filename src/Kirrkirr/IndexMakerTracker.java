package Kirrkirr;


/** IndexMakerTracker
 *  Interface defined to indicate methods which a class must implement if
 *  it wishes to track the progress of the IndexMaker's functionality.
 *  The IndexMaker procedures can take some time, so this might prove
 *  quite useful as a means to keep the user updated (and confident).
 *  <p>
 *  The structure is that an IndexMaker engages in several passes through
 *  the data, and each pass consists of many steps (e.g., words).
 */

public interface IndexMakerTracker {

    /** Called when IndexMaker is beginning a new stage (pass) over its
     * input files.  Gives a total number of steps for that stage.
     */
    void totalStepsForPass(int nSteps);

    /** Called when IndexMaker has completed some number of steps - the
     * number passed is the TOTAL number of steps done so far for this pass.
     */
    void stepsDone(int nStepsDone);
    

    /* Called when all work for the current pass is done.
     */
    void passDone();

    /** Dispose of this dialog if it has not already been done.
     *  It's safe to call this more than once.
     */
    void maybeDispose();

}

