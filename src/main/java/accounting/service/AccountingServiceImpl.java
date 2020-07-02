package accounting.service;

import accounting.dao.UserAccountingRepository;
import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import accounting.exeptions.TokenAuthenticationException;
import accounting.exeptions.UserExistsException;
import accounting.jwt.TokenProvider;
import accounting.model.UserAccount;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.apache.catalina.connector.Response;
import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AccountingServiceImpl implements AccountingService {

    @Autowired
    UserAccountingRepository userAccountingRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Override
    public ProfileUserDto register(NewUserDto newUserDto) {
        String data = "";
        if (newUserDto.getPassword() != null && newUserDto.getEmail() != null) {
            data = newUserDto.getEmail() + ":" + newUserDto.getPassword();
        } else {
            throw new TokenAuthenticationException("Bad json");
        }
        String token = Base64.encode(data.getBytes());
        UserAccount account = new UserAccount("", newUserDto.getName(), newUserDto.getEmail(),
                "", token, false, new HashSet<>(), new HashSet<>());
        if (userAccountingRepository.existsById(newUserDto.getEmail())) {
            throw new UserExistsException();
        }
        userAccountingRepository.save(account);

        return profileUserToProfileUserDto(account);

    }

    @Override
    public ResponseEntity<String> login(String basicToken) {
        String base64Credentials = basicToken.substring("Basic".length()).trim();
        String email = tokenProvider.getEmailFromBasicToken(basicToken);
        ProfileUserDto profileUserDto = new ProfileUserDto();
        if (email != null) {
            if (!userAccountingRepository.existsById(email)) {
                throw new UserExistsException();
            }
            UserAccount account = userAccountingRepository.findById(email).orElse(null);
            if (account.getBasicToken().equals(base64Credentials)) {
                profileUserDto = new ProfileUserDto(account.getAvatar(), account.getName(),
                        account.getEmail(), account.getPhone(), account.isBlock(), account.getRoles());
            } else {
                throw new TokenAuthenticationException("token not valid");
            }
        }
        Gson json = new Gson();
        String jsonResponse = json.toJson(profileUserDto);
        String token = tokenProvider.createJWT(profileUserDto.getEmail(), profileUserDto.getRoles());
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Token", token);
        return ResponseEntity.ok()
                .headers(headers)
                .body(jsonResponse);

    }

    @Override
    public ResponseEntity<ProfileUserDto> userInfo(String xToken, String login) {
        UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        ResponseEntity<ProfileUserDto> response = ResponseEntity.ok(profileUserToProfileUserDto(user));
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<ProfileUserDto> editUser(String token, EditUserDto editUserDto, String login) {
        UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        user.setAvatar(editUserDto.getAvatar());
        user.setName(editUserDto.getName());
        user.setPhone(editUserDto.getPhone());
        userAccountingRepository.save(user);
        ResponseEntity<ProfileUserDto> response = ResponseEntity.ok(profileUserToProfileUserDto(user));
        response.getHeaders().set("X-Token", token);
        return response;
    }

    @Override
    public ResponseEntity<ProfileUserDto> removeUser(String xToken, String login) {
        UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        userAccountingRepository.delete(user);
        ResponseEntity<ProfileUserDto> response = ResponseEntity.ok(profileUserToProfileUserDto(user));
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<Set<String>> addRoles(String xToken, String login, String role) {
        UserAccount account = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        account.addRole(role);
        userAccountingRepository.save(account);
        ResponseEntity<Set<String>> response = ResponseEntity.ok(account.getRoles());
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<Set<String>> removeRoles(String xToken, String login, String role) {
        UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        user.removeRole(role);
        userAccountingRepository.save(user);
        ResponseEntity<Set<String>> response = ResponseEntity.ok(user.getRoles());
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<BlockDto> blockAccount(String xToken, String login, boolean status) {
        UserAccount userAccount = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        userAccount.setBlock(status);
        userAccountingRepository.save(userAccount);
    ResponseEntity<BlockDto> response = ResponseEntity.ok(new BlockDto().builder()
                                                            .login(userAccount.getEmail())
                                                            .block(userAccount.isBlock()).build());
    response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<Set<String>> addFavorite(String xToken, String login, String idFavorite) {
        UserAccount account = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        account.addFavorite(idFavorite);
        Set<String> favorites = account.getFavorites();
        System.out.println(favorites.size());
        userAccountingRepository.save(account);
        ResponseEntity<Set<String>> response = ResponseEntity.ok(favorites);
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<Set<String>> removeFavorite(String xToken, String login, String id) {
        UserAccount userAccount = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        userAccount.removeFavorite(id);
        userAccountingRepository.save(userAccount);
        ResponseEntity<Set<String>> response = ResponseEntity.ok(userAccount.getFavorites());
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public ResponseEntity<Set<String>> getFavorite(String xToken, String login) {
        UserAccount userAccount = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
        ResponseEntity<Set<String>> response = ResponseEntity.ok(userAccount.getFavorites());
        response.getHeaders().set("X-Token", xToken);
        return response;
    }

    @Override
    public boolean tokenValidator(String xToken) {
        return tokenProvider.validateToken(xToken);
    }

    @Override
    public ResponseEntity<String> updateToken(String xToken) {
        if (tokenValidator(xToken)) {
            Claims userClaims = tokenProvider.decodeJWT(xToken);
            UserAccount user = userAccountingRepository.findById(userClaims.getId()).orElseThrow(UserExistsException::new);
            Set<String> roles = user.getRoles();
            String token = tokenProvider.createJWT(userClaims.getId(), roles);
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Token", token);
            headers.set("Id", user.getEmail());
            headers.set("Valid", Boolean.toString(tokenProvider.validateToken(xToken)));
            return ResponseEntity.ok()
                    .headers(headers)
                    .build();
        } else return ResponseEntity.notFound().eTag("Token not a valid").build();

    }

    private ProfileUserDto profileUserToProfileUserDto(UserAccount account) {
        return ProfileUserDto.builder()
                .email(account.getEmail()).avatar(account.getAvatar()).block(account.isBlock())
                .name(account.getName()).phone(account.getPhone()).roles(account.getRoles()).build();

    }

}
