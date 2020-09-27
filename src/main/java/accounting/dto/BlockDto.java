package accounting.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto {
	String login;
	boolean block;
}
