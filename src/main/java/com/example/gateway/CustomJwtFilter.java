package com.example.gateway;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CustomJwtFilter extends ZuulFilter {

    private static final List<String> OPEN_PATH_PREFIXES = Arrays.asList("/internal");
    private static final List<String> OPEN_PATHS = Arrays.asList("/");

    private final String jwtSecret;
    private final String headerName;

    public CustomJwtFilter(
            @Value("${security.jwt.secret:changeitchangeitchangeitchangeitchangeitchangeit}") String jwtSecret,
            @Value("${security.jwt.header:Authorization}") String headerName) {
        this.jwtSecret = jwtSecret;
        this.headerName = headerName;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String requestUri = request.getRequestURI();
        return OPEN_PATH_PREFIXES.stream().noneMatch(requestUri::startsWith)
                && OPEN_PATHS.stream().noneMatch(requestUri::equals);
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        String tokenHeader = request.getHeader(headerName);
        if (!StringUtils.hasText(tokenHeader)) {
            rejectRequest(context, HttpServletResponse.SC_UNAUTHORIZED, "Missing JWT");
            return null;
        }

        String token = tokenHeader;
        if (tokenHeader.toLowerCase().startsWith("bearer ")) {
            token = tokenHeader.substring(7);
        }

        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token);
        } catch (IllegalArgumentException | JwtException ex) {
            rejectRequest(context, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
        }

        return null;
    }

    private void rejectRequest(RequestContext context, int statusCode, String message) {
        context.setSendZuulResponse(false);
        context.setResponseStatusCode(statusCode);
        context.getResponse().setContentType("application/json");
        context.setResponseBody(String.format("{\"error\":\"%s\"}", message));
    }
}

