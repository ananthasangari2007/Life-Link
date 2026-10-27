import React, { useState, useEffect } from 'react';
import {
  Search,
  Building2,
  CheckCircle2,
  AlertTriangle,
  Layers,
  ArrowRight,
  ShieldCheck,
  Send,
  Sparkles,
  Phone,
  Mail,
  MapPin
} from 'lucide-react';
import { matchingService } from '../services/matchingService';
import type {
  BloodGroup,
  ComponentType,
  RequestUrgency,
  MatchResult,
  User
} from '../types';

interface MatchingViewProps {
  user: User | null;
  onInitiateRequest?: (params: { bloodGroup: BloodGroup; componentType: ComponentType; units: number }) => void;
}

export const MatchingView: React.FC<MatchingViewProps> = ({ user, onInitiateRequest }) => {
  const [bloodGroup, setBloodGroup] = useState<BloodGroup>('A_POSITIVE');
  const [componentType, setComponentType] = useState<ComponentType>('RED_BLOOD_CELLS');
  const [unitsRequested, setUnitsRequested] = useState<number>(4);
  const [urgency, setUrgency] = useState<RequestUrgency>('URGENT');
  const [hospitalLocation, setHospitalLocation] = useState<string>('Chennai');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<MatchResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const bloodGroups: { key: BloodGroup; label: string }[] = [
    { key: 'A_POSITIVE', label: 'A+' },
    { key: 'A_NEGATIVE', label: 'A-' },
    { key: 'B_POSITIVE', label: 'B+' },
    { key: 'B_NEGATIVE', label: 'B-' },
    { key: 'AB_POSITIVE', label: 'AB+' },
    { key: 'AB_NEGATIVE', label: 'AB-' },
    { key: 'O_POSITIVE', label: 'O+' },
    { key: 'O_NEGATIVE', label: 'O-' },
  ];

  const components: { key: ComponentType; label: string }[] = [
    { key: 'RED_BLOOD_CELLS', label: 'Packed Red Blood Cells (PRBC)' },
    { key: 'PLATELETS', label: 'Platelet Concentrate' },
    { key: 'FRESH_FROZEN_PLASMA', label: 'Fresh Frozen Plasma (FFP)' },
    { key: 'WHOLE_BLOOD', label: 'Whole Blood' },
    { key: 'CRYOPRECIPITATE', label: 'Cryoprecipitate' },
  ];

  const handleSearch = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await matchingService.findMatches({
        bloodGroup,
        componentType,
        unitsRequested,
        hospitalLocation,
        urgency,
      });
      setResult(data);
    } catch (err: any) {
      setError(err.message || 'Matching query failed.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    handleSearch();
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Search Header */}
      <div className="bg-gradient-to-r from-rose-900 via-rose-800 to-slate-900 rounded-3xl p-6 sm:p-8 text-white space-y-4 shadow-xl">
        <div className="inline-flex items-center gap-1.5 text-xs font-semibold bg-rose-950/80 text-rose-300 px-3 py-1 rounded-full border border-rose-700">
          <Sparkles className="w-3.5 h-3.5 text-rose-400" />
          Deterministic Multi-Bank Coordination Engine
        </div>
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Blood Availability & Compatibility Search
          </h1>
          <p className="text-xs sm:text-sm text-rose-100/80 mt-1 max-w-2xl">
            Query reported inventory across participating blood banks. Applies standard ABO/Rh component
            compatibility policies and generates explainable fulfillment arrangements.
          </p>
        </div>

        {/* Search Controls Form */}
        <form onSubmit={handleSearch} className="bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/15 space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
            {/* Blood Group */}
            <div>
              <label className="block text-xs font-medium text-rose-100 mb-1">Recipient Blood Group</label>
              <select
                value={bloodGroup}
                onChange={(e) => setBloodGroup(e.target.value as BloodGroup)}
                className="w-full bg-slate-900/90 text-white border border-slate-700 rounded-xl px-3 py-2 text-xs font-semibold focus:outline-hidden focus:ring-2 focus:ring-rose-400"
              >
                {bloodGroups.map((bg) => (
                  <option key={bg.key} value={bg.key}>{bg.label}</option>
                ))}
              </select>
            </div>

            {/* Component */}
            <div className="lg:col-span-2">
              <label className="block text-xs font-medium text-rose-100 mb-1">Component Type</label>
              <select
                value={componentType}
                onChange={(e) => setComponentType(e.target.value as ComponentType)}
                className="w-full bg-slate-900/90 text-white border border-slate-700 rounded-xl px-3 py-2 text-xs font-semibold focus:outline-hidden focus:ring-2 focus:ring-rose-400"
              >
                {components.map((c) => (
                  <option key={c.key} value={c.key}>{c.label}</option>
                ))}
              </select>
            </div>

            {/* Units */}
            <div>
              <label className="block text-xs font-medium text-rose-100 mb-1">Required Units</label>
              <input
                type="number"
                min={1}
                max={50}
                value={unitsRequested}
                onChange={(e) => setUnitsRequested(parseInt(e.target.value) || 1)}
                className="w-full bg-slate-900/90 text-white border border-slate-700 rounded-xl px-3 py-2 text-xs font-semibold focus:outline-hidden focus:ring-2 focus:ring-rose-400"
              />
            </div>

            {/* Urgency */}
            <div>
              <label className="block text-xs font-medium text-rose-100 mb-1">Urgency</label>
              <select
                value={urgency}
                onChange={(e) => setUrgency(e.target.value as RequestUrgency)}
                className="w-full bg-slate-900/90 text-white border border-slate-700 rounded-xl px-3 py-2 text-xs font-semibold focus:outline-hidden focus:ring-2 focus:ring-rose-400"
              >
                <option value="ROUTINE">Routine (240m SLA)</option>
                <option value="URGENT">Urgent (60m SLA)</option>
                <option value="CRITICAL">Critical (15m SLA)</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end pt-1">
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 bg-rose-600 hover:bg-rose-500 text-white rounded-xl text-xs font-bold shadow-md shadow-rose-950/40 flex items-center gap-2 transition-all cursor-pointer disabled:opacity-50"
            >
              <Search className="w-4 h-4" />
              {loading ? 'Evaluating Compatibility...' : 'Find Matches'}
            </button>
          </div>
        </form>
      </div>

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-800 flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-rose-600" />
          <span>{error}</span>
        </div>
      )}

      {result && (
        <div className="space-y-8">
          {/* Summary Strip */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
              <span className="text-xs font-medium text-slate-500">Requested Need</span>
              <p className="text-xl font-extrabold text-slate-900 mt-1">
                {result.unitsRequested} Units of {result.requestedGroupDisplay}
              </p>
              <p className="text-xs text-slate-500">{result.componentTypeDisplay}</p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
              <span className="text-xs font-medium text-slate-500">Total Network Available</span>
              <p className="text-xl font-extrabold text-rose-600 mt-1">
                {result.totalAvailableInNetwork} Compatible Units
              </p>
              <p className="text-xs text-slate-500">Across {result.candidateFacilities.length} blood banks</p>
            </div>

            <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs">
              <span className="text-xs font-medium text-slate-500">Fulfillment Status</span>
              <div className="mt-1 flex items-center gap-2">
                {result.fullMatchFound ? (
                  <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Full Single-Bank Match
                  </span>
                ) : result.totalAvailableInNetwork >= result.unitsRequested ? (
                  <span className="inline-flex items-center gap-1 text-xs font-bold text-blue-700 bg-blue-50 px-2.5 py-1 rounded-full border border-blue-200">
                    <Layers className="w-3.5 h-3.5" /> Split Multi-Bank Plan Available
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200">
                    <AlertTriangle className="w-3.5 h-3.5" /> Partial Shortage in Network
                  </span>
                )}
              </div>
              <p className="text-[11px] text-slate-400 mt-1">Updated dynamically</p>
            </div>
          </div>

          {/* Suggested Fulfillment Plans */}
          {result.suggestedPlans.length > 0 && (
            <div className="space-y-4">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <Layers className="w-5 h-5 text-rose-600" />
                Suggested Fulfillment Plans
              </h2>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {result.suggestedPlans.map((plan, i) => (
                  <div
                    key={i}
                    className="bg-white p-5 rounded-2xl border border-slate-200 shadow-2xs space-y-4 flex flex-col justify-between"
                  >
                    <div className="space-y-2">
                      <div className="flex justify-between items-start gap-2">
                        <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-800 border border-slate-200">
                          {plan.planType === 'SINGLE_BANK' ? 'Direct Single-Bank Plan' : 'Multi-Bank Split Arrangement'}
                        </span>
                        <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-100">
                          {plan.totalUnitsProposed} Units
                        </span>
                      </div>
                      <h3 className="font-bold text-sm text-slate-900">{plan.title}</h3>
                      <p className="text-xs text-slate-500">{plan.summary}</p>

                      <div className="space-y-2 pt-2">
                        {plan.segments.map((seg, sIdx) => (
                          <div key={sIdx} className="p-2.5 bg-slate-50 rounded-xl text-xs flex justify-between items-center border border-slate-100">
                            <div>
                              <p className="font-semibold text-slate-800">{seg.facilityName}</p>
                              <p className="text-[11px] text-slate-400">{seg.city} • {seg.bloodGroupDisplay}</p>
                            </div>
                            <span className="font-extrabold text-rose-600 text-sm">{seg.unitsAllocated} units</span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {user?.role === 'HOSPITAL_USER' && onInitiateRequest && (
                      <button
                        onClick={() => onInitiateRequest({ bloodGroup, componentType, units: unitsRequested })}
                        className="w-full py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-xs flex items-center justify-center gap-2 cursor-pointer transition-colors"
                      >
                        <Send className="w-3.5 h-3.5" />
                        Initiate Official Blood Request
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Ranked Candidate Facilities */}
          <div className="space-y-4">
            <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Building2 className="w-5 h-5 text-rose-600" />
              Candidate Blood Banks (Ranked by Compatibility & Availability)
            </h2>

            {result.candidateFacilities.length === 0 ? (
              <div className="bg-white p-8 rounded-2xl border border-slate-200 text-center text-xs text-slate-500">
                No compatible inventory currently reported in participating blood banks.
              </div>
            ) : (
              <div className="space-y-3">
                {result.candidateFacilities.map((fac) => (
                  <div
                    key={fac.facilityId}
                    className="bg-white p-5 rounded-2xl border border-slate-200 hover:border-slate-300 transition-all shadow-2xs flex flex-col md:flex-row justify-between items-start md:items-center gap-4"
                  >
                    <div className="space-y-1 max-w-xl">
                      <div className="flex items-center gap-2">
                        <h3 className="font-bold text-sm text-slate-900">{fac.facilityName}</h3>
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                          fac.compatibilityScore === 100
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : 'bg-blue-50 text-blue-700 border border-blue-200'
                        }`}>
                          Score: {fac.compatibilityScore}%
                        </span>
                        {fac.canFulfillEntirely && (
                          <span className="text-[10px] font-bold bg-purple-50 text-purple-700 px-2 py-0.5 rounded-full border border-purple-200">
                            Entire Request
                          </span>
                        )}
                      </div>

                      <p className="text-xs text-slate-600 font-medium">
                        {fac.compatibilityExplanation}
                      </p>

                      <div className="flex flex-wrap gap-3 text-xs text-slate-400 pt-1">
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5 text-slate-400" /> {fac.city}
                        </span>
                        <span className="flex items-center gap-1">
                          <Phone className="w-3.5 h-3.5 text-slate-400" /> {fac.contactPhone}
                        </span>
                        <span className="flex items-center gap-1">
                          <Mail className="w-3.5 h-3.5 text-slate-400" /> {fac.contactEmail}
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center gap-4 w-full md:w-auto justify-between md:justify-end border-t md:border-t-0 pt-3 md:pt-0 border-slate-100">
                      <div className="text-right">
                        <span className="text-2xl font-extrabold text-slate-900">{fac.availableUnits}</span>
                        <span className="text-xs text-slate-500 block">units in stock</span>
                      </div>

                      {user?.role === 'HOSPITAL_USER' && onInitiateRequest && (
                        <button
                          onClick={() => onInitiateRequest({ bloodGroup, componentType, units: Math.min(unitsRequested, fac.availableUnits) })}
                          className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold cursor-pointer whitespace-nowrap"
                        >
                          Request from Bank
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Medical Notice */}
          <div className="p-4 bg-slate-100 border border-slate-200 rounded-xl text-xs text-slate-600 flex items-start gap-3">
            <ShieldCheck className="w-5 h-5 text-slate-500 shrink-0 mt-0.5" />
            <div>
              <span className="font-semibold text-slate-900">Responsible Clinical Protocol: </span>
              {result.medicalNotice}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
