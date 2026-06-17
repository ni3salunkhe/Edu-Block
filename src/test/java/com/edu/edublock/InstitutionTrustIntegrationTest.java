package com.edu.edublock;

import com.edu.dto.events.InstitutionEvents.InstitutionAccreditationChangedEvent;
import com.edu.entity.*;
import com.edu.entity.InstitutionTrustProfile.TrustTier;
import com.edu.repositories.InstitutionRepository;
import com.edu.repositories.InstitutionTrustProfileRepository;
import com.edu.services.InstitutionTrustService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class InstitutionTrustIntegrationTest {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private InstitutionTrustService trustService;

    @Autowired
    private InstitutionTrustProfileRepository trustProfileRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    public void testAccreditationChangeIntegrationFlow() throws Exception {
        // 1. Create and Save Institution
        Institution inst = new Institution();
        inst.setName("Test University of Trust");
        inst.setCode("TST-UNI-" + UUID.randomUUID().toString().substring(0, 8));
        inst.setOfficialEmail("contact@" + UUID.randomUUID().toString().substring(0, 8) + ".edu");
        inst.setTrustLevel(InstitutionTrustLevel.VERIFIED);
        inst.setStatus(InstitutionStatus.ACTIVE);
        inst = institutionRepository.save(inst);

        UUID instId = inst.getId();

        try {
            // 2. Fetch or create dynamic profile (starts with standard baseline score 50)
            InstitutionTrustProfile profile = trustService.getOrCreateProfile(instId);
            assertNotNull(profile);
            assertEquals(50, profile.getTrustScore());
            assertEquals(TrustTier.MEDIUM, profile.getTrustTier());

            // 3. Publish Accreditation Changed Event (to ACCREDITED)
            inst.setTrustLevel(InstitutionTrustLevel.ACCREDITED);
            institutionRepository.save(inst);

            eventPublisher.publishEvent(new InstitutionAccreditationChangedEvent(instId, "ACCREDITED"));

            // Wait for async thread event execution by polling (up to 5 seconds, checking every 100ms)
            InstitutionTrustProfile updated = null;
            int attempts = 0;
            while (attempts < 50) {
                updated = trustProfileRepository.findById(instId).orElse(null);
                if (updated != null && updated.getTrustScore() >= 60) {
                    break;
                }
                Thread.sleep(100);
                attempts++;
            }

            // 4. Verify score increased
            assertNotNull(updated);
            assertTrue(updated.getTrustScore() >= 60, "Accreditation event did not raise the trust score: " + (updated != null ? updated.getTrustScore() : "null"));
        } finally {
            // Cleanup
            try {
                trustProfileRepository.deleteById(instId);
                institutionRepository.deleteById(instId);
            } catch (Exception ignored) {}
        }
    }
}
