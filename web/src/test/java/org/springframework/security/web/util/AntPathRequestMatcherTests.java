/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.security.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Luke Taylor
 * @author Rob Winch
 */
public class AntPathRequestMatcherTests {

    @Test
    public void singleWildcardMatchesAnyPath() {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/**");
        assertEquals("/**", matcher.getPattern());

        assertTrue(matcher.matches(createRequest("/blah")));

        matcher = new AntPathRequestMatcher("**");
        assertTrue(matcher.matches(createRequest("/blah")));
        assertTrue(matcher.matches(createRequest("")));
    }

    @Test
    public void trailingWildcardMatchesCorrectly() {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/blah/blAh/**");
        assertTrue(matcher.matches(createRequest("/BLAH/blah")));
        assertFalse(matcher.matches(createRequest("/blah/bleh")));
        assertTrue(matcher.matches(createRequest("/blah/blah/")));
        assertTrue(matcher.matches(createRequest("/blah/blah/xxx")));
        assertFalse(matcher.matches(createRequest("/blah/blaha")));
        assertFalse(matcher.matches(createRequest("/blah/bleh/")));
        MockHttpServletRequest request = createRequest("/blah/");

        request.setPathInfo("blah/bleh");
        assertTrue(matcher.matches(request));

        matcher = new AntPathRequestMatcher("/bl?h/blAh/**");
        assertTrue(matcher.matches(createRequest("/BLAH/Blah/aaa/")));
        assertTrue(matcher.matches(createRequest("/bleh/Blah")));

        matcher = new AntPathRequestMatcher("/blAh/**/blah/**");
        assertTrue(matcher.matches(createRequest("/blah/blah")));
        assertFalse(matcher.matches(createRequest("/blah/bleh")));
        assertTrue(matcher.matches(createRequest("/blah/aaa/blah/bbb")));
    }

    @Test
    public void requestHasNullMethodMatches() {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/something/*", "GET");
        MockHttpServletRequest request = createRequest("/something/here");
        request.setMethod(null);
        assertTrue(matcher.matches(request));
    }

    // SEC-2084
    @Test
    public void requestHasNullMethodNoMatch() {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/something/*", "GET");
        MockHttpServletRequest request = createRequest("/nomatch");
        request.setMethod(null);
        assertFalse(matcher.matches(request));
    }

    @Test
    public void requestHasNullMethodAndNullMatcherMatches() {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/something/*");
        MockHttpServletRequest request = createRequest("/something/here");
        request.setMethod(null);
        assertTrue(matcher.matches(request));
    }

    @Test
    public void requestHasNullMethodAndNullMatcherNoMatch() {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/something/*");
        MockHttpServletRequest request = createRequest("/nomatch");
        request.setMethod(null);
        assertFalse(matcher.matches(request));
    }

    @Test
    public void exactMatchOnlyMatchesIdenticalPath() throws Exception {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/login.html");
        assertTrue(matcher.matches(createRequest("/login.html")));
        assertFalse(matcher.matches(createRequest("/login.html/")));
        assertFalse(matcher.matches(createRequest("/login.html/blah")));
    }

    @Test
    public void httpMethodSpecificMatchOnlyMatchesRequestsWithCorrectMethod() throws Exception {
        AntPathRequestMatcher matcher = new AntPathRequestMatcher("/blah", "GET");
        MockHttpServletRequest request = createRequest("/blah");
        request.setMethod("GET");
        assertTrue(matcher.matches(request));
        request.setMethod("POST");
        assertFalse(matcher.matches(request));
    }

    @Test
    public void equalsBehavesCorrectly() throws Exception {
        // Both universal wildcard options should be equal
        assertEquals(new AntPathRequestMatcher("/**"), new AntPathRequestMatcher("**"));
        assertEquals(new AntPathRequestMatcher("/xyz"), new AntPathRequestMatcher("/xyz"));
        assertEquals(new AntPathRequestMatcher("/xyz", "POST"), new AntPathRequestMatcher("/xyz", "POST"));
        assertFalse(new AntPathRequestMatcher("/xyz", "POST").equals(new AntPathRequestMatcher("/xyz", "GET")));
        assertFalse(new AntPathRequestMatcher("/xyz").equals(new AntPathRequestMatcher("/xxx")));
        assertFalse(new AntPathRequestMatcher("/xyz").equals(new AnyRequestMatcher()));
    }

    @Test
    public void toStringIsOk() throws Exception {
        new AntPathRequestMatcher("/blah").toString();
        new AntPathRequestMatcher("/blah", "GET").toString();
    }

    private MockHttpServletRequest createRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("doesntMatter");
        request.setServletPath(path);
        request.setMethod("POST");

        return request;
    }
}
