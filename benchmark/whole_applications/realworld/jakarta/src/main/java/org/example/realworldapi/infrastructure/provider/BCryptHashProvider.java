package org.example.realworldapi.infrastructure.provider;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.realworldapi.domain.model.provider.HashProvider;

@ApplicationScoped
public class BCryptHashProvider implements HashProvider {

    @Override
    public String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    @Override
    public boolean checkPassword(String plaintext, String hashed) {
        return BCrypt.verifyer().verify(plaintext.toCharArray(), hashed).verified;
    }
}
