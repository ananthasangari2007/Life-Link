import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { DemoCredentialsModal } from './components/DemoCredentialsModal';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { HospitalDashboard } from './pages/HospitalDashboard';
import { BloodBankDashboard } from './pages/BloodBankDashboard';
import { AdminDashboard } from './pages/AdminDashboard';
import { MatchingView } from './pages/MatchingView';
import { AuditorView } from './pages/AuditorView';
import { authService } from './services/authService';
import { notificationService } from './services/notificationService';
import type { User, BloodGroup, ComponentType } from './types';

export function App() {
  const [user, setUser] = useState<User | null>(authService.getStoredUser());
  const [activeView, setActiveView] = useState<string>(user ? 'dashboard' : 'landing');
  const [demoModalOpen, setDemoModalOpen] = useState(false);
  const [presetRequest, setPresetRequest] = useState<{
    bloodGroup: BloodGroup;
    componentType: ComponentType;
    units: number;
  } | null>(null);

  // Initialize and verify user session
  useEffect(() => {
    if (authService.isAuthenticated()) {
      authService.getCurrentUser()
        .then((u) => {
          setUser(u);
        })
        .catch(() => {
          authService.logout();
          setUser(null);
        });
    }

    const handleAuthExpired = () => {
      setUser(null);
      setActiveView('login');
    };

    window.addEventListener('lifelink_auth_expired', handleAuthExpired);
    return () => window.removeEventListener('lifelink_auth_expired', handleAuthExpired);
  }, []);

  // Real-time Server-Sent Events (SSE) listener
  useEffect(() => {
    if (!user) return;
    const clientId = `client-${user.id}-${Date.now()}`;
    let eventSource: EventSource | null = null;

    try {
      eventSource = notificationService.createEventSource(clientId);
      eventSource.addEventListener('NOTIFICATION', () => {
        // Real-time notification received
      });
    } catch {
      // SSE fallback to controlled polling
    }

    return () => {
      if (eventSource) {
        eventSource.close();
      }
    };
  }, [user]);

  const handleLoginSuccess = (loggedInUser: User) => {
    setUser(loggedInUser);
    setActiveView('dashboard');
  };

  const handleLogout = () => {
    authService.logout();
    setUser(null);
    setActiveView('landing');
  };

  const handleQuickLogin = async (email: string, pass: string) => {
    try {
      const res = await authService.login(email, pass);
      handleLoginSuccess(res.user);
    } catch {
      setActiveView('login');
    }
  };

  const handleInitiateRequestFromMatching = (params: {
    bloodGroup: BloodGroup;
    componentType: ComponentType;
    units: number;
  }) => {
    setPresetRequest(params);
    setActiveView('dashboard');
  };

  // Render the active view
  const renderView = () => {
    switch (activeView) {
      case 'landing':
        return (
          <LandingPage
            onNavigate={setActiveView}
            onOpenDemo={() => setDemoModalOpen(true)}
          />
        );

      case 'login':
        return (
          <LoginPage
            onLoginSuccess={handleLoginSuccess}
            onNavigate={setActiveView}
            onOpenDemo={() => setDemoModalOpen(true)}
          />
        );

      case 'register':
        return (
          <RegisterPage
            onLoginSuccess={handleLoginSuccess}
            onNavigate={setActiveView}
          />
        );

      case 'matching-public':
      case 'matching':
        return (
          <MatchingView
            user={user}
            onInitiateRequest={handleInitiateRequestFromMatching}
          />
        );

      case 'admin':
        return user?.role === 'NETWORK_ADMIN' ? (
          <AdminDashboard
            user={user}
            onNavigateToAudit={() => setActiveView('audit')}
          />
        ) : (
          <LandingPage onNavigate={setActiveView} onOpenDemo={() => setDemoModalOpen(true)} />
        );

      case 'audit':
        return user && (user.role === 'NETWORK_ADMIN' || user.role === 'AUDITOR') ? (
          <AuditorView user={user} />
        ) : (
          <LandingPage onNavigate={setActiveView} onOpenDemo={() => setDemoModalOpen(true)} />
        );

      case 'dashboard':
      default:
        if (!user) {
          return (
            <LandingPage
              onNavigate={setActiveView}
              onOpenDemo={() => setDemoModalOpen(true)}
            />
          );
        }

        if (user.role === 'HOSPITAL_USER') {
          return (
            <HospitalDashboard
              user={user}
              onNavigateToMatching={() => setActiveView('matching')}
              presetRequestParams={presetRequest}
              clearPresetParams={() => setPresetRequest(null)}
            />
          );
        }

        if (user.role === 'BLOOD_BANK_USER') {
          return <BloodBankDashboard user={user} />;
        }

        if (user.role === 'NETWORK_ADMIN') {
          return (
            <AdminDashboard
              user={user}
              onNavigateToAudit={() => setActiveView('audit')}
            />
          );
        }

        if (user.role === 'AUDITOR') {
          return <AuditorView user={user} />;
        }

        return <LandingPage onNavigate={setActiveView} onOpenDemo={() => setDemoModalOpen(true)} />;
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900 font-sans antialiased selection:bg-rose-500 selection:text-white">
      <Navbar
        user={user}
        onLogout={handleLogout}
        onOpenDemoModal={() => setDemoModalOpen(true)}
        activeView={activeView}
        setActiveView={setActiveView}
      />

      <main className="flex-1">
        {renderView()}
      </main>

      <Footer />

      <DemoCredentialsModal
        isOpen={demoModalOpen}
        onClose={() => setDemoModalOpen(false)}
        onQuickLogin={handleQuickLogin}
      />
    </div>
  );
}

export default App;
