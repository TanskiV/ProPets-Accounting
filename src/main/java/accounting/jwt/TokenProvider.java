package accounting.jwt;


import accounting.exeptions.TokenAuthenticationException;
import com.google.gson.Gson;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Set;


@Component
public class TokenProvider {
    @Value("${jwt.token.secret}")
    public String SECRET_KEY;
    @Value("${jwt.token.expired}")
    public String expiration;


    public String createJWT(String email, Set<String> roles) {
        String jsonRoles = new Gson().toJson(roles);
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder().setId(email)
                .setIssuedAt(now)
                .setAudience(jsonRoles)
                .signWith(signatureAlgorithm, signingKey);

        long expMillis = nowMillis + Long.parseLong(expiration);
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);

        return builder.compact();
    }

    public Claims decodeJWT(String jwt) {
        Claims claims = null;
        try {
             claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
                    .parseClaimsJws(jwt).getBody();  
        }catch (Exception e){
            
        }

        return claims;
    }


    public boolean validateToken(String token) throws TokenAuthenticationException {
        System.out.println(token);
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date(System.currentTimeMillis()))) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }

    }

    public String getEmailFromBasicToken(String basicToken) {
        String email = "";
        if (basicToken != null && basicToken.toLowerCase().startsWith("basic")) {
            String base64Credentials = basicToken.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            email = values[0];
            return email;
        } else return null;

    }

}
