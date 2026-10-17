import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProjectsComponent } from './projects.component';
import { testProviders } from '../../testing/test-providers';
import { Project } from '../../models';

describe('ProjectsComponent', () => {
  let component: ProjectsComponent;
  let fixture: ComponentFixture<ProjectsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectsComponent],
      providers: testProviders,
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render read-only project list for Employee without edit/report/create controls (2.4.4)', () => {
    const proj: Project = {
      id: 1,
      name: 'Project Alpha',
      minEmp: 2,
      manager: { id: 1, firstName: 'Alice', lastName: 'Smith' },
    };
    component.session.current.set({ id: 5, firstName: 'Eve', lastName: 'Davis', role: 'EMPLOYEE' });
    component.projects.set([proj]);
    component.members.set({ 1: [{ id: 5, firstName: 'Eve', lastName: 'Davis', role: 'EMPLOYEE' }] });
    fixture.detectChanges();

    expect(component.canManage(proj)).toBeFalse();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Project Alpha');
    expect(el.textContent).toContain('Eve Davis');
    expect(el.textContent).not.toContain('Save changes');
    expect(el.textContent).not.toContain('Download PDF');
    expect(el.textContent).not.toContain('Open a project');
  });

  it('should allow Manager to edit name, minEmp, manager, and members on own project (2.4.1, 2.4.2)', () => {
    const proj: Project = {
      id: 2,
      name: 'Project Beta',
      minEmp: 3,
      manager: { id: 2, firstName: 'Bob', lastName: 'Johnson' },
    };
    component.session.current.set({ id: 2, firstName: 'Bob', lastName: 'Johnson', role: 'MANAGER' });
    component.projects.set([proj]);
    fixture.detectChanges();

    expect(component.canManage(proj)).toBeTrue();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Save changes');
    expect(el.textContent).toContain('Download PDF');
    expect(el.textContent).toContain('Add member');
    expect(el.textContent).toContain('Open a project');
  });
});
