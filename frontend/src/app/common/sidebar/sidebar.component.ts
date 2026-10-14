import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { fullName, initials } from '../../format';
import { Role } from '../../models';

interface NavItem {
  label: string;
  link: string;
  exact: boolean;
  icon: string;
  roles?: Role[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  private readonly router = inject(Router);
  readonly session = inject(SessionService);
  readonly fullName = fullName;
  readonly initials = initials;

  readonly allItems: NavItem[] = [
    { label: 'Overview', link: '/home', exact: true, icon: 'overview' },
    { label: 'New Request', link: '/new-request', exact: false, icon: 'plus' },
    { label: 'Request History', link: '/request-history', exact: false, icon: 'history' },
    { label: 'Projects', link: '/projects', exact: false, icon: 'projects' },
    { label: 'Management', link: '/management', exact: false, icon: 'team', roles: ['MANAGER', 'ADMIN'] },
  ];

  readonly items = computed(() => {
    const role = this.session.role();
    return this.allItems.filter((item) => !item.roles || (role && item.roles.includes(role)));
  });

  pick(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    this.session.select(id);
    const role = this.session.role();
    if (this.router.url.startsWith('/management') && role === 'EMPLOYEE') {
      this.router.navigate(['/home']);
    }
  }
}
