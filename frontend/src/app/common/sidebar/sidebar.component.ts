import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { fullName, initials } from '../../format';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  readonly session = inject(SessionService);
  readonly fullName = fullName;
  readonly initials = initials;

  readonly items = [
    { label: 'Overview', link: '/home', exact: true, icon: 'overview' },
    { label: 'New Request', link: '/new-request', exact: false, icon: 'plus' },
    { label: 'Request History', link: '/request-history', exact: false, icon: 'history' },
    { label: 'Projects', link: '/projects', exact: false, icon: 'projects' },
    { label: 'Management', link: '/management', exact: false, icon: 'team' },
  ];

  pick(event: Event): void {
    this.session.select(Number((event.target as HTMLSelectElement).value));
  }
}
