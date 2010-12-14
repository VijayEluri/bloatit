package com.bloatit.framework;

import junit.framework.TestCase;

import com.bloatit.model.data.util.SessionManager;

public class FrameworkTestUnit extends TestCase {
    protected AuthToken yoAuthToken;
    protected AuthToken tomAuthToken;
    protected AuthToken fredAuthToken;
    protected TestDB db;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SessionManager.reCreateSessionFactory();
        db = new TestDB();
        SessionManager.beginWorkUnit();
        yoAuthToken = new AuthToken("Yo", "plop");
        tomAuthToken = new AuthToken("Thomas", "password");
        fredAuthToken = new AuthToken("Fred", "other");
        SessionManager.endWorkUnitAndFlush();
        SessionManager.beginWorkUnit();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (SessionManager.getSessionFactory().getCurrentSession().getTransaction().isActive()) {
            SessionManager.endWorkUnitAndFlush();
        }
        SessionManager.getSessionFactory().close();
    }
}