package org.hsbc.api;

import java.util.Set;

/**
 * Demanded api interfaces
 * @author Leen Li
 */
public interface IAuth {
    /**
     * Create user
     * @param username Username as well as user id
     * @param password User cleartext password, need a secure channel between api and consumer
     * @return User created or not
     * @throws RuntimeException when username or password is empty, or user already exists.
     */
    boolean register(String username, String password);

    /**
     * Delete user
     * @param username Username as well as user id
     * @return User deleted or not
     * @throws RuntimeException when user does not exist.
     */
    boolean deleteUser(String username);

    /**
     * Create role
     * @param roleName Role name as well as role id
     * @return role created or not
     * @throws RuntimeException when role name is empty, or role already exists.
     */
    boolean createRole(String roleName);

    /**
     * Delete role
     * @param roleName Role name as well as role id
     * @return Role deleted or not
     * @throws RuntimeException when role already exists.
     */
    boolean deleteRole(String roleName);

    /**
     * Authorizing a role to the user
     * @param roleName Role name, must be a created role.
     * @param username Username, must be a created user.
     * @return Authorized or not
     */
    boolean addRoleToUser(String roleName, String username);

    /**
     * Authenticate user when user login
     * @param username Login username
     * @param password Cleartext password when login, need a secure channel between api and consumer
     * @return a jwt token valid for 2 hours when success, or throws exception when failed
     * @throws RuntimeException when user not exists or password error.
     */
    String login(String username, String password);

    /**
     * Invalidates current token
     * @param token jwt token, if token is invalid, nothing will happen
     */
    void logout(String token);

    /**
     * Check if token related user has current role.
     * @param token jwt token pre-signed
     * @param role check if user has this role
     * @return whether user has current role
     * @throws RuntimeException if token is invalid, expired etc.
     */
    boolean hasRole(String token, String role);

    /**
     * Get all my roles
     * @param token jwt token, related with a user
     * @return All roles for the token related user
     * @throws RuntimeException if token is invalid, expired etc.
     */
    Set<String> myRoles(String token);
}
