package com.improvetest.booking.service;

import com.improvetest.booking.dto.UserDTO;
import com.improvetest.booking.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDTO getUserById(String userId) {
        return userAccountRepository.getUserById(userId);
    }
}
