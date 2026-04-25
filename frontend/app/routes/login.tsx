import { useState, type ChangeEvent, type FormEvent } from "react";
import { Link, useNavigate } from "react-router";
import { logUser } from "../services/auth-service";

export default function Login() {
    const navigate = useNavigate();
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    function onChangeUsername(e: ChangeEvent<HTMLInputElement>){
        setUsername(e.target.value);
    }
    function onChangePassword(e:ChangeEvent<HTMLInputElement>){
        setPassword(e.target.value);
    }

    async function sendForm(e : FormEvent<HTMLFormElement>){
        e.preventDefault();
        setErrorMessage("");
        setSuccessMessage("");

        setIsSubmitting(true);

        try {
            const session = await logUser({ username, password });
            if (session){
                setSuccessMessage("Login successful. Redirecting...");
                navigate(session.admin ? "/admin/tournaments" : "/tournaments", { replace: true });
                return;
            }

            setErrorMessage("Invalid username or password.");
        } catch {
            setErrorMessage("Unable to log in right now.");
        } finally {
            setIsSubmitting(false);
        }
    }
    return (
        <>
            <div className="centered-layout min-vh-100">
                <div className="card glass-card p-4" style={{maxWidth:'400px'}}>
                        <div className="card-body">
                        <div className="text-center mb-4">
                            <Link to="/" className="text-decoration-none">
                                <div className="auth-header-icon">✂️</div>
                                <h1 className="h4 fw-bold text-white mb-0">Scissors, Please</h1>
                            </Link>
                            <p className="text-secondary mt-2">Welcome back! Please enter your details.</p>
                        </div>

                        {errorMessage && <div className="alert alert-danger px-3 py-2 small mb-4" role="alert">{errorMessage}</div>}
                        {successMessage && <div className="alert alert-success px-3 py-2 small mb-4" role="alert">{successMessage}</div>}

                        <form onSubmit={sendForm}>
                            <div className="mb-3">
                                <label htmlFor="username" className="form-label text-uppercase small">Username</label>
                                <input type="text"  value ={username} onChange={onChangeUsername} className="form-control form-control-lg" id="username" name="username"
                                    placeholder="Enter your username" required></input>
                            </div>
                            <div className="mb-3">
                                <label htmlFor="password" className="form-label text-uppercase small">Password</label>
                                <input type="password" value ={password} onChange={onChangePassword} className="form-control form-control-lg" id="password" name="password"
                                    placeholder="••••••••" required></input>
                            </div>

                            <div className="d-flex justify-content-between align-items-center mb-4">
                                <div className="form-check">
                                    <input className="form-check-input" type="checkbox" id="rememberMe"></input>
                                    <label className="form-check-label text-secondary small" htmlFor="rememberMe">Remember me</label>
                                </div>
                                <a href="#" className="small text-primary text-decoration-none">Forgot password?</a>
                            </div>

                            <div className="d-grid mb-3">
                                <button type="submit" className="btn btn-gradient-primary btn-lg fw-bold py-2" disabled={isSubmitting}>
                                    {isSubmitting ? "Logging in..." : "Log In"}
                                </button>
                            </div>
                        </form>

                        <div className="divider">or continue with</div>

                        <div className="d-grid gap-2 mb-4">
                            <a href="/oauth2/authorization/google" className="btn btn-outline-muted btn-sm text-decoration-none">
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                                    className="bi bi-google me-2" viewBox="0 0 16 16">
                                    <path
                                        d="M15.545 6.558a9.42 9.42 0 0 1 .139 1.626c0 2.434-.87 4.492-2.384 5.885h.002C11.978 15.292 10.158 16 8 16A8 8 0 1 1 8 0a7.689 7.689 0 0 1 5.352 2.082l-2.284 2.284A4.347 4.347 0 0 0 8 3.166c-2.087 0-3.86 1.408-4.492 3.304a4.792 4.792 0 0 0 0 3.063h.003c.635 1.893 2.405 3.301 4.492 3.301 1.078 0 2.004-.276 2.722-.764h-.003a3.702 3.702 0 0 0 1.599-2.431H8v-3.08h7.545z" />
                                </svg>
                                Continue with Google
                            </a>
                        </div>

                        <div className="text-center">
                            <p className="text-secondary small mb-0">
                                Don't have an account? <Link to="/sign-up" className="text-primary text-decoration-none fw-bold">Sign
                                    up</Link>
                            </p>
                        </div>
                        <div className="text-center mt-3">
                            <Link to="/" className="small text-secondary text-decoration-none">Back to Home</Link>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}
