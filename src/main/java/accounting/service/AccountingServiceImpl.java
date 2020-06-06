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
import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;


@Service
public class AccountingServiceImpl implements AccountingService {

    @Autowired
    UserAccountingRepository userAccountingRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Override
    public ProfileUserDto register(NewUserDto newUserDto)  {
        String data = "";
        if (newUserDto.getPassword() != null && newUserDto.getEmail() != null){
           data = newUserDto.getEmail() + ":" + newUserDto.getPassword();
        }else {
            throw  new TokenAuthenticationException("Bad json");
        }
        System.out.println(data);
        String token = Base64.encode(data.getBytes());
        UserAccount account = new UserAccount("", newUserDto.getName(), newUserDto.getEmail(),
                "", token, false, new HashSet<>(), new HashSet<>());
        if (userAccountingRepository.existsById(newUserDto.getEmail())) {
            throw new UserExistsException();
        }
        userAccountingRepository.save(account);

        return profileUserToProfileUserDto(account);

    }

    private ProfileUserDto profileUserToProfileUserDto(UserAccount account) {
        return ProfileUserDto.builder()
                .email(account.getEmail()).avatar(account.getAvatar()).block(account.isBlock())
                .name(account.getName()).phone(account.getPhone()).roles(account.getRoles()).build();

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
    public ProfileUserDto userInfo(String xToken, String login) {
        ProfileUserDto profileUserDto = new ProfileUserDto();
        try {
            if (tokenProvider.validateToken(xToken)) {
                UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
                profileUserDto = profileUserToProfileUserDto(user);
            }
        } catch (TokenAuthenticationException e) {
            throw new TokenAuthenticationException(e.getMessage());
        }
        return profileUserDto;
    }

    @Override
    public ProfileUserDto editUser(String token, EditUserDto editUserDto, String login) {
        ProfileUserDto profileUserDto = new ProfileUserDto();
        try {
            if (tokenProvider.validateToken(token)) {
                UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
                user.setAvatar(editUserDto.getAvatar());
                user.setName(editUserDto.getName());
                user.setPhone(editUserDto.getPhone());
                userAccountingRepository.save(user);
                profileUserDto = profileUserToProfileUserDto(user);
            }
        } catch (TokenAuthenticationException e) {
            throw new TokenAuthenticationException(e.getMessage());
        }

        return profileUserDto;
    }

    @Override
    public ProfileUserDto removeUser(String xToken, String login) {
        ProfileUserDto profileUserDto = new ProfileUserDto();
        try {
            if (tokenProvider.validateToken(xToken)) {
                UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
                profileUserDto = profileUserToProfileUserDto(user);
                userAccountingRepository.delete(user);
            }
        } catch (TokenAuthenticationException e) {
            throw new TokenAuthenticationException(e.getMessage());
        }
        return profileUserDto;
    }

    @Override
    public Set<String> addRoles(String xToken, String login, String role) {
        Set<String> roles = new HashSet<>();
        try {
            if (tokenProvider.validateToken(xToken)){
              UserAccount account = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
              account.addRole(role);
             roles = account.getRoles();
             userAccountingRepository.save(account);
            }
        } catch (TokenAuthenticationException e) {
            throw new TokenAuthenticationException(e.getMessage());
        }
        return roles;
    }

    @Override
    public Set<String> removeRoles(String xToken, String login, String role) {
        Set<String> roles = new HashSet<>();
       try{
           if (tokenProvider.validateToken(xToken)){
               UserAccount user = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
               user.removeRole(role);
               roles = user.getRoles();
               userAccountingRepository.save(user);
           }
       }catch (TokenAuthenticationException e){
           throw new TokenAuthenticationException(e.getMessage());
       }
        return roles;
    }

    @Override
    public BlockDto blockAccount(String xToken, String login, boolean status) {
        BlockDto blockDto = new BlockDto();
        try {
            if (tokenProvider.validateToken(xToken)){
                UserAccount userAccount = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
                userAccount.setBlock(status);
                blockDto.setBlock(userAccount.isBlock());
                blockDto.setLogin(userAccount.getEmail());
                userAccountingRepository.save(userAccount);
            }
        }catch (TokenAuthenticationException e){
            throw new TokenAuthenticationException(e.getMessage());
        }

        return blockDto;
    }

    @Override
    public Set<String> addFavorite(String xToken, String login, String idFavorite) {
        Set<String> favoriteDto = new HashSet<>();
        try {
            if (tokenProvider.validateToken(xToken)){
                UserAccount account = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
                account.addFavorite(idFavorite);
                favoriteDto = account.getFavorites();
                userAccountingRepository.save(account);
            }
        }catch (TokenAuthenticationException e){
            throw new TokenAuthenticationException(e.getMessage());
        }
        return favoriteDto;
    }

    @Override
    public Set<String> removeFavorite(String xToken, String login, String id) {
        Set<String> favorite = new HashSet<>();
        try{
            if (tokenProvider.validateToken(xToken)){
                UserAccount userAccount = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
                System.out.println(userAccount);
                userAccount.removeFavorite(id);
                favorite = userAccount.getFavorites();
                userAccountingRepository.save(userAccount);
            }
        }catch (TokenAuthenticationException e){
            throw new TokenAuthenticationException(e.getMessage());
        }
        return favorite;
    }

    @Override
    public Set<String> getFavorite(String xToken, String login) {
        Set<String> favorites = new HashSet<>();
        try{
           if (tokenProvider.validateToken(xToken)){
               UserAccount userAccount = userAccountingRepository.findById(login).orElseThrow(UserExistsException::new);
               favorites = userAccount.getFavorites();
           }
        }catch (TokenAuthenticationException e){
            throw new TokenAuthenticationException(e.getMessage());
        }
        return favorites;
    }

    @Override
    public boolean tokenValidator(String xToken) {
        try {
            return tokenProvider.validateToken(xToken);
        }catch (Exception e){
            return false;
        }

    }

}
