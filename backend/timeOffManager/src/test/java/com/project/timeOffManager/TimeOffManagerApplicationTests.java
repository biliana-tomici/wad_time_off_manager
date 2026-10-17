package com.project.timeOffManager;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.project.timeOffManager.dto.BalanceDto;
import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateBalanceCommand;
import com.project.timeOffManager.dto.command.CreateProjectCommand;
import com.project.timeOffManager.dto.command.CreateRequestCommand;
import com.project.timeOffManager.dto.command.CreateUserCommand;
import com.project.timeOffManager.dto.command.UpdateBalanceCommand;
import com.project.timeOffManager.dto.command.UpdateProjectCommand;
import com.project.timeOffManager.dto.command.UpdateRequestCommand;
import com.project.timeOffManager.dto.command.UpdateUserCommand;
import com.project.timeOffManager.enums.Role;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.exception.AccessDeniedException;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.service.BalanceService;
import com.project.timeOffManager.service.ProjectService;
import com.project.timeOffManager.service.ReportService;
import com.project.timeOffManager.service.RequestService;
import com.project.timeOffManager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TimeOffManagerApplicationTests {

	@Autowired
	private UserService userService;

	@Autowired
	private BalanceService balanceService;

	@Autowired
	private RequestService requestService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ReportService reportService;

	private void asUser(long userId, Runnable action) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-User-Id", String.valueOf(userId));
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		try {
			action.run();
		} finally {
			RequestContextHolder.resetRequestAttributes();
		}
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testGetAllUsers_DoesNotThrowLazyInitializationException() {
		List<UserDto> users = userService.getAllUsers();
		assertNotNull(users);
		assertEquals(6, users.size());
		// Verify project lists can be read
		for (UserDto user : users) {
			assertNotNull(user.projects());
		}
	}

	@Test
	void testGetUserById_LoadsProjects() {
		asUser(1L, () -> {
			UserDto user = userService.getUserById(1L);
			assertNotNull(user);
			assertNotNull(user.projects());
		});
	}

	@Test
	void module1_balancesReadOnlyForEmployeeAndManager() {
		// User 2 (Bob, MANAGER) and User 5 (Eve, EMPLOYEE) cannot create or delete balances
		asUser(2L, () -> {
			assertThrows(AccessDeniedException.class, () ->
					balanceService.createBalance(new CreateBalanceCommand(2L, "Bonus", 5L, 5L)));
			assertThrows(AccessDeniedException.class, () ->
					balanceService.deleteBalance(2L));
		});
		asUser(5L, () -> {
			assertThrows(AccessDeniedException.class, () ->
					balanceService.createBalance(new CreateBalanceCommand(5L, "Bonus", 5L, 5L)));
			assertThrows(AccessDeniedException.class, () ->
					balanceService.deleteBalance(5L));
		});
	}

	@Test
	void module1_editedRequestStaysInManagerQueueAndApprovalDeductsDays() {
		// Request 1 belongs to Bob (id=2), managed by Alice (id=1), on Balance 2 (Sick Leave, 5 daysLeft)
		asUser(2L, () -> {
			RequestDto edited = requestService.updateRequest(
					new UpdateRequestCommand(null, null, null, "Updated explanation"), 1L);
			assertEquals(Status.EDITED, edited.status());
		});

		// Alice's pending queue still contains the EDITED request (2.1.2)
		asUser(1L, () -> {
			Page<RequestDto> queue = requestService.getPendingRequestsByManagerId(1L, PageRequest.of(0, 50));
			assertTrue(queue.getContent().stream().anyMatch(r -> r.id().equals(1L) && r.status() == Status.EDITED));

			RequestDto before = requestService.getRequestById(1L);
			long expectedWorkingDays = requestService.calculateWorkingDays(before.startDate(), before.endDate());
			BalanceDto balanceBefore = balanceService.getBalanceById(2L);

			requestService.updateStatus(1L, Status.APPROVED);

			BalanceDto balanceAfter = balanceService.getBalanceById(2L);
			assertEquals(balanceBefore.daysLeft() - expectedWorkingDays, balanceAfter.daysLeft());
		});
	}

	@Test
	void module2_bobCanSubmitSeveralRequestsOnSameAllowanceAndValidationRulesAreEnforced() {
		LocalDate futureMonday = LocalDate.now().plusDays(60).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

		asUser(2L, () -> {
			// Bob submits two separate non-overlapping requests on balance 7 (Vacation, 15 daysLeft)
			RequestDto r1 = requestService.createRequest(
					new CreateRequestCommand(2L, 7L, futureMonday, futureMonday.plusDays(1), "Short break 1"));
			assertEquals(2L, r1.workingDays());

			RequestDto r2 = requestService.createRequest(
					new CreateRequestCommand(2L, 7L, futureMonday.plusDays(7), futureMonday.plusDays(8), "Short break 2"));
			assertEquals(2L, r2.workingDays());

			// 1. end < start
			BusinessRuleException rangeEx = assertThrows(BusinessRuleException.class, () ->
					requestService.createRequest(new CreateRequestCommand(2L, 7L, futureMonday.plusDays(15), futureMonday.plusDays(14), "Invalid range")));
			assertTrue(rangeEx.getMessage().contains("End date cannot be before start date"));

			// 2. start in the past for non-admin
			BusinessRuleException pastEx = assertThrows(BusinessRuleException.class, () ->
					requestService.createRequest(new CreateRequestCommand(2L, 7L, LocalDate.now().minusDays(3), LocalDate.now().minusDays(2), "Past")));
			assertTrue(pastEx.getMessage().contains("Start date cannot be in the past"));

			// 3. balance belongs to another employee (balance 1 belongs to Alice)
			BusinessRuleException ownerEx = assertThrows(BusinessRuleException.class, () ->
					requestService.createRequest(new CreateRequestCommand(2L, 1L, futureMonday.plusDays(14), futureMonday.plusDays(15), "Wrong balance")));
			assertTrue(ownerEx.getMessage().contains("Balance does not belong to employee"));

			// 4. overlap with existing request
			BusinessRuleException overlapEx = assertThrows(BusinessRuleException.class, () ->
					requestService.createRequest(new CreateRequestCommand(2L, 7L, futureMonday, futureMonday.plusDays(2), "Overlap")));
			assertTrue(overlapEx.getMessage().contains("overlaps with existing request"));

			// 5. requested days > remaining - pending
			BusinessRuleException quotaEx = assertThrows(BusinessRuleException.class, () ->
					requestService.createRequest(new CreateRequestCommand(2L, 7L, futureMonday.plusDays(14), futureMonday.plusDays(30), "Too long")));
			assertTrue(quotaEx.getMessage().contains("Insufficient balance days"));
		});

		// Admin (Alice, id=1) can submit a backdated request
		asUser(1L, () -> {
			LocalDate pastMonday = LocalDate.now().minusDays(30).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			RequestDto adminBackdated = requestService.createRequest(
					new CreateRequestCommand(1L, 1L, pastMonday, pastMonday.plusDays(1), "Backdated by admin"));
			assertEquals(2L, adminBackdated.workingDays());
		});
	}

	@Test
	void module3_stateMachineApproveCancelRestoresDaysAndApprovedCannotBeEditedOrDeletedByOwner() {
		LocalDate futureMonday = LocalDate.now().plusDays(75).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

		// Bob (id=2) creates a 2-working-day request on balance 7 (15 daysLeft)
		final Long[] reqIdHolder = new Long[1];
		asUser(2L, () -> {
			RequestDto created = requestService.createRequest(
					new CreateRequestCommand(2L, 7L, futureMonday, futureMonday.plusDays(1), "Initial"));
			reqIdHolder[0] = created.id();

			// Edit re-runs validations (2.3.3): end < start rejected
			assertThrows(BusinessRuleException.class, () ->
					requestService.updateRequest(new UpdateRequestCommand(null, futureMonday.plusDays(5), futureMonday.plusDays(2), "Bad dates"), created.id()));

			// Valid edit turns status into EDITED
			RequestDto edited = requestService.updateRequest(
					new UpdateRequestCommand(null, futureMonday, futureMonday.plusDays(2), "Extended to 3 days"), created.id());
			assertEquals(Status.EDITED, edited.status());
			assertEquals(3L, edited.workingDays());
		});

		Long reqId = reqIdHolder[0];

		// Alice (id=1, Bob's manager) sees EDITED request in queue and approves it -> deducts 3 days (15 -> 12)
		asUser(1L, () -> {
			Page<RequestDto> queue = requestService.getPendingRequestsByManagerId(1L, PageRequest.of(0, 50));
			assertTrue(queue.getContent().stream().anyMatch(r -> r.id().equals(reqId) && r.status() == Status.EDITED));

			RequestDto approved = requestService.approveRequest(reqId, "Looks good");
			assertEquals(Status.APPROVED, approved.status());
			assertEquals("Looks good", approved.decisionComment());
			assertEquals(12L, balanceService.getBalanceById(7L).daysLeft());
		});

		// Bob cannot edit or delete an APPROVED request (409 BusinessRuleException), but can cancel it before start date to restore days (12 -> 15)
		asUser(2L, () -> {
			assertThrows(BusinessRuleException.class, () ->
					requestService.updateRequest(new UpdateRequestCommand(null, null, null, "Try edit approved"), reqId));
			assertThrows(BusinessRuleException.class, () ->
					requestService.deleteRequest(reqId));

			RequestDto cancelled = requestService.cancelRequest(reqId);
			assertEquals(Status.CANCELLED, cancelled.status());
			assertEquals(15L, balanceService.getBalanceById(7L).daysLeft());

			// Cannot cancel an already CANCELLED request
			assertThrows(BusinessRuleException.class, () ->
					requestService.cancelRequest(reqId));
		});
	}

	@Test
	void module4_projectsRoleScopingMembershipAndPdfReportAvailability() throws Exception {
		// 2.4.4 Role scoping: Admin sees all 4 seeded projects; Bob (Manager, id=2) sees only Project Beta (id=2); Eve (Employee, id=5) sees assigned projects (1 and 4)
		asUser(1L, () -> assertEquals(4, projectService.getAllProjects(PageRequest.of(0, 50)).getTotalElements()));
		asUser(2L, () -> {
			List<ProjectDto> bobProjects = projectService.getAllProjects(PageRequest.of(0, 50)).getContent();
			assertEquals(1, bobProjects.size());
			assertEquals("Project Beta", bobProjects.get(0).name());
		});
		asUser(5L, () -> {
			List<ProjectDto> eveProjects = projectService.getAllProjects(PageRequest.of(0, 50)).getContent();
			assertEquals(2, eveProjects.size());
		});

		// Accept criteria: New project -> add 3 members -> approve leave for one covering today -> PDF shows 2 available
		// Note: Charlie (id=3) already has seeded APPROVED request #2 covering today (CURRENT_DATE - 2 to CURRENT_DATE + 2),
		// while Bob (id=2) and Eve (id=5) have no approved leave covering today.
		asUser(1L, () -> {
			ProjectDto created = projectService.createProject(new CreateProjectCommand("Project Omega", 2L, 2L));
			Long projectId = created.id();

			// 2.4.1 Edit name, minEmp, manager
			ProjectDto updated = projectService.updateProject(new UpdateProjectCommand("Project Omega Prime", 3L, 1L), projectId);
			assertEquals("Project Omega Prime", updated.name());
			assertEquals(3L, updated.minEmp());
			assertEquals(1L, updated.manager().id());

			// 2.4.2 Add 3 members: Bob (2), Charlie (3 - on approved leave covering today), Eve (5)
			assertTrue(projectService.addMember(projectId, 2L));
			assertTrue(projectService.addMember(projectId, 3L));
			assertTrue(projectService.addMember(projectId, 5L));
			assertEquals(3, projectService.getProjectMembers(projectId).size());

			// 2.4.3 Generate PDF report and verify 3 total employees and 2 available today
			byte[] pdf = reportService.getProjectReportPdf(projectId, TimePeriod.WEEKLY);
			assertNotNull(pdf);
			try (PdfReader reader = new PdfReader(pdf)) {
				String text = new PdfTextExtractor(reader).getTextFromPage(1);
				assertTrue(text.contains("Project Omega Prime"));
				assertTrue(text.contains("Total Employees: 3"));
				assertTrue(text.contains("Total available employees as of today: 2"));
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Test
	void module5_approvalsUnassignedCoverageWarningsAndAdminUserBalanceCrud() {
		LocalDate futureMonday = LocalDate.now().plusDays(90).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

		// 2.5.1 & D12: Alice (id=1, no manager) submits a request -> appears in Admin's pending queue
		final Long[] aliceReqId = new Long[1];
		asUser(1L, () -> {
			RequestDto created = requestService.createRequest(
					new CreateRequestCommand(1L, 1L, futureMonday, futureMonday.plusDays(1), "Top-level leave"));
			aliceReqId[0] = created.id();

			List<RequestDto> adminQueue = requestService.getPendingRequestsByManagerId(1L, PageRequest.of(0, 50)).getContent();
			assertTrue(adminQueue.stream().anyMatch(r -> r.id().equals(aliceReqId[0])),
					"Admin queue must include requests of users without a manager (D12)");

			// 2.5.2 (C6): Bob's pending request #1 in Alice's queue warns about Project Beta (2 of 3 required)
			RequestDto bobReq1 = adminQueue.stream().filter(r -> r.id().equals(1L)).findFirst().orElseThrow();
			assertNotNull(bobReq1.coverageWarnings());
			assertTrue(bobReq1.coverageWarnings().stream()
					.anyMatch(w -> w.contains("Approving leaves Project Beta with 2 of 3 required")));
		});

		// Accept criteria: Manager (Bob, id=2) cannot approve non-reports (e.g. Eve's request #4, managed by Charlie) -> 403
		asUser(2L, () -> {
			assertThrows(AccessDeniedException.class, () ->
					requestService.approveRequest(4L, "Unauthorized approval"));
			assertThrows(AccessDeniedException.class, () ->
					requestService.approveRequest(aliceReqId[0], "Cannot approve unassigned user"));
		});

		// 2.5.3 (5b Administration): User create/edit, soft-delete (D11/C11), and Balance CRUD (D9, D10, D18)
		asUser(1L, () -> {
			UserDto newUser = userService.createUser(new CreateUserCommand("Grace", "Hopper", Role.MANAGER, 1L));
			assertEquals(Role.MANAGER, newUser.role());
			assertEquals(1L, newUser.manager().id());

			// Edit user name, role, and clear manager
			UserDto editedUser = userService.updateUser(
					new UpdateUserCommand("Grace", "Hopper-Prime", Role.ADMIN, null, true), newUser.id());
			assertEquals("Hopper-Prime", editedUser.lastName());
			assertEquals(Role.ADMIN, editedUser.role());
			assertNull(editedUser.manager());

			// Partial balance update preserves employee and omitted fields (D9), and validates daysLeft <= maxDays (D18)
			BalanceDto partialBalance = balanceService.updateBalance(2L, new UpdateBalanceCommand(null, 4L, null));
			assertEquals(2L, partialBalance.employeeId());
			assertEquals("Sick Leave", partialBalance.name());
			assertEquals(4L, partialBalance.daysLeft());
			assertEquals(10L, partialBalance.maxDays());

			assertThrows(ResourceNotFoundException.class, () ->
					balanceService.updateBalance(999L, new UpdateBalanceCommand("Ghost", 5L, 10L)));
			assertThrows(BusinessRuleException.class, () ->
					balanceService.updateBalance(2L, new UpdateBalanceCommand(null, 25L, 10L)));

			// Delete balance that has requests (D10) succeeds without 500
			assertTrue(balanceService.deleteBalance(2L));

			// Soft-delete Bob (id=2, who manages people, manages Project Beta, and decided requests) -> no 500 FK error (D11/C11)
			assertTrue(userService.deleteUser(2L));
			assertFalse(userService.getAllUsers().stream().anyMatch(u -> u.id().equals(2L)));
			// Historical requests of Bob remain accessible to Admin
			assertFalse(requestService.getRequestsByEmployeeId(2L, PageRequest.of(0, 50)).isEmpty());
		});
	}
}
