package accounting.controller;

import accounting.dao.UserAccountingRepository;
import accounting.dto.BlockDto;
import accounting.dto.EditUserDto;
import accounting.dto.NewUserDto;
import accounting.dto.ProfileUserDto;
import accounting.model.UserAccount;
import accounting.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/account")
public class UserAccountingController {
    @Autowired
    AccountingService accountingService;

    @PostMapping("/{lang}/v1")
    public ResponseEntity<ProfileUserDto> register(@RequestBody NewUserDto newUserDto) {
        return accountingService.register(newUserDto);
    }

    @PostMapping("/{lang}/v1/login")
    public ResponseEntity<String> login(@RequestHeader("Authorization") String token) {
        return accountingService.login(token);

    }

    @GetMapping("/{lang}/v1/info")
    public ResponseEntity<ProfileUserDto> userInfo(@RequestHeader("X-Token") String xToken,
                                                   @RequestParam("login") String email) {
        return accountingService.userInfo(xToken, email);
    }

    @PutMapping("/{lang}/v1/")
    public ResponseEntity<ProfileUserDto> editUser(@RequestHeader("X-Token") String xToken,
                                                   @RequestBody EditUserDto editUserDto,
                                                   @RequestParam("userId") String login) {
        return accountingService.editUser(xToken, editUserDto, login);
    }

    @DeleteMapping("/{lang}/v1/")
    public ResponseEntity<ProfileUserDto> removeUser(@RequestHeader("X-Token") String xToken,
                                                     @RequestParam("login") String login) {
        return accountingService.removeUser(xToken, login);
    }

    @PutMapping("/{lang}/v1/role/")
    public ResponseEntity<Set<String>> addUserRole(@RequestHeader("X-Token") String xToken,
                                                   @RequestParam("user") String login,
                                                   @RequestParam("role") String role) {
        return accountingService.addRoles(xToken, login, role);
    }

    @DeleteMapping("/{lang}/v1/role/")
    public ResponseEntity<Set<String>> removeRole(@RequestHeader("X-Token") String xToken,
                                                  @RequestParam("user") String login,
                                                  @RequestParam("role") String role) {
        return accountingService.removeRoles(xToken, login, role);
    }

    @PutMapping("/{lang}/v1/block/")
    public ResponseEntity<BlockDto> blockUser(@RequestHeader("X-Token") String xToken,
                                              @RequestParam("login") String login,
                                              @RequestParam("block") boolean status) {
        return accountingService.blockAccount(xToken, login, status);
    }

    @PutMapping("/{lang}/v1/favorite/")
    public ResponseEntity<Set<String>> addFavorite(@RequestHeader("X-Token") String xToken,
                                                   @RequestParam("user") String login,
                                                   @RequestParam("favoriteId") String id) {
        return accountingService.addFavorite(xToken, login, id);
    }

    @DeleteMapping("/{lang}/v1/favorite/")
    public ResponseEntity<Set<String>> removeFavorite(@RequestHeader("X-Token") String xToken,
                                                      @RequestParam("user") String login,
                                                      @RequestParam("favoriteId") String id) {
        return accountingService.removeFavorite(xToken, login, id);
    }

    @GetMapping("/{lang}/v1/favorites")
    public ResponseEntity<Set<String>> getFavorite(@RequestHeader("X-Token") String xToken,
                                                   @RequestParam("userId") String login) {
        return accountingService.getFavorite(xToken, login);
    }


    @PutMapping("/{lang}/v1/validation/update")
    public ResponseEntity<String> updateToken(@RequestHeader("X-Token") String xToken) {
        return accountingService.updateToken(xToken);
    }

    @DeleteMapping("/admin/deleteUsers")
    public ResponseEntity<List<ProfileUserDto>> deleteUsers(@RequestHeader("X-Token") String XToken){
        return accountingService.deleteAllUsers(XToken);
    }

}
