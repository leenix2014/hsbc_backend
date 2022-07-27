package org.hsbc;

import com.auth0.jwt.HeaderParams;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hsbc.api.IAuth;
import org.hsbc.entity.Role;
import org.hsbc.entity.User;
import org.hsbc.util.PBKDF2Util;
import org.hsbc.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Demanded interfaces' implementation using jwt
 * @author Leen Li
 */
public class JWTSolution implements IAuth {

    // Map<username, User>
    private static final Map<String, User> users = new ConcurrentHashMap<>();//if possible, persistent into database, or blockchain storage variable
    // Map<roleName, Role>
    private static final Map<String, Role> roles = new ConcurrentHashMap<>();//if possible, persistent into database, or blockchain storage variable

    // constants
    // token valid period, in millis
    private static final long VALID_PERIOD = 2 * 3600 * 1000;//2 hours
    // jwt token secret, used for hash signature. if leaked, token may be forged.
    private static final String TOKEN_SECRET = "VeKcPGNcxYOCVRUDnzoEzwhtYGQddlRu";

    /**
     * Create user
     * @param username Username as well as user id
     * @param password User cleartext password, need a secure channel between api and consumer
     * @return User created or not
     * @throws RuntimeException when username or password is empty, or user already exists.
     */
    @Override
    public boolean register(String username, String password) {
        if(StringUtil.isEmpty(username)){
            throw new RuntimeException("User name can not be empty!");
        }
        if(StringUtil.isEmpty(password)){
            throw new RuntimeException("User password can not be empty!");
        }
        if(users.containsKey(username)){
            throw new RuntimeException(String.format("User '%s' already exists!", username));
        }
        // not safe, do we need user login?
        String salt = StringUtil.randomString(8);//generate random string as salt
        String encryptedPassword = PBKDF2Util.encryptPassword(password, salt);//slow hash of algorithm PBKDF2
        User user = new User(username, encryptedPassword, salt);//save encryptedPassword and salt in storage
        users.put(username, user);
        return true;
    }

    /**
     * Delete user
     * @param username Username as well as user id
     * @return User deleted or not
     * @throws RuntimeException when user does not exist.
     */
    @Override
    public boolean deleteUser(String username) {
        if(!users.containsKey(username)){
            throw new RuntimeException(String.format("User '%s' does not exists!", username));
        }
        // not safe, do we need user login?
        users.remove(username);
        return true;
    }

    /**
     * Create role
     * @param roleName Role name as well as role id
     * @return role created or not
     * @throws RuntimeException when role name is empty, or role already exists.
     */
    @Override
    public boolean createRole(String roleName) {
        if(StringUtil.isEmpty(roleName)){
            throw new RuntimeException("Role name can not be empty!");
        }
        if(roles.containsKey(roleName)){
            throw new RuntimeException(String.format("Role '%s' already exists!", roleName));
        }
        // not safe, do we need user login?
        Role role = new Role(roleName);
        roles.put(roleName, role);
        return true;
    }

    /**
     * Delete role
     * @param roleName Role name as well as role id
     * @return Role deleted or not
     * @throws RuntimeException when role already exists.
     */
    @Override
    public boolean deleteRole(String roleName) {
        if(!roles.containsKey(roleName)){
            throw new RuntimeException(String.format("Role '%s' does not exists!", roleName));
        }
        // not safe, do we need user login?
        roles.remove(roleName);
        return true;
    }

    /**
     * Authorizing a role to the user
     * @param roleName Role name, must be a created role.
     * @param username Username, must be a created user.
     * @return Authorized or not
     */
    @Override
    public boolean addRoleToUser(String roleName, String username) {
        if(!roles.containsKey(roleName)){
            throw new RuntimeException(String.format("Role '%s' does not exists!", roleName));
        }
        if (!users.containsKey(username)){
            throw new RuntimeException(String.format("User '%s' does not exists!", username));
        }
        // not safe, do we need user login?
        User user = users.get(username);
        Role role = roles.get(roleName);
        user.addRole(role);
        return true;
    }

