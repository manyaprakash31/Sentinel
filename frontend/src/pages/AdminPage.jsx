import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Users, UserPlus, RefreshCw, ShieldCheck, ShieldAlert, Key, Check } from 'lucide-react';

export const AdminPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);
  const [message, setMessage] = useState('');
  const { user: currentOperator } = useAuth();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const res = await api.get('/auth/users');
      if (res.success) {
        setUsers(res.data);
      }
    } catch (err) {
      console.error('Error fetching users:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = async (userId, targetRole) => {
    setActionLoading(userId);
    setMessage('');
    try {
      const res = await api.put(`/auth/users/${userId}/role`, { role: targetRole });
      if (res.success) {
        setMessage(`Updated user role to ${targetRole}`);
        fetchUsers();
      }
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleStatusToggle = async (userId, currentEnabled) => {
    setActionLoading(userId);
    setMessage('');
    try {
      const res = await api.put(`/auth/users/${userId}/status`, { enabled: !currentEnabled });
      if (res.success) {
        setMessage(`Updated account status to ${!currentEnabled ? 'ACTIVE' : 'DISABLED'}`);
        fetchUsers();
      }
    } catch (err) {
      alert(err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const getUserHighestRole = (user) => {
    if (!user.roles) return 'VIEWER';
    if (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN')) return 'ADMIN';
    if (user.roles.includes('ANALYST') || user.roles.includes('ROLE_ANALYST')) return 'ANALYST';
    return 'VIEWER';
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2.5">
            <Users className="w-6 h-6 text-cyan-400" />
            SOC Personnel & Access Control
          </h1>
          <p className="text-xs text-slate-400 font-mono mt-0.5">
            Role-Based Access Control (RBAC), Operator Authorization & Account Lifecycle
          </p>
        </div>

        <button
          onClick={fetchUsers}
          className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:text-white"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {message && (
        <div className="p-3 rounded-lg bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 text-xs font-mono flex items-center gap-2">
          <Check className="w-4 h-4 shrink-0" />
          <span>{message}</span>
        </div>
      )}

      <div className="glass-panel rounded-xl border border-slate-800 overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#090d14] border-b border-slate-800 text-slate-400 font-mono uppercase tracking-wider text-[11px]">
              <tr>
                <th className="py-3 px-4">Operator</th>
                <th className="py-3 px-4">Email</th>
                <th className="py-3 px-4">Primary Role</th>
                <th className="py-3 px-4">Account Status</th>
                <th className="py-3 px-4">Promote / Reassign</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {loading ? (
                <tr>
                  <td colSpan="6" className="text-center py-8 text-slate-500">
                    <RefreshCw className="w-5 h-5 mx-auto animate-spin text-cyan-400" />
                  </td>
                </tr>
              ) : (
                users.map((u) => {
                  const role = getUserHighestRole(u);
                  const isCurrent = u.username === currentOperator?.username;

                  return (
                    <tr key={u.id} className="hover:bg-slate-800/40 transition">
                      <td className="py-3 px-4 font-bold text-slate-200">
                        <div>{u.fullName || u.username} {isCurrent && <span className="text-[10px] text-cyan-400 font-normal">(You)</span>}</div>
                        <div className="text-[10px] text-slate-500">@{u.username}</div>
                      </td>
                      <td className="py-3 px-4 text-slate-300">{u.email}</td>
                      <td className="py-3 px-4">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          role === 'ADMIN' ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40' :
                          role === 'ANALYST' ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40' :
                          'bg-slate-800 text-slate-400 border border-slate-700'
                        }`}>
                          {role}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        {u.enabled ? (
                          <span className="text-emerald-400 flex items-center gap-1 text-[11px]">
                            <ShieldCheck className="w-3.5 h-3.5" /> ACTIVE
                          </span>
                        ) : (
                          <span className="text-rose-400 flex items-center gap-1 text-[11px]">
                            <ShieldAlert className="w-3.5 h-3.5" /> DISABLED
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4">
                        <select
                          disabled={actionLoading === u.id || isCurrent}
                          value={role}
                          onChange={(e) => handleRoleChange(u.id, e.target.value)}
                          className="bg-slate-900 border border-slate-700 rounded px-2 py-1 text-[11px] text-slate-300 focus:outline-none focus:border-cyan-500 disabled:opacity-50"
                        >
                          <option value="VIEWER">VIEWER</option>
                          <option value="ANALYST">ANALYST</option>
                          <option value="ADMIN">ADMIN</option>
                        </select>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <button
                          disabled={actionLoading === u.id || isCurrent}
                          onClick={() => handleStatusToggle(u.id, u.enabled)}
                          className={`px-2.5 py-1 rounded text-[11px] font-sans border transition disabled:opacity-40 ${
                            u.enabled
                              ? 'bg-rose-500/10 text-rose-300 border-rose-500/30 hover:bg-rose-500/20'
                              : 'bg-emerald-500/10 text-emerald-300 border-emerald-500/30 hover:bg-emerald-500/20'
                          }`}
                        >
                          {u.enabled ? 'Disable' : 'Enable'}
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
