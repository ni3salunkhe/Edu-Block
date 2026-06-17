package com.edu.edublock;

import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentIntelligence.DocumentClassification;
import com.edu.entity.Institution;
import com.edu.services.InstitutionResolver;
import com.edu.servicesImpl.RegexMetadataExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class RegexMetadataExtractorTest {

    private InstitutionResolver institutionResolver;
    private RegexMetadataExtractor extractor;

    @BeforeEach
    public void setUp() {
        institutionResolver = Mockito.mock(InstitutionResolver.class);
        extractor = new RegexMetadataExtractor(institutionResolver);
    }

    @Test
    public void testClassifyDegree() {
        String ocrText = "This is to certify that John Doe has completed the Degree of Master of Computer Applications.";
        assertEquals(DocumentClassification.DEGREE_CERTIFICATE, extractor.classify(ocrText));
    }

    @Test
    public void testClassifyTranscript() {
        String ocrText = "Official Transcript of Academic Record. Semester 1, 2, 3 details.";
        assertEquals(DocumentClassification.TRANSCRIPT, extractor.classify(ocrText));
    }

    @Test
    public void testExtractMetadata() {
        String ocrText = "Name: ALICE SMITH\nEnrollment No: ENR-9901\nCGPA: 9.5\nPassed in: 2024\nDegree: MCA\nCertificate No: CERT-001";
        
        Institution mockInst = new Institution();
        mockInst.setName("XYZ University");
        when(institutionResolver.resolve(anyString())).thenReturn(Optional.of(mockInst));

        DocumentIntelligence target = new DocumentIntelligence();
        extractor.extractMetadata(ocrText, target);

        assertEquals("ALICE SMITH", target.getStudentName());
        assertEquals("XYZ University", target.getInstitutionName());
        assertEquals("ENR-9901", target.getEnrollmentNumber());
        assertEquals("MCA", target.getDegreeProgram());
        assertEquals(2024, target.getGraduationYear());
        assertEquals("9.5", target.getAcademicScores());
        assertEquals("CERT-001", target.getCredentialReference());
    }
}
