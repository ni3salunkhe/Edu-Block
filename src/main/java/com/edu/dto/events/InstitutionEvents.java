package com.edu.dto.events;

import java.util.UUID;

public class InstitutionEvents {

    public static class InstitutionAccreditationChangedEvent {
        private final UUID institutionId;
        private final String newLevel;

        public InstitutionAccreditationChangedEvent(UUID institutionId, String newLevel) {
            this.institutionId = institutionId;
            this.newLevel = newLevel;
        }

        public UUID getInstitutionId() { return institutionId; }
        public String getNewLevel() { return newLevel; }
    }
}
