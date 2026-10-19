import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResetPasswordComponent } from './reset-password.component';
import { testProviders } from '../../testing/test-providers';

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetPasswordComponent],
      providers: [...testProviders]
    }).compileComponents();

    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and generate a randomized secure password', () => {
    expect(component).toBeTruthy();
    component.generateRandom();
    const generated = component.form.controls.newPassword.value;
    expect(generated.length).toBeGreaterThanOrEqual(8);
    expect(component.showPassword()).toBeTrue();
  });
});
