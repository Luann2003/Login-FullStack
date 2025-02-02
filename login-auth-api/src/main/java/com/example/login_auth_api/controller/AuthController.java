package com.example.login_auth_api.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.login_auth_api.controller.dto.LoginRequestDTO;
import com.example.login_auth_api.controller.dto.ResponseDTO;
import com.example.login_auth_api.controller.dto.registerRequestDTO;
import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.infra.security.TokenService;
import com.example.login_auth_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	@Autowired
	private  UserRepository repository;
	
	@Autowired
	private  PasswordEncoder encoder;
	
	@Autowired
	private TokenService service;
	
	@PostMapping("/login")
	public ResponseEntity login(@RequestBody LoginRequestDTO body) {
		User user = this.repository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("User not found"));
		
		if(encoder.matches(body.password(), user.getPassword())) {
			String token = this.service.generateToken(user);
			return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
		}
		return ResponseEntity.badRequest().build();
	}
	
	@PostMapping("/register")
	public ResponseEntity register(@RequestBody registerRequestDTO body) {
		
		Optional<User> user = this.repository.findByEmail(body.email());

		if(user.isEmpty()) {
			User newUser = new User();
			newUser.setPassword(encoder.encode(body.password()));
			newUser.setEmail(body.email());
			newUser.setName(body.name());
			this.repository.save(newUser);
			
			String token = this.service.generateToken(newUser);
			return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
		}
		
		return ResponseEntity.badRequest().build();
	}
}
