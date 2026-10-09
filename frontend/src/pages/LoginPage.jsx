import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, User, AlertCircle, ArrowRight } from 'lucide-react';

export const LoginPage = () => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Admin@123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const setDemoCreds = (u, p) => {
    setUsername(u);
    setPassword(p);
  };

  return (
    <div className="min-h-screen bg-[#07090e] flex items-center justify-center p-4 relative overflow-hidden">
      {/* Decorative cyber grid lines */}
      <div className="absolute inset-0 bg-[radial-gradient(#1e293b_1px,transparent_1px)] [background-size:24px_24px] opacity-25"></div>
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="w-full max-w-md relative z-10">
        <div className="glass-panel rounded-2xl p-8 border border-slate-800 shadow-2xl backdrop-blur-2xl">
          {/* Header */}
          <div className="text-center mb-8">
            <div className="inline-flex p-3 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 mb-4 shadow-[0_0_20px_rgba(0,240,255,0.25)]">
              <Shield className="w-8 h-8" />
            </div>
            <h1 className="text-2xl font-black tracking-wide text-white font-sans">
              SENTINEL
            </h1>
            <p className="text-xs font-mono text-cyan-400 tracking-widest uppercase mt-1">
              Intelligent Cybersecurity Operations Platform
            </p>
          </div>

          {error && (
            <div className="mb-6 p-3.5 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-slate-400 mb-1.5">
                Username
              </label>
              <div className="relative">
                <User className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="analyst.user"
                  className="w-full bg-[#0d1117]/80 border border-slate-700 rounded-lg pl-10 pr-4 py-2.5 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-mono uppercase tracking-wider text-slate-400 mb-1.5">
                Password
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full bg-[#0d1117]/80 border border-slate-700 rounded-lg pl-10 pr-4 py-2.5 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 transition"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full mt-2 py-3 px-4 rounded-lg bg-gradient-to-r from-cyan-500 to-blue-600 text-slate-950 font-bold text-sm tracking-wide flex items-center justify-center gap-2 hover:from-cyan-400 hover:to-blue-500 transition shadow-[0_0_20px_rgba(0,240,255,0.3)] disabled:opacity-50"
            >
              {loading ? 'AUTHENTICATING...' : 'ACCESS SOC CONSOLE'}
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Quick Demo Credentials */}
          <div className="mt-8 pt-6 border-t border-slate-800 text-xs">
            <span className="text-[11px] font-mono text-slate-400 uppercase tracking-wider block mb-2 text-center">
              Quick Switch Demo Roles
            </span>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => setDemoCreds('admin', 'Admin@123')}
                className="py-1.5 px-2 rounded bg-slate-800/60 hover:bg-slate-700 border border-slate-700 text-cyan-300 font-mono text-[11px] text-center transition"
              >
                ADMIN
              </button>
              <button
                type="button"
                onClick={() => setDemoCreds('analyst', 'Analyst@123')}
                className="py-1.5 px-2 rounded bg-slate-800/60 hover:bg-slate-700 border border-slate-700 text-amber-300 font-mono text-[11px] text-center transition"
              >
                ANALYST
              </button>
              <button
                type="button"
                onClick={() => setDemoCreds('viewer', 'Viewer@123')}
                className="py-1.5 px-2 rounded bg-slate-800/60 hover:bg-slate-700 border border-slate-700 text-slate-300 font-mono text-[11px] text-center transition"
              >
                VIEWER
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
