package accounting.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import accounting.model.UserAccount;

public interface UserAccountingRepository extends MongoRepository<UserAccount, String> {

}
