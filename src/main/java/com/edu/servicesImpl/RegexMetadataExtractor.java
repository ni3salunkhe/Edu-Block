package com.edu.servicesImpl;

import com.edu.entity.DocumentIntelligence;
import com.edu.entity.DocumentIntelligence.DocumentClassification;
import com.edu.services.InstitutionResolver;
import com.edu.services.MetadataExtractor;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RegexMetadataExtractor implements MetadataExtractor {

    private final InstitutionResolver institutionResolver;

    private static final Pattern STUDENT_NAME_PATTERN = Pattern.compile(
        "(?i)(?:student\\s+name|candidate\\s+name|name)[:\\s]+([A-Z ]{3,50})"
    );
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile(
        "(?i)(?:enrollment|registration|reg|roll)[.\\s]*no[:\\s]+([A-Z0-9\\-]+)"
    );
    private static final Pattern DEGREE_PATTERN = Pattern.compile(
        "(?i)(?:master\\s+of\\s+computer\\s+applications|mca|bachelor\\s+of\\s+technology|b\\.tech|mba|b\\.sc|m\\.sc|ba|phd)"
    );
    private static final Pattern GRAD_YEAR_PATTERN = Pattern.compile(
        "(?i)(?:year\\s+of\\s+passing|passed\\s+in|year|graduation\\s+year)[:\\s]*([12][0-9]{3})"
    );
    private static final Pattern ACADEMIC_SCORE_PATTERN = Pattern.compile(
        "(?i)(?:cgpa|gpa|percentage|marks|grade)[:\\s]+([0-9.]+(?:\\s*/\\s*[0-9.]+)?%?|[A-F][+-]?)"
    );
    private static final Pattern CREDENTIAL_REF_PATTERN = Pattern.compile(
        "(?i)(?:certificate|degree|serial|ref|doc)[.\\s]*no[:\\s]+([A-Z0-9\\-]+)"
    );

    public RegexMetadataExtractor(InstitutionResolver institutionResolver) {
        this.institutionResolver = institutionResolver;
    }

    @Override
    public DocumentClassification classify(String ocrText) {
        if (ocrText == null) return DocumentClassification.UNKNOWN;
        String lower = ocrText.toLowerCase();
        if (lower.contains("degree") || lower.contains("certify that")) {
            return DocumentClassification.DEGREE_CERTIFICATE;
        } else if (lower.contains("transcript") || lower.contains("semester")) {
            return DocumentClassification.TRANSCRIPT;
        } else if (lower.contains("marksheet") || lower.contains("marks statement")) {
            return DocumentClassification.MARKSHEET;
        } else if (lower.contains("identity card") || lower.contains("id card") || lower.contains("student card")) {
            return DocumentClassification.ID_CARD;
        }
        return DocumentClassification.UNKNOWN;
    }

    @Override
    public void extractMetadata(String ocrText, DocumentIntelligence target) {
        if (ocrText == null) return;

        // Classify first
        target.setClassification(classify(ocrText));

        // 1. Extract Student Name
        Matcher nameMatcher = STUDENT_NAME_PATTERN.matcher(ocrText);
        if (nameMatcher.find()) {
            target.setStudentName(nameMatcher.group(1).trim());
        }

        // 2. Resolve Institution Name (Hierarchical Resolution)
        institutionResolver.resolve(ocrText)
            .ifPresent(inst -> target.setInstitutionName(inst.getName()));

        // 3. Extract Enrollment Number
        Matcher enrollMatcher = ENROLLMENT_PATTERN.matcher(ocrText);
        if (enrollMatcher.find()) {
            target.setEnrollmentNumber(enrollMatcher.group(1).trim());
        }

        // 4. Extract Degree
        Matcher degreeMatcher = DEGREE_PATTERN.matcher(ocrText);
        if (degreeMatcher.find()) {
            target.setDegreeProgram(degreeMatcher.group(0).trim().toUpperCase());
        }

        // 5. Extract Graduation Year
        Matcher yearMatcher = GRAD_YEAR_PATTERN.matcher(ocrText);
        if (yearMatcher.find()) {
            try {
                target.setGraduationYear(Integer.parseInt(yearMatcher.group(1)));
            } catch (NumberFormatException ignored) {}
        } else {
            // General 4 digit year fallback
            Pattern generalYear = Pattern.compile("\\b(20[0-2][0-9]|2030)\\b");
            Matcher genYearMatcher = generalYear.matcher(ocrText);
            if (genYearMatcher.find()) {
                target.setGraduationYear(Integer.parseInt(genYearMatcher.group(1)));
            }
        }

        // 6. Extract Academic Scores
        Matcher scoreMatcher = ACADEMIC_SCORE_PATTERN.matcher(ocrText);
        if (scoreMatcher.find()) {
            target.setAcademicScores(scoreMatcher.group(1).trim());
        }

        // 7. Extract Credential Reference
        Matcher refMatcher = CREDENTIAL_REF_PATTERN.matcher(ocrText);
        if (refMatcher.find()) {
            target.setCredentialReference(refMatcher.group(1).trim());
        }
    }
}
