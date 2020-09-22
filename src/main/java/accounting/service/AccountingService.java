package accounting.service;

import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import org.springframework.http.ResponseEntity;

import java.util.Set;

public interface AccountingService {
    ResponseEntity<ProfileUserDto> register(NewUserDto newUserDto);

    ResponseEntity<String> login(String basicToken);

    ResponseEntity<ProfileUserDto> userInfo(String xToken, String login) ;

    ResponseEntity<ProfileUserDto> editUser(String token, EditUserDto editUserDto, String login);

    ResponseEntity<ProfileUserDto> removeUser(String xToken, String login);

    ResponseEntity<Set<String>> addRoles(String xToken, String login, String role);

    ResponseEntity<Set<String>> removeRoles(String xToken, String login, String role);

    ResponseEntity<BlockDto> blockAccount(String xToken, String login, boolean status);

    ResponseEntity<Set<String>> addFavorite(String xToken, String login, String id);

    ResponseEntity<Set<String>> removeFavorite(String xToken, String login, String id);

    ResponseEntity<Set<String>> getFavorite(String xToken, String login);

    ResponseEntity<String> updateToken(String xToken);

}
