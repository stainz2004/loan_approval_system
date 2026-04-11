import { useState } from 'react';
import './App.css';
import { NewApplicationView } from './views/NewApplicationView';
import { ReviewApplicationsView } from './views/ReviewApplicationsView';

type Tab = 'new' | 'review';

function App() {
  const [tab, setTab] = useState<Tab>('new');

  return (
    <div>
      <header className="site-header">
        <div className="header-inner">
          <a className="brand" href="/" aria-label="Loan Approval System home">
            Loan Approval
          </a>
          <nav className="header-nav" aria-label="Main navigation">
            <button
              className={`nav-tab${tab === 'new' ? ' active' : ''}`}
              onClick={() => setTab('new')}
            >
              New Application
            </button>
            <button
              className={`nav-tab${tab === 'review' ? ' active' : ''}`}
              onClick={() => setTab('review')}
            >
              Review Queue
            </button>
          </nav>
        </div>
      </header>

      <main className="main-content">
        {tab === 'new' ? <NewApplicationView /> : <ReviewApplicationsView />}
      </main>
    </div>
  );
}

export default App;
