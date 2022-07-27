import com.auth0.jwt.exceptions.TokenExpiredException;
import org.hsbc.JWTSolution;
import org.hsbc.api.IAuth;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JWTSolution
 * @author Leen Li
 */
public class JWTSolutionTest {

    private final static IAuth jwt = new JWTSolution();

    // global created users and roles.
    private static final String USER1 = "Leen Li";
    private static final String USER2 = "HSBC";
    private static final String PWD1 = "123";
    private static final String PWD2 = "hsbc";
    private static final String ROLE1 = "Developer";//user1 role
    private static final String ROLE2 = "Admin";//user2 role

    @BeforeAll
    static void initAll() {
        jwt.register(USER1, PWD1);
        jwt.register(USER2, PWD2);
        jwt.createRole(ROLE1);
        jwt.createRole(ROLE2);
        jwt.addRoleToUser(ROLE1, USER1);
        jwt.addRoleToUser(ROLE2, USER2);
    }

    @Test
    void createUserTest() {
        assertTrue(jwt.register("test", "abc"));
        assertTrue(jwt.register("username", "password"));

        // register error test
        Exception userNameEmpty = assertThrows(RuntimeException.class, () -> jwt.register(null, "321"));
        assertEquals(userNameEmpty.getMessage(), "User name can not be empty!");

        Exception passwordEmpty = assertThrows(RuntimeException.class, () -> jwt.register(USER1, null));
        assertEquals(passwordEmpty.getMessage(), "User password can not be empty!");

        Exception duplicated = assertThrows(RuntimeException.class, () -> jwt.register(USER1, "321"));
        assertEquals(duplicated.getMessage(), "User 'Leen Li' already exists!");
    }

    @Test
    void deleteUserTest() {
        assertTrue(jwt.deleteUser(USER1));
        // after test, recover USER1
        assertTrue(jwt.register(USER1, PWD1));

        //deleteUser error test
        Exception e = assertThrows(RuntimeException.class, () -> jwt.deleteUser("not created user"));
        assertEquals(e.getMessage(), "User 'not created user' does not exists!");
    }

    @Test
    void createRoleTest() {
        assertTrue(jwt.createRole("tester"));
        assertTrue(jwt.createRole("worker"));

        //createRole error test
        Exception roleNameEmpty = assertThrows(RuntimeException.class, () -> jwt.createRole(" "));
        assertEquals(roleNameEmpty.getMessage(), "Role name can not be empty!");

        Exception duplicated = assertThrows(RuntimeException.class, () -> jwt.createRole(ROLE1));
        assertEquals(duplicated.getMessage(), String.format("Role '%s' already exists!", ROLE1));
    }

    @Test
    void deleteRoleTest() {
        assertTrue(jwt.deleteRole(ROLE1));
        // after test, recover ROLE1
        assertTrue(jwt.createRole(ROLE1));

        // deleteRole error test
        Exception e = assertThrows(RuntimeException.class, () -> jwt.deleteRole("not created role"));
        assertEquals(e.getMessage(), "Role 'not created role' does not exists!");
    }

    @Test
    void addRoleToUserTest() {
        assertTrue(jwt.addRoleToUser(ROLE1, USER1));

        // addRoleToUser error test
        Exception errorRole = assertThrows(RuntimeException.class, () -> jwt.addRoleToUser("not created role", USER1));
        assertEquals(errorRole.getMessage(), "Role 'not created role' does not exists!");

        Exception errorUser = assertThrows(RuntimeException.class, () -> jwt.addRoleToUser(ROLE1, "not created user"));
        assertEquals(errorUser.getMessage(), "User 'not created user' does not exists!");
    }

    @Test
    void loginTest() {
        // no exception means login success
        jwt.login(USER1, PWD1);
        jwt.login(USER2, PWD2);

        // login error test
        Exception errorPwd = assertThrows(RuntimeException.class, () -> jwt.login(USER1, PWD2));
        assertEquals(errorPwd.getMessage(), "error: user or password error!");

        Exception errorUser = assertThrows(RuntimeException.class, () -> jwt.login("not created user", PWD1));
        assertEquals(errorUser.getMessage(), "error: user or password error!");
    }

