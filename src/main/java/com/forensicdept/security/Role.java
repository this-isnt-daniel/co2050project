package com.forensicdept.security;

/**
 * Application roles. Stored as the {@code user_role} column value in the {@code users} table
 * and embedded as the {@code role} claim in the JWT.
 */
public enum Role {
    ADMIN,
    DOCTOR,
    JMO,
    LAB_STAFF,
    CLERICAL,
    RESEARCHER
}
