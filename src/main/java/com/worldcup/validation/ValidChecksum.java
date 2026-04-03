package com.worldcup.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChecksumValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidChecksum {
    String message() default "{validation.checksum.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
