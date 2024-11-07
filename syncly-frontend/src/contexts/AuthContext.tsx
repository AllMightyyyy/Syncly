import React, { createContext, useState, useEffect } from 'react';
import api from '../api';

const jwt_decode = require('jwt-decode');

interface DecodedToken {
    exp: number;
    [key: string]: any; // Adjust properties as per your token structure
}

interface AuthContextType {
    isAuthenticated: boolean;
    user: DecodedToken | null;
    login: (token: string) => void;
    logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({
    isAuthenticated: false,
    user: null,
    login: () => {},
    logout: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<DecodedToken | null>(null);

    useEffect(() => {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            // @ts-ignore
            const decoded = jwt_decode<DecodedToken>(token);
            // Specify DecodedToken type here
            if (decoded.exp * 1000 > Date.now()) {
                setUser(decoded);
            } else {
                localStorage.removeItem('jwtToken');
            }
        }
    }, []);

    const login = (token: string) => {
        localStorage.setItem('jwtToken', token);
        // @ts-ignore
        const decoded = jwt_decode<DecodedToken>(token); // Specify DecodedToken type here
        setUser(decoded);
    };

    const logout = () => {
        localStorage.removeItem('jwtToken');
        api.post('/auth/logout');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated: !!user, user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
