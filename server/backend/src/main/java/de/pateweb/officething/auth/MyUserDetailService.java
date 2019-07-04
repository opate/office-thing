package de.pateweb.officething.auth;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.pateweb.officething.workinghours.dao.UserRepository;
import de.pateweb.officething.workinghours.model.User;

/**
 *
 * @author Octavian Pate
 */
@Service
public class MyUserDetailService implements UserDetailsService {

	private static final Logger LOG = LoggerFactory.getLogger(MyUserDetailService.class);

	String rootUserName;

	private String rootUserPassword;

	// properties set in Other Sources/src/main/resources/application.properties
	@Value("${general.rootusername}")
	public void setRootUserName(String rootName) {
		rootUserName = rootName;
	}

	@Value("${general.rootuserpassword}")
	public void setRootUserPassword(String rootPassword) {
		rootUserPassword = rootPassword;
	}

	@Autowired
	UserRepository userRepository;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(11);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		if (username.equals(rootUserName)) {
			User rootUser = new User();
			rootUser.setEmail(rootUserName);
			rootUser.setPassword(passwordEncoder().encode(rootUserPassword));
			rootUser.setDeactivated(false);
			rootUser.setName(rootUserName);
			return new MyUserPrincipal(rootUser);

		} else {
			Optional<User> userCandidate = userRepository.findFirstByEmail(username);

			if (userCandidate.isPresent()) {
				LOG.info("successfull login of user: {}", username);
				return new MyUserPrincipal(userCandidate.get());
			} else {
				LOG.info("FAIL login of user {} ", username);
				throw new UsernameNotFoundException("could not find the user with username'" + username + "'");
			}
		}

	}

}
