package com.edu.services;

import java.io.File;

import com.edu.dto.AIResult;

public interface AIService {
	AIResult analyze(File file);
}
