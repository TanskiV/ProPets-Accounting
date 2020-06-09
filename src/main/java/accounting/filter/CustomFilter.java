package accounting.filter;

import accounting.exeptions.TokenAuthenticationException;
import accounting.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.GenericFilterBean;

import javax.naming.AuthenticationException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomFilter extends GenericFilterBean {

    @Autowired
    TokenProvider tokenProvider;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader("X-Token");
        if (!req.getMethod().equals(RequestMethod.POST.toString())) {
            try {
                if (tokenProvider.validateToken(token)) {
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
