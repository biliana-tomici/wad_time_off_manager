package com.project.timeOffManager.security;

import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Role;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.exception.AccessDeniedException;
import com.project.timeOffManager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AuthorizationService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    public AuthorizationService(CurrentUserProvider currentUserProvider, UserRepository userRepository) {
        this.currentUserProvider = currentUserProvider;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        return currentUserProvider.requireCurrentUser();
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public boolean isManager(User user) {
        return user != null && user.getRole() == Role.MANAGER;
    }

    public boolean isEmployee(User user) {
        return user != null && user.getRole() == Role.EMPLOYEE;
    }

    public boolean isDirectReport(User manager, User employee) {
        if (manager == null || employee == null || employee.getManager() == null) {
            return false;
        }
        return manager.getId().equals(employee.getManager().getId());
    }

    public void checkCanViewBalances(Long targetEmployeeId) {
        User current = getCurrentUser();
        if (isAdmin(current) || current.getId().equals(targetEmployeeId)) {
            return;
        }
        if (isManager(current)) {
            User target = userRepository.findCustomerById(targetEmployeeId);
            if (target != null && isDirectReport(current, target)) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to view balances for employee ID " + targetEmployeeId);
    }

    public void checkCanViewBalance(Balance balance) {
        if (balance == null || balance.getEmployee() == null) {
            return;
        }
        checkCanViewBalances(balance.getEmployee().getId());
    }

    public void checkCanManageBalances() {
        User current = getCurrentUser();
        if (!isAdmin(current)) {
            throw new AccessDeniedException("Only administrators can manage allowances/balances");
        }
    }

    public void checkCanViewRequests(Long targetEmployeeId) {
        User current = getCurrentUser();
        if (isAdmin(current) || current.getId().equals(targetEmployeeId)) {
            return;
        }
        if (isManager(current)) {
            User target = userRepository.findCustomerById(targetEmployeeId);
            if (target != null && isDirectReport(current, target)) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to view requests for employee ID " + targetEmployeeId);
    }

    public void checkCanViewRequest(Request request) {
        if (request == null || request.getEmployee() == null) {
            return;
        }
        checkCanViewRequests(request.getEmployee().getId());
    }

    public void checkCanViewPendingQueue(Long managerId, boolean includeUnassigned) {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (!current.getId().equals(managerId)) {
            throw new AccessDeniedException("Managers can only view their own pending queue");
        }
        if (includeUnassigned) {
            throw new AccessDeniedException("Only administrators can view unassigned requests");
        }
    }

    public void checkCanCreateRequest(Long employeeId) {
        User current = getCurrentUser();
        if (!current.getId().equals(employeeId) && !isAdmin(current)) {
            throw new AccessDeniedException("Users can only create requests for themselves");
        }
    }

    public void checkCanEditRequest(Request request) {
        User current = getCurrentUser();
        if (request.getEmployee() == null || !current.getId().equals(request.getEmployee().getId())) {
            throw new AccessDeniedException("Only the request owner can edit this request");
        }
    }

    public void checkCanChangeStatus(Request request, Status targetStatus) {
        User current = getCurrentUser();
        User owner = request.getEmployee();
        boolean isOwner = owner != null && current.getId().equals(owner.getId());
        boolean currentIsAdmin = isAdmin(current);
        boolean isManagerOfOwner = owner != null && isDirectReport(current, owner);

        if (targetStatus == Status.CANCELLED) {
            if (request.getStatus() == Status.PENDING || request.getStatus() == Status.EDITED) {
                if (isOwner || currentIsAdmin) {
                    return;
                }
                throw new AccessDeniedException("Only the owner or an admin can cancel a pending request");
            }
            if (request.getStatus() == Status.APPROVED) {
                LocalDate today = LocalDate.now();
                if (isOwner && !today.isAfter(request.getStartDate())) {
                    return;
                }
                if (isManagerOfOwner || currentIsAdmin) {
                    return;
                }
                throw new AccessDeniedException("Cannot cancel approved request: must be before start date for owner, or performed by manager/admin");
            }
            throw new AccessDeniedException("Cannot cancel request in status " + request.getStatus());
        }

        if (targetStatus == Status.APPROVED || targetStatus == Status.REJECTED) {
            if (isOwner && !currentIsAdmin) {
                throw new AccessDeniedException("Self-approval is not permitted");
            }
            if (currentIsAdmin) {
                return;
            }
            if (owner != null && owner.getManager() == null) {
                throw new AccessDeniedException("Only administrators can decide requests for users without a manager");
            }
            if (isManagerOfOwner) {
                return;
            }
            throw new AccessDeniedException("Only the direct manager or an administrator can approve or reject this request");
        }

        throw new AccessDeniedException("Unsupported status transition target: " + targetStatus);
    }

    public void checkCanDeleteRequest(Request request) {
        User current = getCurrentUser();
        User owner = request.getEmployee();
        boolean isOwner = owner != null && current.getId().equals(owner.getId());

        if (isAdmin(current)) {
            return;
        }
        if (isOwner && (request.getStatus() == Status.PENDING || request.getStatus() == Status.EDITED)) {
            return;
        }
        throw new AccessDeniedException("Only the owner (while pending/edited) or an administrator can delete a request");
    }

    public void checkCanViewProject(Project project) {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (project.getManager() != null && project.getManager().getId().equals(current.getId())) {
            return;
        }
        if (project.getEmployees() != null && project.getEmployees().stream().anyMatch(e -> e.getId().equals(current.getId()))) {
            return;
        }
        throw new AccessDeniedException("Not authorized to view project ID " + project.getId());
    }

    public void checkCanCreateProject(Project project) {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (isManager(current)) {
            if (project.getManager() == null || project.getManager().getId().equals(current.getId())) {
                project.setManager(current);
                return;
            }
        }
        throw new AccessDeniedException("Only administrators and managers can create projects");
    }

    public void checkCanManageProject(Project project) {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (isManager(current) && project.getManager() != null && project.getManager().getId().equals(current.getId())) {
            return;
        }
        throw new AccessDeniedException("Only the project manager or an administrator can manage this project");
    }

    public void checkCanDeleteProject(Project project) {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (isManager(current) && project.getManager() != null && project.getManager().getId().equals(current.getId())) {
            return;
        }
        throw new AccessDeniedException("Only the project manager or an administrator can delete this project");
    }

    public void checkCanDownloadReport(Project project) {
        User current = getCurrentUser();
        if (isAdmin(current)) {
            return;
        }
        if (isManager(current) && project.getManager() != null && project.getManager().getId().equals(current.getId())) {
            return;
        }
        throw new AccessDeniedException("Only the project manager or an administrator can download project reports");
    }

    public void checkCanViewUser(Long targetUserId) {
        User current = getCurrentUser();
        if (isAdmin(current) || current.getId().equals(targetUserId)) {
            return;
        }
        if (isManager(current)) {
            User target = userRepository.findCustomerById(targetUserId);
            if (target != null && isDirectReport(current, target)) {
                return;
            }
        }
        throw new AccessDeniedException("Not authorized to view user ID " + targetUserId);
    }

    public void checkCanManageUsers() {
        User current = getCurrentUser();
        if (!isAdmin(current)) {
            throw new AccessDeniedException("Only administrators can manage users");
        }
    }
}
