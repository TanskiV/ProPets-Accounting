package accounting.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NewUserDto {
	String name;
	String email;
	String password;
}
