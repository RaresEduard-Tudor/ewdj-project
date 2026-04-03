package com.worldcup.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPasswords {
    String message() default "{validation.passwords.mismatch}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
