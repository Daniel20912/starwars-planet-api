package com.danieloliveira.starwarsplanetapi.jacoco;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // indica que ela é aplicada em tempo de execução
@Target(ElementType.METHOD) // indica que ela é aplicada em métodos
public @interface ExcludeFromJacocoGeneratedReport {
    // ela pode ficar vazia pois o jacoco já entende que um método com essa annotation deve ser desconsiderado
}
