import Header from "~/components/header";
import Footer from "~/components/footer";

import GuestHomeContent from "~/components/guestHomeContent";
import { useLoaderData } from "react-router";

export async function loader() {
  return {
    logged:false,
    admin: false,
  };
}
interface HomeProps{logged:boolean,admin:boolean}

export default function Home() {
  const { logged, admin } = useLoaderData<typeof loader>();
  
  return (<>
  <Header admin={admin} logged={logged} />
  {!logged && <GuestHomeContent/>}
  <Footer></Footer>
  </>
  )
}
