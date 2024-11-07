import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../contexts/AuthContext';

const Navigation: React.FC = () => {
    const { isAuthenticated, logout } = useContext(AuthContext);

    return (
        <nav>
            <Link to="/">Home</Link>
            {isAuthenticated ? (
                <>
                    <Link to="/clipboard">Clipboard</Link>
                    <Link to="/devices">Devices</Link>
                    <Link to="/pastebin">PasteBin</Link>
                    <Link to="/profile">Profile</Link>
                    <button onClick={logout}>Logout</button>
                </>
            ) : (
                <>
                    <Link to="/login">Login</Link>
                    <Link to="/signup">Signup</Link>
                </>
            )}
        </nav>
    );
};

export default Navigation;
