package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@CrossOrigin
public class ProfileController {
    private final ProfileDao profileDao;
    private final UserDao userDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public Profile create(@RequestBody Profile profile) {
        try {
            return profileDao.create(profile);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public Profile getProfile(Principal principal) {
        String userName = principal.getName();
        int userId = userDao.getByUserName(userName).getId();
        return profileDao.getByUserId(userId);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public Profile updateProfile(@RequestBody Profile updatedProfile, Principal principal) {
        String userName = principal.getName();
        int userId = userDao.getByUserName(userName).getId();
        updatedProfile.setUserId(userId);
        return profileDao.update(updatedProfile);
    }
}
