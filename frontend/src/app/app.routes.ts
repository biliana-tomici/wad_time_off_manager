import { Routes } from '@angular/router';
import { NewRequestComponent } from './pages/new-request/new-request.component';
import { HomeComponent } from './pages/home/home.component';
import { RequestHistoryComponent } from './pages/request-history/request-history.component';
import { ProjectsComponent } from './pages/projects/projects.component';
import { ApprovalsComponent } from './pages/approvals/approvals.component';
import { AdministrationComponent } from './pages/administration/administration.component';
import { CalendarComponent } from './pages/calendar/calendar.component';
import { LoginComponent } from './pages/login/login.component';
import { authGuard, guestGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'new-request', component: NewRequestComponent, canActivate: [authGuard] },
  { path: 'calendar', component: CalendarComponent, canActivate: [authGuard] },
  { path: 'projects', component: ProjectsComponent, canActivate: [authGuard] },
  { path: 'request-history', component: RequestHistoryComponent, canActivate: [authGuard] },
  { path: 'approvals', component: ApprovalsComponent, canActivate: [authGuard, roleGuard(['MANAGER', 'ADMIN'])] },
  { path: 'administration', component: AdministrationComponent, canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'management', redirectTo: 'approvals', pathMatch: 'full' },
  { path: '**', redirectTo: 'home' }
];
