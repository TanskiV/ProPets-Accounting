package accounting.controller;

import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import accounting.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Set;

@RestController
@RequestMapping("/account")
public class UserAccountingController {
	@Autowired
	AccountingService accountingService;

	@PostMapping("/{lang}/v1")
	public ProfileUserDto register(@RequestBody NewUserDto newUserDto)  {
	return accountingService.register(newUserDto);
	}

	@PostMapping("/{lang}/v1/login")
	public ResponseEntity<String> login(@RequestHeader("Authorization") String token) {
		return accountingService.login(token);

	}

	@GetMapping("/{lang}/v1/{login}/info")
	public ResponseEntity<ProfileUserDto> userInfo(@RequestHeader("X-Token") String xToken, @PathVariable("login")String email) {
		return accountingService.userInfo(xToken, email);
	}

	@PutMapping("/{lang}/v1/{login}")
	public ResponseEntity<ProfileUserDto> editUser(@RequestHeader("X-Token") String xToken, @RequestBody EditUserDto editUserDto, @PathVariable("login")String login) {
		return accountingService.editUser(xToken, editUserDto, login);
	}

	@DeleteMapping("/{lang}/v1/{login}")
	public ResponseEntity<ProfileUserDto> removeUser(@RequestHeader("X-Token") String xToken , @PathVariable("login")String login) {
		return accountingService.removeUser(xToken, login);
	}

	@PutMapping("/{lang}/v1/{login}/role/{role}")
	public ResponseEntity<Set<String>> addUserRole(@RequestHeader("X-Token") String xToken, @PathVariable("login")String login, @PathVariable("role")String role) {
		return accountingService.addRoles(xToken, login, role);
	}

	@DeleteMapping("/{lang}/v1/{login}/role/{role}")
	public ResponseEntity <Set<String>> removeRole(@RequestHeader("X-Token") String xToken, @PathVariable("login")String login, @PathVariable("role")String role) {
		return accountingService.removeRoles(xToken, login, role);
	}

	@PutMapping("/{lang}/v1/{login}/block/{status}")
	public ResponseEntity<BlockDto> blockUser(@RequestHeader("X-Token") String xToken , @PathVariable("login")String login, @PathVariable("status")boolean status) {
		return accountingService.blockAccount(xToken, login , status);
	}

	@PutMapping("/{lang}/v1/{login}/favorite/{id}")
	public ResponseEntity<Set<String>> addFavorite(@RequestHeader("X-Token") String xToken, @PathVariable("login")String login, @PathVariable("id")String id) {
		return accountingService.addFavorite(xToken, login , id);
	}

	@DeleteMapping("/{lang}/v1/{login}/favorite/{id}")
	public ResponseEntity<Set<String>> removeFavorite(@RequestHeader("X-Token") String xToken , @PathVariable("login")String login, @PathVariable("id")String id) {
		return accountingService.removeFavorite(xToken, login , id);
	}

	@GetMapping("/{lang}/v1/{login}/favorites")
	public ResponseEntity<Set<String>> getFavorite(@RequestHeader("X-Token") String xToken, @PathVariable("login")String login) {
		return accountingService.getFavorite(xToken, login);
	}


	@PutMapping("/{lang}/v1/validation/update")
	public ResponseEntity<String>  updateToken(@RequestHeader("X-Token") String xToken){
		return accountingService.updateToken(xToken);
	}

}
