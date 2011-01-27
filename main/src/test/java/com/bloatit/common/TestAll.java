package com.bloatit.common;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all
 * of the tests within its package as well as within any subpackages of its
 * package.
 *
 * @generatedBy CodePro at 27/01/11 17:05
 * @author tom
 * @version $Revision: 1.0 $
 */
public class TestAll {

    /**
     * Create a test suite that can run all of the test cases in this package
     * and all subpackages.
     *
     * @return the test suite that was created
     *
     * @generatedBy CodePro at 27/01/11 17:05
     */
    public static Test suite() {
        TestSuite suite;
    
        suite = new TestSuite("Tests in package com.bloatit.common");
        suite.addTestSuite(ImageTest.class);
        return suite;
    }
}
