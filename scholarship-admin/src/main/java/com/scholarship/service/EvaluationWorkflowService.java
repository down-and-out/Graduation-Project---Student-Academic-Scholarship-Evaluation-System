package com.scholarship.service;

import java.util.Map;

public interface EvaluationWorkflowService {

    Map<String, Object> evaluateBatch(Long batchId);
}
