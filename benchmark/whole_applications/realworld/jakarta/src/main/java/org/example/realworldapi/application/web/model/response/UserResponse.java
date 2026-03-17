package org.example.realworldapi.application.web.model.response;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.realworldapi.domain.model.user.User;

@Getter
@Setter
@NoArgsConstructor
@JsonRootName("user")
public class UserResponse {

    private String username;
    private String bio;
    private String image;
    private String email;
    private String token;

    public UserResponse(User user, String token) {
        this.username = user.getUsername();
        this.bio = user.getBio();
        this.image = user.getImage();
        this.email = user.getEmail();
        this.token = token;
    }
}
