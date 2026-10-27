import React, { useState } from 'react';
import { Heart, Lock, Mail, ArrowRight, AlertCircle, Building2, ShieldCheck, KeyRound } from 'lucide-react';
import { authService } from '../services/authService';
import type { User } from '../types';

interface LoginPageProps {
  onLoginSuccess: (user: User) => void;
  onNavigate: (view: string) => void;
  onOpenDemo: () => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess, onNavigate, onOpenDemo }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await authService.login(email, password);
      onLoginSuccess(res.user);
    } catch (err: any) {
      setError(err.message || 'Login failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const fillCredentials = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError(null);
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 sm:px-6 lg:px-8 py-12">
      <div className="max-w-md w-full space-y-6">
        {/* Brand header */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-rose-600 to-red-500 mx-auto flex items-center justify-center text-white shadow-lg shadow-rose-500/20">
            <Heart className="w-6 h-6 fill-white" />
          </div>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Sign In to LifeLink
          </h2>
          <p className="text-xs text-slate-500">
            Access your facility dashboard and coordinate blood transfers
          </p>
        </div>

        {/* Quick Demo Pre-fill Pills */}
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-3 space-y-2">
          <div className="flex justify-between items-center text-[11px] font-semibold text-slate-600">
            <span className="flex items-center gap-1">
              <KeyRound className="w-3.5 h-3.5 text-amber-500" /> Quick-Fill Demo Logins:
            </span>
            <button
              onClick={onOpenDemo}
              className="text-rose-600 hover:underline cursor-pointer"
            >
              Setup details
            </button>
          </div>
          <div className="grid grid-cols-3 gap-1.5 text-xs">
            <button
              type="button"
              onClick={() => fillCredentials('hospital@lifelink.demo', 'Demo@Hospital2026!')}
              className="px-2 py-1.5 bg-white border border-slate-200 rounded-lg hover:border-blue-400 hover:bg-blue-50/50 text-[11px] font-medium text-slate-700 transition-colors cursor-pointer text-center"
            >
              Hospital
            </button>
            <button
              type="button"
              onClick={() => fillCredentials('bloodbank@lifelink.demo', 'Demo@BloodBank2026!')}
              className="px-2 py-1.5 bg-white border border-slate-200 rounded-lg hover:border-rose-400 hover:bg-rose-50/50 text-[11px] font-medium text-slate-700 transition-colors cursor-pointer text-center"
            >
              Blood Bank
            </button>
            <button
              type="button"
              onClick={() => fillCredentials('admin@lifelink.network', 'Admin@LifeLink2026!')}
              className="px-2 py-1.5 bg-white border border-slate-200 rounded-lg hover:border-amber-400 hover:bg-amber-50/50 text-[11px] font-medium text-slate-700 transition-colors cursor-pointer text-center"
            >
              Admin
            </button>
          </div>
        </div>

        {/* Form Card */}
        <div className="bg-white p-6 sm:p-8 rounded-2xl shadow-xl border border-slate-200/80 space-y-5">
          {error && (
            <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-800">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Facility Email Address
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="e.g. coordinator@hospital.com"
                  required
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-xs text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-rose-500/20 focus:border-rose-600 transition-all"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  required
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-xs text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-rose-500/20 focus:border-rose-600 transition-all"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-md shadow-rose-600/25 flex items-center justify-center gap-2 transition-all disabled:opacity-50 cursor-pointer"
            >
              {loading ? 'Authenticating with LifeLink...' : 'Sign In to Portal'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          <div className="text-center pt-2 border-t border-slate-100">
            <p className="text-xs text-slate-500">
              New facility?{' '}
              <button
                type="button"
                onClick={() => onNavigate('register')}
                className="text-rose-600 font-semibold hover:underline cursor-pointer"
              >
                Register Hospital or Blood Bank
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
