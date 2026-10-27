import React, { useState } from 'react';
import { Building2, Heart, ArrowRight, AlertCircle, CheckCircle2 } from 'lucide-react';
import { authService } from '../services/authService';
import type { User } from '../types';

interface RegisterPageProps {
  onLoginSuccess: (user: User) => void;
  onNavigate: (view: string) => void;
}

export const RegisterPage: React.FC<RegisterPageProps> = ({ onLoginSuccess, onNavigate }) => {
  const [facilityType, setFacilityType] = useState<'HOSPITAL' | 'BLOOD_BANK'>('HOSPITAL');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Form Fields
  const [facilityName, setFacilityName] = useState('');
  const [licenseNumber, setLicenseNumber] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('Chennai');
  const [state, setState] = useState('Tamil Nadu');
  const [pincode, setPincode] = useState('600001');
  const [contactPhone, setContactPhone] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [adminFullName, setAdminFullName] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setLoading(true);

    try {
      if (facilityType === 'HOSPITAL') {
        const res = await authService.registerHospital({
          hospitalName: facilityName,
          licenseNumber,
          address,
          city,
          state,
          pincode,
          contactPhone,
          contactEmail,
          adminFullName,
          adminEmail,
          adminPassword,
        });
        setSuccessMsg('Hospital registration submitted successfully! Awaiting verification.');
        setTimeout(() => onLoginSuccess(res.user), 1000);
      } else {
        const res = await authService.registerBloodBank({
          bloodBankName: facilityName,
          licenseNumber,
          address,
          city,
          state,
          pincode,
          contactPhone,
          contactEmail,
          adminFullName,
          adminEmail,
          adminPassword,
        });
        setSuccessMsg('Blood bank registration submitted successfully! Awaiting verification.');
        setTimeout(() => onLoginSuccess(res.user), 1000);
      }
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please check inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200/80 overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-slate-900 via-slate-800 to-slate-900 p-6 sm:p-8 text-white space-y-2">
          <div className="inline-flex items-center gap-1.5 text-xs font-semibold bg-rose-950 text-rose-400 px-3 py-1 rounded-full border border-rose-800">
            <Building2 className="w-3.5 h-3.5" />
            Accredited Facility Onboarding
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Register Your Healthcare Institution</h2>
          <p className="text-xs text-slate-300">
            Join the LifeLink network to coordinate blood availability, requests, and emergency transfers.
          </p>
        </div>

        {/* Facility Type Selector Tabs */}
        <div className="flex border-b border-slate-200 bg-slate-50">
          <button
            type="button"
            onClick={() => setFacilityType('HOSPITAL')}
            className={`flex-1 py-3 text-xs font-bold transition-all border-b-2 cursor-pointer ${
              facilityType === 'HOSPITAL'
                ? 'border-rose-600 text-rose-600 bg-white'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Hospital Registration
          </button>
          <button
            type="button"
            onClick={() => setFacilityType('BLOOD_BANK')}
            className={`flex-1 py-3 text-xs font-bold transition-all border-b-2 cursor-pointer ${
              facilityType === 'BLOOD_BANK'
                ? 'border-rose-600 text-rose-600 bg-white'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Blood Bank Registration
          </button>
        </div>

        <div className="p-6 sm:p-8">
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 flex items-start gap-2.5 text-xs text-rose-800">
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {successMsg && (
            <div className="mb-6 p-4 rounded-xl bg-emerald-50 border border-emerald-200 flex items-start gap-2.5 text-xs text-emerald-800">
              <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
              <span>{successMsg}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Section 1: Facility Details */}
            <div className="space-y-4">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                1. Organization Information
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    {facilityType === 'HOSPITAL' ? 'Hospital Name' : 'Blood Bank Name'} *
                  </label>
                  <input
                    type="text"
                    value={facilityName}
                    onChange={(e) => setFacilityName(e.target.value)}
                    placeholder="e.g. Apollo Super Specialty Hospital"
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Accreditation / License Number *
                  </label>
                  <input
                    type="text"
                    value={licenseNumber}
                    onChange={(e) => setLicenseNumber(e.target.value)}
                    placeholder="e.g. LIC-HOSP-TN-2026-901"
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Physical Address *</label>
                <input
                  type="text"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Street, Sector, Landmark"
                  required
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">City *</label>
                  <input
                    type="text"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">State *</label>
                  <input
                    type="text"
                    value={state}
                    onChange={(e) => setState(e.target.value)}
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Pincode *</label>
                  <input
                    type="text"
                    value={pincode}
                    onChange={(e) => setPincode(e.target.value)}
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Contact Phone *</label>
                  <input
                    type="text"
                    value={contactPhone}
                    onChange={(e) => setContactPhone(e.target.value)}
                    placeholder="+91 44 2800 1234"
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Official Email *</label>
                  <input
                    type="email"
                    value={contactEmail}
                    onChange={(e) => setContactEmail(e.target.value)}
                    placeholder="contact@facility.com"
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
              </div>
            </div>

            {/* Section 2: Initial Admin Credentials */}
            <div className="space-y-4 pt-4 border-t border-slate-100">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
                2. Facility Administrator Account
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Admin Full Name *</label>
                  <input
                    type="text"
                    value={adminFullName}
                    onChange={(e) => setAdminFullName(e.target.value)}
                    placeholder="Dr. S. K. Raman"
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Admin Login Email *</label>
                  <input
                    type="email"
                    value={adminEmail}
                    onChange={(e) => setAdminEmail(e.target.value)}
                    placeholder="admin@facility.com"
                    required
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Password *</label>
                <input
                  type="password"
                  value={adminPassword}
                  onChange={(e) => setAdminPassword(e.target.value)}
                  placeholder="Minimum 8 characters"
                  required
                  minLength={8}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-300 rounded-lg text-xs"
                />
              </div>
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 px-4 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold shadow-md shadow-rose-600/25 flex items-center justify-center gap-2 transition-all disabled:opacity-50 cursor-pointer"
              >
                {loading ? 'Submitting Registration...' : 'Complete Facility Registration'}
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </form>

          <div className="mt-6 text-center text-xs text-slate-500 border-t border-slate-100 pt-4">
            Already have an account?{' '}
            <button
              onClick={() => onNavigate('login')}
              className="text-rose-600 font-semibold hover:underline cursor-pointer"
            >
              Sign In
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
