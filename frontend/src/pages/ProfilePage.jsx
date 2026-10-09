import React from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Shield, Key } from 'lucide-react';

export const ProfilePage = () => {
  const { user } = useAuth();

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
          <User className="w-6 h-6 text-cyan-400" />
          Operator Profile
        </h1>
        <p className="text-xs text-slate-400 font-mono mt-0.5">
          Current Analyst Credentials and Cryptographic Identity
        </p>
      </div>

      <div className="glass-panel rounded-2xl border border-slate-800 p-6 space-y-6 shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-cyan-500/20 to-blue-600/20 border border-cyan-500/40 flex items-center justify-center font-black text-xl text-cyan-300">
            {user?.username ? user.username.slice(0, 2).toUpperCase() : 'OP'}
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">{user?.fullName || user?.username}</h2>
            <div className="text-xs font-mono text-cyan-400">@{user?.username}</div>
            <div className="text-xs text-slate-400 mt-0.5">{user?.email}</div>
          </div>
        </div>

        <div className="border-t border-slate-800 pt-4 space-y-3 font-mono text-xs">
          <div className="flex items-center justify-between p-3 rounded-lg bg-slate-900/60 border border-slate-800">
            <span className="text-slate-400 flex items-center gap-2">
              <Shield className="w-4 h-4 text-cyan-400" /> Assigned Roles
            </span>
            <div className="flex gap-1.5">
              {user?.roles?.map((r) => (
                <span key={r} className="px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-300 border border-cyan-500/30">
                  {r.replace('ROLE_', '')}
                </span>
              ))}
            </div>
          </div>

          <div className="flex items-center justify-between p-3 rounded-lg bg-slate-900/60 border border-slate-800">
            <span className="text-slate-400 flex items-center gap-2">
              <Key className="w-4 h-4 text-emerald-400" /> Session Security
            </span>
            <span className="text-emerald-400 font-bold">JWT HMAC-SHA384 ACTIVE</span>
          </div>
        </div>
      </div>
    </div>
  );
};
