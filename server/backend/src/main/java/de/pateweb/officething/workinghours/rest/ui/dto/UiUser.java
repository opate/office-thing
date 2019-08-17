package de.pateweb.officething.workinghours.rest.ui.dto;

import de.pateweb.officething.workinghours.UserRole;
import lombok.Data;

@Data
public class UiUser {
	
	private UserRole role;
	
	private Long customerId;
	
	private String email;
	
	private String name;
	
	private String given_name;

}