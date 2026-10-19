import { EnvironmentProviders, Provider } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

export const testProviders: Array<Provider | EnvironmentProviders> = [
  provideRouter([]),
  provideHttpClient(),
  provideHttpClientTesting(),
];
