import { Routes } from '@angular/router';
import { NewRequestComponent } from './pages/new-request/new-request.component';
import { HomeComponent } from './pages/home/home.component';
import { RequestHistoryComponent } from './pages/request-history/request-history.component';
import { ProjectsComponent } from './pages/projects/projects.component';
import { ApprovalsComponent } from './pages/approvals/approvals.component';
import { AdministrationComponent } from './pages/administration/administration.component';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'new-request', component: NewRequestComponent },
  { path: 'projects', component: ProjectsComponent },
  { path: 'request-history', component: RequestHistoryComponent },
  { path: 'approvals', component: ApprovalsComponent, canActivate: [roleGuard(['MANAGER', 'ADMIN'])] },
  { path: 'administration', component: AdministrationComponent, canActivate: [roleGuard(['ADMIN'])] },
  { path: 'management', redirectTo: 'approvals', pathMatch: 'full' },
];
