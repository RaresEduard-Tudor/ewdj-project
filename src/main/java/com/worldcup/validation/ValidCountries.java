package com.worldcup.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CountriesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountries {
    String message() default "{validation.country.same}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
