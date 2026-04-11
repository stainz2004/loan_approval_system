import { apiFetch } from './http';
import type {
  LoanApplicationCreationResponse,
  LoanApplicationRequest,
  LoanApplicationResponse,
  LoanRejectionReason,
  RegenerateScheduleRequest,
} from '../types/loan';

export const getLoanApplications = () =>
  apiFetch<LoanApplicationResponse[]>('/api/loan-applications', { method: 'GET' });

export const requestLoanDecision = (payload: LoanApplicationRequest) =>
  apiFetch<LoanApplicationCreationResponse>('/api/loan-applications', { method: 'POST', body: JSON.stringify(payload) });

export const approveLoanApplication = (id: number) =>
  apiFetch(`/api/loan-applications/${id}/approve`, { method: 'POST' });

export const rejectLoanApplication = (id: number, reason: LoanRejectionReason) =>
  apiFetch(`/api/loan-applications/${id}/reject?${new URLSearchParams({ reason })}`, { method: 'POST' });

export const regenerateSchedule = (id: number, payload: RegenerateScheduleRequest) =>
  apiFetch(`/api/loan-applications/${id}/regenerate-schedule`, { method: 'POST', body: JSON.stringify(payload) });
