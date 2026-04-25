import { useEffect, useMemo, useState, type ChangeEvent, type FormEvent } from "react";
import type { TournamentMutationData } from "~/services/tournament-service";

export interface AdminTournamentFormValues {
  name: string;
  slots: string;
  registrationStarts: string;
  startDate: string;
  description: string;
  price: string;
  status: string;
}

interface AdminTournamentFormProps {
  mode: "create" | "edit";
  initialValues: AdminTournamentFormValues;
  errorMessage?: string;
  successMessage?: string;
  isSubmitting: boolean;
  onSubmit: (data: TournamentMutationData) => Promise<void>;
}

const STATUS_OPTIONS = [
  "Upcoming",
  "Scheduled",
  "Registration Open",
  "In Progress",
  "Completed",
];

export default function AdminTournamentForm({
  mode,
  initialValues,
  errorMessage,
  successMessage,
  isSubmitting,
  onSubmit,
}: AdminTournamentFormProps) {
  const [values, setValues] = useState(initialValues);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [localErrors, setLocalErrors] = useState<string[]>([]);
  const [previewUrl, setPreviewUrl] = useState("");

  useEffect(() => {
    setValues(initialValues);
    setImageFile(null);
    setLocalErrors([]);
  }, [initialValues]);

  useEffect(() => {
    if (!imageFile) {
      setPreviewUrl("");
      return;
    }

    const nextPreviewUrl = URL.createObjectURL(imageFile);
    setPreviewUrl(nextPreviewUrl);

    return () => {
      URL.revokeObjectURL(nextPreviewUrl);
    };
  }, [imageFile]);

  const submitLabel = mode === "create" ? "Create Tournament" : "Save Changes";
  const busyLabel = mode === "create" ? "Creating..." : "Saving...";
  const selectedFileLabel = useMemo(() => {
    return imageFile ? `Selected: ${imageFile.name}` : "No file selected";
  }, [imageFile]);

  function updateValue(field: keyof AdminTournamentFormValues, value: string) {
    setValues((currentValues) => ({
      ...currentValues,
      [field]: value,
    }));
  }

  function onImageChange(event: ChangeEvent<HTMLInputElement>) {
    setImageFile(event.target.files?.[0] ?? null);
  }

  function validate(): string[] {
    const errors: string[] = [];
    const title = values.name.trim();
    const slots = Number(values.slots);

    if (!title) {
      errors.push("Title is required.");
    } else if (title.length > 80) {
      errors.push("Title cannot exceed 80 characters.");
    }

    if (!Number.isInteger(slots)) {
      errors.push("Max players must be a valid number.");
    } else if (slots < 4 || slots > 128) {
      errors.push("Max players must be between 4 and 128.");
    }

    if (!values.registrationStarts) {
      errors.push("Registration opens date is required.");
    }

    if (!values.startDate) {
      errors.push("Start date is required.");
    }

    if (
      values.registrationStarts &&
      values.startDate &&
      values.registrationStarts > values.startDate
    ) {
      errors.push("Registration opens date must be before or equal to start date.");
    }

    if (values.description.length > 500) {
      errors.push("Description cannot exceed 500 characters.");
    }

    if (values.price.length > 120) {
      errors.push("Prize cannot exceed 120 characters.");
    }

    return errors;
  }

  async function submitForm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const errors = validate();
    setLocalErrors(errors);

    if (errors.length > 0) {
      return;
    }

    await onSubmit({
      name: values.name.trim(),
      description: values.description.trim(),
      slots: Number(values.slots),
      registrationStarts: values.registrationStarts,
      startDate: values.startDate,
      price: values.price.trim(),
      status: values.status,
      imageFile,
    });
  }

  return (
    <div className="card glass-card p-4">
      <h2 className="h5 fw-bold mb-3">Tournament Details</h2>

      {localErrors.map((error) => (
        <div className="alert alert-danger px-3 py-2 small mb-3" role="alert" key={error}>
          {error}
        </div>
      ))}

      {errorMessage && (
        <div className="alert alert-danger px-3 py-2 small mb-3" role="alert">
          {errorMessage}
        </div>
      )}

      {successMessage && (
        <div className="alert alert-success px-3 py-2 small mb-3" role="alert">
          {successMessage}
        </div>
      )}

      <form onSubmit={submitForm}>
        <div className="row g-3">
          <div className="col-md-7">
            <label htmlFor="adminTournamentName" className="form-label text-uppercase small">
              Title
            </label>
            <input
              type="text"
              className="form-control form-control-lg"
              id="adminTournamentName"
              value={values.name}
              placeholder="Winter Cup 2026"
              maxLength={80}
              required
              onChange={(event) => updateValue("name", event.target.value)}
            />
          </div>

          <div className="col-md-5">
            <label htmlFor="adminTournamentSlots" className="form-label text-uppercase small">
              Max Players
            </label>
            <input
              type="number"
              className="form-control form-control-lg"
              id="adminTournamentSlots"
              value={values.slots}
              placeholder="32"
              min={4}
              max={128}
              required
              onChange={(event) => updateValue("slots", event.target.value)}
            />
          </div>

          <div className="col-md-6">
            <label htmlFor="adminTournamentRegistration" className="form-label text-uppercase small">
              Registration Opens
            </label>
            <input
              type="date"
              className="form-control form-control-lg"
              id="adminTournamentRegistration"
              value={values.registrationStarts}
              required
              onChange={(event) => updateValue("registrationStarts", event.target.value)}
            />
          </div>

          <div className="col-md-6">
            <label htmlFor="adminTournamentStart" className="form-label text-uppercase small">
              Start Date
            </label>
            <input
              type="date"
              className="form-control form-control-lg"
              id="adminTournamentStart"
              value={values.startDate}
              required
              onChange={(event) => updateValue("startDate", event.target.value)}
            />
          </div>

          {mode === "edit" && (
            <div className="col-md-6">
              <label htmlFor="adminTournamentStatus" className="form-label text-uppercase small">
                Status
              </label>
              <select
                className="form-select form-control-lg"
                id="adminTournamentStatus"
                value={values.status}
                onChange={(event) => updateValue("status", event.target.value)}
              >
                {STATUS_OPTIONS.map((status) => (
                  <option value={status} key={status}>
                    {status}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div className={mode === "edit" ? "col-md-6" : "col-md-6"}>
            <label htmlFor="adminTournamentPrize" className="form-label text-uppercase small">
              Prize
            </label>
            <input
              type="text"
              className="form-control form-control-lg"
              id="adminTournamentPrize"
              value={values.price}
              placeholder="Optional: Prize or reward"
              maxLength={120}
              onChange={(event) => updateValue("price", event.target.value)}
            />
          </div>

          <div className="col-12">
            <label htmlFor="adminTournamentDescription" className="form-label text-uppercase small">
              Description
            </label>
            <textarea
              className="form-control form-control-lg"
              id="adminTournamentDescription"
              rows={4}
              value={values.description}
              placeholder="Explain the rules, prizes, or special conditions."
              maxLength={500}
              onChange={(event) => updateValue("description", event.target.value)}
            />
          </div>

          <div className="col-12">
            <label htmlFor="adminTournamentImage" className="form-label text-uppercase small">
              Tournament Image
            </label>
            <div className="d-flex flex-wrap gap-3 align-items-center">
              <input
                className="form-control"
                type="file"
                id="adminTournamentImage"
                accept="image/png, image/jpeg"
                onChange={onImageChange}
              />
              <span className={imageFile ? "text-success small" : "text-info small"}>
                {selectedFileLabel}
              </span>
            </div>
            {previewUrl && (
              <img
                src={previewUrl}
                alt="Tournament preview"
                className="admin-image-preview mt-3"
              />
            )}
          </div>

          <div className="col-12 d-flex flex-wrap gap-2 mt-2">
            <button type="submit" className="btn btn-gradient-primary" disabled={isSubmitting}>
              {isSubmitting ? busyLabel : submitLabel}
            </button>
            <button
              type="button"
              className="btn btn-outline-muted"
              disabled={isSubmitting}
              onClick={() => {
                setValues(initialValues);
                setImageFile(null);
                setLocalErrors([]);
              }}
            >
              Reset
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
