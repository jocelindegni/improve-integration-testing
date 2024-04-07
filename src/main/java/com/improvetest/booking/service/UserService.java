package com.improvetest.booking.service;

import com.improvetest.booking.dto.UserDTO;
import com.improvetest.booking.exception.ServerIsDownException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public interface UserService {

    @Retryable(retryFor = ServerIsDownException.class, maxAttempts = 3, backoff = @Backoff(delay = 500))
    UserDTO getUserById(String userId);
}
