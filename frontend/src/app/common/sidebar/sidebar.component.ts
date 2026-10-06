import { Component } from '@angular/core';
import {NgForOf} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [
    NgForOf,
    RouterLink
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  navButtons = [
    { label: 'Overview', link: 'home', icon: 'assets/Overview.png' },
    { label: 'New Request', link: 'new-request', icon: 'assets/NewRequest.png' },
    { label: 'Request History', link: 'request-history', icon: 'assets/RequestHistory.png' },
    { label: 'Projects', link: 'projects', icon: 'assets/Projects.png' },
    { label: 'Management', link: 'management', icon: 'assets/Management.png' },
  ];
}
