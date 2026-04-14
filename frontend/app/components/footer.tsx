export default function Footer() {
    return (
        <>
            <footer className="footer mt-auto py-4 bg-body-tertiary">
                <div className="container">
                    <div className="row align-items-center">
                        <div className="col-md-6 text-center text-md-start">
                            <p className="mb-0 text-secondary small">&copy; 2026 Scissors, Please.</p>
                        </div>
                        <div className="col-md-6 text-center text-md-end">
                            <ul className="list-inline mb-0 small">
                                <li className="list-inline-item"><a href="#" className="text-secondary text-decoration-none">GitHub</a></li>
                                <li className="list-inline-item"><span className="text-secondary mx-2">·</span></li>
                                <li className="list-inline-item"><a href="#" className="text-secondary text-decoration-none">Privacy</a>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </footer>
        </>
    )
}