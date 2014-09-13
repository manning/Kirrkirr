/** The FunListener interface provides methods needed by the Fun graphical
 *  interface which are given to it by the providing GraphPanel interface.
 *
 *  @author Christopher Manning
 */
package Kirrkirr.ui;

public interface FunListener {

    public abstract void funGetLinks(String uniqueKey);
    public abstract void funFindWord(String uniqueKey);
    public abstract void funSetRandom();

}

