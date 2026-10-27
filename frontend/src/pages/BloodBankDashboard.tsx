import React, { useState, useEffect } from 'react';
import {
  Activity,
  Plus,
  Clock,
  Truck,
  ShieldAlert,
  CheckCircle2,
  AlertTriangle,
  Layers,
  ArrowRight,
  Send,
  X,
  FileText,
  Filter,
  Package
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Cell
} from 'recharts';
import { inventoryService } from '../services/inventoryService';
import { requestService } from '../services/requestService';
import { transferService } from '../services/transferService';
import { reservationService } from '../services/reservationService';
import type {
  BloodInventoryBatch,
  InventorySummary,
  BloodRequest,
  StockReservation,
  User,
  BloodGroup,
  ComponentType
} from '../types';

interface BloodBankDashboardProps {
  user: User;
}

export const BloodBankDashboard: React.FC<BloodBankDashboardProps> = ({ user }) => {
  const [summary, setSummary] = useState<InventorySummary | null>(null);
  const [batches, setBatches] = useState<BloodInventoryBatch[]>([]);
  const [incomingRequests, setIncomingRequests] = useState<BloodRequest[]>([]);
  const [reservations, setReservations] = useState<StockReservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionMsg, setActionMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // Add Batch Modal State
  const [addBatchModalOpen, setAddBatchModalOpen] = useState(false);
  const [batchNumber, setBatchNumber] = useState('');
  const [bloodGroup, setBloodGroup] = useState<BloodGroup>('A_POSITIVE');
  const [componentType, setComponentType] = useState<ComponentType>('RED_BLOOD_CELLS');
  const [quantity, setQuantity] = useState<number>(10);
  const [expiryDays, setExpiryDays] = useState<number>(35);
  const [storageLocation, setStorageLocation] = useState('Refrigerated Shelf R-01');
  const [notes, setNotes] = useState('');

  // Dispatch Transfer Modal State
  const [dispatchModalOpen, setDispatchModalOpen] = useState(false);
  const [selectedReservation, setSelectedReservation] = useState<StockReservation | null>(null);
  const [courierDetails, setCourierDetails] = useState('LifeLink Cold Chain Unit #12');
  const [temperatureLog, setTemperatureLog] = useState('Monitored temperature: 3.5°C');

  // Response Modal State (Accept / Reject)
  const [responseModalOpen, setResponseModalOpen] = useState(false);
  const [activeRequest, setActiveRequest] = useState<BloodRequest | null>(null);
  const [offeredUnits, setOfferedUnits] = useState<number>(1);
  const [responseNotes, setResponseNotes] = useState('');

  const fetchData = async () => {
    if (!user.facilityId) return;
    setLoading(true);
    try {
      const [sumData, batchPage, reqPage, resPage] = await Promise.all([
        inventoryService.getSummary(user.facilityId),
        inventoryService.getBatches(user.facilityId, 0, 50),
        requestService.getBankIncomingRequests(user.facilityId, 0, 20),
        reservationService.getBankReservations(user.facilityId, 0, 20),
      ]);
      setSummary(sumData);
      setBatches(batchPage.content);
      setIncomingRequests(reqPage.content);
      setReservations(resPage.content);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const handleAddBatch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user.facilityId) return;
    setActionMsg(null);
    try {
      const expiryDate = new Date(Date.now() + expiryDays * 24 * 3600 * 1000).toISOString();
      await inventoryService.addBatch(user.facilityId, {
        batchNumber,
        bloodGroup,
        componentType,
        quantity,
        expiryDate,
        storageLocation,
        notes,
      });
      setActionMsg({ type: 'success', text: `Batch ${batchNumber} added and verified in inventory.` });
      setAddBatchModalOpen(false);
      setBatchNumber('');
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Failed to add batch.' });
    }
  };

  const handleOpenResponse = (req: BloodRequest) => {
    setActiveRequest(req);
    const needed = req.unitsRequested - req.unitsFulfilled;
    setOfferedUnits(needed > 0 ? needed : 1);
    setResponseModalOpen(true);
  };

  const handleSubmitResponse = async (action: 'ACCEPT' | 'PARTIALLY_ACCEPT' | 'REJECT') => {
    if (!activeRequest || !user.facilityId) return;
    setActionMsg(null);
    try {
      await requestService.respondToRequest(
        activeRequest.id,
        user.facilityId,
        action,
        offeredUnits,
        responseNotes
      );
      setActionMsg({ type: 'success', text: `Response (${action}) recorded and stock reserved safely!` });
      setResponseModalOpen(false);
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Failed to record response.' });
    }
  };

  const handleOpenDispatch = (res: StockReservation) => {
    setSelectedReservation(res);
    setDispatchModalOpen(true);
  };

  const handleDispatch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedReservation) return;
    setActionMsg(null);
    try {
      await transferService.dispatchTransfer(
        selectedReservation.id,
        courierDetails,
        temperatureLog,
        'Authorized blood bank dispatch'
      );
      setActionMsg({ type: 'success', text: `Transfer dispatched for reservation ${selectedReservation.reservationReference}!` });
      setDispatchModalOpen(false);
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Dispatch failed.' });
    }
  };

  const handleQuarantine = async (batchId: number) => {
    if (!user.facilityId) return;
    const reason = prompt('Enter reason for moving batch to quarantine:');
    if (!reason) return;
    try {
      await inventoryService.quarantineBatch(user.facilityId, batchId, reason);
      setActionMsg({ type: 'success', text: 'Batch placed in quarantine status.' });
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Quarantine failed.' });
    }
  };

  const handleRestore = async (batchId: number) => {
    if (!user.facilityId) return;
    try {
      await inventoryService.restoreBatch(user.facilityId, batchId);
      setActionMsg({ type: 'success', text: 'Batch restored to active inventory.' });
      fetchData();
    } catch (err: any) {
      setActionMsg({ type: 'error', text: err.message || 'Restore failed.' });
    }
  };

  // Prepare chart data
  const chartData = summary?.stockByBloodGroup
    ? Object.entries(summary.stockByBloodGroup).map(([group, units]) => ({ group, units }))
    : [];

  const pendingRequests = incomingRequests.filter(r =>
    !r.status.includes('FULFILLED') && r.status !== 'CANCELLED' && r.status !== 'REJECTED'
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Banner */}
      <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-xs flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-rose-700 bg-rose-50 px-2.5 py-1 rounded-full border border-rose-200">
              Blood Bank Facility Management
            </span>
            <span className="text-xs text-slate-400">•</span>
            <span className="text-xs font-medium text-slate-500">{user.facilityName}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight mt-1">
            Inventory & Dispatch Control
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage tested blood batches, evaluate incoming hospital requests, and dispatch verified cold-chain units.
          </p>
        </div>

        <button
          onClick={() => setAddBatchModalOpen(true)}
          className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-md shadow-rose-600/25 flex items-center gap-1.5 transition-all cursor-pointer whitespace-nowrap"
        >
          <Plus className="w-4 h-4" />
          Add Inventory Batch
        </button>
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
      <div className="grid grid-cols-2 lg:grid-cols-5 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Total Stock</span>
          <p className="text-2xl font-extrabold text-slate-900 mt-1">{summary?.totalUnits || 0}</p>
          <span className="text-[11px] text-slate-400">Total physical units</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Available Units</span>
          <p className="text-2xl font-extrabold text-emerald-600 mt-1">{summary?.availableUnits || 0}</p>
          <span className="text-[11px] text-emerald-600 font-medium">Ready for allocation</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Reserved Units</span>
          <p className="text-2xl font-extrabold text-blue-600 mt-1">{summary?.reservedUnits || 0}</p>
          <span className="text-[11px] text-blue-600 font-medium">Locked for hospitals</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Near-Expiry (&lt;7d)</span>
          <p className="text-2xl font-extrabold text-amber-600 mt-1">{summary?.nearExpiryUnits || 0}</p>
          <span className="text-[11px] text-amber-600 font-medium">FIFO prioritized</span>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
          <span className="text-xs font-medium text-slate-500">Quarantined</span>
          <p className="text-2xl font-extrabold text-rose-600 mt-1">{summary?.quarantinedUnits || 0}</p>
          <span className="text-[11px] text-rose-600 font-medium">Pending test clearance</span>
        </div>
      </div>

      {/* Analytics Chart Strip */}
      {chartData.length > 0 && (
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-2xs space-y-4">
          <div className="flex justify-between items-center">
            <h3 className="font-bold text-sm text-slate-900">Current Available Stock by Blood Group</h3>
            <span className="text-xs text-slate-400">Live authoritative balance</span>
          </div>
          <div className="h-44 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
                <XAxis dataKey="group" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip
                  formatter={(val: any) => [`${val} Units`, 'Available']}
                  contentStyle={{ borderRadius: '12px', border: '1px solid #E2E8F0', fontSize: '12px' }}
                />
                <Bar dataKey="units" fill="#E11D48" radius={[6, 6, 0, 0]}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.units < 4 ? '#E11D48' : '#3B82F6'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Incoming Hospital Requests Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-rose-50/40">
          <div className="flex items-center gap-2">
            <Activity className="w-5 h-5 text-rose-600" />
            <h2 className="font-bold text-sm text-slate-900">Incoming Hospital Requests Requiring Action</h2>
          </div>
          <span className="text-xs font-semibold text-rose-700 bg-rose-100 px-2.5 py-0.5 rounded-full border border-rose-200">
            {pendingRequests.length} Pending
          </span>
        </div>

        {pendingRequests.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            No pending hospital requests currently awaiting response from your blood bank.
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {pendingRequests.map((r) => (
              <div key={r.id} className="p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 hover:bg-slate-50/50 transition-colors">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-sm text-slate-900">{r.hospitalName}</span>
                    <span className="text-xs font-mono text-slate-400">({r.requestReference})</span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      r.urgency === 'CRITICAL' ? 'bg-red-100 text-red-800' : r.urgency === 'URGENT' ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-800'
                    }`}>
                      {r.urgency}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600">
                    Requested: <span className="font-bold text-rose-600">{r.unitsRequested} units</span> of {r.bloodGroupDisplay} ({r.componentTypeDisplay})
                    <span className="text-slate-400 ml-2">• Required by {new Date(r.requiredBy).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </p>
                  {r.clinicalNotes && <p className="text-[11px] text-slate-500 italic">Notes: {r.clinicalNotes}</p>}
                </div>

                <div className="flex items-center gap-2 w-full md:w-auto">
                  <button
                    onClick={() => handleOpenResponse(r)}
                    className="flex-1 md:flex-none px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold transition-colors cursor-pointer"
                  >
                    Respond & Reserve Stock
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Active Reservations & Dispatch Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Package className="w-5 h-5 text-blue-600" />
            <h2 className="font-bold text-sm text-slate-900">Active Stock Reservations ({reservations.length})</h2>
          </div>
          <span className="text-xs text-slate-400">Ready for courier dispatch</span>
        </div>

        {reservations.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-400">
            No active reservations currently locked in inventory.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
                <tr>
                  <th className="px-6 py-3">Reservation Ref</th>
                  <th className="px-6 py-3">Request Ref</th>
                  <th className="px-6 py-3">Batch Number</th>
                  <th className="px-6 py-3">Group / Component</th>
                  <th className="px-6 py-3">Units</th>
                  <th className="px-6 py-3">Status</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reservations.map((res) => (
                  <tr key={res.id} className="hover:bg-slate-50/50">
                    <td className="px-6 py-3 font-mono font-semibold text-slate-900">{res.reservationReference}</td>
                    <td className="px-6 py-3 text-slate-600">{res.requestReference}</td>
                    <td className="px-6 py-3 font-mono text-slate-600">{res.batchNumber}</td>
                    <td className="px-6 py-3">{res.bloodGroupDisplay} ({res.componentTypeDisplay})</td>
                    <td className="px-6 py-3 font-bold text-rose-600">{res.unitsReserved} units</td>
                    <td className="px-6 py-3">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                        res.status === 'ACTIVE' ? 'bg-blue-50 text-blue-700' : res.status === 'DISPATCHED' ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'
                      }`}>
                        {res.status}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-right">
                      {res.status === 'ACTIVE' && (
                        <button
                          onClick={() => handleOpenDispatch(res)}
                          className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold cursor-pointer"
                        >
                          Dispatch
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Inventory Batches Table */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Package className="w-5 h-5 text-rose-600" />
            <h2 className="font-bold text-sm text-slate-900">Facility Blood Inventory Batches</h2>
          </div>
          <span className="text-xs text-slate-500">{batches.length} audited batches</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-100">
              <tr>
                <th className="px-6 py-3">Batch ID</th>
                <th className="px-6 py-3">Group</th>
                <th className="px-6 py-3">Component</th>
                <th className="px-6 py-3">Available / Total</th>
                <th className="px-6 py-3">Expiry Date</th>
                <th className="px-6 py-3">Status</th>
                <th className="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {batches.map((b) => (
                <tr key={b.id} className="hover:bg-slate-50/50">
                  <td className="px-6 py-3 font-mono font-semibold text-slate-900">{b.batchNumber}</td>
                  <td className="px-6 py-3 font-bold text-slate-900">{b.bloodGroupDisplay}</td>
                  <td className="px-6 py-3 text-slate-600">{b.componentTypeDisplay}</td>
                  <td className="px-6 py-3">
                    <span className="font-extrabold text-emerald-600">{b.availableQuantity}</span> / {b.quantity}
                    {b.reservedQuantity > 0 && <span className="text-[10px] text-blue-500 ml-1">({b.reservedQuantity} res)</span>}
                  </td>
                  <td className="px-6 py-3">
                    <span className={b.nearExpiry ? 'text-amber-600 font-bold' : b.expired ? 'text-rose-600 font-bold' : 'text-slate-600'}>
                      {new Date(b.expiryDate).toLocaleDateString()}
                    </span>
                    <span className="text-[10px] text-slate-400 block">{b.daysUntilExpiry} days left</span>
                  </td>
                  <td className="px-6 py-3">
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                      b.status === 'AVAILABLE' ? 'bg-emerald-50 text-emerald-700' : b.status === 'QUARANTINED' ? 'bg-rose-50 text-rose-700' : 'bg-slate-100 text-slate-600'
                    }`}>
                      {b.status}
                    </span>
                  </td>
                  <td className="px-6 py-3 text-right">
                    {b.status === 'AVAILABLE' ? (
                      <button
                        onClick={() => handleQuarantine(b.id)}
                        className="text-[11px] font-semibold text-rose-600 hover:underline"
                      >
                        Quarantine
                      </button>
                    ) : b.status === 'QUARANTINED' ? (
                      <button
                        onClick={() => handleRestore(b.id)}
                        className="text-[11px] font-semibold text-emerald-600 hover:underline"
                      >
                        Restore
                      </button>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Batch Modal */}
      {addBatchModalOpen && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="bg-slate-900 px-6 py-4 text-white flex justify-between items-center">
              <h3 className="font-bold text-base">Add Audited Blood Inventory Batch</h3>
              <button onClick={() => setAddBatchModalOpen(false)} className="text-slate-400 hover:text-white p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleAddBatch} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Batch Identifier *</label>
                <input
                  type="text"
                  value={batchNumber}
                  onChange={(e) => setBatchNumber(e.target.value)}
                  placeholder="e.g. BATCH-2026-A101"
                  required
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

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
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Quantity (Units) *</label>
                  <input
                    type="number"
                    min={1}
                    max={100}
                    value={quantity}
                    onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
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
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Shelf Life (Days)</label>
                  <input
                    type="number"
                    min={1}
                    max={365}
                    value={expiryDays}
                    onChange={(e) => setExpiryDays(parseInt(e.target.value) || 35)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Storage Location</label>
                  <input
                    type="text"
                    value={storageLocation}
                    onChange={(e) => setStorageLocation(e.target.value)}
                    placeholder="e.g. Rack A-02"
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setAddBatchModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold shadow-xs cursor-pointer"
                >
                  Confirm Batch Intake
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Response Modal */}
      {responseModalOpen && activeRequest && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="bg-slate-900 px-6 py-4 text-white flex justify-between items-center">
              <h3 className="font-bold text-base">Respond to Hospital Request</h3>
              <button onClick={() => setResponseModalOpen(false)} className="text-slate-400 hover:text-white p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4">
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 text-xs space-y-1">
                <p><span className="font-semibold">Hospital:</span> {activeRequest.hospitalName}</p>
                <p><span className="font-semibold">Need:</span> {activeRequest.unitsRequested} units of {activeRequest.bloodGroupDisplay} ({activeRequest.componentTypeDisplay})</p>
                <p><span className="font-semibold">Urgency:</span> {activeRequest.urgency}</p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Units Offered to Reserve *
                </label>
                <input
                  type="number"
                  min={1}
                  max={activeRequest.unitsRequested}
                  value={offeredUnits}
                  onChange={(e) => setOfferedUnits(parseInt(e.target.value) || 1)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs font-bold"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Response Notes</label>
                <textarea
                  value={responseNotes}
                  onChange={(e) => setResponseNotes(e.target.value)}
                  placeholder="e.g. Units crossmatch pre-screened and ready for packaging"
                  rows={2}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

              <div className="pt-2 flex flex-col gap-2">
                <button
                  type="button"
                  onClick={() => handleSubmitResponse('ACCEPT')}
                  className="w-full py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-xs cursor-pointer"
                >
                  Accept & Reserve {offeredUnits} Units
                </button>

                <button
                  type="button"
                  onClick={() => handleSubmitResponse('REJECT')}
                  className="w-full py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl text-xs font-semibold cursor-pointer"
                >
                  Decline Request
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Dispatch Modal */}
      {dispatchModalOpen && selectedReservation && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-slate-200 overflow-hidden animate-in fade-in zoom-in-95">
            <div className="bg-slate-900 px-6 py-4 text-white flex justify-between items-center">
              <h3 className="font-bold text-base">Authorize Blood Dispatch</h3>
              <button onClick={() => setDispatchModalOpen(false)} className="text-slate-400 hover:text-white p-1">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleDispatch} className="p-6 space-y-4">
              <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 text-xs space-y-1">
                <p><span className="font-semibold">Reservation Ref:</span> {selectedReservation.reservationReference}</p>
                <p><span className="font-semibold">Units:</span> {selectedReservation.unitsReserved} units ({selectedReservation.bloodGroupDisplay})</p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Courier / Transport Unit *</label>
                <input
                  type="text"
                  value={courierDetails}
                  onChange={(e) => setCourierDetails(e.target.value)}
                  required
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Cold-Chain Temperature Log *</label>
                <input
                  type="text"
                  value={temperatureLog}
                  onChange={(e) => setTemperatureLog(e.target.value)}
                  required
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setDispatchModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 text-slate-600 rounded-lg text-xs font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-bold shadow-xs cursor-pointer"
                >
                  Authorize Dispatch & Transit
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
