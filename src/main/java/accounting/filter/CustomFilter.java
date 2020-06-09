package accounting.filter;

import accounting.jwt.TokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerMapping;

import javax.naming.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomFilter extends GenericFilterBean {

    @Autowired
    TokenProvider tokenProvider;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("X-Token");
        String idFromPath = req.getServletPath().split("/")[4];
        Claims claimsToken = tokenProvider.decodeJWT(token);
        if (!req.getMethod().equals(RequestMethod.POST.toString())) {
            try {
                if (tokenProvider.validateToken(token) && idFromPath.equals(claimsToken.getId())) {
                    chain.doFilter(request, response);
                } else {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Token not a valid");
                }
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
