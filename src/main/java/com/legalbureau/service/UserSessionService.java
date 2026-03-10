package com.legalbureau.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final SessionRegistry sessionRegistry;

    public void kickUserByEmail(String email) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                if (userDetails.getUsername().equals(email)) {
                    for (SessionInformation info : sessionRegistry.getAllSessions(userDetails, false)) {
                        info.expireNow();
                    }
                }
            }
        }
    }
}