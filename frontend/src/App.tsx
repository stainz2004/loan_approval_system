import { useState } from 'react';
import './App.css';
import { NewApplicationView } from './views/NewApplicationView';
import { ReviewApplicationsView } from './views/ReviewApplicationsView';

type Tab = 'new' | 'review';

function App() {
  const [tab, setTab] = useState<Tab>('new');

  return (
    <main className="container">
      <header className="row">
        <h1>Loan Approval System</h1>
        <nav className="row">
          <button type="button" onClick={() => setTab('new')} disabled={tab === 'new'}>
            New Application
          </button>
          <button type="button" onClick={() => setTab('review')} disabled={tab === 'review'}>
            Review Queue
          </button>
        </nav>
      </header>

      {tab === 'new' ? <NewApplicationView /> : <ReviewApplicationsView />}
    </main>
  );
}

export default App;
