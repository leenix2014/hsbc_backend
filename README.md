# hsbc_backend
hsbc homework of authentication and authorization  

mainly business:  
1、create/delete User;  
2、create/delete Role;  
3、add role to user;  
4、authenticate user by username and password, return a passed around token;【login】  
5、check if user has role;【needs login】  
6、query all roles for user;【needs login】  
7、invalidate token;【logout】  

file introduction:  
src/main/java/org.hsbc.api.IAuth, clean api of all demanded interfaces.  
src/main/java/org.hsbc.entity.Role, class of business entity role.  
src/main/java/org.hsbc.entity.User, class of business entity user.  
src/main/java/org.hsbc.util.PBKDF2Util, utility of algorithm PBKDF2 to sign user password.  
src/main/java/org.hsbc.util.StringUtil, utility of string, mainly purpose is to get a random string for password as salt.  
src/main/java/org.hsbc.JWTSolution, implementation of IAuth using method JWT.  
test/java/JWTSolutionTest, thoroughly test of JWTSolution including token expiry.  

important files:  
IAuth: tells us how the interfaces is designed;  
JWTSolution: gives the detail of implementation of IAuth;  
JWTSolutionTest: should be the entry point of readers;  

external libraries  
java-jwt: to generate jwt tokens.  
guava: cache tokens with expiry time to prevent memory leak, if possible, use redis instead  

external test libraries  
junit-jupiter: junit5 test library  
mockito-junit-jupiter: mock to make a fake "now"  
mockito-inline: if mocking method is static, this module is required  

Every method is commented, feel free to ask Leen Li, 17620373536 or zs08378022@126.com  