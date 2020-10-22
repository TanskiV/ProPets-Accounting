package accounting.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import accounting.model.UserAccount;

import java.util.Optional;

public interface UserAccountingRepository extends MongoRepository<UserAccount, String> {
Optional<UserAccount> findByEmail(String email);
boolean existsByEmail(String email);
}
