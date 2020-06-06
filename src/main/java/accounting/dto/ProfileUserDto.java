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
	boolean block;
	Set<String> roles;
	
	public String print() {
		return name +" "+ avatar+ " "+ email+ " " +phone+ " " +block+" " +roles;
	}
}