    @Test
    void hasRoleTest() {
        String token1 = jwt.login(USER1, PWD1);
        assertTrue(jwt.hasRole(token1, ROLE1));// Leen Li has role Developer
        assertFalse(jwt.hasRole(token1, ROLE2));// Leen Li does not have role Admin

        String token2 = jwt.login(USER2, PWD2);
        assertTrue(jwt.hasRole(token2, ROLE2));// HSBC has role Admin
        assertFalse(jwt.hasRole(token2, ROLE1));// HSBC does not have role Developer
    }

    @Test
    void allRolesTest() {
        String token1 = jwt.login(USER1, PWD1);
        Set<String> roleSet1 = new HashSet<>();
        roleSet1.add(ROLE1);
        assertEquals(roleSet1, jwt.myRoles(token1));

        roleSet1.add(ROLE2);
        assertNotEquals(roleSet1, jwt.myRoles(token1));//Leen Li does not hava role 'Admin'
    }

    @Test
    void invalidateTest() {
        // login
        String token1 = jwt.login(USER1, PWD1);
        // do some action that needs login
        assertTrue(jwt.hasRole(token1, ROLE1));
        Set<String> roleSet1 = new HashSet<>();
        roleSet1.add(ROLE1);
        assertEquals(roleSet1, jwt.myRoles(token1));

        // logout
        jwt.logout(token1);
        // can not do any action that needs login
        Exception hasRoleError = assertThrows(RuntimeException.class, () -> jwt.hasRole(token1, ROLE1));
        assertEquals(hasRoleError.getMessage(), "error: token invalid");
        Exception myRolesError = assertThrows(RuntimeException.class, () -> jwt.myRoles(token1));
        assertEquals(myRolesError.getMessage(), "error: token invalid");
    }

    @Test
    void invalidTokenTest() {
        String selfCreatedToken = "aaaaa.bbbb.cccc";
        Exception hasRoleError = assertThrows(RuntimeException.class, () -> jwt.hasRole(selfCreatedToken, ROLE1));
        assertEquals(hasRoleError.getMessage(), "error: token invalid");
        Exception myRolesError = assertThrows(RuntimeException.class, () -> jwt.myRoles(selfCreatedToken));
        assertEquals(myRolesError.getMessage(), "error: token invalid");
    }

    @Test
    void expireTest() throws ParseException {
        // login
        String token1 = jwt.login(USER1, PWD1);

        // do some action that needs login before mock
        assertTrue(jwt.hasRole(token1, ROLE1));
        Set<String> roleSet1 = new HashSet<>();
        roleSet1.add(ROLE1);
        assertEquals(roleSet1, jwt.myRoles(token1));

        /** Mock Clock.systemUTC().instant()
         *  JWTVerifier use Clock.systemUTC() to initialize instance variable clock, see JWTVerifier.java:272
         *  JWTVerifier use clock.instant() to get now, see JWTVerifier.java:337
         *  So we need to mock Clock.systemUTC() and clock.instant() to cheat JWTVerifier
         */
        // generate a fake time
        long fakeNow = new SimpleDateFormat("yyyy-MM-dd").parse("2030-08-01").getTime();
        // hijack Clock.class
        MockedStatic<Clock> clockMock = Mockito.mockStatic(Clock.class);
        // create a spy clock
        Clock spyClock = Mockito.spy(Clock.class);
        // when call Clock.systemUTC(), return spy clock
        clockMock.when(Clock::systemUTC).thenReturn(spyClock);
        // when call spy clock's instant() method, return fakeNow
        Mockito.when(spyClock.instant()).thenReturn(Instant.ofEpochMilli(fakeNow));
        // check whether mock success.
        assertEquals(fakeNow, Clock.systemUTC().instant().toEpochMilli());

        // can not do any action that needs login after mock
        assertThrows(TokenExpiredException.class, () -> jwt.hasRole(token1, ROLE1));
        assertThrows(TokenExpiredException.class, () -> jwt.myRoles(token1));

        clockMock.close();//release Clock.class
    }

    @AfterAll
    static void tearDownAll() {
        jwt.deleteUser(USER1);
        jwt.deleteUser(USER2);
        jwt.deleteRole(ROLE1);
        jwt.deleteRole(ROLE2);
    }
}
