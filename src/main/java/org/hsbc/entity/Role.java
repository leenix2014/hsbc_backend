package org.hsbc.entity;

/**
 * Business entity "role"
 * @author Leen Li
 */
public class Role {
    private final String roleName;//role name as well as role id.

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
