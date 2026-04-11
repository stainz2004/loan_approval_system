export type LoanRejectionReason = 'CUSTOMER_TOO_OLD' | 'INSUFFICIENT_DATA' | 'OTHER';

export interface LoanApplicationRequest {
  firstName: string;
  lastName: string;
  personalCode: string;
  loanPeriodMonths: number;
  interestMargin: number;
  baseInterest: number;
  loanAmount: number;
}

export interface LoanApplicationDecisionResponse {
  isAccepted: boolean;
  rejectionReason?: LoanRejectionReason;
}

export interface PaymentScheduleItemDTO {
  id: number;
  paymentNumber: number;
  dueDate: string;
  totalAmount: number;
  remainingBalance: number;
}

export type LoanApplicationStatus = 'IN_REVIEW' | 'APPROVED' | 'REJECTED';

export interface LoanApplicationResponse {
  id: number;
  firstName: string;
  lastName: string;
  personalCode: string;
  loanPeriodMonths: number;
  interestMargin: number;
  baseInterest: number;
  loanAmount: number;
  status: LoanApplicationStatus;
  paymentScheduleItems: PaymentScheduleItemDTO[];
}

export interface RegenerateScheduleRequest {
  loanAmount: number;
  interestMargin: number;
  baseInterest: number;
  loanPeriodMonths: number;
}
