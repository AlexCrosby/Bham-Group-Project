package test;

import client.Client;
import client.CreateAccountController;
import client.Ranking;
import messaging.Command;
import messaging.Message;
import org.junit.jupiter.api.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientTest {

    static Client client;
    static Thread server;
    CreateAccountController createAccountController;
    Ranking ranking;

    @BeforeAll
    static void init() {
        server = new DummyServer();
        server.start();
        client = new Client();
    }

    @BeforeEach
    public void setup() {
        createAccountController = new CreateAccountController();
    }

    @Order(1)
    @Test
    void testLogin() {
        assertTrue(client.login("User", "Password1234!"));
        Message m = ((DummyServer) server).getMessage();
        assertEquals(m.getCommand(), Command.LOGIN);
        assertArrayEquals(m.getData(), new String[]{"User", "Password1234!"});
        assertEquals("User", client.getUsername());
    }

    @Order(2)
    @Test
    void testReadConfig() {
        client.readConfig();
        assertEquals(50000, client.getPort());
        assertEquals("127.0.0.1", client.getHost());
    }

    @Order(3)
    @Test
    void testSendMessage() throws InterruptedException {
        client.sendMessage(Command.CHAT_MESSAGE_FROM_CLIENT, "Hello World");
        Thread.sleep(200);
        Message m = ((DummyServer) server).getMessage();
        assertEquals(Command.CHAT_MESSAGE_FROM_CLIENT, m.getCommand());
        assertArrayEquals(m.getData(), new String[]{"Hello World"});
    }

    @Order(4)
    @Test
    void testSendMessagePath() {
        fail("Not yet implemented");
    }

    @Order(5)
    @Test
    void testCreateAccountValidation() {
        String usernameValid = "Jamie";
        String usernameTooShort = "Bob";
        String usernameTooLong = "AlexIsTheBest2000";
        String usernameInvalidChar = "Adam#";
        String passwordValid = "Oliver2000!";
        String passwordValid2 = "Oliver2001!";
        String passwordTooShort = "Max2019";
        String passwordTooLong = "ThisIsAReallyLongPassword123456789";
        String passwordNoUpper = "richard65";
        String passwordNoLower = "05BETHANY01";
        String passwordNoNumber = "SecurePassword";
        String emailValid = "AXC1153@student.bham.ac.uk";
        String emailInvalidUser = "sop%hie@gmail.com";
        String emailInvalidDomain = "sop%hie@gm%ail.com";
        String emailInvalidTLD = "sophie@gmail.commercial";
        String emailInvalidFormat = "pineapple";
        assertTrue(createAccountController.validateDetails(usernameValid, emailValid, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameTooShort, emailValid, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameTooLong, emailValid, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameInvalidChar, emailValid, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameValid, emailValid, passwordValid, passwordValid2));
        assertFalse(createAccountController.validateDetails(usernameValid, emailValid, passwordTooShort, passwordTooShort));
        assertFalse(createAccountController.validateDetails(usernameValid, emailValid, passwordTooLong, passwordTooLong));
        assertFalse(createAccountController.validateDetails(usernameValid, emailValid, passwordNoUpper, passwordNoUpper));
        assertFalse(createAccountController.validateDetails(usernameValid, emailValid, passwordNoLower, passwordNoLower));
        assertFalse(createAccountController.validateDetails(usernameValid, emailValid, passwordNoNumber, passwordNoNumber));
        assertFalse(createAccountController.validateDetails(usernameValid, emailInvalidUser, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameValid, emailInvalidDomain, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameValid, emailInvalidTLD, passwordValid, passwordValid));
        assertFalse(createAccountController.validateDetails(usernameValid, emailInvalidFormat, passwordValid, passwordValid));
    }

    @Order(6)
    @Test
    public void testRanking() {
        String input = "14:Alex:451:8";
        ranking = new Ranking(input.split(":"));
        assertEquals(14, ranking.getRank());
        assertEquals("Alex", ranking.getUsername());
        assertEquals(451, ranking.getScore());
        assertEquals(8, ranking.getWins());
    }

    @Order(7)
    @Test
    void testDisconnect() {
        client.disconnect();
        assertThrows(IOException.class, () -> {
            client.getInput().read();
        });
    }

}
