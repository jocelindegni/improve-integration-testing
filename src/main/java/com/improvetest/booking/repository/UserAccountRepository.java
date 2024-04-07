package com.improvetest.booking.repository;

import com.improvetest.booking.dto.UserDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface UserAccountRepository {
    @GetExchange(url = "${user-account.get-user-path}")
    UserDTO getUserById(@PathVariable String userId);
}
