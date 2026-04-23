import { useState, type ChangeEvent, type FormEvent } from "react";
import { Link } from "react-router";
import { registerUser } from "~/services/auth-service";

export default function SignUp() {
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function onChangeEmail(event: ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
  }

  function onChangeUsername(event: ChangeEvent<HTMLInputElement>) {
    setUsername(event.target.value);
  }

  function onChangePassword(event: ChangeEvent<HTMLInputElement>) {
    setPassword(event.target.value);
  }

  function onChangeConfirmPassword(event: ChangeEvent<HTMLInputElement>) {
    setConfirmPassword(event.target.value);
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setErrorMessage("");
    setSuccessMessage("");

    if (password !== confirmPassword) {
      setErrorMessage("Passwords do not match.");
      return;
    }

    if (password.length < 8) {
      setErrorMessage("Password must contain at least 8 characters.");
      return;
    }

    setIsSubmitting(true);

    try {
      const result = await registerUser({ email, username, password });
      if (!result.ok) {
        setErrorMessage(result.message);
        return;
      }

      setSuccessMessage("Account created successfully. Redirecting...");
      window.location.assign("/tournaments");
    } catch {
      setErrorMessage("Unable to create your account right now.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="centered-layout min-vh-100">
      <div className="card glass-card p-4 auth-card">
        <div className="card-body">
          <div className="text-center mb-4">
            <Link to="/" className="text-decoration-none">
              <div className="auth-header-icon">✂️</div>
              <h1 className="h4 fw-bold text-white mb-0">Scissors, Please</h1>
            </Link>
            <p className="text-secondary mt-2 mb-0">
              Create your account to start competing.
            </p>
          </div>

          {errorMessage && (
            <div className="alert alert-danger px-3 py-2 small mb-4" role="alert">
              {errorMessage}
            </div>
          )}
          {successMessage && (
            <div className="alert alert-success px-3 py-2 small mb-4" role="alert">
              {successMessage}
            </div>
          )}

          <form onSubmit={onSubmit}>
            <div className="mb-3">
              <label htmlFor="email" className="form-label text-uppercase small">
                Email Address
              </label>
              <input
                id="email"
                name="email"
                type="email"
                className="form-control form-control-lg"
                value={email}
                onChange={onChangeEmail}
                placeholder="name@example.com"
                autoComplete="email"
                required
              />
            </div>

            <div className="mb-3">
              <label htmlFor="username" className="form-label text-uppercase small">
                Username
              </label>
              <input
                id="username"
                name="username"
                type="text"
                className="form-control form-control-lg"
                value={username}
                onChange={onChangeUsername}
                placeholder="Choose a unique username"
                autoComplete="username"
                required
              />
            </div>

            <div className="mb-3">
              <label htmlFor="password" className="form-label text-uppercase small">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                className="form-control form-control-lg"
                value={password}
                onChange={onChangePassword}
                placeholder="Min. 8 characters"
                autoComplete="new-password"
                minLength={8}
                required
              />
            </div>

            <div className="mb-4">
              <label htmlFor="confirmPassword" className="form-label text-uppercase small">
                Confirm Password
              </label>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                className="form-control form-control-lg"
                value={confirmPassword}
                onChange={onChangeConfirmPassword}
                placeholder="Confirm your password"
                autoComplete="new-password"
                required
              />
            </div>

            <div className="d-grid mb-3">
              <button
                type="submit"
                className="btn btn-gradient-primary btn-lg fw-bold py-2"
                disabled={isSubmitting}
              >
                {isSubmitting ? "Creating account..." : "Create Account"}
              </button>
            </div>
          </form>

          <div className="text-center">
            <p className="text-secondary small mb-0">
              Already have an account?{" "}
              <Link to="/login" className="text-primary text-decoration-none fw-bold">
                Log in
              </Link>
            </p>
          </div>
          <div className="text-center mt-3">
            <Link to="/" className="small text-secondary text-decoration-none">
              Back to Home
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
