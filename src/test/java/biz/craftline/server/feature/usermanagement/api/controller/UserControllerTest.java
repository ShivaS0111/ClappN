package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.UserCreateRequest;
import biz.craftline.server.feature.usermanagement.api.dto.UserUpdateRequest;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock private UserService userService;
    private UserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
    }

    private User user(long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFullName("User " + id);
        return u;
    }

    @Test
    void getAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(user(1L, "a@test.com")));
        assertEquals(1, controller.getAllUsers().getBody().getData().size());
    }

    @Test
    void getUserById_and_email() {
        User u = user(1L, "a@test.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(u));
        when(userService.getUserByEmail("a@test.com")).thenReturn(Optional.of(u));
        doNothing().when(userService).assertCanViewUser(u);

        assertNotNull(controller.getUserById(1L).getBody().getData());
        assertNotNull(controller.getUserByEmail("a@test.com").getBody().getData());
        assertThrows(EntityNotFoundException.class, () -> controller.getUserById(9L));
    }

    @Test
    void createUser_adminOnly() {
        when(userService.isCurrentUserSystemAdmin()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> controller.createUser(new UserCreateRequest()));

        when(userService.isCurrentUserSystemAdmin()).thenReturn(true);
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("new@test.com");
        when(userService.getUserByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userService.createUserWithHashedPassword(any())).thenReturn(user(2L, "new@test.com"));
        assertNotNull(controller.createUser(req).getBody().getData());
    }

    @Test
    void updateDeleteAssignRole() {
        User u = user(1L, "a@test.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(u));
        when(userService.updateUser(1L, u)).thenReturn(u);
        doNothing().when(userService).assertCanViewUser(u);
        doNothing().when(userService).deleteUser(1L);
        when(userService.assignRole(1L, 5L)).thenReturn(u);

        assertNotNull(controller.updateUser(1L, new UserUpdateRequest()).getBody().getData());
        controller.deleteUser(1L);
        verify(userService).deleteUser(1L);
        assertNotNull(controller.assignRole(1L, 5L).getBody().getData());
    }
}
