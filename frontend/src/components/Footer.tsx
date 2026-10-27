import React from 'react';
import { Heart, ShieldCheck, Database, Server, AlertCircle } from 'lucide-react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-900 text-slate-300 border-t border-slate-800 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand & Purpose */}
          <div className="md:col-span-2 space-y-3">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-rose-600 flex items-center justify-center text-white">
                <Heart className="w-4 h-4 fill-white" />
              </div>
              <span className="text-xl font-bold tracking-tight text-white">LifeLink</span>
            </div>
            <p className="text-sm text-slate-400 max-w-md">
              Smart Blood Bank Network & Hospital Coordination System. Connecting verified blood facilities,
              eliminating visibility fragmentation, and accelerating life-saving transfusion logistics.
            </p>
            <div className="flex flex-wrap gap-2 pt-2">
              <span className="inline-flex items-center gap-1 text-[11px] font-medium bg-slate-800 text-slate-300 px-2.5 py-1 rounded-md border border-slate-700">
                <Server className="w-3 h-3 text-emerald-400" />
                Java 21 / Spring Boot
              </span>
              <span className="inline-flex items-center gap-1 text-[11px] font-medium bg-slate-800 text-slate-300 px-2.5 py-1 rounded-md border border-slate-700">
                <Database className="w-3 h-3 text-cyan-400" />
                Aiven PostgreSQL + Flyway
              </span>
              <span className="inline-flex items-center gap-1 text-[11px] font-medium bg-slate-800 text-slate-300 px-2.5 py-1 rounded-md border border-slate-700">
                <ShieldCheck className="w-3 h-3 text-indigo-400" />
                SSL / TLS Encrypted
              </span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-sm font-semibold text-white uppercase tracking-wider mb-3">Coordination Platform</h4>
            <ul className="space-y-2 text-xs text-slate-400">
              <li>Multi-Bank Blood Matching</li>
              <li>Expiry-Aware Inventory (FIFO)</li>
              <li>Duplicate-Reservation Safety</li>
              <li>Cold-Chain Custody Tracking</li>
              <li>Network Shortage Analytics</li>
            </ul>
          </div>

          {/* Responsible Use Notice */}
          <div>
            <h4 className="text-sm font-semibold text-rose-400 uppercase tracking-wider mb-3 flex items-center gap-1">
              <AlertCircle className="w-4 h-4" />
              Responsible Use
            </h4>
            <p className="text-xs text-slate-400 leading-relaxed">
              LifeLink is an administrative coordination platform. It does not replace medical judgment,
              antibody screening, laboratory crossmatching, or certified blood-bank release protocols.
            </p>
          </div>
        </div>

        <div className="border-t border-slate-800 mt-8 pt-6 flex flex-col sm:flex-row justify-between items-center text-xs text-slate-500 gap-3">
          <p>© {new Date().getFullYear()} LifeLink Network. College Java OOP & Healthcare Coordination Project.</p>
          <p>Tagline: "Find Availability. Coordinate Faster. Support Every Life."</p>
        </div>
      </div>
    </footer>
  );
};
