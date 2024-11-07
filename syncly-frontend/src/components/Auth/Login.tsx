import React, { useContext } from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import api from '../../api';
import { loginSchema } from '../../utils/validation';
import { AuthContext } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

const Login: React.FC = () => {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    return (
        <div>
            <h2>Login</h2>
            <Formik
                initialValues={{ usernameOrEmail: '', password: '' }}
                validationSchema={loginSchema}
                onSubmit={async (values, { setSubmitting, setErrors }) => {
                    try {
                        const response = await api.post('/auth/login', values);
                        if (response.status === 206) {
                            // 2FA required
                            navigate('/2fa', { state: { username: response.data.username } });
                        } else {
                            login(response.data.token);
                            navigate('/');
                        }
                    } catch (error: any) {
                        setErrors({ password: error.response.data });
                    } finally {
                        setSubmitting(false);
                    }
                }}
            >
                {({ isSubmitting }) => (
                    <Form>
                        <div>
                            <label>Username or Email</label>
                            <Field type="text" name="usernameOrEmail" />
                            <ErrorMessage name="usernameOrEmail" component="div" />
                        </div>
                        <div>
                            <label>Password</label>
                            <Field type="password" name="password" />
                            <ErrorMessage name="password" component="div" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Login
                        </button>
                        <div>
                            <a href="/signup">Don't have an account? Sign up</a>
                        </div>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default Login;
