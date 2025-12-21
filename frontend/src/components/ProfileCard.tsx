import type {User} from "../types";

interface Props {
    user: User;
}

export default function ProfileCard({user}: Props) {
    return <section className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Profile</h3>
        <div className="flex items-center space-x-4">
            {user.avatarUrl && (
                <img
                    src={user.avatarUrl}
                    alt={user.displayName || user.username}
                    className="h-16 w-16 rounded-full"
                />
            )}
            <div>
                <p className="font-medium text-gray-900">
                    {user.displayName || user.username}
                </p>
                <p className="text-sm text-gray-500">Steam ID: {user.steamId}</p>
                {user.profileUrl && (
                    <a
                        href={user.profileUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-blue-600 hover:text-blue-800"
                    >
                        View Steam Profile
                    </a>
                )}
            </div>
        </div>
    </section>
}