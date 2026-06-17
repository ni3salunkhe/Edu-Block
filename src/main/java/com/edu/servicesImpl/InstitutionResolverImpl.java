package com.edu.servicesImpl;

import com.edu.entity.Institution;
import com.edu.repositories.InstitutionRepository;
import com.edu.services.InstitutionResolver;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InstitutionResolverImpl implements InstitutionResolver {

    private final InstitutionRepository institutionRepository;
    
    private static final Pattern INSTITUTION_PATTERN = Pattern.compile(
        "\\b((?:University|Institute|College|Academy|School)(?:\\s+of\\s+|\\-of\\-|\\s+|\\-)[A-Z][a-zA-Z0-9\\-]*|[A-Z][a-zA-Z0-9\\-]*\\s+(?:University|Institute|College|Academy|School))\\b"
    );

    public InstitutionResolverImpl(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @Override
    public Optional<Institution> resolve(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = INSTITUTION_PATTERN.matcher(ocrText);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String candidate = matcher.group(1).trim();

        // Layer 1: Exact Case-Insensitive Match (Index-driven)
        Optional<Institution> inst = institutionRepository.findByNameIgnoreCase(candidate);
        if (inst.isPresent()) {
            return inst;
        }

        // Layer 2: Normalized Match (Index-driven via normalizedName column)
        String normCandidate = normalize(candidate);
        Optional<Institution> normInst = institutionRepository.findByNormalizedName(normCandidate);
        if (normInst.isPresent()) {
            return normInst;
        }

        // Layer 3: Contains Matching (Look up database names containing candidate tokens)
        String[] tokens = candidate.split(" ");
        for (String token : tokens) {
            if (token.length() > 3 && !token.equalsIgnoreCase("university") && !token.equalsIgnoreCase("institute") && !token.equalsIgnoreCase("college")) {
                Optional<Institution> containsInst = institutionRepository.findByNameContainingIgnoreCase(token);
                if (containsInst.isPresent()) {
                    return containsInst;
                }
            }
        }

        return Optional.empty();
    }

    private String normalize(String value) {
        return value.toLowerCase()
            .replaceAll("\\b(the|of|and|for|in)\\b", "")
            .replaceAll("[^a-z0-9]", "")
            .trim();
    }
}
