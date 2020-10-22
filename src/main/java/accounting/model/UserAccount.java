package accounting.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"email"})
@Document(collection = "ProPetsUsers")

public class UserAccount {
    @Id
    String id;
    String avatar;
    String name;
    String email;
    String phone;
    String basicToken;
    String facebookUrl;
    boolean block;
    Set<String> roles;
    Set<String> favorites;

    public void addRole(String role) {
        roles.add(role);
    }

    public void removeRole(String role) {
		roles.remove(role);
    }

    public void addFavorite(String favorite){
        favorites.add(favorite);
    }

    public void removeFavorite(String favorite) {
        favorites.remove(favorite);
    }

}
