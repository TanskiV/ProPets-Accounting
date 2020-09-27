package accounting.service;

import accounting.dao.UserAccountingRepository;
import accounting.dto.NewUserDto;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountingServiceImplTest {
    private MockMvc mvc;
    public  NewUserDto newUserDto = NewUserDto.builder()
            .email("test@test.com")
            .name("JUnit Test")
            .password("test")
            .build();

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Autowired
    private AccountingServiceImpl accountingService = new AccountingServiceImpl() ;
@Autowired
    private UserAccountingRepository userAccountingRepository ;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register() {
        userAccountingRepository.deleteById(newUserDto.getEmail());
        accountingService.register(newUserDto);
        boolean check = userAccountingRepository.existsById(newUserDto.getEmail());
        assertEquals(check, true);
        String errorMessage = "400 BAD_REQUEST";
        try{
         accountingService.register(newUserDto);
        }catch (ResponseStatusException e){
           assertTrue(e.getMessage().contains(errorMessage));
        }
        String email = newUserDto.getEmail().replace(".", "");
        newUserDto.setEmail(email);
        try {
            accountingService.register(newUserDto);
        }catch (ResponseStatusException e){
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains(errorMessage));
        }
    }

//    @Test
//    void login() {
//    }
//
//    @Test
//    void userInfo() {
//    }
//
//    @Test
//    void editUser() {
//    }
//
//    @Test
//    void removeUser() {
//    }
//
//    @Test
//    void addRoles() {
//    }
//
//    @Test
//    void removeRoles() {
//    }
//
//    @Test
//    void blockAccount() {
//    }
//
//    @Test
//    void addFavorite() {
//    }
//
//    @Test
//    void removeFavorite() {
//    }
//
//    @Test
//    void getFavorite() {
//    }
//
//    @Test
//    void updateToken() {
//    }
//
//    @Test
//    void deleteAllUsers() {
//    }
}