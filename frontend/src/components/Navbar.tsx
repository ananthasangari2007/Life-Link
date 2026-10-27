import React, { useState, useEffect } from 'react';
import {
  Heart,
  Bell,
  LogOut,
  Building2,
  ShieldAlert,
  Search,
  Activity,
  KeyRound,
  CheckCircle2,
  Clock,
  Menu,
  X
} from 'lucide-react';
import type { User, AppNotification } from '../types';
import { notificationService } from '../services/notificationService';

interface NavbarProps {
  user: User | null;
  onLogout: () => void;
  onOpenDemoModal: () => void;
  activeView: string;
  setActiveView: (view: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  user,
  onLogout,
  onOpenDemoModal,
  activeView,
  setActiveView,
}) => {
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const fetchNotifications = async () => {
    if (!user) return;
    try {
      const count = await notificationService.getUnreadCount();
      setUnreadCount(count);
      const res = await notificationService.getNotifications(0, 5);
      setNotifications(res.content);
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 12000);
    return () => clearInterval(interval);
  }, [user]);

  const handleMarkAsRead = async (id: number) => {
    try {
      await notificationService.markAsRead(id);
      fetchNotifications();
    } catch {
      // ignore
    }
  };

  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'NETWORK_ADMIN':
        return <span className="bg-amber-100 text-amber-800 text-xs font-semibold px-2.5 py-0.5 rounded-full border border-amber-200">Network Admin</span>;
      case 'HOSPITAL_USER':
        return <span className="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded-full border border-blue-200">Hospital Staff</span>;
      case 'BLOOD_BANK_USER':
        return <span className="bg-rose-100 text-rose-800 text-xs font-semibold px-2.5 py-0.5 rounded-full border border-rose-200">Blood Bank</span>;
      case 'AUDITOR':
        return <span className="bg-slate-100 text-slate-800 text-xs font-semibold px-2.5 py-0.5 rounded-full border border-slate-200">Auditor</span>;
      default:
        return null;
    }
  };

  return (
    <header className="sticky top-0 z-50 bg-white/95 backdrop-blur-md border-b border-slate-200 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo & Brand */}
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => setActiveView(user ? 'dashboard' : 'landing')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-rose-600 to-red-500 flex items-center justify-center text-white shadow-md shadow-rose-500/20">
              <Heart className="w-5 h-5 fill-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xl font-bold tracking-tight text-slate-900">LifeLink</span>
                <span className="hidden sm:inline-block bg-rose-50 text-rose-700 text-[10px] font-semibold px-2 py-0.5 rounded-full border border-rose-200">
                  Network Active
                </span>
              </div>
              <p className="text-[11px] text-slate-500 hidden sm:block">Smart Blood Bank & Hospital Coordination</p>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center gap-1">
            {!user ? (
              <>
                <button
                  onClick={() => setActiveView('landing')}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    activeView === 'landing' ? 'bg-slate-100 text-slate-900' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`}
                >
                  Overview
                </button>
                <button
                  onClick={() => setActiveView('matching-public')}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                    activeView === 'matching-public' ? 'bg-slate-100 text-slate-900' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`}
                >
                  <Search className="w-4 h-4 text-rose-600" />
                  Find Availability
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => setActiveView('dashboard')}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                    activeView === 'dashboard' ? 'bg-rose-50 text-rose-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`}
                >
                  <Activity className="w-4 h-4" />
                  Dashboard
                </button>

                <button
                  onClick={() => setActiveView('matching')}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                    activeView === 'matching' ? 'bg-rose-50 text-rose-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                  }`}
                >
                  <Search className="w-4 h-4" />
                  Availability Search
                </button>

                {user.role === 'NETWORK_ADMIN' && (
                  <button
                    onClick={() => setActiveView('admin')}
                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                      activeView === 'admin' ? 'bg-rose-50 text-rose-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                    }`}
                  >
                    <Building2 className="w-4 h-4" />
                    Admin Portal
                  </button>
                )}

                {(user.role === 'NETWORK_ADMIN' || user.role === 'AUDITOR') && (
                  <button
                    onClick={() => setActiveView('audit')}
                    className={`px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
                      activeView === 'audit' ? 'bg-rose-50 text-rose-700 font-semibold' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
                    }`}
                  >
                    <ShieldAlert className="w-4 h-4" />
                    Audit Log
                  </button>
                )}
              </>
            )}
          </nav>

          {/* Right Action Items */}
          <div className="flex items-center gap-2 sm:gap-3">
            {/* Demo Accounts Quick-Trigger */}
            <button
              onClick={onOpenDemoModal}
              className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 border border-slate-300 px-2.5 py-1.5 rounded-lg transition-colors cursor-pointer"
              title="View Simulated Demo Logins and Seed Demo Network"
            >
              <KeyRound className="w-3.5 h-3.5 text-amber-600" />
              <span className="hidden sm:inline">Demo Access</span>
            </button>

            {user ? (
              <>
                {/* Notification Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setShowNotifications(!showNotifications)}
                    className="p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 relative cursor-pointer"
                    aria-label="View notifications"
                  >
                    <Bell className="w-5 h-5" />
                    {unreadCount > 0 && (
                      <span className="absolute top-1 right-1 w-4 h-4 bg-rose-600 text-white text-[10px] font-bold rounded-full flex items-center justify-center ring-2 ring-white">
                        {unreadCount > 9 ? '9+' : unreadCount}
                      </span>
                    )}
                  </button>

                  {/* Notification Flyout */}
                  {showNotifications && (
                    <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-xl shadow-xl border border-slate-200 py-2 z-50 animate-in fade-in slide-in-from-top-2">
                      <div className="px-4 py-2 border-b border-slate-100 flex justify-between items-center">
                        <span className="font-semibold text-sm text-slate-900">Notifications</span>
                        <span className="text-xs text-slate-500">{unreadCount} unread</span>
                      </div>
                      <div className="max-h-72 overflow-y-auto divide-y divide-slate-100">
                        {notifications.length === 0 ? (
                          <div className="px-4 py-6 text-center text-xs text-slate-500">
                            No notifications yet
                          </div>
                        ) : (
                          notifications.map((n) => (
                            <div
                              key={n.id}
                              onClick={() => handleMarkAsRead(n.id)}
                              className={`px-4 py-3 hover:bg-slate-50 transition-colors cursor-pointer ${
                                !n.isRead ? 'bg-rose-50/40' : ''
                              }`}
                            >
                              <div className="flex justify-between items-start gap-2">
                                <h4 className="text-xs font-semibold text-slate-900">{n.title}</h4>
                                <span className="text-[10px] text-slate-400 whitespace-nowrap">
                                  {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                </span>
                              </div>
                              <p className="text-xs text-slate-600 mt-1 line-clamp-2">{n.message}</p>
                            </div>
                          ))
                        )}
                      </div>
                      <div className="px-4 py-1.5 border-t border-slate-100 text-center">
                        <button
                          onClick={() => {
                            setShowNotifications(false);
                            setActiveView('notifications');
                          }}
                          className="text-xs text-rose-600 hover:text-rose-700 font-medium"
                        >
                          View all alerts
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                {/* User info & Logout */}
                <div className="hidden lg:flex items-center gap-2 pl-2 border-l border-slate-200">
                  <div className="text-right">
                    <p className="text-xs font-semibold text-slate-900">{user.fullName}</p>
                    <p className="text-[11px] text-slate-500 truncate max-w-[140px]">{user.facilityName || 'System Admin'}</p>
                  </div>
                  {getRoleBadge(user.role)}
                </div>

                <button
                  onClick={onLogout}
                  className="p-2 text-slate-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors cursor-pointer"
                  title="Sign out"
                >
                  <LogOut className="w-5 h-5" />
                </button>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setActiveView('login')}
                  className="px-3.5 py-1.5 text-xs font-medium text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
                >
                  Sign In
                </button>
                <button
                  onClick={() => setActiveView('register')}
                  className="px-3.5 py-1.5 text-xs font-medium text-white bg-rose-600 hover:bg-rose-700 rounded-lg transition-colors shadow-xs shadow-rose-600/30 cursor-pointer"
                >
                  Register Facility
                </button>
              </div>
            )}

            {/* Mobile menu button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-slate-600 hover:text-slate-900 rounded-lg"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Navigation Dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-slate-200 py-3 space-y-1">
            {!user ? (
              <>
                <button
                  onClick={() => { setActiveView('landing'); setMobileMenuOpen(false); }}
                  className="block w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-lg"
                >
                  Overview
                </button>
                <button
                  onClick={() => { setActiveView('matching-public'); setMobileMenuOpen(false); }}
                  className="block w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-lg"
                >
                  Find Availability
                </button>
                <button
                  onClick={() => { setActiveView('login'); setMobileMenuOpen(false); }}
                  className="block w-full text-left px-3 py-2 text-sm font-medium text-rose-600 hover:bg-rose-50 rounded-lg"
                >
                  Sign In
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={() => { setActiveView('dashboard'); setMobileMenuOpen(false); }}
                  className="block w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-lg"
                >
                  Dashboard
                </button>
                <button
                  onClick={() => { setActiveView('matching'); setMobileMenuOpen(false); }}
                  className="block w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-lg"
                >
                  Availability Search
                </button>
                {user.role === 'NETWORK_ADMIN' && (
                  <button
                    onClick={() => { setActiveView('admin'); setMobileMenuOpen(false); }}
                    className="block w-full text-left px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 rounded-lg"
                  >
                    Admin Portal
                  </button>
                )}
                <button
                  onClick={() => { onLogout(); setMobileMenuOpen(false); }}
                  className="block w-full text-left px-3 py-2 text-sm text-rose-600 hover:bg-rose-50 rounded-lg"
                >
                  Sign Out
                </button>
              </>
            )}
          </div>
        )}
      </div>
    </header>
  );
};
