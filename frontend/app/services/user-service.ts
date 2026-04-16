
interface UserProfileData{
    id: string,
    username:string,
    email:string,
    imageUrl:string,
    createdAt:string,
    blocked:boolean
}

async function getUserById(id :string): Promise<UserProfileData | null> { 
    const url = `/api/v1/users/${id}`;
    const response = await fetch(url);

    if (!response.ok){
        return null
    }
    const data = await response.json();
    return data
}

