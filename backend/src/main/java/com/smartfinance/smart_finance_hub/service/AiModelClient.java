package com.smartfinance.smart_finance_hub.service;

public interface AiModelClient {

    boolean isAvailable();

    String generate(String prompt);
}
