import React from 'react';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import api from '../../api';
import { clipboardSchema } from '../../utils/validation';

const ClipboardInput: React.FC = () => {
    return (
        <div>
            <h2>Save to Clipboard</h2>
            <Formik
                initialValues={{ content: '' }}
                validationSchema={clipboardSchema}
                onSubmit={async (values, { setSubmitting, resetForm }) => {
                    try {
                        await api.post('/clipboard/save', {
                            content: values.content,
                            deviceInfo: navigator.userAgent,
                        });
                        resetForm();
                    } catch (error: any) {
                        console.error(error);
                    } finally {
                        setSubmitting(false);
                    }
                }}
            >
                {({ isSubmitting }) => (
                    <Form>
                        <div>
                            <Field as="textarea" name="content" />
                            <ErrorMessage name="content" component="div" />
                        </div>
                        <button type="submit" disabled={isSubmitting}>
                            Save
                        </button>
                    </Form>
                )}
            </Formik>
        </div>
    );
};

export default ClipboardInput;
