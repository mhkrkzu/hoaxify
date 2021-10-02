package com.hoaxify.ws.user;

import com.hoaxify.ws.error.NotFoundException;
import com.hoaxify.ws.file.FileService;
import com.hoaxify.ws.hoax.HoaxService;
import com.hoaxify.ws.user.vm.UserUpdateVm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService {


    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    FileService fileService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }


    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public Page<User> getUsers(Pageable page, User user) {

        if (user != null) {
            return userRepository.findByUsernameNot(user.getUsername(), page);
        }
        return userRepository.findAll(page);

    }

    public User getUserByUsername(String username) {
        User inDB = userRepository.findByUsername(username);
        if (inDB == null) {
            throw new NotFoundException();
        }
        return inDB;
    }

    public User updateUser(String username, UserUpdateVm updatedUser) {
        User inDB = getUserByUsername(username);
        inDB.setDisplayName(updatedUser.getDisplayName());
        if (updatedUser.getImage() != null) {
            String oldImageName = inDB.getImage();
            try {
                String storedFileName = fileService.writeBase64EncodedStringToFile(updatedUser.getImage());
                inDB.setImage(storedFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileService.deleteProfileImage(oldImageName);
        }
        return userRepository.save(inDB);
    }

    public void deleteUser(String username) {
        User inDB = userRepository.findByUsername(username);
        fileService.deleteAllStoredFilesForUser(inDB);
        userRepository.delete(inDB);
    }
}
