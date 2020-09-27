package accounting.service;

import accounting.dao.UserAccountingRepository;
import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import accounting.exceptions.BadJWTTokenException;
import accounting.exceptions.ForbiddenAccessException;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class AccountingServiceImpl implements AccountingService {

    @Autowired
    UserAccountingRepository userAccountingRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Override
    public ResponseEntity<ProfileUserDto> register(NewUserDto newUserDto)  {
        String userData;
        if (userAccountingRepository.existsById(newUserDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.valueOf(400), "User exist");
        }
     //Check email valid
        try{
       isEmail(newUserDto.getEmail());
        if (newUserDto.getPassword().length() != 0 && newUserDto.getName().length() != 0) {
            String email = newUserDto.getEmail().toLowerCase();
            userData = email + ":" + newUserDto.getPassword();
        } else {
            /*check if password or user name null*/
            String messageError = newUserDto.getPassword().length() == 0 ? "field 'password:' without data"
                    :  "field 'name:' without data ";
            throw new ResponseStatusException(HttpStatus.valueOf(400), messageError);
        }}catch (NullPointerException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Null pointer exception");
        }
        String token = Base64.encode(userData.getBytes());
        UserAccount account = new UserAccount("", newUserDto.getName(), newUserDto.getEmail().toLowerCase(),
                "", token, false, new HashSet<>(), new HashSet<>());
        account.getRoles().add("ROLE_USER");
        userAccountingRepository.save(account);

        return ResponseEntity.ok(profileUserToProfileUserDto(account));

    }


    @Override
    public ResponseEntity<String> login(String basicToken) {
        String[] tempDataFromToken = tokenProvider.getEmailAndPasswordFromBasicToken(basicToken);
        isEmail(tempDataFromToken[0]);
        UserAccount user = userAccountingRepository.findById(tempDataFromToken[0].toLowerCase()).orElseThrow(UserNotExistsException::new);
        String[] currentDataFromToken = tokenProvider.getEmailAndPasswordFromBasicToken("basic"+user.getBasicToken());
            if (!tempDataFromToken[1].equals(currentDataFromToken[1])) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "bad password");
            }
       ProfileUserDto profileUserDto = new ProfileUserDto(user.getAvatar(), user.getName(),
                user.getEmail(), user.getPhone(), user.isBlock(), user.getRoles());
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
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(profileUserToProfileUserDto(user));
    }

    @Override
    public ResponseEntity<ProfileUserDto> editUser(String token, EditUserDto editUserDto, String login) {
      UserAccount user = checkAccess(token, login);
        if (user == null){
            throw new ForbiddenAccessException();
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

    @Override
    public ResponseEntity<ProfileUserDto> removeUser(String xToken, String login) {
        if (checkAccess(xToken, login) == null){
            throw new ForbiddenAccessException();
        }
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        userAccountingRepository.delete(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(profileUserToProfileUserDto(user));
    }

    @Override
    public ResponseEntity<Set<String>> addRoles(String xToken, String login, String role) {
        isJWTAdmin(xToken);
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.addRole(role.toUpperCase());
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(user.getRoles());

    }

    @Override
    public ResponseEntity<Set<String>> removeRoles(String xToken, String login, String role) {
        isJWTAdmin(xToken);
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        if (!user.getRoles().contains(role)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User don't have role "+ role);
        }
        user.removeRole(role.toUpperCase());
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        return ResponseEntity.ok().headers(header).body(user.getRoles());
    }

    @Override
    public ResponseEntity<BlockDto> blockAccount(String xToken, String login, boolean status) {
        isJWTAdmin(xToken);
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        user.setBlock(status);
        userAccountingRepository.save(user);
        HttpHeaders header = new HttpHeaders();
        String newToken = tokenProvider.createJWT(user.getEmail(), user.getRoles());
        header.set("X-Token", newToken);
        BlockDto blockDto = BlockDto.builder()
                .login(user.getEmail())
                .block(user.isBlock()).build();
        return ResponseEntity.ok().headers(header).body(blockDto);
    }

    @Override
    public ResponseEntity<Set<String>> addFavorite(String xToken, String login, String idFavorite) {
        if (checkAccess(xToken, login) == null){
            throw new ForbiddenAccessException();
        }
        if( !userAccountingRepository.existsById(idFavorite.toLowerCase()) ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Favorite user with id "+idFavorite
            +" not found");
        }
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
        if (checkAccess(xToken, login) == null){
            throw new ForbiddenAccessException();
        }
        UserAccount user = userAccountingRepository.findById(login.toLowerCase()).orElseThrow(UserNotExistsException::new);
        if (!user.getFavorites().contains(id)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User " + login + " don't have in favorite " + id);
        }
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

    @Override
    public ResponseEntity<List<ProfileUserDto>> deleteAllUsers(String XToken) {
        isJWTAdmin(XToken);
        List <UserAccount> userAccounts = userAccountingRepository.findAll();
        List<UserAccount> usersToDell = userAccounts.stream()
                .filter(user -> !user.getRoles().contains("SUPER_USER"))
                .collect(Collectors.toList());
        usersToDell.forEach(user -> userAccountingRepository.deleteById(user.getEmail()));
        List<ProfileUserDto> dellUsersToResponse = usersToDell.stream()
                .map(user -> profileUserToProfileUserDto(user))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dellUsersToResponse);

    }

    private ProfileUserDto profileUserToProfileUserDto(UserAccount account) {
        return ProfileUserDto.builder()
                .email(account.getEmail()).avatar(account.getAvatar()).block(account.isBlock())
                .name(account.getName()).phone(account.getPhone()).roles(account.getRoles()).build();
    }

    private void isEmail(String email) {
        try {
            InternetAddress   emailAdr = new InternetAddress(email);
            emailAdr.validate();
        } catch (AddressException e) {
            throw new ResponseStatusException(HttpStatus.valueOf(400), "Bad email");
        }
        int start = email.indexOf("@");
        String subEmail = email.substring(start);
        if (!subEmail.contains(".")){
            throw new ResponseStatusException(HttpStatus.valueOf(400), "Bad email");
        }
    }

    private UserAccount checkAccess(String XToken, String login) {
        UserAccount check = null;
        String userId;
        try {
             userId = tokenProvider.decodeJWT(XToken).getId();
        }catch (Exception e){
            throw new BadJWTTokenException();
        }
        UserAccount user = userAccountingRepository.findById(userId.toLowerCase()).orElseThrow(UserNotExistsException::new);
        if (user.getRoles().contains("SUPER_USER")|| userId.equals(login.toLowerCase())){
            check = user;
        }
        return check;
    }

    private void isJWTAdmin(String xToken) {
        String jsonRoles = tokenProvider.decodeJWT(xToken).getAudience();
        Gson gson = new Gson();
        List<String> roles = gson.fromJson(jsonRoles, List.class);
        if (!roles.contains("SUPER_USER")){
            throw new ForbiddenAccessException();
        }
    }

}
