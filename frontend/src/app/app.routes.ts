import { Routes } from '@angular/router';
import {NewRequestComponent} from './pages/new-request/new-request.component';
import {HomeComponent} from './pages/home/home.component';
import {RequestHistoryComponent} from './pages/request-history/request-history.component';
import {ProjectsComponent} from './pages/projects/projects.component';
import {ManagementComponent} from './pages/management/management.component';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'new-request', component: NewRequestComponent},
  {path: 'projects', component: ProjectsComponent},
  {path: 'request-history', component: RequestHistoryComponent},
  {path: 'management', component: ManagementComponent, canActivate: [roleGuard(['MANAGER', 'ADMIN'])]},
];
