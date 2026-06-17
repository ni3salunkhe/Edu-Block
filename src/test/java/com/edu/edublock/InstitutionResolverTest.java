package com.edu.edublock;

import com.edu.entity.Institution;
import com.edu.repositories.InstitutionRepository;
import com.edu.servicesImpl.InstitutionResolverImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class InstitutionResolverTest {

    private InstitutionRepository institutionRepository;
    private InstitutionResolverImpl resolver;

    @BeforeEach
    public void setUp() {
        institutionRepository = Mockito.mock(InstitutionRepository.class);
        resolver = new InstitutionResolverImpl(institutionRepository);
    }

    @Test
    public void testResolveExactMatch() {
        String ocrText = "Awarded by University of Mumbai in the year 2025.";
        Institution mockInst = new Institution();
        mockInst.setName("University of Mumbai");

        when(institutionRepository.findByNameIgnoreCase("University of Mumbai"))
            .thenReturn(Optional.of(mockInst));

        Optional<Institution> resolved = resolver.resolve(ocrText);
        assertTrue(resolved.isPresent());
        assertEquals("University of Mumbai", resolved.get().getName());
    }

    @Test
    public void testResolveNormalizedMatch() {
        String ocrText = "Awarded by University-of-Mumbai in 2025.";
        Institution mockInst = new Institution();
        mockInst.setName("University of Mumbai");

        // Exact match fails
        when(institutionRepository.findByNameIgnoreCase("University-of-Mumbai"))
            .thenReturn(Optional.empty());
            
        // Normalized match succeeds
        when(institutionRepository.findByNormalizedName("universitymumbai"))
            .thenReturn(Optional.of(mockInst));

        Optional<Institution> resolved = resolver.resolve(ocrText);
        assertTrue(resolved.isPresent());
        assertEquals("University of Mumbai", resolved.get().getName());
    }
}
