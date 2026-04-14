interface HeaderProps{logged:boolean,admin:boolean}
export default function Header({logged,admin}:HeaderProps){
    
    admin = logged && admin;
    return(
        <>
    <nav className="navbar navbar-expand-lg navbar-dark sticky-top">
    <div className="container">
        <a className="navbar-brand" href="/">✂️ Scissors, Please</a>
        <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNav">
            <ul className="navbar-nav me-auto">
                <li className="nav-item"><a className="nav-link" href="/home">Home</a></li>
                <li className="nav-item"><a className="nav-link" href="/matches/list">Best Matches</a></li>
                {admin && <><li className="nav-item"><a className="nav-link" href="/admin/tournaments">Tournaments</a></li>
                <li className="nav-item"><a className="nav-link" href="/admin/users">Users</a></li>
                <li className="nav-item"><a className="nav-link" href="/admin/bots">Bots</a></li></>}
                
                {!admin && <><li className="nav-item"><a className="nav-link" href="/tournaments">Tournaments</a></li></>}
            </ul>
            <div className="d-flex gap-2 align-items-center">
                {logged && admin && <span className="badge bg-primary me-2">Admin</span>}
   
                {logged && !admin && <a href="/user/profile" className="btn btn-outline-muted btn-sm px-3">Profile</a>}

                {logged && <><form action="/logout" method="post" className="d-inline">
                    <input type="hidden" name="_csrf" value="{{_csrf.token}}" />
                    <button type="submit" className="btn btn-primary btn-sm px-3">Log Out</button>
                </form></>}
                
                {!logged && <> <a href="/login" className="btn btn-outline-muted btn-sm px-3">Log In</a>
                <a href="/sign-up" className="btn btn-primary btn-sm px-3">Sign Up</a> </>}
            </div>
        </div>
    </div>
    </nav>
    </>
    )


}