import React, { useState, useEffect } from 'react';
import { ShieldCheck, ShieldAlert, Clock, Filter, FileText, CheckCircle2 } from 'lucide-react';
import { analyticsService } from '../services/analyticsService';
import type { AuditLog, User } from '../types';

interface AuditorViewProps {
  user: User;
}

export const AuditorView: React.FC<AuditorViewProps> = ({ user }) => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchLogs = async (p = 0) => {
    setLoading(true);
    try {
      const res = await analyticsService.getAuditLogs(undefined, p, 20);
      setLogs(res.content);
      setPage(res.page);
      setTotalPages(res.totalPages);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(0);
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header Banner */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-xs flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-slate-800 bg-slate-100 px-2.5 py-1 rounded-full border border-slate-200">
              Compliance & Security Review
            </span>
            <span className="text-xs text-slate-400">•</span>
            <span className="text-xs font-medium text-slate-500">Read-Only Audit Trail</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-1">
            System & Custody Audit Ledger
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Immutable chronological records of inventory alterations, stock reservations, custody transfers, and logins.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-xl border border-emerald-200">
            <ShieldCheck className="w-4 h-4 text-emerald-600" />
            Append-Only Database Ledger
          </span>
        </div>
      </div>

      {/* Audit Logs Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-indigo-600" />
            <h2 className="font-bold text-sm text-slate-900">Chronological Event Logs</h2>
          </div>
          <span className="text-xs text-slate-400">Page {page + 1} of {totalPages}</span>
        </div>

        {logs.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            No audit records logged yet.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Timestamp (UTC)</th>
                  <th className="px-6 py-3">Action</th>
                  <th className="px-6 py-3">Actor / Email</th>
                  <th className="px-6 py-3">Role</th>
                  <th className="px-6 py-3">Target Entity</th>
                  <th className="px-6 py-3">Operational Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-3 text-slate-500 font-mono whitespace-nowrap">
                      {new Date(log.timestamp).toLocaleString()}
                    </td>
                    <td className="px-6 py-3 font-mono font-bold text-slate-900">
                      <span className="bg-slate-100 px-2 py-0.5 rounded text-[11px] border border-slate-200">
                        {log.action}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-slate-700 font-medium">{log.userEmail || 'System Process'}</td>
                    <td className="px-6 py-3">
                      <span className="text-[10px] font-semibold bg-slate-50 text-slate-600 px-2 py-0.5 rounded-full border border-slate-200">
                        {log.userRole || 'SYSTEM'}
                      </span>
                    </td>
                    <td className="px-6 py-3 font-mono text-slate-600">
                      {log.entityType} {log.entityId ? `#${log.entityId}` : ''}
                    </td>
                    <td className="px-6 py-3 text-slate-600 max-w-sm truncate" title={log.details || ''}>
                      {log.details || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination controls */}
        {totalPages > 1 && (
          <div className="px-6 py-3 border-t border-slate-100 flex justify-between items-center bg-slate-50">
            <button
              onClick={() => fetchLogs(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-3 py-1 bg-white border border-slate-200 rounded text-xs font-medium disabled:opacity-50"
            >
              Previous
            </button>
            <span className="text-xs text-slate-500">Page {page + 1} of {totalPages}</span>
            <button
              onClick={() => fetchLogs(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="px-3 py-1 bg-white border border-slate-200 rounded text-xs font-medium disabled:opacity-50"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
