package com.edu.services;

import java.nio.file.AccessDeniedException;

import com.edu.entity.Document;

public interface DocumentAccessService {
    void assertCanAccess(Document document);
}
