package com.noplan.services;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.noplan.data.UserDTO;

/**
 * @author DaHu4wA (Stefan Huber)
 */
public interface UserService {

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	List<UserDTO> getAllUsers();

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	UserDTO getUserById(@PathParam("id") Long id);
	
	@GET
	@Path("/name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	UserDTO getUserByUsername(@PathParam("name") String username);
	
	@GET
	@Path("/createold/{name}/{pw}")
	@Produces(MediaType.APPLICATION_JSON)
	UserDTO createUser(@PathParam("name") String username, @PathParam("pw") String pw);

	@POST
	@Path("/create")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	UserDTO createUser(UserDTO user);

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	UserDTO updateUser(UserDTO user);

}
