package accounting.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class NewUserDto {
	String name;
	String email;
	String password;
}
