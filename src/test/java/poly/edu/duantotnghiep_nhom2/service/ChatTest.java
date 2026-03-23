package poly.edu.duantotnghiep_nhom2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import poly.edu.duantotnghiep_nhom2.entity.SupportMessage;
import poly.edu.duantotnghiep_nhom2.entity.User;
import poly.edu.duantotnghiep_nhom2.repository.SupportMessageRepository;
import poly.edu.duantotnghiep_nhom2.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatTest {

    @Mock
    private SupportMessageRepository supportMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupportMessageService supportMessageService;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        admin = new User();
        admin.setId(2L);
    }

    // =========================
    // 1. Send message success
    // =========================
    @Test
    void testSendMessage_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supportMessageRepository.save(any(SupportMessage.class)))
                .thenReturn(new SupportMessage());

        SupportMessage result = supportMessageService.sendMessage(1L, "Hello");

        assertNotNull(result);
        verify(supportMessageRepository).save(any(SupportMessage.class));
    }

    // =========================
    // 2. Send message - user not found
    // =========================
    @Test
    void testSendMessage_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () ->
                supportMessageService.sendMessage(1L, "Hello")
        );

        assertEquals("User not found", ex.getMessage());
    }

    // =========================
    // 3. Reply message success
    // =========================
    @Test
    void testReplyMessage_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(supportMessageRepository.save(any(SupportMessage.class)))
                .thenReturn(new SupportMessage());

        SupportMessage result = supportMessageService.replyMessage(1L, 2L, "Reply");

        assertNotNull(result);
        verify(supportMessageRepository).save(any(SupportMessage.class));
    }

    // =========================
    // 4. Reply message - user not found
    // =========================
    @Test
    void testReplyMessage_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                supportMessageService.replyMessage(1L, 2L, "Reply")
        );
    }

    // =========================
    // 5. Reply message - admin not found
    // =========================
    @Test
    void testReplyMessage_AdminNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                supportMessageService.replyMessage(1L, 2L, "Reply")
        );
    }

    // =========================
    // 6. Get messages by user
    // =========================
    @Test
    void testGetMessagesByUser() {
        when(supportMessageRepository.findByUserIdOrderByTimestampAsc(1L))
                .thenReturn(List.of(new SupportMessage(), new SupportMessage()));

        List<SupportMessage> result = supportMessageService.getMessagesByUser(1L);

        assertEquals(2, result.size());
    }

    // =========================
    // 7. Get users with support request
    // =========================
    @Test
    void testGetUsersWithSupportRequest() {
        when(supportMessageRepository.findUsersWithMessagesOrderByLatest())
                .thenReturn(List.of(user));

        List<User> result = supportMessageService.getUsersWithSupportRequest();

        assertEquals(1, result.size());
    }

    // =========================
    // 8. Mark as read
    // =========================
    @Test
    void testMarkAsRead() {
        doNothing().when(supportMessageRepository).markMessagesAsRead(1L);

        supportMessageService.markAsRead(1L);

        verify(supportMessageRepository).markMessagesAsRead(1L);
    }

    // =========================
    // 9. End support
    // =========================
    @Test
    void testEndSupport() {
        doNothing().when(supportMessageRepository).archiveMessages(1L);

        supportMessageService.endSupport(1L);

        verify(supportMessageRepository).archiveMessages(1L);
    }
}