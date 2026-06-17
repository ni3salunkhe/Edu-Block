package com.edu.servicesImpl;

import com.edu.entity.Institution;
import com.edu.services.VerificationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VerificationContextImpl implements VerificationContext {

    private final Institution resolvedInstitution;
    private final long referenceReuseCount;
    private final boolean accredited;
    private final Map<String, Object> extendedData = new HashMap<>();

    public VerificationContextImpl(Institution resolvedInstitution, long referenceReuseCount, boolean accredited) {
        this.resolvedInstitution = resolvedInstitution;
        this.referenceReuseCount = referenceReuseCount;
        this.accredited = accredited;
    }

    @Override
    public Optional<Institution> getResolvedInstitution() {
        return Optional.ofNullable(resolvedInstitution);
    }

    @Override
    public long getReferenceReuseCount(String credentialReference) {
        return referenceReuseCount;
    }

    @Override
    public boolean hasAccreditation(UUID institutionId) {
        return accredited;
    }

    @Override
    public Map<String, Object> getExtendedData() {
        return extendedData;
    }
}
