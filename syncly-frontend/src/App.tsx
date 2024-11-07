import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navigation from './components/Navigation';
import Home from './components/Home';
import Login from './components/Auth/Login';
import Signup from './components/Auth/Signup';
import TwoFactorAuth from './components/Auth/TwoFactorAuth';
import ClipboardInput from './components/Clipboard/ClipboardInput';
import ClipboardHistory from './components/Clipboard/ClipboardHistory';
import ClipboardRealTime from './components/Clipboard/ClipboardRealTime';
import DeviceList from './components/Devices/DeviceList';
import AddDevice from './components/Devices/AddDevice';
import PasteBinList from './components/PasteBin/PasteBinList';
import CreatePasteBin from './components/PasteBin/CreatePasteBin';
import UserProfile from './components/Profile/UserProfile';
import PrivateRoute from './utils/PrivateRoute';
import { AuthProvider } from './contexts/AuthContext';

const App: React.FC = () => {
  return (
      <AuthProvider>
        <Router>
          <Navigation />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/2fa" element={<TwoFactorAuth />} />
            <Route
                path="/clipboard"
                element={
                  <PrivateRoute>
                    <>
                      <ClipboardInput />
                      <ClipboardHistory />
                      <ClipboardRealTime />
                    </>
                  </PrivateRoute>
                }
            />
            <Route
                path="/devices"
                element={
                  <PrivateRoute>
                    <>
                    <DeviceList />
                    <AddDevice />
                    </>
                  </PrivateRoute>
                }
            />
            <Route
                path="/pastebin"
                element={
                  <PrivateRoute>
                    <>
                    <CreatePasteBin />
                    <PasteBinList />
                    </>
                  </PrivateRoute>
                }
            />
            <Route
                path="/profile"
                element={
                  <PrivateRoute>
                    <UserProfile />
                  </PrivateRoute>
                }
            />
          </Routes>
        </Router>
      </AuthProvider>
  );
};

export default App;
