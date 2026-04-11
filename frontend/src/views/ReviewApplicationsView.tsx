import { useEffect, useState } from 'react';
import { approveLoanApplication, getLoanApplications, regenerateSchedule, rejectLoanApplication } from '../api/loanApi';
import type { LoanApplicationResponse, LoanApplicationStatus, LoanRejectionReason } from '../types/loan';

const REJECTION_REASONS: LoanRejectionReason[] = ['CUSTOMER_TOO_OLD', 'INSUFFICIENT_DATA', 'OTHER'];

const STATUS_LABEL: Record<LoanApplicationStatus, string> = {
  IN_REVIEW: 'In Review',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
};

export function ReviewApplicationsView() {
  const [applications, setApplications] = useState<LoanApplicationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reasonById, setReasonById] = useState<Record<number, LoanRejectionReason>>({});

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setApplications(await getLoanApplications());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected error');
    } finally {
      setLoading(false);
    }
  }

  async function act(action: () => Promise<unknown>) {
    setLoading(true);
    setError(null);
    try {
      await action();
      setApplications(await getLoanApplications());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected error');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void load(); }, []);

  return (
    <section className="stack">
      <div className="row">
        <h2>Applications In Review</h2>
        <button type="button" onClick={load} disabled={loading}>Refresh</button>
      </div>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {!applications.length && !loading && <p>No applications in review.</p>}
      {applications.map((app) => {
        const reason = reasonById[app.id] ?? 'OTHER';
        return (
          <article className="card" key={app.id}>
            <h3>#{app.id} — {app.firstName} {app.lastName} <small>({STATUS_LABEL[app.status] ?? app.status})</small></h3>
            <p>Amount: {app.loanAmount} | Period: {app.loanPeriodMonths} months</p>
            <p>Margin: {app.interestMargin} | Base: {app.baseInterest}</p>
            <div className="actions">
              <button type="button" disabled={loading} onClick={() => act(() => approveLoanApplication(app.id))}>
                Approve
              </button>
              <select
                value={reason}
                disabled={loading}
                onChange={(e) => setReasonById((prev) => ({ ...prev, [app.id]: e.target.value as LoanRejectionReason }))}
              >
                {REJECTION_REASONS.map((r) => <option key={r} value={r}>{r}</option>)}
              </select>
              <button type="button" disabled={loading} onClick={() => act(() => rejectLoanApplication(app.id, reason))}>
                Reject
              </button>
              <button
                type="button"
                disabled={loading}
                onClick={() => act(() => regenerateSchedule(app.id, {
                  loanAmount: app.loanAmount,
                  interestMargin: app.interestMargin,
                  baseInterest: app.baseInterest,
                  loanPeriodMonths: app.loanPeriodMonths,
                }))}
              >
                Regenerate schedule
              </button>
            </div>
            {app.paymentScheduleItems?.length > 0 && (
              <details>
                <summary>Payment schedule ({app.paymentScheduleItems.length})</summary>
                <ul>
                  {app.paymentScheduleItems.slice(0, 12).map((item) => (
                    <li key={item.id}>
                      #{item.paymentNumber} | {item.dueDate} | total {item.totalAmount} | remaining {item.remainingBalance}
                    </li>
                  ))}
                </ul>
              </details>
            )}
          </article>
        );
      })}
    </section>
  );
}
