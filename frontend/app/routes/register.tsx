import { useState, type ChangeEvent } from "react";
import { Link } from "react-router";
import { registerUser } from "~/services/auth-service";

export default function Register() {
    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    function onChangeEmail(e: ChangeEvent<HTMLInputElement>) {
        setEmail(e.target.value);
    }

    function onChangeUsername(e: ChangeEvent<HTMLInputElement>) {
        setUsername(e.target.value);
    }

    function onChangePassword(e: ChangeEvent<HTMLInputElement>) {
        setPassword(e.target.value);
    }

    function onChangeConfirmPassword(e: ChangeEvent<HTMLInputElement>) {
        setConfirmPassword(e.target.value);
    }

    async function sendForm(e: React.MouseEvent<HTMLButtonElement>) {
        e.preventDefault();

        if (password !== confirmPassword) {
            setErrorMessage("Las contraseñas no coinciden");
            return;
        }

        if (password.length < 8) {
            setErrorMessage("La contraseña debe tener al menos 8 caracteres");
            return;
        }

        try {
            const result = await registerUser({ username, password, email });

            if (result.ok) {
                setSuccessMessage("Usuario registrado correctamente");
                setErrorMessage("");
                //TODO:Add the navigation here
            } else {
                const data = await result.json();
                setErrorMessage(data.message);
                setSuccessMessage("");
            }
        } catch (error) {
            setErrorMessage("Error de servidor");
            setSuccessMessage("");
        }
    }

    return (
        <div className="d-flex justify-content-center align-items-center min-vh-100">
            <div className="card glass-card p-4" style={{ maxWidth: '400px' }}>
                <div className="card-body">
                    <div className="text-center mb-4">
                        <a href="/" className="text-decoration-none">
                            <div className="auth-header-icon">✂️</div>
                            <h1 className="h4 fw-bold text-white mb-0">Scissors, Please</h1>
                        </a>
                        <p className="text-secondary mt-2">Create your account to start competing.</p>
                    </div>

                    {errorMessage && <div className="alert alert-danger">{errorMessage}</div>}
                    {successMessage && <div className="alert alert-success">{successMessage}</div>}

                    <form>
                        <div className="mb-3">
                            <label className="form-label text-uppercase small">Email Address</label>
                            <input
                                type="email"
                                value={email}
                                onChange={onChangeEmail}
                                className="form-control form-control-lg"
                                placeholder="name@example.com"
                                required
                            />
                        </div>

                        <div className="mb-3">
                            <label className="form-label text-uppercase small">Username</label>
                            <input
                                type="text"
                                value={username}
                                onChange={onChangeUsername}
                                className="form-control form-control-lg"
                                minLength={5}
                                required
                            />
                        </div>

                        <div className="mb-3">
                            <label className="form-label text-uppercase small">Password</label>
                            <input
                                type="password"
                                value={password}
                                onChange={onChangePassword}
                                className="form-control form-control-lg"
                                minLength={8}
                                required
                            />
                        </div>

                        <div className="mb-4">
                            <label className="form-label text-uppercase small">Confirm Password</label>
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={onChangeConfirmPassword}
                                className="form-control form-control-lg"
                                required
                            />
                        </div>

                        <div className="d-grid mb-4">
                            <button onClick={sendForm} className="btn btn-gradient-primary btn-lg">
                                Create Account
                            </button>
                        </div>
                    </form>

                        <div className="text-center mb-0">
                            <p className="text-secondary small mb-0">
                                Already have an account? <Link to="/login" className="text-primary text-decoration-none fw-bold">Log
                                    in</Link>
                            </p>
                        </div>
                    <div className="text-center mt-3">
                        <a href="/" className="small text-secondary text-decoration-none">Back to Home</a>
                    </div>
                </div>
            </div>
        </div>
    );
}