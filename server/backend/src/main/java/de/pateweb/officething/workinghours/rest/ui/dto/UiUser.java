package de.pateweb.officething.workinghours.rest.ui.dto;

import java.util.List;

import de.pateweb.officething.workinghours.UserRole;
import lombok.Data;

@Data
public class UiUser {
	
	private String role;
		
	private List<String> rights;
	
	private Long customerId;
	
	private Long userId;
	
	private String email;
	
	private String name;
	
	private String givenName;
	
	private String password;

}