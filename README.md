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
src/main/java/IAuth: clean api of all demanded interfaces.  
src/main/java/Role: class of business entity role.  
src/main/java/User: class of business entity user.  
src/main/java/PasswordUtil: use spring security crypto to encode password.    
src/main/java/StringUtil: utility of string.  
src/main/java/JWTSolution: implementation of IAuth using method JWT.  
src/test/java/JWTSolutionTest: thoroughly test of JWTSolution including token expiry.  

important files:  
IAuth: tells us how the interfaces is designed;  
JWTSolution: gives the detail of implementation of IAuth;  
JWTSolutionTest: should be the entry point of readers;  

external libraries  
java-jwt: to generate jwt tokens.  
guava: cache tokens with expiry time to prevent memory leak, if possible, use redis instead  
spring-security-crypto: encode password in default algorithm bcrypt.
commons-logging: spring-security-crypto depends on it.  

external test libraries  
junit-jupiter: junit5 test library  
mockito-junit-jupiter: mock to make a fake "now"  
mockito-inline: if mocking method is static, this module is required  

Every method is commented, feel free to ask Leen Li, 17620373536 or zs08378022@126.com  