package com.scholarship.service;

public interface LoginAttemptService {

    void recordFailure(String username, String clientIp);

    void resetFailures(String username, String clientIp);

    boolean isAccountLocked(String username);

    boolean isIpLocked(String clientIp);

    long getAccountRemainingLockTime(String username);

    long getIpRemainingLockTime(String clientIp);
}
