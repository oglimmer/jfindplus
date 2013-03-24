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
public class DuplicateFinder extends ClassFinderBase {

    public DuplicateFinder(ProgramArguments params) {
	super(params);
    }

    @Override
    protected int getOutputMinOccurrenceNumber() {
	return 2;
    }

}
