package com.lushan.security;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Security Test Suite")
@SelectClasses({
    AuthenticationTest.class,
    JwtUtilTest.class,
    SecurityUtilsTest.class
})
public class SecurityTestSuite {
    // Test suite configuration class
}