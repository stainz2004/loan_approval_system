import { useEffect, useState } from 'react';
import { approveLoanApplication, getLoanApplications, regenerateSchedule, rejectLoanApplication } from '../api/loanApi';
import type { LoanApplicationResponse, LoanRejectionReason, RegenerateScheduleRequest } from '../types/loan';

const REJECTION_REASONS: LoanRejectionReason[] = ['CUSTOMER_TOO_OLD', 'INSUFFICIENT_DATA', 'OTHER'];

export function ReviewApplicationsView() {
  const [applications, setApplications] = useState<LoanApplicationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reasonById, setReasonById] = useState<Record<number, LoanRejectionReason>>({});
  const [fieldsById, setFieldsById] = useState<Record<number, RegenerateScheduleRequest>>({});

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const apps = (await getLoanApplications()).sort((a, b) => a.id - b.id);
      setApplications(apps);
      setFieldsById((prev) => {
        const next = { ...prev };
        apps.forEach((app) => {
          if (!next[app.id]) {
            next[app.id] = {
              loanAmount: app.loanAmount,
              interestMargin: app.interestMargin,
              loanPeriodMonths: app.loanPeriodMonths,
            };
          }
        });
        return next;
      });
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
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unexpected error');
      setLoading(false);
    }
  }

  function setField(appId: number, field: keyof RegenerateScheduleRequest, value: string) {
    setFieldsById((prev) => ({
      ...prev,
      [appId]: { ...prev[appId], [field]: Number(value) },
    }));
  }

  useEffect(() => { void load(); }, []);

  return (
    <div className="stack">
      <div className="section-header">
        <h2>Applications In Review</h2>
        <button className="btn btn-secondary" onClick={load} disabled={loading}>
          {loading ? 'Loading…' : 'Refresh'}
        </button>
      </div>

      {error && <div className="error-banner" role="alert">{error}</div>}

      {!applications.length && !loading && (
        <p style={{ color: 'var(--muted)' }}>No applications currently in review.</p>
      )}

      {applications.map((app) => {
        const reason = reasonById[app.id] ?? 'OTHER';
        const fields = fieldsById[app.id] ?? {
          loanAmount: app.loanAmount,
          interestMargin: app.interestMargin,
          loanPeriodMonths: app.loanPeriodMonths,
        };

        return (
          <article className="app-card" key={app.id}>
            <h3>#{app.id} — {app.firstName} {app.lastName}</h3>

            <div className="app-meta">
              <p>Personal code: <strong>{app.personalCode}</strong></p>
              <p>Amount: <strong>€{app.loanAmount}</strong> · Period: <strong>{app.loanPeriodMonths} months</strong> · Margin: <strong>{app.interestMargin}%</strong></p>
            </div>

            <div className="actions">
              <button className="btn btn-primary" disabled={loading} onClick={() => act(() => approveLoanApplication(app.id))}>
                Approve
              </button>

              <select
                value={reason}
                disabled={loading}
                onChange={(e) => setReasonById((prev) => ({ ...prev, [app.id]: e.target.value as LoanRejectionReason }))}
                aria-label="Rejection reason"
                style={{ width: 'auto' }}
              >
                {REJECTION_REASONS.map((r) => (
                  <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>
                ))}
              </select>

              <button className="btn btn-danger" disabled={loading} onClick={() => act(() => rejectLoanApplication(app.id, reason))}>
                Reject
              </button>
            </div>

            <details>
              <summary>Regenerate schedule</summary>
              <div className="regenerate-fields">
                <label>Amount (€)<input type="number" min={0} step={100} value={fields.loanAmount} onChange={(e) => setField(app.id, 'loanAmount', e.target.value)} /></label>
                <label>Period (mo)<input type="number" min={1} step={1} value={fields.loanPeriodMonths} onChange={(e) => setField(app.id, 'loanPeriodMonths', e.target.value)} /></label>
                <label>Margin (%)<input type="number" min={0} step={0.1} value={fields.interestMargin} onChange={(e) => setField(app.id, 'interestMargin', e.target.value)} /></label>
              </div>
              <button className="btn btn-secondary" disabled={loading} onClick={() => act(() => regenerateSchedule(app.id, fields))}>
                ↺ Regenerate
              </button>
            </details>

            {app.paymentScheduleItems?.length > 0 && (
              <details>
                <summary>Payment schedule ({app.paymentScheduleItems.length} payments)</summary>
                <ul>
                  {app.paymentScheduleItems.map((item) => (
                    <li key={item.id}>
                      #{item.paymentNumber} · {item.dueDate} · <strong>€{item.totalAmount}</strong> · balance €{item.remainingBalance}
                    </li>
                  ))}
                </ul>
              </details>
            )}
          </article>
        );
      })}
    </div>
  );
}
