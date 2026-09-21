package biz.craftline.server.feature.employeemanagement.api.controller;

import biz.craftline.server.config.mail.MailService;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeRequest;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.domain.service.EmployeeService;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock private EmployeeService employeeService;
    @Mock private UserService userService;
    @Mock private MailService mailService;

    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EmployeeController();
        ReflectionTestUtils.setField(controller, "employeeService", employeeService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "mailService", mailService);
    }

    private Employee employee(long id, String name) {
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        return e;
    }

    @Test
    void listEndpoints() {
        Employee e = employee(1L, "E");
        when(employeeService.getAllEmployees()).thenReturn(List.of(e));
        when(employeeService.getEmployeesByBusinessId(10L)).thenReturn(List.of(e));
        when(employeeService.getEmployeesByStoreId(1L)).thenReturn(List.of(e));

        assertEquals(1, controller.getAllEmployees().getBody().getData().size());
        assertEquals(1, controller.getAllEmployeesByBusiness(10L).getBody().getData().size());
        assertEquals(1, controller.getAllEmployeesByStore(1L).getBody().getData().size());
    }

    @Test
    void createEmployee_validation() {
        assertThrows(IllegalArgumentException.class, () -> controller.createEmployee(null));
        EmployeeRequest req = new EmployeeRequest();
        assertThrows(IllegalArgumentException.class, () -> controller.createEmployee(req));
    }

    @Test
    void createEmployee_withExistingUserId() {
        EmployeeRequest req = new EmployeeRequest();
        req.setBusinessId(10L);
        req.setUserId(5L);
        req.setName("Alice");
        Employee created = employee(1L, "Alice");
        created.setUserId(5L);
        when(employeeService.createEmployee(any())).thenReturn(created);

        assertNotNull(controller.createEmployee(req).getBody().getData());
    }

    @Test
    void createEmployee_createsUserFromEmail() {
        EmployeeRequest req = new EmployeeRequest();
        req.setStoreId(1L);
        req.setEmail("new@test.com");
        req.setFirstName("New");
        User user = new User();
        user.setId(7L);
        user.setEmail("new@test.com");
        when(userService.findUserIdByIdOrEmail(null, "new@test.com")).thenReturn(Optional.empty());
        when(userService.createUserWithHashedPassword(any())).thenReturn(user);
        doNothing().when(mailService).sendText(anyString(), anyString(), anyString());
        Employee created = employee(1L, "New");
        created.setUserId(7L);
        when(employeeService.createEmployee(any())).thenReturn(created);

        assertNotNull(controller.createEmployee(req).getBody().getData());
    }

    @Test
    void updateAndDelete() {
        EmployeeRequest req = new EmployeeRequest();
        req.setFirstName("A");
        req.setLastName("B");
        Employee updated = employee(1L, "A B");
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(updated);
        assertNotNull(controller.updateEmployee(1L, req).getBody().getData());

        controller.deleteEmployee(1L);
        verify(employeeService).deleteEmployee(1L);
    }
}
