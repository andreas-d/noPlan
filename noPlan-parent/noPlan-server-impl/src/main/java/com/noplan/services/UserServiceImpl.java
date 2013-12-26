package com.noplan.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.noplan.UserRoles;
import com.noplan.data.UserDTO;
import com.noplan.persistence.entity.UserEntity;
import com.noplan.persistence.repositories.UserEventMappingRepository;
import com.noplan.persistence.repositories.UserRepository;
import com.noplan.security.TokenUtils;

/**
 * This is the main user service
 * 
 * @author DaHu4wA (Stefan Huber)
 */
@Path("/user")
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDetailsService userService;

	@Autowired
	private UserEventMappingRepository userEventMappingRepository;

	@Autowired
	@Qualifier("authenticationManager")
	private AuthenticationManager authManager;

	@Override
	public UserDTO createUserPublic(UserDTO user) {
		return createUser(user, false);
	}

	@Override
	public UserDTO authenticate(UserDTO userToLogin) {

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userToLogin.getUsername(), userToLogin.getPassword());
		Authentication authentication = this.authManager.authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		Map<String, Boolean> roles = new HashMap<String, Boolean>();

		/*
		 * Reload user as password of authentication principal will be null
		 * after authorization and password is needed for token generation
		 */
		UserDetails userDetails = this.userService.loadUserByUsername(userToLogin.getUsername());

		for (GrantedAuthority authority : userDetails.getAuthorities()) {
			roles.put(authority.toString(), Boolean.TRUE);
		}

		return new UserDTO(userDetails.getUsername(), roles, TokenUtils.createToken(userDetails));
	}

	@Override
	public UserDTO getUserById(Long id) {

		UserEntity user = userRepository.getUserById(id);

		if (user == null) {
			return null;
		}

		return user.toDTO(userRepository.hasRole(user.getId(), UserRoles.ADMIN_ROLE));
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		UserEntity user = userRepository.getUserByUsername(username);

		if (user == null) {
			return null;
		}

		return user.toDTO(userRepository.hasRole(user.getId(), UserRoles.ADMIN_ROLE));
	}

	@Override
	public UserDTO createUser(UserDTO user) {
		return createUser(user, true);
	}

	private UserDTO createUser(UserDTO user, boolean calledFromBackend) {

		if (getUserByUsername(user.getUsername()) != null) {
			throw new IllegalArgumentException("User " + user.getUsername() + " already exists!");
		}

		UserEntity userEntity = new UserEntity(user);
		userEntity.setId(null);
		userRepository.saveUser(userEntity);

		userRepository.addRoleToUser(userEntity.getId(), UserRoles.USER_ROLE);

		if (calledFromBackend) {
			correctAdminRoles(user, userEntity);
		}

		return userEntity.toDTO(userRepository.hasRole(user.getId(), UserRoles.ADMIN_ROLE));
	}

	private void correctAdminRoles(UserDTO user, UserEntity userEntity) {
		boolean hasAdminRole = userRepository.hasRole(userEntity.getId(), UserRoles.ADMIN_ROLE);

		if (hasAdminRole && !user.getIsadmin()) {
			userRepository.removeRoleFromUser(userEntity.getId(), UserRoles.ADMIN_ROLE);
		} else if (!hasAdminRole && user.getIsadmin()) {
			userRepository.addRoleToUser(userEntity.getId(), UserRoles.ADMIN_ROLE);
		}
	}

	@Override
	public List<UserDTO> getAllUsers() {

		List<UserDTO> result = new ArrayList<UserDTO>();

		for (UserEntity user : userRepository.getAllUsers()) {
			UserDTO dto = user.toDTO(userRepository.hasRole(user.getId(), UserRoles.ADMIN_ROLE));
			dto.setIsadmin((userRepository.hasRole(user.getId(), UserRoles.ADMIN_ROLE)));
			result.add(dto);
		}

		return result;
	}

	@Override
	public UserDTO updateUser(UserDTO user) {
		UserEntity entity = userRepository.getUserById(user.getId());

		entity.fromDTO(user, true);

		userRepository.updateUser(entity);

		correctAdminRoles(user, entity);

		return entity.toDTO(userRepository.hasRole(user.getId(), UserRoles.ADMIN_ROLE));
	}

	@Override
	public void deleteUserById(Long userId) {

		UserEntity userEntity = userRepository.getUserById(userId);

		userRepository.deleteAllRolesForUser(userId);

		userEventMappingRepository.deleteAllMappingsForUser(userEntity);

		userRepository.deleteUser(userEntity);

	}

}
