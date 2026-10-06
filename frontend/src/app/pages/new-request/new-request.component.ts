import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NewRequestService} from '../../services/new-request.service';
import {NgForOf} from '@angular/common';

@Component({
  standalone: true,
  selector: 'app-new-request',
  imports: [
    ReactiveFormsModule,
    NgForOf,
  ],
  templateUrl: './new-request.component.html',
  styleUrl: './new-request.component.scss'
})
export class NewRequestComponent {
  requestForm: FormGroup;

  leaveTypes = ['Rest Leave', 'Sick Leave', 'Parental Leave'];

  constructor(private fb: FormBuilder, private requestService: NewRequestService) {
    this.requestForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      explanation: ['', Validators.required],
      type: ['', Validators.required],
    });
  }
  submit() {
    if (this.requestForm.invalid) return;

    const form = this.requestForm.value;

    const requestBodyData = {
      id: 0,
      employee: {
        id: 1,
        firstName: "Alice",
        lastName: "Doe",
        projects: [
          {
            id: 1,
            minEmp: 2,
            name: "VacationPlanner",
            manager: {
              id: 2,
              firstName: "Bob",
              lastName: "Smith"
            }
          }
        ],
        manager: {
          id: 2,
          firstName: "Bob",
          lastName: "Smith"
        },
        balances: [
          {
            id: 1,
            employee: "Alice Doe",
            daysLeft: 10,
            name: "Paid Leave",
            maxDays: 30
          }
        ],
        requests: []
      },
      balance: {
        id: 1,
        employee: "Alice Doe",
        daysLeft: 10,
        name: "Paid Leave",
        maxDays: 30
      },
      status: "PENDING",
      startDate: this.requestForm.value.startDate,
      endDate: this.requestForm.value.endDate,
      explanation: this.requestForm.value.explanation,
      creationDate: new Date().toISOString().split('T')[0],
      modifiedDate: new Date().toISOString().split('T')[0]
    };
    this.requestService.createNewRequest(requestBodyData).subscribe({
      next: () => alert('Request submitted successfully!'),
      error: () => alert('Error submitting request')
    });
  }
}
