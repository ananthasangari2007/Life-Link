import React, { useState, useEffect } from 'react';
import {
  Activity,
  Plus,
  Search,
  Clock,
  Truck,
  CheckCircle2,
  AlertTriangle,
  Building2,
  ArrowRight,
  ShieldCheck,
  X,
  FileText
} from 'lucide-react';
import { requestService } from '../services/requestService';
import { transferService } from '../services/transferService';
import { facilityService } from '../services/facilityService';
import { StatusTimeline } from '../components/StatusTimeline';
import type {
  BloodRequest,
  BloodTransfer,
  Facility,
  User,
  BloodGroup,
  ComponentType,
  RequestUrgency
} from '../types';

interface HospitalDashboardProps {
  user: User;
  onNavigateToMatching: () => void;
  presetRequestParams?: { bloodGroup: BloodGroup; componentType: ComponentType; units: number } | null;
  clearPresetParams?: () => void;
}

export const HospitalDashboard: React.FC<HospitalDashboardProps> = ({
  user,
  onNavigateToMatching,
  presetRequestParams,
  clearPresetParams,
}) => {
  const [requests, setRequests] = useState<BloodRequest[]>([]);
  const [transfers, setTransfers] = useState<BloodTransfer[]>([]);
  const [bloodBanks, setBloodBanks] = useState<Facility[]>([]);
  const [loading, setLoading] = useState(true);

  // New Request Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [bloodGroup, setBloodGroup] = useState<BloodGroup>('A_POSITIVE');
  const [componentType, setComponentType] = useState<ComponentType>('RED_BLOOD_CELLS');
  const [unitsRequested, setUnitsRequested] = useState<number>(4);
  const [urgency, setUrgency] = useState<RequestUrgency>('URGENT');
  const [requiredByHours, setRequiredByHours] = useState<number>(6);
  const [patientRef, setPatientRef] = useState<string>('');
  const [notes, setNotes] = useState<string>('');
  const [selectedBanks, setSelectedBanks] = useState<number[]>([]);
  const [actionMsg, setActionMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchData = async () => {
    if (!user.facilityId) return;
    setLoading(true);
    try {
      const [reqPage, transPage, banks] = await Promise.all([
        requestService.getHospitalRequests(user.facilityId, 0, 20),
        transferService.getDestinationTransfers(user.facilityId, 0, 20),
        facilityService.getPublicApproved('BLOOD_BANK'),
      ]);
      setRequests(reqPage.content);
      setTransfers(transPage.content);
      setBloodBanks(banks);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  useEffect(() => {
    if (presetRequestParams) {
      setBloodGroup(presetRequestParams.bloodGroup);
      setComponentType(presetRequestParams.componentType);
      setUnitsRequested(presetRequestParams.units);
      setModalOpen(true);
      if (clearPresetParams) clearPresetParams();
    }
  }, [presetRequestParams]);

  const handleCreateRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setActionMsg(null);
    try {
      const requiredBy = new Date(Date.now() + requiredByHours * 3600 * 1000).toISOString();
      await requestService.createRequest({
        bloodGroup,
        componentType,
        unitsRequested,
        urgency,
        requiredBy,
        patientReferenceCode: patientRef,
        clinicalNotes: notes,
        targetBloodBankIds: selectedBanks.length > 0 ? selectedBanks : undefined,
      });
      setActionMsg({ type: 'success', text: 'Blood request submitted to matching blood banks successfully!' });
      setModalOpen(false);
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Failed to submit blood request.' });
    } finally {
      setSubmitting(false);
    }
  };

  const handleConfirmReceipt = async (transferId: number) => {
    if (!confirm('Confirm safe physical receipt of blood components and cold-chain integrity?')) return;
    try {
      await transferService.confirmReceipt(transferId, 'Receipt confirmed by authorized hospital staff');
      setActionMsg({ type: 'success', text: 'Transfer receipt confirmed! Request marked as FULFILLED.' });
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Receipt confirmation failed.' });
    }
  };

  const handleCancelRequest = async (requestId: number) => {
    if (!confirm('Are you sure you want to cancel this request? Any active reservations will be released.')) return;
    try {
      await requestService.cancelRequest(requestId, 'Cancelled by hospital staff');
      setActionMsg({ type: 'success', text: 'Request cancelled and reserved stock released.' });
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Cancellation failed.' });
    }
  };

  // Metrics
  const openRequestsCount = requests.filter(r => !r.status.includes('FULFILLED') && r.status !== 'CANCELLED' && r.status !== 'REJECTED').length;
  const inTransitCount = transfers.filter(t => t.status === 'IN_TRANSIT').length;
  const fulfilledCount = requests.filter(r => r.status === 'FULFILLED').length;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header Banner */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-xs flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-blue-700 bg-blue-50 px-2.5 py-1 rounded-full border border-blue-200">
              Hospital Coordination Hub
            </span>
            <span className="text-xs text-slate-400">•</span>
            <span className="text-xs font-medium text-slate-500">{user.facilityName}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-1">
            Emergency Blood Coordination
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage outgoing blood requests, review multi-bank offers, and verify incoming transfer dispatches.
          </p>
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <button
            onClick={onNavigateToMatching}
            className="flex-1 md:flex-none px-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
          >
            <Search className="w-4 h-4 text-rose-600" />
            Check Availability
          </button>

          <button
            onClick={() => setModalOpen(true)}
            className="flex-1 md:flex-none px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-md shadow-rose-600/25 flex items-center justify-center gap-1.5 transition-all cursor-pointer whitespace-nowrap"
          >
            <Plus className="w-4 h-4" />
            New Blood Request
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
          <div className="flex justify-between items-start">
            <span className="text-xs font-medium text-slate-500">Active Requests</span>
            <Clock className="w-4 h-4 text-blue-600" />
          </div>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{openRequestsCount}</p>
          <span className="text-[11px] text-blue-600 font-medium">Awaiting fulfillment</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <div className="flex justify-between items-start">
            <span className="text-xs font-medium text-slate-500">In Transit Transfers</span>
            <Truck className="w-4 h-4 text-amber-600" />
          </div>
          <p className="text-2xl font-extrabold text-amber-600 mt-2">{inTransitCount}</p>
          <span className="text-[11px] text-slate-500">En route from blood banks</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <div className="flex justify-between items-start">
            <span className="text-xs font-medium text-slate-500">Fulfilled Requests</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{fulfilledCount}</p>
          <span className="text-[11px] text-emerald-600 font-medium">Completed custody receipts</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <div className="flex justify-between items-start">
            <span className="text-xs font-medium text-slate-500">Active Network Banks</span>
            <Building2 className="w-4 h-4 text-rose-600" />
          </div>
          <p className="text-2xl font-extrabold text-slate-900 mt-2">{bloodBanks.length}</p>
          <span className="text-[11px] text-slate-500">Ready to respond</span>
        </div>
      </div>

      {/* In-Transit Transfers (Requiring Hospital Confirmation) */}
      {inTransitCount > 0 && (
        <div className="bg-amber-50/70 border border-amber-200 rounded-2xl p-5 space-y-4">
          <div className="flex items-center gap-2">
            <Truck className="w-5 h-5 text-amber-700 animate-bounce" />
            <h3 className="font-bold text-sm text-amber-950">
              Active In-Transit Blood Dispatches ({inTransitCount})
            </h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {transfers.filter(t => t.status === 'IN_TRANSIT').map((t) => (
              <div key={t.id} className="bg-white p-4 rounded-xl border border-amber-200 shadow-xs space-y-3">
                <div className="flex justify-between items-start">
                  <div>
                    <span className="text-xs font-bold text-slate-900">{t.transferReference}</span>
                    <p className="text-xs text-slate-500 mt-0.5">Origin: {t.sourceFacilityName}</p>
                  </div>
                  <span className="text-xs font-extrabold text-rose-600 bg-rose-50 px-2 py-0.5 rounded-md border border-rose-100">
                    {t.unitsTransferred} Units
                  </span>
                </div>

                <div className="text-xs text-slate-600 space-y-1 bg-slate-50 p-2 rounded-lg">
                  <p><span className="font-semibold">Courier:</span> {t.courierDetails || 'Direct Medical Courier'}</p>
                  <p><span className="font-semibold">Cold Chain:</span> {t.temperatureLogSummary || 'Monitored standard temp'}</p>
                  <p className="text-[11px] text-slate-400">Dispatched: {t.dispatchedAt ? new Date(t.dispatchedAt).toLocaleTimeString() : 'N/A'}</p>
                </div>

                <button
                  onClick={() => handleConfirmReceipt(t.id)}
                  className="w-full py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  Confirm Physical Receipt
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Hospital Blood Requests Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-rose-600" />
            <h2 className="font-bold text-sm text-slate-900">Hospital Request History & Live Status</h2>
          </div>
          <span className="text-xs text-slate-500">{requests.length} requests total</span>
        </div>

        {requests.length === 0 ? (
          <div className="p-12 text-center text-xs text-slate-400 space-y-3">
            <FileText className="w-8 h-8 text-slate-300 mx-auto" />
            <p>No blood requests submitted yet.</p>
            <button
              onClick={() => setModalOpen(true)}
              className="text-rose-600 font-semibold hover:underline"
            >
              Submit your first request
            </button>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {requests.map((r) => (
              <div key={r.id} className="p-6 hover:bg-slate-50/50 transition-colors space-y-4">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-sm text-slate-900">{r.requestReference}</span>
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        r.urgency === 'CRITICAL' ? 'bg-red-100 text-red-800' : r.urgency === 'URGENT' ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-800'
                      }`}>
                        {r.urgency}
                      </span>
                      <span className="text-xs text-slate-400">• Required by: {new Date(r.requiredBy).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>
                    <p className="text-xs text-slate-600">
                      Need: <span className="font-bold text-rose-600">{r.unitsRequested} Units</span> of {r.bloodGroupDisplay} ({r.componentTypeDisplay})
                      {r.patientReferenceCode && <span className="text-slate-400 ml-2">Patient Ref: {r.patientReferenceCode}</span>}
                    </p>
                  </div>

                  <div className="flex items-center gap-3">
                    <div className="text-right">
                      <span className="text-xs font-bold text-slate-900">
                        {r.unitsFulfilled} / {r.unitsRequested} Fulfilled
                      </span>
                      <span className="block text-[11px] text-slate-400">{r.status}</span>
                    </div>

                    {!r.status.includes('FULFILLED') && r.status !== 'CANCELLED' && (
                      <button
                        onClick={() => handleCancelRequest(r.id)}
                        className="px-2.5 py-1 text-[11px] font-semibold text-rose-600 hover:bg-rose-50 rounded-lg border border-rose-200 cursor-pointer"
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>

                {/* Progress Stepper */}
                <StatusTimeline status={r.status} />

                {/* Responses from Blood Banks */}
                {r.responses && r.responses.length > 0 && (
                  <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-2">
                    <span className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                      Blood Bank Responses ({r.responses.length})
                    </span>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      {r.responses.map((resp) => (
                        <div key={resp.id} className="p-2 bg-white rounded-lg border border-slate-200 text-xs flex justify-between items-center">
                          <div>
                            <span className="font-semibold text-slate-800">{resp.bloodBankName}</span>
                            <p className="text-[11px] text-slate-400">{resp.notes || 'No comments'}</p>
                          </div>
                          <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            resp.status === 'ACCEPTED' ? 'bg-emerald-50 text-emerald-700' : resp.status === 'REJECTED' ? 'bg-rose-50 text-rose-700' : 'bg-slate-100 text-slate-600'
                          }`}>
                            {resp.status} ({resp.offeredUnits}u)
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* New Request Modal */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-xl w-full shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="bg-slate-900 px-6 py-4 text-white flex justify-between items-center">
              <h3 className="font-bold text-base">Submit Hospital Blood Request</h3>
              <button onClick={() => setModalOpen(false)} className="text-slate-400 hover:text-white p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateRequest} className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Blood Group *</label>
                  <select
                    value={bloodGroup}
                    onChange={(e) => setBloodGroup(e.target.value as BloodGroup)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs font-semibold"
                  >
                    <option value="A_POSITIVE">A+</option>
                    <option value="A_NEGATIVE">A-</option>
                    <option value="B_POSITIVE">B+</option>
                    <option value="B_NEGATIVE">B-</option>
                    <option value="AB_POSITIVE">AB+</option>
                    <option value="AB_NEGATIVE">AB-</option>
                    <option value="O_POSITIVE">O+</option>
                    <option value="O_NEGATIVE">O-</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Required Units *</label>
                  <input
                    type="number"
                    min={1}
                    max={50}
                    value={unitsRequested}
                    onChange={(e) => setUnitsRequested(parseInt(e.target.value) || 1)}
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Component Type *</label>
                <select
                  value={componentType}
                  onChange={(e) => setComponentType(e.target.value as ComponentType)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs font-semibold"
                >
                  <option value="RED_BLOOD_CELLS">Packed Red Blood Cells (PRBC)</option>
                  <option value="PLATELETS">Platelet Concentrate</option>
                  <option value="FRESH_FROZEN_PLASMA">Fresh Frozen Plasma (FFP)</option>
                  <option value="WHOLE_BLOOD">Whole Blood</option>
                  <option value="CRYOPRECIPITATE">Cryoprecipitate</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Coordination Urgency</label>
                  <select
                    value={urgency}
                    onChange={(e) => setUrgency(e.target.value as RequestUrgency)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs font-semibold"
                  >
                    <option value="ROUTINE">Routine (240m SLA)</option>
                    <option value="URGENT">Urgent (60m SLA)</option>
                    <option value="CRITICAL">Critical (15m SLA)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Required Within</label>
                  <select
                    value={requiredByHours}
                    onChange={(e) => setRequiredByHours(parseInt(e.target.value) || 6)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  >
                    <option value={2}>2 Hours (Immediate)</option>
                    <option value={6}>6 Hours</option>
                    <option value={12}>12 Hours</option>
                    <option value={24}>24 Hours</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Patient or Case Code (No sensitive PHI)
                </label>
                <input
                  type="text"
                  value={patientRef}
                  onChange={(e) => setPatientRef(e.target.value)}
                  placeholder="e.g. CAS-SURG-9901"
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Clinical Notes</label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="e.g. Emergency transfusion requirement, crossmatching ready"
                  rows={2}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold shadow-xs cursor-pointer disabled:opacity-50"
                >
                  {submitting ? 'Submitting...' : 'Broadcast Request to Network'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
