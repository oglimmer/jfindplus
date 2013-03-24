/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

/**
 * 
 * @author oli
 * 
 */
public class Statistics {

    private long startTime;

    private int container;

    public Statistics() {
	this.startTime = System.currentTimeMillis();
    }

    public long getTime() {
	return System.currentTimeMillis() - startTime;
    }

    public int getContainer() {
	return container;
    }

    public void addContainer() {
	this.container++;
    }

    public void print(FinderBase parent) {
	System.out.println("Processing time:" + getTime() + " millis");
	System.out.println("Total class files found "
		+ parent.getNumberClasses());
	System.out.println("Total processed class files found "
		+ parent.getNumberProcessedClasses());
	System.out.println("Total containers searched " + getContainer());
    }

}
