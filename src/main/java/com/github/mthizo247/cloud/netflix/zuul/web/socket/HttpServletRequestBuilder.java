package com.github.mthizo247.cloud.netflix.zuul.web.socket;

import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

final class HttpServletRequestBuilder {

    private HttpServletRequestBuilder() {
    }
    
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "This operation is not supported";

    static HttpServletRequest request(InetSocketAddress remoteAddress, HttpHeaders httpHeaders) {
        final HttpHeaders localHttpHeaders = httpHeaders;
        return new HttpServletRequest() {
            @Override
            public String getAuthType() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Cookie[] getCookies() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public long getDateHeader(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getHeader(String headerName) {
                List<String> headersList = localHttpHeaders.get(headerName);
                if (!CollectionUtils.isEmpty(headersList)) {
                    return StringUtils.join(headersList, ",");
                } else {
                    return null;
                }
            }

            @Override
            public Enumeration<String> getHeaders(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public int getIntHeader(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getMethod() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getPathInfo() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getPathTranslated() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getContextPath() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getQueryString() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getRemoteUser() {
                return null;
            }

            @Override
            public boolean isUserInRole(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Principal getUserPrincipal() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getRequestedSessionId() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getRequestURI() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public StringBuffer getRequestURL() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getServletPath() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public HttpSession getSession(boolean b) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public HttpSession getSession() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String changeSessionId() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean authenticate(HttpServletResponse httpServletResponse) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public void login(String s, String s1) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public void logout() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Collection<Part> getParts() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Part getPart(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Object getAttribute(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getCharacterEncoding() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public void setCharacterEncoding(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public int getContentLength() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public long getContentLengthLong() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getContentType() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public ServletInputStream getInputStream() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getParameter(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String[] getParameterValues(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getProtocol() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getScheme() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getServerName() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public int getServerPort() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public BufferedReader getReader() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getRemoteAddr() {
                return remoteAddress.getHostName();
            }

            @Override
            public String getRemoteHost() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public void setAttribute(String s, Object o) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public void removeAttribute(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Locale getLocale() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public Enumeration<Locale> getLocales() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isSecure() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getRealPath(String s) {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public int getRemotePort() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getLocalName() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public String getLocalAddr() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public int getLocalPort() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public ServletContext getServletContext() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public AsyncContext startAsync() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isAsyncStarted() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public boolean isAsyncSupported() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public AsyncContext getAsyncContext() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }

            @Override
            public DispatcherType getDispatcherType() {
                throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
            }
        };
    }
}
