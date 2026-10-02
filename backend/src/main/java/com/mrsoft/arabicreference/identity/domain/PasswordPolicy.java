package com.mrsoft.arabicreference.identity.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {
    }

    public static void check(String password) {
        List<FieldErrorDetail> errors = new ArrayList<>();
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            errors.add(new FieldErrorDetail("password", "Password must be 12 to 128 characters."));
        }
        if (password == null || password.chars().noneMatch(Character::isUpperCase)) {
            errors.add(new FieldErrorDetail("password", "Password must include an uppercase letter."));
        }
        if (password == null || password.chars().noneMatch(Character::isLowerCase)) {
            errors.add(new FieldErrorDetail("password", "Password must include a lowercase letter."));
        }
        if (password == null || password.chars().noneMatch(Character::isDigit)) {
            errors.add(new FieldErrorDetail("password", "Password must include a number."));
        }
        if (password == null || password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
            errors.add(new FieldErrorDetail("password", "Password must include a special character."));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Password does not meet the policy.", errors);
        }
    }
}
