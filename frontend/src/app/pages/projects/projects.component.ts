import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { AppUser, Project, TimePeriod } from '../../models';
import { errorText, fullName } from '../../format';

interface ProjectDraft {
  name: string;
  minEmp: number;
  managerId: number | null;
}

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.scss'
})
export class ProjectsComponent {
  private readonly api = inject(ApiService);
  readonly session = inject(SessionService);
  private readonly fb = inject(FormBuilder);

  readonly projects = signal<Project[]>([]);
  readonly members = signal<Record<number, AppUser[]>>({});
  readonly memberToAdd = signal<Record<number, string>>({});
  readonly periods = signal<Record<number, TimePeriod>>({});
  readonly drafts = signal<Record<number, ProjectDraft>>({});
  readonly confirmId = signal<number | null>(null);
  readonly error = signal('');
  readonly notice = signal('');
  readonly loading = signal(false);
  readonly fullName = fullName;
  readonly periodOptions: TimePeriod[] = ['WEEKLY', 'MONTHLY', 'YEARLY'];

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    minEmp: [2, [Validators.required, Validators.min(1)]],
    managerId: ['', Validators.required],
  });

  constructor() {
    effect(() => {
      const current = this.session.current();
      this.notice.set('');
      this.error.set('');
      this.confirmId.set(null);
      if (current?.id) {
        this.form.controls.managerId.setValue(String(current.id));
        this.load();
      } else {
        this.projects.set([]);
        this.members.set({});
      }
    }, { allowSignalWrites: true });
  }

  canManage(project: Project): boolean {
    if (this.session.isAdmin()) {
      return true;
    }
    const currentId = this.session.current()?.id;
    return this.session.isManager() && !!currentId && project.manager?.id === currentId;
  }

  load(): void {
    this.loading.set(true);
    this.api.projects().subscribe({
      next: (rows) => {
        this.projects.set(rows);
        this.loading.set(false);
        for (const project of rows) {
          this.loadMembers(project.id);
        }
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loading.set(false);
      }
    });
  }

  loadMembers(projectId: number): void {
    this.api.projectMembers(projectId).subscribe({
      next: (users) => {
        this.members.update((curr) => ({ ...curr, [projectId]: users }));
      },
      error: () => {
        this.members.update((curr) => ({ ...curr, [projectId]: [] }));
      }
    });
  }

  membersFor(projectId: number): AppUser[] {
    return this.members()[projectId] ?? [];
  }

  availableUsersFor(projectId: number): AppUser[] {
    const assignedIds = new Set(this.membersFor(projectId).map((u) => u.id));
    return this.session.users().filter((u) => !assignedIds.has(u.id));
  }

  selectedMemberToAdd(projectId: number): string {
    return this.memberToAdd()[projectId] ?? '';
  }

  setMemberToAdd(projectId: number, event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.memberToAdd.update((curr) => ({ ...curr, [projectId]: value }));
  }

  addMember(project: Project): void {
    const employeeId = Number(this.selectedMemberToAdd(project.id));
    if (!employeeId) return;
    this.error.set('');
    this.api.addProjectMember(project.id, employeeId).subscribe({
      next: () => {
        this.notice.set('Member added to project.');
        this.memberToAdd.update((curr) => ({ ...curr, [project.id]: '' }));
        this.loadMembers(project.id);
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  removeMember(project: Project, employeeId: number): void {
    this.error.set('');
    this.api.removeProjectMember(project.id, employeeId).subscribe({
      next: () => {
        this.notice.set('Member removed from project.');
        this.loadMembers(project.id);
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  periodFor(id: number): TimePeriod {
    return this.periods()[id] ?? 'MONTHLY';
  }

  setPeriod(id: number, event: Event): void {
    const value = (event.target as HTMLSelectElement).value as TimePeriod;
    this.periods.update((current) => ({ ...current, [id]: value }));
  }

  private getDraft(project: Project): ProjectDraft {
    return this.drafts()[project.id] ?? {
      name: project.name,
      minEmp: project.minEmp,
      managerId: project.manager?.id ?? null,
    };
  }

  draftName(project: Project): string {
    return this.getDraft(project).name;
  }

  draftMinEmp(project: Project): number {
    return this.getDraft(project).minEmp;
  }

  draftManagerId(project: Project): string {
    const id = this.getDraft(project).managerId;
    return id != null ? String(id) : '';
  }

  setDraftName(project: Project, event: Event): void {
    const name = (event.target as HTMLInputElement).value;
    const prev = this.getDraft(project);
    this.drafts.update((curr) => ({ ...curr, [project.id]: { ...prev, name } }));
  }

  setDraftMinEmp(project: Project, event: Event): void {
    const minEmp = Number((event.target as HTMLInputElement).value);
    const prev = this.getDraft(project);
    this.drafts.update((curr) => ({ ...curr, [project.id]: { ...prev, minEmp } }));
  }

  setDraftManagerId(project: Project, event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    const managerId = val ? Number(val) : null;
    const prev = this.getDraft(project);
    this.drafts.update((curr) => ({ ...curr, [project.id]: { ...prev, managerId } }));
  }

  create(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.error.set('');
    const value = this.form.getRawValue();
    this.api.createProject({
      name: value.name.trim(),
      minEmp: Number(value.minEmp),
      managerId: Number(value.managerId),
    }).subscribe({
      next: () => {
        this.notice.set('Project created.');
        this.form.controls.name.setValue('');
        this.load();
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  save(project: Project): void {
    const draft = this.getDraft(project);
    const name = draft.name.trim();
    if (!name || draft.minEmp < 0) return;
    this.error.set('');
    const body: { name: string; minEmp: number; managerId?: number } = {
      name,
      minEmp: Number(draft.minEmp),
    };
    if (draft.managerId != null) {
      body.managerId = Number(draft.managerId);
    }
    this.api.updateProject(project.id, body).subscribe({
      next: () => {
        this.notice.set('Project saved.');
        this.load();
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  remove(project: Project): void {
    this.error.set('');
    this.api.deleteProject(project.id).subscribe({
      next: () => {
        this.confirmId.set(null);
        this.notice.set('Project removed.');
        this.load();
        this.session.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  download(project: Project): void {
    this.error.set('');
    this.api.report(project.id, this.periodFor(project.id)).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `project_report_${project.id}.pdf`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.error.set(errorText(err))
    });
  }
}
