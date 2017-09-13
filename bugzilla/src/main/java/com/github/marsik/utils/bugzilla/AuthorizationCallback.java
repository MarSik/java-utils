package com.github.marsik.utils.bugzilla;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizationCallback {
    String name;
    String password;
}
