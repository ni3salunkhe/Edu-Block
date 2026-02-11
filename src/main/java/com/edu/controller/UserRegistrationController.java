package com.edu.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.entity.EmployerProfile;
import com.edu.entity.IndividualProfile;
import com.edu.entity.InstitutionProfile;
import com.edu.entity.User;
import com.edu.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRegistrationController {

    private final UserService userService;
    
    public UserRegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/individual")
    public User registerIndividual(@RequestBody IndividualProfile profile,
                                   @RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password) {
        return userService.registerIndividual(profile, username, email, password);
    }

    @PostMapping("/register/employer")
    public User registerEmployer(@RequestBody EmployerProfile profile,
                                 @RequestParam String username,
                                 @RequestParam String email,
                                 @RequestParam String password) {
        return userService.registerEmployer(profile, username, email, password);
    }

    @PostMapping("/register/institution")
    public User registerInstitution(@RequestBody InstitutionProfile profile,
                                    @RequestParam String username,
                                    @RequestParam String email,
                                    @RequestParam String password) {
        return userService.registerInstitution(profile, username, email, password);
    }
}
