import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { CalendarComponent } from './calendar.component';
import { testProviders } from '../../testing/test-providers';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { CalendarResponse } from '../../models';

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let api: ApiService;
  let session: SessionService;

  const mockResponse: CalendarResponse = {
    from: '2026-10-01',
    to: '2026-10-31',
    absences: [
      {
        requestId: 101,
        employeeId: 2,
        employeeName: 'Bob Johnson',
        balanceId: 1,
        balanceName: 'Vacation',
        status: 'APPROVED',
        startDate: '2026-10-15',
        endDate: '2026-10-18',
        workingDays: 2,
        explanation: 'Autumn break',
        projectIds: [2]
      },
      {
        requestId: 102,
        employeeId: 3,
        employeeName: 'Charlie Williams',
        balanceId: 2,
        balanceName: 'Sick Leave',
        status: 'PENDING',
        startDate: '2026-10-20',
        endDate: '2026-10-22',
        workingDays: 3,
        explanation: 'Doctor appointment',
        projectIds: [2]
      }
    ],
    coverages: [
      {
        date: '2026-10-15',
        projectId: 2,
        projectName: 'Project Beta',
        minEmp: 3,
        totalMembers: 3,
        availableCount: 2,
        understaffed: true
      }
    ]
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: testProviders,
    }).compileComponents();

    api = TestBed.inject(ApiService);
    session = TestBed.inject(SessionService);
    session.users.set([
      { id: 1, firstName: 'Alice', lastName: 'Smith', role: 'ADMIN', manager: null },
      { id: 2, firstName: 'Bob', lastName: 'Johnson', role: 'MANAGER', manager: { id: 1, firstName: 'Alice', lastName: 'Smith' } }
    ]);
    session.current.set(session.users()[0]);

    spyOn(api, 'projects').and.returnValue(of([
      { id: 2, minEmp: 3, name: 'Project Beta', manager: null }
    ]));
    spyOn(api, 'calendar').and.returnValue(of(mockResponse));

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('renders month grid, absences, and coverage alerts', () => {
    component.currentYear.set(2026);
    component.currentMonth.set(9); // October (0-indexed 9)
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('October 2026');
    expect(text).toContain('Bob Johnson');

    // Day 15 should have understaffed warning
    const day15 = component.gridDays().find((d) => d.isoString === '2026-10-15');
    expect(day15).toBeDefined();
    expect(day15?.understaffed).toBeTrue();
    expect(day15?.absences.length).toBe(1);

    // Clicking day 15 opens the detail view
    component.selectDay(day15!);
    fixture.detectChanges();

    const modalText = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(modalText).toContain('Project Staffing Coverage');
    expect(modalText).toContain('Understaffed');
    expect(modalText).toContain('Autumn break');
  });

  it('navigates between months', () => {
    component.currentYear.set(2026);
    component.currentMonth.set(9); // October
    fixture.detectChanges();

    component.nextMonth();
    expect(component.currentMonth()).toBe(10); // November

    component.prevMonth();
    expect(component.currentMonth()).toBe(9); // October
  });

  it('greys out weekend cells and does not count leaves on weekends', () => {
    component.currentYear.set(2026);
    component.currentMonth.set(9); // October
    fixture.detectChanges();

    const sat = component.gridDays().find((d) => d.isoString === '2026-10-17');
    const sun = component.gridDays().find((d) => d.isoString === '2026-10-18');

    expect(sat?.isWeekend).toBeTrue();
    expect(sun?.isWeekend).toBeTrue();
    expect(sat?.absences.length).toBe(0);
    expect(sun?.absences.length).toBe(0);
    expect(sat?.understaffed).toBeFalse();
    expect(sun?.understaffed).toBeFalse();

    const el: HTMLElement = fixture.nativeElement;
    const weekendCells = el.querySelectorAll('.calendar-day-cell.weekend');
    expect(weekendCells.length).toBeGreaterThanOrEqual(8);
  });
});
