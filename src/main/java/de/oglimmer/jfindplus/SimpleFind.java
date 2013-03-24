/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer.jfindplus;

public class SimpleFind extends ClassFinderBase {

    public SimpleFind(ProgramArguments params) {
	super(params);
    }

    @Override
    protected int getOutputMinOccurrenceNumber() {
	return 1;
    }

}
