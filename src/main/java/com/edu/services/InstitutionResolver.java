package com.edu.services;

import com.edu.entity.Institution;
import java.util.Optional;

public interface InstitutionResolver {
    Optional<Institution> resolve(String ocrText);
}
