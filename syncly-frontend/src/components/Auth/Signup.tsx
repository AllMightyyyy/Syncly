import React from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import api from '../../api';
import { signupSchema } from '../../utils/validation';
import { useNavigate } from 'react-router-dom';

const Signup: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div>
            <h2>Signup</h2>
            <Formik
                initialValues={{ username: '', email: '', password: '' }}
                validationSchema={signupSchema}
                onSubmit={async (values, { setSubmitting, setErrors }) => {
                    try {
                        await api.post('/auth/signup', values);
                        navigate('/login');
                    } catch (error: any) {
                        setErrors({ email: error.response.data });
                    } finally {
                        setSubmitting(false);
                    }
                }}
            >
                {({ isSubmitting }) => (
                    <Form>
                        <div>
                            <label>Username</label>
                            <Field type="text" name="username" />
                            <ErrorMessage name="username" component="div" />
                        </div>
                        <div>
                            <label>Email</label>
                            <Field type="email" name="email" />
                            <ErrorMessage name="email" component="div" />
                        </div>
                        <div>
                            <label>Password</label>
                            <Field type="password" name="password" />
                            <ErrorMessage name="password" component="div" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Signup
                        </button>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default Signup;
