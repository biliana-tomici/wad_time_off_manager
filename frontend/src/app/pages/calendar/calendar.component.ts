import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { CalendarAbsence, CalendarProjectCoverage, CalendarResponse, Project } from '../../models';
import { errorText, fullName, prettyDate, statusLabel, workingDays } from '../../format';

export interface CalendarGridDay {
  date: Date;
  isoString: string;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isWeekend: boolean;
  absences: CalendarAbsence[];
  coverages: CalendarProjectCoverage[];
  understaffed: boolean;
}

function toIso(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss'
})
export class CalendarComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);

  readonly currentYear = signal(new Date().getFullYear());
  readonly currentMonth = signal(new Date().getMonth()); // 0-indexed
  readonly selectedProjectId = signal<number | null>(null);

  readonly projects = signal<Project[]>([]);
  readonly calendarData = signal<CalendarResponse | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly notice = signal('');

  readonly selectedDayIso = signal<string | null>(null);

  readonly fullName = fullName;
  readonly prettyDate = prettyDate;
  readonly statusLabel = statusLabel;
  readonly workingDays = workingDays;

  readonly monthTitle = computed(() => {
    return `${MONTH_NAMES[this.currentMonth()]} ${this.currentYear()}`;
  });

  readonly gridDays = computed<CalendarGridDay[]>(() => {
    const year = this.currentYear();
    const month = this.currentMonth();
    const firstDay = new Date(year, month, 1);
    const dayOfWeek = firstDay.getDay(); // 0 is Sunday, 1 is Monday ... 6 is Saturday
    const diffToMonday = (dayOfWeek + 6) % 7;

    const todayIso = toIso(new Date());
    const data = this.calendarData();
    const absences = data?.absences ?? [];
    const coverages = data?.coverages ?? [];

    const days: CalendarGridDay[] = [];
    const startDate = new Date(year, month, 1 - diffToMonday);

    // Build 35 or 42 cells (5 or 6 weeks)
    for (let i = 0; i < 42; i++) {
      const d = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate() + i);
      // If we are at day 35 and already past the current month, stop at 35 days
      if (i === 35 && d.getMonth() !== month) {
        break;
      }

      const iso = toIso(d);
      const isCurrentMonth = d.getMonth() === month;
      const isToday = iso === todayIso;
      const dayOfWeek = d.getDay(); // 0 is Sunday, 6 is Saturday
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;

      // System rule: Leave is not counted on weekends even if a request spans Fri-Mon
      const dayAbsences = isWeekend ? [] : absences.filter((a) => a.startDate <= iso && a.endDate >= iso);
      const dayCoverages = isWeekend ? [] : coverages.filter((c) => c.date === iso);
      const understaffed = !isWeekend && dayCoverages.some((c) => c.understaffed);

      days.push({
        date: d,
        isoString: iso,
        dayNumber: d.getDate(),
        isCurrentMonth,
        isToday,
        isWeekend,
        absences: dayAbsences,
        coverages: dayCoverages,
        understaffed
      });
    }

    return days;
  });

  readonly selectedDayData = computed(() => {
    const iso = this.selectedDayIso();
    if (!iso) return null;
    return this.gridDays().find((d) => d.isoString === iso) ?? null;
  });

  constructor() {
    effect(() => {
      // Reload projects and calendar when user session changes
      const current = this.session.current();
      if (!current) {
        this.projects.set([]);
        this.calendarData.set(null);
        return;
      }
      this.loadProjects();
      this.loadCalendar();
    }, { allowSignalWrites: true });
  }

  loadProjects(): void {
    this.api.projects().subscribe({
      next: (list) => this.projects.set(list),
      error: () => this.projects.set([])
    });
  }

  loadCalendar(): void {
    const year = this.currentYear();
    const month = this.currentMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    // Expand window slightly to include grid padding days
    const dayOfWeek = firstDay.getDay();
    const diffToMonday = (dayOfWeek + 6) % 7;
    const windowStart = new Date(year, month, 1 - diffToMonday);
    const windowEnd = new Date(windowStart.getFullYear(), windowStart.getMonth(), windowStart.getDate() + 41);

    const from = toIso(windowStart);
    const to = toIso(windowEnd);
    const projectId = this.selectedProjectId() ?? undefined;

    this.loading.set(true);
    this.error.set('');
    this.api.calendar(from, to, projectId).subscribe({
      next: (res) => {
        this.calendarData.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loading.set(false);
      }
    });
  }

  prevMonth(): void {
    let m = this.currentMonth() - 1;
    let y = this.currentYear();
    if (m < 0) {
      m = 11;
      y--;
    }
    this.currentMonth.set(m);
    this.currentYear.set(y);
    this.selectedDayIso.set(null);
    this.loadCalendar();
  }

  nextMonth(): void {
    let m = this.currentMonth() + 1;
    let y = this.currentYear();
    if (m > 11) {
      m = 0;
      y++;
    }
    this.currentMonth.set(m);
    this.currentYear.set(y);
    this.selectedDayIso.set(null);
    this.loadCalendar();
  }

  goToToday(): void {
    const now = new Date();
    this.currentYear.set(now.getFullYear());
    this.currentMonth.set(now.getMonth());
    this.selectedDayIso.set(toIso(now));
    this.loadCalendar();
  }

  filterProject(value: string): void {
    const id = value ? Number(value) : null;
    this.selectedProjectId.set(id);
    this.loadCalendar();
  }

  selectDay(day: CalendarGridDay): void {
    this.selectedDayIso.set(day.isoString);
  }

  closeDayModal(): void {
    this.selectedDayIso.set(null);
  }

  quickApprove(requestId: number): void {
    this.api.approveRequest(requestId).subscribe({
      next: () => {
        this.notice.set('Request approved.');
        this.loadCalendar();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  leaveTypeClass(balanceName?: string | null): string {
    const name = (balanceName ?? '').toLowerCase();
    if (name.includes('sick')) return 'type-sick';
    if (name.includes('personal')) return 'type-personal';
    return 'type-vacation';
  }
}
