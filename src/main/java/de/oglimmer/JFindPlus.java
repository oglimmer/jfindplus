/**
 * Author: mail@oglimmer.de
 * License: LGPL (http://www.gnu.org/copyleft/lesser.html)
 */
package de.oglimmer;

import java.io.IOException;

import de.oglimmer.jfindplus.DuplicateFinder;
import de.oglimmer.jfindplus.FinderBase;
import de.oglimmer.jfindplus.JarSubset;
import de.oglimmer.jfindplus.ProgramArguments;
import de.oglimmer.jfindplus.SimpleFind;

public class JFindPlus {

    public static void main(String[] args) {
        try {
            ProgramArguments pa = new ProgramArguments(args);
            FinderBase finderBase = null;
            if (pa.isCommandDuplicate()) {
                finderBase = new DuplicateFinder(pa);
            } else if (pa.isCommandSubset()) {
                finderBase = new JarSubset(pa);
            } else if (pa.isCommendFind()) {
                finderBase = new SimpleFind(pa);
            }

            if (finderBase != null) {
                finderBase.processFind();
            } else {
                System.err.println("No valid command given!");
                System.exit(1);
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(2);
        }
    }
}
