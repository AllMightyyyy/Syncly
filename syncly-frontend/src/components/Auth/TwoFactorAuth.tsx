import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import api from '../../api';
import { AuthContext } from '../../contexts/AuthContext';

interface LocationState {
    username?: string;
}

const TwoFactorAuth: React.FC = () => {
    const location = useLocation() as unknown as Location & { state: LocationState };
    const navigate = useNavigate();
    const { login } = React.useContext(AuthContext);
    const username = location.state?.username;

    if (!username) {
        navigate('/login');
        return null;
    }

    return (
        <div>
            <h2>Two-Factor Authentication</h2>
            <Formik
                initialValues={{ code: '' }}
                onSubmit={async (values, { setSubmitting, setErrors }) => {
                    try {
                        const response = await api.post('/auth/verify-2fa', {
                            username,
                            code: values.code,
                            deviceInfo: navigator.userAgent,
                            deviceType: 'WEB',
                        });
                        login(response.data.token);
                        navigate('/');
                    } catch (error: any) {
                        setErrors({ code: error.response.data });
                    } finally {
                        setSubmitting(false);
                    }
                }}
            >
                {({ isSubmitting }) => (
                    <Form>
                        <div>
                            <label>Authentication Code</label>
                            <Field type="text" name="code" />
                            <ErrorMessage name="code" component="div" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Verify
                        </button>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default TwoFactorAuth;
