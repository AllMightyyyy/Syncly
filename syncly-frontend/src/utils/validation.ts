import * as Yup from 'yup';

export const signupSchema = Yup.object({
    username: Yup.string().min(3).max(20).required(),
    email: Yup.string().email().required(),
    password: Yup.string().min(8).max(100).required(),
});

export const loginSchema = Yup.object({
    usernameOrEmail: Yup.string().required('Username or Email is required'),
    password: Yup.string().required('Password is required'),
});

export const clipboardSchema = Yup.object({
    content: Yup.string().max(10000).required(),
});
