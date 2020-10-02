package accounting.dto;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProfileUserDto {
	String avatar;
	String name;
	String email;
	String phone;
	String facebookUrl;
	boolean block;
	Set<String> roles;
}
