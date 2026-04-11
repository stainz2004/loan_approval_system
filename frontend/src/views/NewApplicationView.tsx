import { useState, type FormEvent } from 'react';
import { requestLoanDecision } from '../api/loanApi';
import type { LoanApplicationCreationResponse, LoanApplicationRequest } from '../types/loan';

const defaultForm: LoanApplicationRequest = {
  firstName: '',
  lastName: '',
  personalCode: '',
  loanPeriodMonths: 12,
  interestMargin: 1.01,
  baseInterest: 1.01,
  loanAmount: 5000,
};

export function NewApplicationView() {
  const [form, setForm] = useState<LoanApplicationRequest>(defaultForm);
  const [loading, setLoading] = useState(false);
  const [decision, setDecision] = useState<LoanApplicationCreationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  function set<K extends keyof LoanApplicationRequest>(key: K, value: LoanApplicationRequest[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setDecision(null);
    try {
      setDecision(await requestLoanDecision(form));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected error');
    } finally {
      setLoading(false);
    }
  }

  const isAccepted = decision?.isAccepted;

  return (
    <div className="stack">
      {error && (
        <div className="error-banner" role="alert">⚠ {error}</div>
      )}

      <form className="card" onSubmit={handleSubmit}>
        <h2>New Loan Application</h2>

        <div className="form-grid">
          <label>
            First name
            <input
              value={form.firstName}
              onChange={(e) => set('firstName', e.target.value)}
              maxLength={32}
              placeholder="e.g. Mari"
              required
            />
          </label>

          <label>
            Last name
            <input
              value={form.lastName}
              onChange={(e) => set('lastName', e.target.value)}
              maxLength={32}
              placeholder="e.g. Tamm"
              required
            />
          </label>

          <label>
            Personal code
            <input
              value={form.personalCode}
              onChange={(e) => set('personalCode', e.target.value)}
              minLength={11}
              maxLength={11}
              placeholder="11 digits"
              required
            />
          </label>

          <label>
            Loan amount (€)
            <input
              type="number"
              value={form.loanAmount}
              onChange={(e) => set('loanAmount', Number(e.target.value))}
              min={5000}
              step="0.01"
              required
            />
          </label>

          <label>
            Loan period (months)
            <input
              type="number"
              value={form.loanPeriodMonths}
              onChange={(e) => set('loanPeriodMonths', Number(e.target.value))}
              min={6}
              max={360}
              required
            />
          </label>

          <label>
            Interest margin (%)
            <input
              type="number"
              value={form.interestMargin}
              onChange={(e) => set('interestMargin', Number(e.target.value))}
              min={0}
              step="0.01"
              required
            />
          </label>

          <label>
            Base interest (%)
            <input
              type="number"
              value={form.baseInterest}
              onChange={(e) => set('baseInterest', Number(e.target.value))}
              min={0}
              step="0.01"
              required
            />
          </label>
        </div>

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Submitting…' : 'Submit application'}
        </button>
      </form>

      {decision && (
        <div className={`decision-card ${isAccepted ? 'accepted' : 'rejected'}`}>
          <span className={`badge ${isAccepted ? 'badge-success' : 'badge-danger'}`}>
            {isAccepted ? '✓ Moved into review' : '✗ Rejected'}
          </span>

          {!isAccepted && decision.rejectionReason && (
            <p className="decision-reason">Reason: {decision.rejectionReason}</p>
          )}
        </div>
      )}
    </div>
  );
}
