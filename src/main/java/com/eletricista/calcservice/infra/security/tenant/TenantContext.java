package com.eletricista.calcservice.infra.security.tenant; // Ajuste o package por API

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentPlanType = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentPlanType(String planType) {
        currentPlanType.set(planType);
    }

    public static String getCurrentPlanType() {
        return currentPlanType.get();
    }

    public static void clear() {
        currentTenant.remove();
        currentPlanType.remove();
    }
}