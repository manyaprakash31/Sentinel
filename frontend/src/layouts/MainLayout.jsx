import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Shield,
  LayoutDashboard,
  Radio,
  Bell,
  AlertTriangle,
  Sliders,
  Server,
  FileText,
  Users,
  User,
  LogOut,
  Activity,
  ChevronRight
} from 'lucide-react';

export const MainLayout = () => {
  const { user, logout, isAdmin, isAnalyst } = useAuth();
  const navigate = useNavigate();

  const navItems = [
    { label: 'Dashboard', path: '/', icon: LayoutDashboard, show: true },
    { label: 'Security Events', path: '/events', icon: Radio, show: true },
    { label: 'Alerts', path: '/alerts', icon: Bell, show: true },
    { label: 'Incidents', path: '/incidents', icon: AlertTriangle, show: true },
    { label: 'Detection Rules', path: '/rules', icon: Sliders, show: isAnalyst() },
    { label: 'Services', path: '/services', icon: Server, show: true },
    { label: 'Audit Logs', path: '/audit-logs', icon: FileText, show: isAdmin() },
    { label: 'Administration', path: '/admin', icon: Users, show: isAdmin() },
    { label: 'Profile', path: '/profile', icon: User, show: true },
  ];

  return (
    <div className="flex h-screen bg-[#07090e] text-slate-100 overflow-hidden font-sans">
      {/* Sidebar */}
      <aside className="w-64 flex flex-col border-r border-slate-800/80 bg-[#0d1117]/90 backdrop-blur-xl">
        {/* Logo / Brand Header */}
        <div className="h-16 flex items-center px-6 border-b border-slate-800/80 gap-3">
          <div className="p-2 rounded-lg bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 shadow-[0_0_15px_rgba(0,240,255,0.2)]">
            <Shield className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <div className="font-extrabold text-base tracking-wide bg-gradient-to-r from-cyan-400 to-blue-500 bg-clip-text text-transparent">
              SENTINEL
            </div>
            <div className="text-[10px] tracking-widest text-slate-400 uppercase font-mono">
              SecOps Platform
            </div>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {navItems.filter(i => i.show).map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all group ${
                    isActive
                      ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 shadow-[0_0_12px_rgba(0,240,255,0.12)]'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`
                }
              >
                <Icon className="w-4 h-4 transition-transform group-hover:scale-110" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>

        {/* SOC Live Pill & User Footer */}
        <div className="p-4 border-t border-slate-800/80 bg-[#090d14]/70">
          <div className="flex items-center gap-2 mb-3 px-2 py-1.5 rounded-md bg-emerald-500/10 border border-emerald-500/20 text-xs font-mono text-emerald-400">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
            <span>SOC REALTIME ACTIVE</span>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5 overflow-hidden">
              <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-xs text-cyan-400">
                {user?.username ? user.username.slice(0, 2).toUpperCase() : 'US'}
              </div>
              <div className="truncate">
                <div className="text-xs font-semibold text-slate-200 truncate">{user?.fullName || user?.username}</div>
                <div className="text-[10px] font-mono text-cyan-400/80 truncate">
                  {user?.roles?.[0]?.replace('ROLE_', '') || 'VIEWER'}
                </div>
              </div>
            </div>
            <button
              onClick={logout}
              title="Logout"
              className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded transition"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Navbar */}
        <header className="h-16 border-b border-slate-800/80 bg-[#0d1117]/60 backdrop-blur-md px-8 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Activity className="w-4 h-4 text-cyan-400" />
            <span className="text-xs font-mono text-slate-400 uppercase tracking-widest">
              Security Operations Center &bull; Live Telemetry Feed
            </span>
          </div>

          <div className="flex items-center gap-4 text-xs font-mono">
            <span className="text-slate-400">STATUS:</span>
            <span className="flex items-center gap-1.5 text-emerald-400 bg-emerald-500/10 px-2.5 py-1 rounded border border-emerald-500/20">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
              ALL SYSTEMS OPERATIONAL
            </span>
          </div>
        </header>

        {/* Nested Page View */}
        <main className="flex-1 overflow-y-auto p-8 bg-gradient-to-b from-[#0d1117]/30 to-[#07090e]">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
