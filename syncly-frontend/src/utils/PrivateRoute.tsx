import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../contexts/AuthContext';

const PrivateRoute: React.FC<{ children: React.ReactElement }> = ({ children }) => {
    const { isAuthenticated } = useContext(AuthContext);
    return isAuthenticated ? children : <Navigate to="/login" />;
};

export default PrivateRoute;
