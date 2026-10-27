import React, { useState, useEffect } from 'react';
import {
  ShieldAlert,
  Building2,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Sparkles,
  Layers,
  Activity,
  FileText,
  Clock,
  ArrowRight
} from 'lucide-react';
import { facilityService } from '../services/facilityService';
import { analyticsService } from '../services/analyticsService';
import { requestService } from '../services/requestService';
import { demoService } from '../services/demoService';
import type { Facility, NetworkAnalytics, ShortageMatrixItem, BloodRequest, User } from '../types';

interface AdminDashboardProps {
  user: User;
  onNavigateToAudit: () => void;
}

export const AdminDashboard: React.FC<AdminDashboardProps> = ({ user, onNavigateToAudit }) => {
  const [pendingFacilities, setPendingFacilities] = useState<Facility[]>([]);
  const [analytics, setAnalytics] = useState<NetworkAnalytics | null>(null);
  const [shortages, setShortages] = useState<ShortageMatrixItem[]>([]);
  const [allRequests, setAllRequests] = useState<BloodRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [seeding, setSeeding] = useState(false);
  const [actionMsg, setActionMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [pendPage, netData, shortData, reqPage] = await Promise.all([
        facilityService.getAllForAdmin('PENDING', 0, 20),
        analyticsService.getNetworkAnalytics(),
        analyticsService.getShortageMatrix(),
        requestService.getAllRequests(undefined, 0, 20),
      ]);
      setPendingFacilities(pendPage.content);
      setAnalytics(netData);
      setShortages(shortData);
      setAllRequests(reqPage.content);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleApproval = async (facilityId: number, approve: boolean) => {
    const notes = prompt(approve ? 'Enter approval verification notes:' : 'Enter rejection reason:');
    if (notes === null) return;
    try {
      await facilityService.updateApproval(facilityId, approve, notes || 'Admin verified');
      setActionMsg({
        type: 'success',
        text: `Facility ${approve ? 'approved' : 'rejected'} successfully.`
      });
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Action failed.' });
    }
  };

  const handleSeedDemo = async () => {
    if (!confirm('Populate the platform with simulated demo facilities (1 Hospital, 3 Blood Banks) and inventory?')) return;
    setSeeding(true);
    try {
      const res = await demoService.seedNetwork();
      setActionMsg({ type: 'success', text: typeof res === 'string' ? res : 'Simulated demo network seeded successfully!' });
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Seeding failed.' });
    } finally {
      setSeeding(false);
    }
  };

  const criticalShortagesCount = shortages.filter(s => s.shortageSeverity === 'CRITICAL').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 rounded-3xl p-6 sm:p-8 text-white shadow-xl flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-amber-400 bg-amber-950/80 px-2.5 py-1 rounded-full border border-amber-800">
              Network Operations Control
            </span>
            <span className="text-xs text-slate-400">•</span>
            <span className="text-xs font-medium text-slate-300">Administrator Portal</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight mt-1">
            System Administration & Network Oversight
          </h1>
          <p className="text-xs text-slate-400 mt-0.5">
            Verify facility accreditations, monitor regional blood shortages, and inspect security audit records.
          </p>
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <button
            onClick={onNavigateToAudit}
            className="flex-1 md:flex-none px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
          >
            <ShieldAlert className="w-4 h-4 text-indigo-400" />
            Security Audit Logs
          </button>

          <button
            onClick={handleSeedDemo}
            disabled={seeding}
            className="flex-1 md:flex-none px-5 py-2.5 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold shadow-md shadow-rose-950/40 flex items-center justify-center gap-1.5 transition-all cursor-pointer whitespace-nowrap disabled:opacity-50"
          >
            <Sparkles className="w-4 h-4" />
            {seeding ? 'Seeding...' : 'Seed Demo Data'}
          </button>
        </div>
      </div>

      {actionMsg && (
        <div className={`p-4 rounded-xl border text-xs flex justify-between items-center ${
          actionMsg.type === 'success' ? 'bg-emerald-50 border-emerald-200 text-emerald-800' : 'bg-rose-50 border-rose-200 text-rose-800'
        }`}>
          <span>{actionMsg.text}</span>
          <button onClick={() => setActionMsg(null)} className="font-bold ml-2">✕</button>
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Pending Approvals</span>
          <p className="text-2xl font-extrabold text-amber-600 mt-1">{pendingFacilities.length}</p>
          <span className="text-[11px] text-amber-600 font-medium">Facilities awaiting review</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Total Network Facilities</span>
          <p className="text-2xl font-extrabold text-slate-900 mt-1">
            {analytics ? analytics.totalApprovedHospitals + analytics.totalApprovedBloodBanks : 0}
          </p>
          <span className="text-[11px] text-slate-400">Approved & active</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Reported Available Units</span>
          <p className="text-2xl font-extrabold text-emerald-600 mt-1">
            {analytics?.totalAvailableUnitsInNetwork || 0}
          </p>
          <span className="text-[11px] text-emerald-600 font-medium">Across all blood banks</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Critical Shortages</span>
          <p className="text-2xl font-extrabold text-rose-600 mt-1">{criticalShortagesCount}</p>
          <span className="text-[11px] text-rose-600 font-medium">Groups &lt; 3 units</span>
        </div>
      </div>

      {/* Pending Facility Approvals Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-amber-50/40">
          <div className="flex items-center gap-2">
            <Building2 className="w-5 h-5 text-amber-600" />
            <h2 className="font-bold text-sm text-slate-900">Pending Facility Accreditation Reviews</h2>
          </div>
          <span className="text-xs text-amber-800 font-semibold">{pendingFacilities.length} awaiting action</span>
        </div>

        {pendingFacilities.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            No pending facility registrations at this time. All institutions verified.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Facility Name</th>
                  <th className="px-6 py-3">Type</th>
                  <th className="px-6 py-3">License Number</th>
                  <th className="px-6 py-3">Location</th>
                  <th className="px-6 py-3">Contact Email / Phone</th>
                  <th className="px-6 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {pendingFacilities.map((fac) => (
                  <tr key={fac.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-3 font-semibold text-slate-900">{fac.name}</td>
                    <td className="px-6 py-3">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        fac.type === 'HOSPITAL' ? 'bg-blue-50 text-blue-700' : 'bg-rose-50 text-rose-700'
                      }`}>
                        {fac.type}
                      </span>
                    </td>
                    <td className="px-6 py-3 font-mono text-slate-600">{fac.licenseNumber}</td>
                    <td className="px-6 py-3 text-slate-600">{fac.city}, {fac.state}</td>
                    <td className="px-6 py-3 text-slate-500">{fac.contactEmail} • {fac.contactPhone}</td>
                    <td className="px-6 py-3 text-right space-x-2">
                      <button
                        onClick={() => handleApproval(fac.id, true)}
                        className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold cursor-pointer"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => handleApproval(fac.id, false)}
                        className="px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg text-xs font-semibold cursor-pointer"
                      >
                        Reject
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Network Blood Shortage Matrix */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Layers className="w-5 h-5 text-rose-600" />
            <h2 className="font-bold text-sm text-slate-900">Network Blood Group Shortage Matrix</h2>
          </div>
          <span className="text-xs text-slate-400">Automated safety threshold: 10 units</span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-6">
          {shortages.map((s) => (
            <div
              key={s.bloodGroup}
              className={`p-4 rounded-xl border flex flex-col justify-between ${
                s.shortageSeverity === 'CRITICAL'
                  ? 'bg-rose-50/70 border-rose-200'
                  : s.shortageSeverity === 'LOW'
                  ? 'bg-amber-50/70 border-amber-200'
                  : 'bg-emerald-50/40 border-emerald-200'
              }`}
            >
              <div className="flex justify-between items-center">
                <span className="text-lg font-extrabold text-slate-900">{s.bloodGroup}</span>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                  s.shortageSeverity === 'CRITICAL'
                    ? 'bg-rose-600 text-white'
                    : s.shortageSeverity === 'LOW'
                    ? 'bg-amber-600 text-white'
                    : 'bg-emerald-600 text-white'
                }`}>
                  {s.shortageSeverity}
                </span>
              </div>
              <div className="mt-3">
                <p className="text-2xl font-black text-slate-900">{s.availableUnits}</p>
                <p className="text-[11px] text-slate-500 mt-0.5">Threshold: {s.minimumSafetyThreshold}u</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Network Request Activity Overview */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-blue-600" />
            <h2 className="font-bold text-sm text-slate-900">Network Request Stream ({allRequests.length})</h2>
          </div>
          <span className="text-xs text-slate-400">Cross-facility coordination tracking</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
              <tr>
                <th className="px-6 py-3">Reference</th>
                <th className="px-6 py-3">Hospital</th>
                <th className="px-6 py-3">Need</th>
                <th className="px-6 py-3">Urgency</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3">Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {allRequests.slice(0, 10).map((r) => (
                <tr key={r.id} className="hover:bg-slate-50/50">
                  <td className="px-6 py-3 font-mono font-semibold text-slate-900">{r.requestReference}</td>
                  <td className="px-6 py-3 font-medium text-slate-800">{r.hospitalName}</td>
                  <td className="px-6 py-3">
                    <span className="font-bold text-rose-600">{r.unitsRequested}u</span> {r.bloodGroupDisplay} ({r.componentTypeDisplay})
                  </td>
                  <td className="px-6 py-3">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      r.urgency === 'CRITICAL' ? 'bg-red-100 text-red-800' : r.urgency === 'URGENT' ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-800'
                    }`}>
                      {r.urgency}
                    </span>
                  </td>
                  <td className="px-6 py-3 font-semibold text-slate-700">{r.status}</td>
                  <td className="px-6 py-3 text-slate-400">{new Date(r.createdAt).toLocaleTimeString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
