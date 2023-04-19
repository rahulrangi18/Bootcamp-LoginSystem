package com.boolcamp.AssignmentFive.services;

import com.boolcamp.AssignmentFive.models.User;
import com.boolcamp.AssignmentFive.repositories.UserMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserMongoRepository userMongoRepository;

    @Autowired
    private EmailService emailService;
    public String setPassLogin(String email, String password) {
        User existingUser = userMongoRepository.findByEmail(email);
        if (existingUser != null) {
            return "User already exists";
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        userMongoRepository.save(user);
        return "Signup Successful";
    }

    public String passLogin(String email, String password) {
        User user = userMongoRepository.findByEmail(email);
        if (user == null) {
            return "User does not exist";
        }
        if (!user.getPassword().equals(password)) {
            return "Incorrect password";
        }
        String token = generateToken();
        userMongoRepository.delete(user);
        user.setToken(token);
        userMongoRepository.save(user);
        return "Login Successful with token: "+ token;
    }

    public String updatePass(String email, String password, String newPass) {
        User user = userMongoRepository.findByEmail(email);
        if (user == null) {
            return "User does not exist";
        }
        if (!user.getPassword().equals(password)) {
            return "Incorrect password";
        }
        userMongoRepository.delete(user);
        user.setPassword(newPass);
        userMongoRepository.save(user);
        return "Password Updated";
    }
    public String requestPasswordReset(String email) throws MessagingException {
        User user = userMongoRepository.findByEmail(email);
        if (user == null) {
            return "User does not exist";
        }
        String resetToken = generateToken();
        user.setResetToken(resetToken);
        userMongoRepository.save(user);

        String resetLink = "http://localhost:4567/ResetLink?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetLink);
        return "Password reset email sent to " + email;
    }

    public String resetPassword(String email, String newPassword,String token) {
        User user = userMongoRepository.findByResetToken(token);
        if (user == null) {
            return "Invalid reset token for "+email;
        }
        user.setPassword(newPassword);
        user.setResetToken(null);
        userMongoRepository.save(user);
        return "Password reset successfully";
    }
    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
