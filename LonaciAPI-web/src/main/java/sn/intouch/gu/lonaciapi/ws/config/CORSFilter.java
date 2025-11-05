package sn.intouch.gu.lonaciapi.ws.config;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.EJBRegistry;
import sn.intouch.gu.lonaciapi.ejb.jndiutils.JNDIUtils;
import sn.intouch.gu.lonaciapi.ejb.parameter.entities.Parameter;
import sn.intouch.gu.lonaciapi.ejb.parameter.services.ParameterService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Log4j2
public class CORSFilter implements Filter {
    private static final String URL_CORS_ORIGIN = "URL_CORS_ORIGIN";
    private static String lonaciUrlCorsOrigin= null;
    private static String getCors() {
        if (lonaciUrlCorsOrigin == null) {
            ParameterService parameterService = (ParameterService) JNDIUtils.lookUpEJB(EJBRegistry.ParameterServiceBean);
            Parameter boBusinessClientIdParam = parameterService.getParameterByCode(URL_CORS_ORIGIN);
            if (boBusinessClientIdParam == null)
                throw new APIException("Cannot find parameter of code " + URL_CORS_ORIGIN);
            lonaciUrlCorsOrigin = boBusinessClientIdParam.getPrmStringValue();
        }
        return lonaciUrlCorsOrigin;
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        HttpServletResponse resp = (HttpServletResponse) servletResponse;
//        String origin = request.getHeader("Origin");
//        List<String> allowedOrigins = Arrays.asList(
//                "https://dev.gutouch.net/bomonitoring/*",
//                "https://monitoring-lonaci.gutouch.net/*"
//        );
//        if (origin != null && allowedOrigins.contains(origin)) {
//            resp.addHeader("Access-Control-Allow-Origin", origin);
//        }
        log.error("function getCors()"+getCors());
        resp.addHeader("Access-Control-Allow-Origin", getCors());
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
