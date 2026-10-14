import { Component, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { SessionService } from '../../services/session.service';
import { Project, TimePeriod } from '../../models';
import { errorText, fullName } from '../../format';

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
  readonly periods = signal<Record<number, TimePeriod>>({});
  readonly drafts = signal<Record<number, string>>({});
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
      const managerId = this.session.current()?.id;
      if (managerId && !this.form.controls.managerId.value) {
        this.form.controls.managerId.setValue(String(managerId));
      }
    });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.projects().subscribe({
      next: (rows) => {
        this.projects.set(rows);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorText(err));
        this.loading.set(false);
      }
    });
  }

  periodFor(id: number): TimePeriod {
    return this.periods()[id] ?? 'MONTHLY';
  }

  setPeriod(id: number, event: Event): void {
    const value = (event.target as HTMLSelectElement).value as TimePeriod;
    this.periods.update((current) => ({ ...current, [id]: value }));
  }

  draftName(project: Project): string {
    return this.drafts()[project.id] ?? project.name;
  }

  setDraft(id: number, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.drafts.update((current) => ({ ...current, [id]: value }));
  }

  create(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
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
    const name = this.draftName(project).trim();
    if (!name) return;
    this.api.updateProject(project.id, { name }).subscribe({
      next: () => {
        this.notice.set('Project saved.');
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  remove(project: Project): void {
    this.api.deleteProject(project.id).subscribe({
      next: () => {
        this.confirmId.set(null);
        this.notice.set('Project removed.');
        this.load();
      },
      error: (err) => this.error.set(errorText(err))
    });
  }

  download(project: Project): void {
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
