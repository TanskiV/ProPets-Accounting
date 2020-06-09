package accounting.service;

import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;
import java.util.Set;

public interface AccountingService {
	ProfileUserDto register(NewUserDto newUserDto) ;

	ResponseEntity<String> login(String basicToken) ;

	ProfileUserDto userInfo(String xToken, String login);

	ProfileUserDto editUser(String token,EditUserDto editUserDto, String login);

	ProfileUserDto removeUser(String xToken, String login);

	Set<String> addRoles(String xToken, String login, String role);

	Set<String> removeRoles(String xToken, String login, String role);

	BlockDto blockAccount(String xToken, String login, boolean status);

	Set<String> addFavorite(String xToken, String login, String id);

	Set<String> removeFavorite(String xToken, String login, String id);

	Set<String> getFavorite(String xToken, String login);

	boolean tokenValidator( String xToken) throws AuthenticationException;

	ResponseEntity<String>  updateToken(String xToken);

}
