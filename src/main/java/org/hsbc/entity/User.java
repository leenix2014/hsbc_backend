package org.hsbc.entity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business entity "user"
 * @author Leen Li
 */
public class User {
    private final String username;//username as well as user id.
    private final String password;//encrypted password.
    private final String salt;//thanks for the salt, same cleartext passwords will generate different encrypted password.

    private final Map<String, Role> roles = new ConcurrentHashMap<>();//current user authorized roles

    public User(String username, String password, String salt) {
        this.username = username;
        this.password = password;
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public void addRole(Role role){
        roles.put(role.getRoleName(), role);
    }

    public void removeRole(String roleName){
        roles.remove(roleName);
    }

    public boolean hasRole(String roleName){
        return roles.containsKey(roleName);
    }

    public Set<String> getAllRoles(){
        return roles.keySet();
    }
}