    /**
     * Authenticate user when user login
     * @param username Login username
     * @param password Cleartext password when login, need a secure channel between api and consumer
     * @return a jwt token valid for 2 hours when success, or throws exception when failed
     * @throws RuntimeException when user not exists or password error.
     */
    @Override
    public String login(String username, String password) {
        if (!users.containsKey(username)){
            throw new RuntimeException("error: user or password error!");
        }
        User user = users.get(username);
        if(!PBKDF2Util.checkPassword(password, user.getSalt(), user.getPassword())){
            throw new RuntimeException("error: user or password error!");
        }
        Date expireDate = new Date(System.currentTimeMillis() + VALID_PERIOD);
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);//algorithm with signing key
        // jwt header
        Map<String, Object> header = new HashMap<>();
        header.put(HeaderParams.TYPE, "JWT");
        header.put(HeaderParams.ALGORITHM, "HS256");
        String token;
        try {
            token = JWT.create()
                    .withHeader(header)
                    .withClaim("username", username)
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
        } catch (Exception e) {//JWTCreationException – if the claims could not be converted to a valid JSON or there was a problem with the signing key.
            e.printStackTrace(); // Log sign error, if fully tested, should not reach here.
            throw e;// make caller aware, should not reach here
        }
        invalidatedTokens.invalidate(token);// remove invalidatedTokens for safety, not necessary
        return token;
    }

    /** JWT can not invalidate once issued. see @<a href="https://blog.indrek.io/articles/invalidate-jwt/">invalidate-jwt</a>
     *  JWT solution means to make server stateless.
     *  In order to invalidate jwt, we need to keep some state in server.
     *  We can keep state in User entity, but that's invasive, we should not contaminate business entity.
     *  We keep state in guava cache. if possible, use redis instead.
     */
    // Cache<token, User>, entry value is reserved for later use
    private static final Cache<String, User> invalidatedTokens = CacheBuilder.newBuilder().expireAfterWrite(VALID_PERIOD, TimeUnit.MILLISECONDS).build();

    /**
     * Invalidates current token
     * @param token jwt token, if token is invalid, nothing will happen
     */
    @Override
    public void logout(String token){
        DecodedJWT jwt = verify(token);
        if(jwt == null){
            return; // token is invalid, no need to do anything
        }
        String username = jwt.getClaim("username").asString();
        if (!users.containsKey(username)){
            System.out.printf("token secret maybe is leaked!! or maybe user '%s' is deleted accidentally %n", username);
            return;// should not reach here
        }
        User user = users.get(username);
        invalidatedTokens.put(token, user);
    }

    /**
     * Check if token related user has current role.
     * @param token jwt token pre-signed
     * @param role check if user has this role
     * @return whether user has current role
     * @throws RuntimeException if token is invalid, expired etc.
     */
    @Override
    public boolean hasRole(String token, String role){
        DecodedJWT jwt = verify(token);
        if(jwt == null){
            throw new RuntimeException("error: token invalid");
        }
        String username = jwt.getClaim("username").asString();
        if (!users.containsKey(username)){
            System.out.printf("token secret maybe is leaked!! or maybe user '%s' is deleted accidentally %n", username);
            return false;// should not reach here
        }
        User user = users.get(username);
        return user.hasRole(role);
    }

    /**
     * Get all my roles
     * @param token jwt token, related with a user
     * @return All roles for the token related user
     * @throws RuntimeException if token is invalid, expired etc.
     */
    @Override
    public Set<String> myRoles(String token){
        DecodedJWT jwt = verify(token);
        if(jwt == null){
            throw new RuntimeException("error: token invalid");
        }
        String username = jwt.getClaim("username").asString();
        if (!users.containsKey(username)){
            System.out.printf("token secret maybe is leaked!! or maybe user '%s' is deleted accidentally %n", username);
            throw new RuntimeException("error: token invalid");// should not reach here
        }
        User user = users.get(username);
        return user.getAllRoles();
    }

    /**
     * verify current jwt token
     * @param token jwt token
     * @return Decoded jwt information if valid, null if not valid
     */
    protected DecodedJWT verify(String token){
        if (invalidatedTokens.asMap().containsKey(token)){
            return null;
        }
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt;
        try {
            jwt = verifier.verify(token);
        } catch (TokenExpiredException e) {// if the token has expired.
            throw e;// tell user token expired
        } catch (Exception e){
            //SignatureVerificationException – if the signature is invalid.
            return null; // ignore
        }
        return jwt;
    }
}
