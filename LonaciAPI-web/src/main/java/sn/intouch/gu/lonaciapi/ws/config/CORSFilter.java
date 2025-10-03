package sn.intouch.gu.lonaciapi.ws.config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        String origin = request.getHeader("Origin");
        List<String> allowedOrigins = Arrays.asList(
                "https://dev.gutouch.net/bomonitoring/*",
                "https://monitoring-lonaci.gutouch.net/*"
        );
        if (origin != null && allowedOrigins.contains(origin)) {
            resp.addHeader("Access-Control-Allow-Origin", origin);
        }
        //resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "*");
        resp.addHeader("Access-Control-Allow-Headers", "*");
        resp.addHeader("Strict-Transport-Security", "max-age=12960000; includeSubdomains");
        // Just ACCEPT and REPLY OK if OPTIONS
        if (request.getMethod().equals("OPTIONS")) {
            System.out.println("IN CORS FILTER");
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        chain.doFilter(request, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
