package de.pateweb.officething.workinghours.rest.ui;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.pateweb.officething.workinghours.dao.UserRepository;
import de.pateweb.officething.workinghours.model.User;
import de.pateweb.officething.workinghours.rest.ui.dto.UiUser;

//@CrossOrigin(origins = "*")
@CrossOrigin()
@RestController
@RequestMapping(path = "/ui/workinghours")
public class UiUserController {

	private static final Logger LOG = LoggerFactory.getLogger(UiUserController.class);

	private final String UNAUTHORIZED = "Unauthorized";
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@PostMapping("/change")
	public ResponseEntity<?> changePassword(@RequestBody UiUser uiUser)
	{
		//TODO check if current user is allowed to change password
		
		User user = userRepository.findFirstByEmail(uiUser.getEmail()).get();
		
		String newDecodedPassword = Base64.getDecoder().decode(uiUser.getPassword()).toString();
		uiUser.setPassword(passwordEncoder.encode(newDecodedPassword));
		
		userRepository.save(user);
		
		return new ResponseEntity<>("Password changed", HttpStatus.OK);
		
	}

}
