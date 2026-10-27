import React, { useState } from 'react';
import { X, KeyRound, Sparkles, Check, Copy, AlertTriangle, ShieldCheck } from 'lucide-react';
import { demoService } from '../services/demoService';

interface DemoCredentialsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onQuickLogin: (email: string, pass: string) => void;
}

export const DemoCredentialsModal: React.FC<DemoCredentialsModalProps> = ({
  isOpen,
  onClose,
  onQuickLogin,
}) => {
  const [seeding, setSeeding] = useState(false);
  const [seedResult, setSeedResult] = useState<string | null>(null);
  const [copiedKey, setCopiedKey] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSeedNetwork = async () => {
    setSeeding(true);
    setSeedResult(null);
    try {
      const res = await demoService.seedNetwork();
      setSeedResult(typeof res === 'string' ? res : 'Simulated demo network seeded successfully!');
    } catch (e: any) {
      setSeedResult(e.message || 'Seeding complete (or already seeded).');
    } finally {
      setSeeding(false);
    }
  };

  const copyToClipboard = (text: string, key: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const demoAccounts = [
    {
      title: 'Hospital Coordinator',
      role: 'HOSPITAL_USER',
      facility: 'Metro City General Hospital (DEMO)',
      email: 'hospital@lifelink.demo',
      pass: 'Demo@Hospital2026!',
      desc: 'Can search multi-bank availability, submit blood requests, and confirm transfer receipt.',
      color: 'blue',
    },
    {
      title: 'Blood Bank Officer',
      role: 'BLOOD_BANK_USER',
      facility: 'Red Cross Central Blood Center (DEMO)',
      email: 'bloodbank@lifelink.demo',
      pass: 'Demo@BloodBank2026!',
      desc: 'Can manage batch inventory, accept/reject hospital requests, and dispatch cold-chain transfers.',
      color: 'rose',
    },
    {
      title: 'Network Administrator',
      role: 'NETWORK_ADMIN',
      facility: 'LifeLink Central Network',
      email: 'admin@lifelink.network',
      pass: 'Admin@LifeLink2026!',
      desc: 'Can approve/suspend registered facilities, inspect security audit logs, and monitor shortages.',
      color: 'amber',
    },
  ];

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
        {/* Header */}
        <div className="bg-gradient-to-r from-slate-900 to-slate-800 px-6 py-4 text-white flex justify-between items-center">
          <div className="flex items-center gap-2">
            <KeyRound className="w-5 h-5 text-amber-400" />
            <h3 className="font-bold text-base">Demonstration Credentials & Network Setup</h3>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* Simulated Disclaimer */}
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-3.5 flex gap-3 text-xs text-amber-900">
            <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold">Simulated Demonstration Environment: </span>
              All inventory, hospitals, and blood banks shown under demo accounts are simulated test models.
              Never rely on demonstration data for clinical emergency requests.
            </div>
          </div>

          {/* Seed Button Section */}
          <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row justify-between items-center gap-4">
            <div>
              <h4 className="text-sm font-semibold text-slate-900 flex items-center gap-1.5">
                <Sparkles className="w-4 h-4 text-rose-600" />
                Initialize Demo Facilities & Inventory
              </h4>
              <p className="text-xs text-slate-500 mt-0.5">
                Populates 1 Hospital, 3 Blood Banks, multi-group blood batches, and a sample active transfer.
              </p>
            </div>
            <button
              onClick={handleSeedNetwork}
              disabled={seeding}
              className="w-full sm:w-auto px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-semibold shadow-xs disabled:opacity-50 flex items-center justify-center gap-1.5 transition-all cursor-pointer whitespace-nowrap"
            >
              {seeding ? (
                <>Seeding Network...</>
              ) : (
                <>
                  <Sparkles className="w-3.5 h-3.5" />
                  Seed Demo Data
                </>
              )}
            </button>
          </div>

          {seedResult && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-xs text-emerald-800 flex items-center gap-2">
              <Check className="w-4 h-4 text-emerald-600" />
              <span>{seedResult}</span>
            </div>
          )}

          {/* Accounts List */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
              1-Click Demo Logins
            </h4>

            {demoAccounts.map((acc) => (
              <div
                key={acc.email}
                className="border border-slate-200 rounded-xl p-4 hover:border-slate-300 transition-all bg-white shadow-2xs flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3"
              >
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-slate-900">{acc.title}</span>
                    <span className="text-[10px] font-semibold bg-slate-100 text-slate-700 px-2 py-0.5 rounded-full border border-slate-200">
                      {acc.facility}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500">{acc.desc}</p>
                  <div className="flex items-center gap-3 text-xs text-slate-600 pt-1 font-mono">
                    <span>{acc.email}</span>
                    <span className="text-slate-300">•</span>
                    <span>{acc.pass}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2 w-full sm:w-auto">
                  <button
                    onClick={() => copyToClipboard(`${acc.email} / ${acc.pass}`, acc.email)}
                    className="p-2 border border-slate-200 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-50 text-xs transition-colors cursor-pointer"
                    title="Copy credentials"
                  >
                    {copiedKey === acc.email ? <Check className="w-4 h-4 text-emerald-600" /> : <Copy className="w-4 h-4" />}
                  </button>
                  <button
                    onClick={() => {
                      onQuickLogin(acc.email, acc.pass);
                      onClose();
                    }}
                    className="flex-1 sm:flex-none px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition-colors cursor-pointer"
                  >
                    Login as {acc.title.split(' ')[0]}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-slate-50 px-6 py-3 border-t border-slate-100 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-white border border-slate-200 hover:bg-slate-100 text-slate-700 rounded-lg text-xs font-semibold cursor-pointer"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
