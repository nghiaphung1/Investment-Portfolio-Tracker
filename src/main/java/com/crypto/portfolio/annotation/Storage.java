package com.crypto.portfolio.annotation;

import com.crypto.portfolio.constants.StorageProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Storage {
    StorageProvider value();
}
