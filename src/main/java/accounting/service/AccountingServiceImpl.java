package accounting.service;

import accounting.dao.UserAccountingRepository;
import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import accounting.exceptions.UserNotExistsException;
import accounting.jwt.TokenProvider;
import accounting.model.UserAccount;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.Set;


@Service
public class AccountingServiceImpl implements AccountingService {

    @Autowired
    UserAccountingRepository userAccountingRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Override
    public ResponseEntity<ProfileUserDto> register(NewUserDto newUserDto)  {
        String userData = null;
     //Check email valid
        if(!isEmail(newUserDto.getEmail())){
            throw new ResponseStatusException(HttpStatus.valueOf(400), "Bad email");
        }
        if (newUserDto.getPassword().length() != 0) {
            userData = newUserDto.getEmail() + ":" + newUserDto.getPassword();
        } else {
            throw new ResponseStatusException(HttpStatus.valueOf(400), "Request with password");
        }
        String token = Base64.encode(userData.getBytes());
        UserAccount account = new UserAccount("", newUserDto.getName(), newUserDto.getEmail().toLowerCase(),
                "", token, false, new HashSet<>(), new HashSet<>());
        if (userAccountingRepository.existsById(newUserDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.valueOf(400), "User exist");
        }
        userAccountingRepository.save(account);

        return ResponseEntity.ok(profileUserToProfileUserDto(account));

    }


    @Override
    public ResponseEntity<String> login(String basicToken) {
        String base64Credentials = basicToken.substring("Basic".length()).trim();
        String email = tokenProvider.getEmailFromBasicToken(basicToken);
        UserAccount account = userAccountingRepository.findById(email).orElseThrow(UserNotExistsException::new);
            if (!account.getBasicToken().equals(base64Credentials) ) {
                throw new ResponseStatusException(HttpStatus.valueOf(400), "Request with bad Authentication param");
            }
       ProfileUserDto profileUserDto = new ProfileUserDto(account.getAvatar(), account.getName(),
                account.getEmail(), account.getPhone(), account.isBlock(), account.getRoles());
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
        UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserNotExistsException::new);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(profileUserToProfileUserDto(user));
    }

    @Override
    public ResponseEntity<ProfileUserDto> editUser(String token, EditUserDto editUserDto, String login) {
      UserAccount user = checkAccess(token, login);
        if (user == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not owner and not administrator");
        }
        user.setAvatar(editUserDto.getAvatar());
        user.setName(editUserDto.getName());
        user.setPhone(editUserDto.getPhone());
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(profileUserToProfileUserDto(user));
    }

    private UserAccount checkAccess(String token, String login) {
        UserAccount check = null;
        String userId = tokenProvider.decodeJWT(token).getId();
        UserAccount user = userAccountingRepository.findById(userId.toLowerCase()).orElseThrow(UserNotExistsException::new);
        if (user.getRoles().contains("SUPER_USER")|| userId.equals(login)){
            check = user;
        }
        return check;
    }

    @Override
    public ResponseEntity<ProfileUserDto> removeUser(String xToken, String login) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        userAccountingRepository.delete(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(profileUserToProfileUserDto(user));
    }

    @Override
    public ResponseEntity<Set<String>> addRoles(String xToken, String login, String role) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.addRole(role.toUpperCase());
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", xToken);
        return ResponseEntity.ok().headers(header).body(user.getRoles());

    }

    @Override
    public ResponseEntity<Set<String>> removeRoles(String xToken, String login, String role) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.removeRole(role.toUpperCase());
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(user.getRoles());
    }

    @Override
    public ResponseEntity<BlockDto> blockAccount(String xToken, String login, boolean status) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.setBlock(status);
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", xToken);
        BlockDto blockDto = new BlockDto().builder()
                .login(user.getEmail())
                .block(user.isBlock()).build();
        return ResponseEntity.ok().headers(header).body(blockDto);
    }

    @Override
    public ResponseEntity<Set<String>> addFavorite(String xToken, String login, String idFavorite) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.addFavorite(idFavorite);
        Set<String> favorites = user.getFavorites();
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(favorites);
    }

    @Override
    public ResponseEntity<Set<String>> removeFavorite(String xToken, String login, String id) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.removeFavorite(id);
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(user.getFavorites());
    }

    @Override
    public ResponseEntity<Set<String>> getFavorite(String xToken, String login) {
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(user.getFavorites());
    }

    @Override
    public ResponseEntity<String> updateToken(String xToken) {
        if (tokenProvider.validateToken(xToken)) {
            Claims userClaims = tokenProvider.decodeJWT(xToken);
            UserAccount user = userAccountingRepository.findById(userClaims.getId()).orElseThrow(UserNotExistsException::new);
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

    private boolean isEmail(String email) {
        boolean result = true;
        try {
            InternetAddress   emailAdr = new InternetAddress(email);
            emailAdr.validate();
        } catch (AddressException e) {
            result = false;
        }
        return result;
    }

}
